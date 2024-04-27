import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Classe che gestisce i servizi del cliente per Hotelier tramite connessione TCP e multicast UDP.
 */
public class HOTELIERCustomerClientService {

    /** Il socket multicast UDP per la ricezione di notifiche automatiche. */
    private MulticastSocket multicastSocket;
    /** Lo scanner per l'input dell'utente. */
    private final Scanner scanner = new Scanner(System.in);
    /** L'indirizzo del server a cui il client si connetterà. */
    private final String serverAddress;
    /** La porta del server a cui il client si connetterà tramite connessione TCP. */
    private final int serverPort;
    /** La porta UDP per la ricezione di notifiche automatiche. */
    private final String UDP_port;
    /** L'indirizzo UDP per la ricezione di notifiche automatiche. */
    private final String UDP_addr;
    /** Oggetto di blocco per la CLI. */
    private final Object CLILock = new Object();
    /** Flag indicante se il servizio è in esecuzione. */
    private volatile boolean running = true;
    /** Codici escape ANSI per i colori */
    String blue = "\u001B[34m";
    String purple = "\u001B[35m";
    String red = "\u001B[31m";
    //private BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));


    /** Classe per i messaggi di Errore */
    private static class ErrorMessages {
        private static final String USER_ALREADY_LOGGED_IN_SESSION = "User already logged in another session";
        private static final String USER_ALREADY_LOGGED_IN = "User already logged in";
        private static final String USER_LOGGED_SIGNUP = "Cannot create a new account in this session while you are logged in";
        private static final String USER_NOT_EXIST = "This username doesn't exist";
        private static final String AUTHENTICATION_FAILED = "Password must be at least 8 characters and contain at least one special character. Please try again.";
        private static final String USER_ALREADY_EXIST = "User already exist";
        private static final String MUST_BE_LOGGED = "User must be logged in to post a review";
        private static final String HOTEL_NOT_FOUND = "Hotel not found";
        private static final String USER_NOT_LOGGED = "User not logged in this session";
        private static final String NOT_VALID_RATE_PARAMETER = "Please enter a rate between 0 and 5";
        private static final String NOT_VALID_RATE_PARAMETER_1 = "Invalid input. Please enter a numeric value for the rate";
        private static final String NOT_VALID_RATINGS_PARAMETER = "Please enter ratings between 0 and 5";
        private static final String NOT_VALID_RATINGS_PARAMETER_1 = "Invalid input. Please enter numeric values for ratings";
        private static final String NOT_ENOUGH_PARAMETERS = "Not enough ratings provided. Please provide ratings for cleaning, position, services, and quality";
        private static final String ERROR = "An error occurred";
    }

    /**
     * Costruttore della classe HOTELIERCustomerClientService.
     *
     * @param serverAddress Indirizzo del server.
     * @param serverPort Porta del server.
     * @param UDP_addr Indirizzo IP del gruppo multicast.
     * @param UDP_port Porta del gruppo multicast.
     */
    public HOTELIERCustomerClientService(String serverAddress, int serverPort, String UDP_addr, String UDP_port){
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.UDP_addr = UDP_addr;
        this.UDP_port = UDP_port;
    }

