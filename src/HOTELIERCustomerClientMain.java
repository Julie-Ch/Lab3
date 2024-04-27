import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * La classe HOTELIERCustomerClientMain rappresenta il punto di ingresso per avviare il client del sistema Hotelier.
 */
public class HOTELIERCustomerClientMain {

    /** Il percorso del file di configurazione del client. */
    public static final String configFile = "client.properties";
    /** La porta del server a cui il client si connetterà. */
    private static int port;
    /** L'indirizzo del server a cui il client si connetterà. */
    private static String server_address;
    /** La porta UDP per la ricezione di notifiche automatiche. */
    private static String UDP_port;
    /** L'indirizzo UDP per la ricezione di notifiche automatiche. */
    private static String UDP_addr;

    /**
     * Legge le configurazioni dal file di configurazione e inizializza le variabili di connessione del client.
     * @throws IOException Se si verificano errori durante la lettura del file di configurazione.
     */
    private static void readConfig() throws IOException {
        InputStream input = new FileInputStream(configFile);
        Properties prop = new Properties();
        prop.load(input);
        port = Integer.parseInt(prop.getProperty("port"));
        server_address = prop.getProperty("address");
        UDP_addr = prop.getProperty("UDP_addr");
        UDP_port = prop.getProperty("UDP_port");
        input.close();
    }

    /**
     * Punto di ingresso principale per avviare il client Hotelier.
     * @param args Gli argomenti della riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        try {
            // Legge dal file di configurazione
            readConfig();
        } catch (Exception e) {
            System.err.println("Error during configfile reading.");
            e.printStackTrace();
            System.exit(1);
        }

        // Crea istanza del servizio che gestisce il client
        HOTELIERCustomerClientService clientService = new HOTELIERCustomerClientService(server_address, port, UDP_addr, UDP_port);

        try {
            clientService.begin();
            System.exit(0);
        } catch (Exception e) {
            System.out.println("An error occurred");
            System.exit(1);
            //e.printStackTrace();
        }
    }
}
