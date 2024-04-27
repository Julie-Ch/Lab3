import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Classe che gestisce la comunicazione con un singolo client tramite socket.
 */
public class ClientHandler implements Runnable {

    /** Socket associato al client per la comunicazione */
    private final Socket clientSocket;
    /** Servizio di autenticazione */
    private final AuthenticationService authservice;
    /** Servizio di gestione degli hotel */
    private final HotelService hotelService;
    /** Codici Unicode per emoji */
    String hand = "\uD83D\uDC4B";
    String soap = "\uD83E\uDDFC";
    String pin = "\uD83D\uDCCD";
    String sofa = "\uD83D\uDECB";
    String hundred = "\uD83D\uDCAF";
    String star = "\u2B50";  
    String tick = "\u2705";        
    String Hotel = "\uD83C\uDFE8";  
    String City = "\uD83C\uDFD9";

    // Logger per la registrazione degli eventi
    // private final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    /**
     * Costruttore della classe `ClientHandler`.
     *
     * @param clientSocket  Il socket del client.
     * @param authService   Servizio di autenticazione.
     * @param hotelService  Servizio degli hotel.
     */
    public ClientHandler(Socket clientSocket, AuthenticationService authService, HotelService hotelService) {
        this.clientSocket = clientSocket;
        this.authservice = authService;
        this.hotelService = hotelService;
    }

    /**
     * Logga un errore e stampa un messaggio nel protocollo di comunicazione.
     *
     * @param out  Oggetto PrintWriter per inviare messaggi al client.
     * @param e    Eccezione da registrare e stampare.
     */
    private void logErrorAndPrintMessage(PrintWriter out, Exception e) {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        printProtocol(e.getMessage(), out);
    }

    /**
     * Stampa un messaggio nel protocollo di comunicazione.
     *
     * @param msg  Messaggio da stampare.
     * @param out  Oggetto PrintWriter per inviare messaggi al client.
     */
    private void printProtocol(String msg, PrintWriter out) {
        out.println(msg);
        out.println("");
    }

    /**
     * Gestisce il processo di accesso di un utente.
     *
     * @param user  Utente corrente (se già autenticato).
     * @param in    Oggetto BufferedReader per leggere input dal client.
     * @param out   Oggetto PrintWriter per inviare messaggi al client.
     * @return      L'utente autenticato.
     */
    private User login(User user, BufferedReader in, PrintWriter out) {
        
        User newUser;

        // Controlla se l'utente è già autenticato in questa sessione
        if (user != null) {
            printProtocol("User already logged in", out);
            return user;
        }

        try {
            printProtocol("Insert your username", out);
            String username = in.readLine();
            // Controlla se l'utente esiste come utente registrato
            authservice.checkAlreadyLogin(username);
            // Controlla se l'utente è già autenticato in un'altra sessione
            authservice.checkLogin(username);

            printProtocol("Insert your password", out);
            String password = in.readLine();

            // Usa metodo del servizio di autenticazione
            newUser = authservice.login(username, password);
            printProtocol("Access succeeded", out);
            authservice.printLoggedIn();

        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
            printProtocol("An error occurred", out);
            return user;
        } catch (AuthenticationException e) {
            logErrorAndPrintMessage(out, e);
            return user;
        }
        return newUser;
    }

    /**
     * Gestisce il processo di registrazione di un nuovo utente.
     *
     * @param user  Utente corrente (se già autenticato).
     * @param in    Oggetto BufferedReader per leggere input dal client.
     * @param out   Oggetto PrintWriter per inviare messaggi al client.
     */
    private void signup(User user, BufferedReader in, PrintWriter out) {
        boolean validPassword = false;
        Pattern specialCharacterPattern = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>?]+");

