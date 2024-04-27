import com.google.gson.JsonParseException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Classe principale per l'avvio del server HOTELIER. Gestisce la configurazione, 
 * l'inizializzazione dei servizi e la gestione dei thread per la comunicazione con i client.*/

public class HOTELIERServerMain {
    
    /** Istanza del servizio di autenticazione degli utenti.*/
    private static AuthenticationService authservice;
    /** Istanza del servizio degli hotel.*/
    private static HotelService hotelService;
    /** Scheduler per la gestione delle attività pianificate.*/ 
    private static ScheduledExecutorService scheduler;
    /** Percorso del file di configurazione.*/
    private static final String configFile = "server.properties";
    /** Porta del server. */
    private static int port; 
    /** Timeout del server.*/
    private static long timeout_server;
    /** Timeout per l'aggiornamento degli utenti.*/
    private static long timeout_user;
    /**Timeout per l'aggiornamento degli hotel.*/
    private static long timeout_hotels;
    /** Percorso del file degli utenti. */
    private static String user_path;
    /** Percorso del file degli hotel. */
    private static String hotel_path;
    /**Porta per le notifiche UDP.*/
    private static String UDP_port;
    /** Indirizzo per le notifiche UDP.*/
    private static String UDP_addr;
    /** Numero di threads da schedulare */
    private static int number_threads;
    /** ServerSocket per la comunicazione con i client. */
    private static ServerSocket serverSocket;
    /** ThreadPool per la gestione concorrente dei client. */
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    /** */
    private static int wait_term;
    /** Variabili per la gestione della chiusura del server per inattività. */
    private static Long lastAccessTime = System.currentTimeMillis();
    private static AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    /** Oggetti future per gestire la chiusura */
    private static ScheduledFuture<?> futureUser;
    private static ScheduledFuture<?> futureHotel; 
    private static ScheduledFuture<?> futureCheck;



    /**
     * Legge la configurazione dal file di configurazione (server.properties).
     *
     * @throws IOException In caso di errore durante la lettura del file di configurazione.
     */
    private static void readConfig() throws IOException {
        
        //InputStream input = new FileInputStream(configFile);
        try (InputStream input = new FileInputStream(configFile)) {
            Properties prop = new Properties();
            prop.load(input);
            port = Integer.parseInt(prop.getProperty("port"));
            timeout_server = Integer.parseInt(prop.getProperty("timeout_server"));
            user_path = prop.getProperty("user_file");
            hotel_path = prop.getProperty("hotel_file");
            timeout_user = Integer.parseInt(prop.getProperty("timeout_users"));
            timeout_hotels = Integer.parseInt(prop.getProperty("timeout_hotels"));
            UDP_port = prop.getProperty("UDP_port");
            UDP_addr = prop.getProperty("UDP_addr");
            number_threads = Integer.parseInt(prop.getProperty("number_threads"));
            wait_term = Integer.parseInt(prop.getProperty("wait_term"));
        }
    }

    /**
     * Inizia l'esecuzione del server HOTELIER. Inizializza i servizi, avvia il socket del server e gestisce la comunicazione con i client tramite thread.
     *
     * @throws IOException           In caso di errore durante l'avvio del socket del server.
     * @throws JsonParseException    In caso di errore durante il parsing di dati JSON.
     */
    private static void begin() throws IOException, JsonParseException {

        // Inizializzo i servizi
        authservice = new AuthenticationService(user_path);
        hotelService = new HotelService(hotel_path, UDP_addr, UDP_port);
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newCachedThreadPool();
        scheduler = Executors.newScheduledThreadPool(number_threads);
        final File file_user = new File(user_path);
        System.out.println("Server has started executing");

        if (file_user.length() == 0) {
            try (FileWriter filew = new FileWriter(file_user)) {
                filew.write("[]");
                filew.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Schedula le attività di persistenza dei dati
        Runnable saveUsers = () -> {
            try {
                authservice.saveUsersToFile(file_user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        futureUser = scheduler.scheduleWithFixedDelay(saveUsers, timeout_user, timeout_user, TimeUnit.MILLISECONDS);
        
        Runnable saveHotels = () -> {
            try {
                hotelService.updateJsonFileAndRankCache();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        futureHotel = scheduler.scheduleWithFixedDelay(saveHotels, timeout_hotels, timeout_hotels, TimeUnit.MILLISECONDS);

         // Task programmato per verificare l'inattività e chiudere il server se necessario
         futureCheck = scheduler.scheduleAtFixedRate(() -> {
            // Se non ci sono nuovi client connessi per un certo periodo di tempo, chiude il server
            if (System.currentTimeMillis() - lastAccessTime > timeout_server && ((ThreadPoolExecutor) threadPool).getActiveCount() == 0) {
                shutdown(false);
            }
        }, 0, 1, TimeUnit.MINUTES);

        // Si mette in ascolto in attesa di client
        while (!serverSocket.isClosed()) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                lastAccessTime = System.currentTimeMillis();
                threadPool.execute(new ClientHandler(clientSocket, authservice, hotelService));
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Questo metodo gestisce la chiusura del server.
     *
     * @param isHook Un flag booleano che indica se il metodo è stato chiamato da un hook di shutdown.
     */
    private static void shutdown(Boolean isHook) {

        int activeThreads = ((ThreadPoolExecutor) threadPool).getActiveCount();
    
        // Se il server si sta già spegnendo o ci sono thread attivi, ritorna
        if (!isHook && (isShuttingDown.get() || activeThreads > 0)) {
            return;
        }
        isShuttingDown.set(true);
    
        System.out.println("The server is shutting down");
    
        // Chiude il ServerSocket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        // Impedisci al task di essere rischedulato
        futureUser.cancel(false); 
        futureHotel.cancel(false);
        futureCheck.cancel(false); 

        if (scheduler != null) {
            // Interruzione delle attività pianificate
            scheduler.shutdown();
        
            // Attende la terminazione delle attività pianificate prima di chiudere
            try {
                if (!scheduler.awaitTermination(wait_term, TimeUnit.SECONDS)) {
                    //System.err.println("Scheduler not terminated");
                    //scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Scheduler awaitTermination interrupted");
            }
        }
    
        // Chiude il threapool
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                // Attende la terminazione delle attività pianificate prima di chiudere il ThreadPool
                if (!threadPool.awaitTermination(wait_term, TimeUnit.SECONDS)) {
                    //System.err.println("ThreadPool not terminated"); ignore
                }
            } catch (InterruptedException e) {
                // Gestisci l'eccezione di interruzione
                //e.printStackTrace();
                System.err.println("ThreadPool awaitTermination interrupted");
                //Thread.currentThread().interrupt(); // Ri-imposta il flag di interruzione
            }
        }
    }
    
    /**
     * Metodo principale di avvio del programma. Gestisce la configurazione, l'esecuzione del server e la chiusura controllata 
     * dei servizi e dei thread.
     *
     * @param args Gli argomenti della riga di comando.
     */
    public static void main(String[] args){

        try {
            // Legge dal file di configurazione
            readConfig();
        }
        catch (Exception e) {
            System.err.println("Error during configfile reading.");
            e.printStackTrace();
            System.exit(1);
        }

        // Aggiunge il gestore di interruzioni
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Esegue la chiusura controllata
            shutdown(true);
        }));

        try {
            begin();
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
        }
    }
}