    /**
     * Stampa a schermo utilizzando sequenze di escape ANSI
     * 
     * @param colorStart
     * @param msg
     */
    private void printColored(String colorStart, String msg){
        
        System.out.println(colorStart +  msg +  "\u001B[0m");
    }

    
      /**
     * Unisce il socket multicast al gruppo specificato.
     *
     * @param UDP_addr Indirizzo IP del gruppo multicast.
     * @param UDP_port Porta del gruppo multicast.
     */
    private void joinMulticastGroup(String UDP_addr, String UDP_port) {

        try {
            InetAddress multicastGroup = InetAddress.getByName(UDP_addr);
            // Crea un socket multicast e lo unisce al gruppo
            multicastSocket = new MulticastSocket(Integer.parseInt(UDP_port));
            multicastSocket.joinGroup(multicastGroup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Riceve un messaggio multicast e lo stampa a console.
     * Gestisce un timeout per evitare blocchi indefiniti durante l'attesa del messaggio.
     */
    private void receiveMulticastMessage() {
        // Crea un array di byte 
        byte[] buffer = new byte[2048];
        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
        try {
            multicastSocket.setSoTimeout(1000); // Imposta un timeout di 1 secondo
            multicastSocket.receive(messageIn);
            String message = new String(messageIn.getData(), 0, messageIn.getLength(), StandardCharsets.UTF_8);
            synchronized (CLILock) {
                LocalDateTime now = LocalDateTime.now();
                String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                // Stampa a schermo la notifica
                printColored(red, "[Automatic Notification] " + formattedDate + ": " + message);
            }
           // System.in.skip(System.in.available());
        } catch (SocketTimeoutException e) {
            // Non fare nulla in caso di timeout
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Avvia un thread per ascoltare i messaggi multicast in modo asincrono.
     */
    private void startListening() {
        new Thread(() -> {
            joinMulticastGroup(UDP_addr, UDP_port);
            while (running) {
                receiveMulticastMessage();
            }
        }).start();
    }

    /**
     * Attende e gestisce le risposte dal server.
     *
     * @param in BufferedReader da cui leggere la risposta.
     * @return Il valore risultante dalla risposta.
     * @throws IOException Se si verificano errori durante la lettura.
     */
    private int wait_response(BufferedReader in) throws IOException {
        String line;
        int v = 0;

        boolean firstLine = true;

        line = in.readLine();

        while (!line.trim().isEmpty()) {
            if (firstLine) {
                printColored(blue, "[SERVER]: " + line);
                firstLine = false;
            } else {
                // Stampa le linee successive
                printColored(blue, line);
            }

            // Gestione dei messaggi di errore
            if (line.equals(ErrorMessages.AUTHENTICATION_FAILED) || line.equals(ErrorMessages.MUST_BE_LOGGED) || line.equals(ErrorMessages.HOTEL_NOT_FOUND)
             || line.equals(ErrorMessages.USER_NOT_LOGGED) || line.equals(ErrorMessages.ERROR)) {
                v = -1;
            }
            if (line.equals(ErrorMessages.USER_ALREADY_EXIST) || line.equals(ErrorMessages.USER_ALREADY_LOGGED_IN) || line.equals(ErrorMessages.USER_ALREADY_LOGGED_IN_SESSION)
                    || line.equals(ErrorMessages.USER_LOGGED_SIGNUP) || line.equals(ErrorMessages.USER_NOT_EXIST)) {
                v = 1;
            }

            // Messaggi di errore riguardanti parametri delle recensioni
            if(line.equals(ErrorMessages.NOT_VALID_RATE_PARAMETER) || line.equals(ErrorMessages.NOT_VALID_RATE_PARAMETER_1) ||
            line.equals(ErrorMessages.NOT_VALID_RATINGS_PARAMETER) || line.equals(ErrorMessages.NOT_VALID_RATINGS_PARAMETER_1) || line.equals(ErrorMessages.NOT_ENOUGH_PARAMETERS)) 
                v = 2;

            // Leggi la prossima linea
            line = in.readLine();
        }
        return v;
    }

    /**
     * Gestisce il processo di registrazione dell'utente lato client.
     *
     * @param action Tipo di azione da eseguire.
     * @param in BufferedReader per leggere le risposte dal server.
     * @param out PrintWriter per inviare dati al server.
     * @throws IOException In caso di errori di I/O.
    */
    private void signup(int action, BufferedReader in, PrintWriter out) throws IOException{

        synchronized (CLILock) {
            out.println(action);
            int r = wait_response(in);
            if (r == 1) {
                return;
            }
    
            // Leggi l'username e invialo al server
            readInputAndSendToServer(out);
            r = wait_response(in);

            if (r == 1) {
                return;
            }
    
            while (true) {
                // Leggi la password e inviala al server
                readInputAndSendToServer(out);
                r = wait_response(in);
    
                // Se la password non soddisfa i criteri, entra in un ciclo finchè non ne viene inserita una corretta
                if (r == -1) {
                    continue;
                }
                // Se la risposta del server è buona, interrompi il ciclo
                if (r != -1) {
                    return;
                }
                break;
            }
        }
    }
    
    
    /**
     * Gestisce l'autenticazione dell'utente.
     *
     * @param action Tipo di azione da eseguire.
     * @param in BufferedReader per leggere le risposte dal server.
     * @param out PrintWriter per inviare dati al server.
     * @param UDP_addr Indirizzo IP del gruppo multicast.
     * @param UDP_port Porta del gruppo multicast.
     * @throws IOException In caso di errori di I/O.
    */
    private void auth(int action, BufferedReader in, PrintWriter out, String UDP_addr, String UDP_port) throws IOException {

        synchronized (CLILock){
            out.println(action);
            int r = wait_response(in);
            if (r == 1) {return;}

            while(true) {
                readInputAndSendToServer(out);
                r = wait_response(in);

                if (r == 1) {return;}

                readInputAndSendToServer(out);
                r = wait_response(in);

                if(r == -1){
                    continue;
                }
                break;
            }
        }
        startListening();
    }

    
   /**
     * Invia una richiesta di ricerca di un hotel al server e gestisce la risposta.
     *
     * @param action Tipo di azione da eseguire.
     * @param in BufferedReader per leggere le risposte dal server.
     * @param out PrintWriter per inviare dati al server.
     * @throws IOException In caso di errori di I/O durante la comunicazione con il server.
     */
    private void search_hotel(int action, BufferedReader in, PrintWriter out) throws IOException {

        synchronized (CLILock) {
            out.println(action);
            wait_response(in);
            readInputAndSendToServer(out);
            wait_response(in);
            readInputAndSendToServer(out);
            wait_response(in);
        }
    }

    /**
     * Invia una richiesta di ricerca di tutti gli hotel al server e gestisce la risposta.
     *
     * @param action Tipo di azione da eseguire.
     * @param in BufferedReader per leggere le risposte dal server.
     * @param out PrintWriter per inviare dati al server.
     * @throws IOException In caso di errori di I/O durante la comunicazione con il server.
     */
    private void search_all_hotel(int action, BufferedReader in, PrintWriter out) throws IOException {

        synchronized (CLILock) {
            out.println(action);
            wait_response(in);
            readInputAndSendToServer(out);
            wait_response(in);
        }
    }

    /**
     * Invia una richiesta di inserimento di una recensione al server e gestisce la risposta.
     *
     * @param action Tipo di azione da eseguire.
     * @param in BufferedReader per leggere le risposte dal server.
     * @param out PrintWriter per inviare dati al server.
     * @throws IOException In caso di errori di I/O durante la comunicazione con il server.
     */
    private void insert_review(int action, BufferedReader in, PrintWriter out) throws IOException {

        int r;
        synchronized (CLILock){
            out.println(action);
            if(wait_response(in) == -1) return;
            //quale hotel
            readInputAndSendToServer(out);
            wait_response(in);
            //quale città
            readInputAndSendToServer(out);
            r = (wait_response(in));
            if(r == -1) return;
            //recensione sintetica
            readInputAndSendToServer(out);
            r = (wait_response(in));
            while(r != 0){
                if(wait_response(in) == -1) return;
                readInputAndSendToServer(out);
                r = (wait_response(in));
            }
            //ratings
            for(int i = 0; i < 4; i++){
                readInputAndSendToServer(out);
                r = wait_response(in);
                while(r != 0){
                    if(wait_response(in) == -1) return;
                    readInputAndSendToServer(out);
                    r = (wait_response(in));
                }
            }
        }
    }


    /**
     * Rileva l'input da console e lo invia al server.
     *
     * @param out PrintWriter per inviare dati al server.
     */
    private void readInputAndSendToServer(PrintWriter out) {
        String input = scanner.nextLine();
        out.println(input);
    }

    /**
     * Inizia l'esecuzione del client Hotelier.
     */
    protected void begin() {
        
        try (
                Socket socket = new Socket(serverAddress, serverPort);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            int action;
            System.out.println("\u001B[31m******************************************");
                    System.out.println("*        \u001B[33mWelcome to Hotelier! 😁\u001B[31m         *");
                    System.out.println("******************************************\u001B[0m");
                

            while (true) {
                
                System.out.println("\u001B[34mPlease choose an option: [1]Signup, [2]Login, [3]Show badge, [4]Search Hotel, [5]Search all hotels, [6]Insert review, [7]Logout, [8]Exit\u001B[0m");

                while (!scanner.hasNextLine()) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.nextLine(); // scarta l'input non valido
                }
                
                String input = scanner.nextLine();
                
                if (!input.matches("\\d+")) {
                    System.out.println("Invalid input. Please enter a number.");
                    continue;
                }
                
                action = Integer.parseInt(input);

                switch (action) {
                    case 1:
                        printColored(purple, "Option Signup");
                        signup(action, in, out);
                        break;
                    case 2:
                        printColored(purple, "Option Login");
                        auth(action, in, out, UDP_addr, UDP_port);
                        break;
                    case 3:
                        printColored(purple, "Option Show Badge");
                        out.println(action);
                        wait_response(in);
                        break;
                    case 4:
                        printColored(purple, "Option Search Hotel");
                        search_hotel(action, in, out);
                        break;
                    case 5:
                        printColored(purple, "Option Search All Hotels");
                        search_all_hotel(action, in, out);
                        break;
                    case 6:
                        printColored(purple, "Option Insert Review");
                        insert_review(action, in, out);
                        break;
                    case 7:
                        printColored(purple, "Option Logout");
                        out.println(action);
                        int r = wait_response(in);
                        if(r != -1) {
                            multicastSocket.leaveGroup(InetAddress.getByName(UDP_addr));
                        }
                        running = false;
                        break;
                    case 8:
                        printColored(purple, "Option Exit");
                        out.println(action);
                        wait_response(in);
                        running = false;
                        return;
                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Connection Error");
            //e.printStackTrace();
        }
    }
}