        try {
            // Controlla se l'utente è loggato anche in un'altra sessione
            if (user != null) {
                printProtocol("Cannot create a new account in this session while you are logged in", out);
                return;
            }

            printProtocol("Insert a username", out);
            String username = in.readLine();
            // Controlla se lo username è già presente
            authservice.checkSignup(username);
            // Richiede la password finchè non ne viene inserita una che rispetta i parametri di sicurezza
            do {
                printProtocol("Insert a password: minimum 8 characters and at least one special character", out);
                String password = in.readLine();

                if (password.length() >= 8 && specialCharacterPattern.matcher(password).find()) {
                    validPassword = true;
                    // Usa metodo del servizio di autenticazione per registrare
                    authservice.signup(user, username, password);
                    printProtocol("Signup succeeded", out);

                } else {
                    out.println("Password must be at least 8 characters and contain at least one special character. Please try again.");
                }
            } while (!validPassword);

        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
            printProtocol("An error occurred", out);
        } catch (AuthenticationException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
            logErrorAndPrintMessage(out, e);
        }
    }

    /**
     * Mostra il badge associato a un utente.
     *
     * @param user  Utente corrente (se autenticato).
     * @param out   Oggetto PrintWriter per inviare messaggi al client.
     */
    private void showBadge(User user, PrintWriter out) {
        try {
            printProtocol(authservice.showBadge(user).prettyPrint(), out);
        } catch (AuthenticationException e) {
            logErrorAndPrintMessage(out, e);
        }
    }

    /**
     * Gestisce il processo di logout di un utente.
     *
     * @param user  Utente corrente (se autenticato).
     * @param out   Oggetto PrintWriter per inviare messaggi al client.
     */
    private void logout(User user, PrintWriter out) {
        try {
            // Usa metodo del servizio di autenticazione per fare il logout
            String username = authservice.logout(user);
            printProtocol("Logout. Goodbye " + username + hand, out);
        } catch (AuthenticationException e) {
            logErrorAndPrintMessage(out, e);
        }
    }

    /**
     * Gestisce la ricerca di un hotel.
     *
     * @param out  Oggetto PrintWriter per inviare messaggi al client.
     * @param in   Oggetto BufferedReader per leggere input dal client.
     */
    private void searchHotel(PrintWriter out, BufferedReader in) {
        Hotel h;
        try {
            printProtocol("Insert Hotel " +  Hotel, out);
            String hotel = in.readLine();
            printProtocol("Insert City " + City, out);
            String city = in.readLine();
            // (sa metodo del servizio di gestione degli hotel per la ricerca
            h = hotelService.searchHotel(hotel, city);
            if (h != null) {
                printProtocol(h.printPretty(), out);
            } else printProtocol("Hotel " + "\"" + hotel + "\"" + " in " + city + " not found", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestisce la ricerca di tutti gli hotel in una città.
     *
     * @param out  Oggetto PrintWriter per inviare messaggi al client.
     * @param in   Oggetto BufferedReader per leggere input dal client.
     */
    private void searchAllHotels(PrintWriter out, BufferedReader in) {
        List<Hotel> hotel_list;
        try {
            printProtocol("Insert City " +  City, out);
            // controllo con enum città
            String city = in.readLine();
            // Usa metodo del servizio di gestione degli hotel per la ricerca
            hotel_list = hotelService.searchAllHotels(city);
            if (!hotel_list.isEmpty()) {
                for (Hotel hotel : hotel_list) {
                    out.println(hotel.printPretty());
                }
                out.println("");
            } else printProtocol(city + " not found", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestisce l'inserimento di una recensione per un hotel.
     *
     * @param user  Utente corrente (se autenticato).
     * @param out   Oggetto PrintWriter per inviare messaggi al client.
     * @param in    Oggetto BufferedReader per leggere input dal client.
     */
    private void insertReview(User user, PrintWriter out, BufferedReader in) {
        try {
            if (user == null) {
                printProtocol("User must be logged in to post a review", out);
                return;
            }
            printProtocol("Insert Hotel " + Hotel, out);
            String hotel = in.readLine();
            printProtocol("Insert City " + City, out);
            String city = in.readLine();
            // Usa metodo del servizio di gestione degli hotel per la ricerca
            Hotel h = hotelService.searchHotel(hotel, city);
            if (h == null) {
                printProtocol("Hotel not found" , out);
                return;
            } 
            float rate;
            // Richiede l'inserimento del rate finché non vengono forniti valori validi
            while (true) {
                printProtocol("Insert a synthetic review from 0 to 5 " + star + " for the hotel", out);
                try {
                    rate = (float) Double.parseDouble(in.readLine());
                    if (rate >= 0 && rate <= 5) {
                        break; // Esci dal ciclo se il valore è valido
                    } else {
                        printProtocol("Please enter a rate between 0 and 5", out);
                    }
                } catch (NumberFormatException e) {
                    printProtocol("Invalid input. Please enter a numeric value for the rate", out);
                }
            }

            // Richiede l'inserimento delle ratings finché non vengono forniti valori validi
            /*float[] floatRate = new float[4];
            while (true) {
                printProtocol("Insert rating from 0 to 5 for " + "1) " + soap + " Cleaning, " +  "2) " + pin + " Position, " + "3) " + sofa + " Services, " + "4) " + hundred + " Quality", out);
                String[] ratings = in.readLine().split(" ");

                if (ratings.length >= 4) {
                    boolean validInput = true;
                    for (int i = 0; i < 4; i++) {
                        try {
                            floatRate[i] = (float) Double.parseDouble(ratings[i]);
                            if (floatRate[i] < 0 || floatRate[i] > 5) {
                                validInput = false;
                                printProtocol("Please enter ratings between 0 and 5", out);
                                //break;
                            }
                        } catch (NumberFormatException e) {
                            validInput = false;
                            printProtocol("Invalid input. Please enter numeric values for the ratings", out);
                            break;
                        }
                    }

                    if (validInput) {
                        break; // Esce dal ciclo se tutti i valori sono validi
                    }
                } else {
                    printProtocol("Not enough ratings provided. Please provide ratings for cleaning, position, services, and quality", out);
                }
            }*/
            float[] floatRate = new float[4];
            String[] categories = new String[]{soap + " Cleaning", pin + " Position", sofa + " Services", hundred + " Quality"};
            for (int i = 0; i < 4; i++) {
                while (true) {
                    try {
                        printProtocol("Enter rating between 0 and 5 for " + categories[i], out);
                        floatRate[i] = Float.parseFloat(in.readLine());
                        if (floatRate[i] < 0 || floatRate[i] > 5) {
                            printProtocol("Please enter ratings between 0 and 5", out);
                        } else {
                            break; // Esce dal ciclo se il valore è valido
                        }
                    } catch (NumberFormatException e) {
                        printProtocol("Invalid input. Please enter numeric values for ratings", out);
                    }
                }
            }
            

            Review r = new Review(user.getUsername(), h.getName(), rate, new Ratings(floatRate[0], floatRate[1], floatRate[2], floatRate[3]));
            hotelService.writeReview(user, h, r);
            user.setNumber_review();
            printProtocol("Review posted " + tick, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo che gestisce l'esecuzione del thread del client.
     */
    @Override
    public void run() {
        User user = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            int exit = 0;

            while (exit == 0) {
                String actionStr = in.readLine();
                int action = Integer.parseInt(actionStr.trim()); // Converte la stringa in un intero

                switch (action) {
                    case 1: // signup
                        signup(user, in, out);
                        break;
                    case 2: // login
                        user = login(user, in, out);
                        break;
                    case 3: // show badge
                        showBadge(user, out);
                        break;
                    case 4: // search hotel
                        searchHotel(out, in);
                        break;
                    case 5: // search all hotels
                        searchAllHotels(out, in);
                        break;
                    case 6: // insert review
                        insertReview(user, out, in);
                        break;
                    case 7: // logout
                        logout(user, out);
                        user = null;
                        break;
                    case 8: // exit
                        if (user != null) {
                            printProtocol("Goodbye " + user.getUsername() + " " + hand, out);
                            logout(user, out);
                            user = null;
                        } else printProtocol("Goodbye visitor " + hand, out);
                        exit = 1;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + action);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
