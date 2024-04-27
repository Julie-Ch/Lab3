import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * Servizio di autenticazione per gestire la registrazione, l'accesso e altre operazioni sugli utenti nel sistema HOTELIER.
 */
public class AuthenticationService {

    // Cache per gli utenti registrati
    private final ConcurrentHashMap<String, User> UsersCache;
    // Cache per gli utenti loggati
    private final ConcurrentHashMap<String, User> loggedInUsers;
    // Lock per scrivere nel file degli utenti
    //private final ReadWriteLock lock;
    private final Object lock = new Object();
    // Path del file signedUpUsers.json
    private final String user_path;

    /**
     * Costruttore della classe `AuthenticationService`.
     *
     * @param user_path Percorso del file degli utenti.
     */
    public AuthenticationService(String user_path) {
        this.user_path = user_path;
        UsersCache = new ConcurrentHashMap<>();
        loggedInUsers = new ConcurrentHashMap<>();
        //lock = new ReentrantReadWriteLock();
    }

    
    /** 
     * Metodo che verifica se un utente già esiste al momento della registrazione
     * @param user
     * @throws AuthenticationException
     * @throws IOException
     */
    protected void checkSignup(String user) throws AuthenticationException, IOException {

        if(UsersCache.containsKey(user) || isUserInFile(user)){
            throw new AuthenticationException("User already exist");
        }
    }
      
    /** 
     * Metodo che verifica se un utente è già loggato quando tenta di loggarsi
     * @param user
     * @return String
     * @throws AuthenticationException
     */
    protected void checkAlreadyLogin (String user) throws AuthenticationException{
        if (loggedInUsers.containsKey(user)){
            throw new AuthenticationException("User already logged in another session");
        }
    }
   
    /** 
     * Metodo che verifica se un utente esiste
     * @param user
     * @throws AuthenticationException
     * @throws IOException
     */
    protected void checkLogin (String user) throws AuthenticationException, IOException {

        if (!UsersCache.containsKey(user) && !isUserInFile(user)){
            throw new AuthenticationException("This username doesn't exist");
        }
    }
   
    /** 
     * Metodo che registra un utente a sistema
     * @param user
     * @param username
     * @param password
     */
    protected void signup(User user, String username, String password) {

        user = new User(username, password);
        UsersCache.put(username, user);
        //printSignedUp();
    }

    
    /** 
     * Metodo che logga l'utente nel sistema
     * @param username
     * @param password
     * @return User
     * @throws AuthenticationException
     * @throws IOException
     */
    protected User login (String username, String password) throws AuthenticationException, IOException {

        User user;
        // Controlla se l'utente è in cache o nel file
        if(UsersCache.containsKey(username)){
            user = UsersCache.get(username);
        }else{
            user = getUserFromFile(username);
        }

        if (user == null) {
            throw new AuthenticationException("This username doesn't exist");
        }

        else if(!user.getPassword().equals(password)){
            throw new AuthenticationException("Password not valid");
        }
        // Inserisce nella cache degli utenti loggati
        loggedInUsers.put(username, user);
        return user;
    }

    
    /** 
     * Metodo che permette all'utente di fare logout
     * @param user
     * @return String
     * @throws AuthenticationException
     */
    protected String logout(User user) throws AuthenticationException {

        if(user == null) throw new AuthenticationException("User not logged in this session");
        loggedInUsers.remove(user.getUsername(), user);
        UsersCache.put(user.getUsername(), user);
        return user.getUsername();
    }

    
    /** 
     * Metodo che restituisce il badge
     * @param user
     * @return Badge
     * @throws AuthenticationException
     */
    protected Badge showBadge(User user) throws AuthenticationException{
        //if(user != null && loggedInUsers.containsKey(user.getUsername())){
        if(user != null){
            return user.getBadge();
        }else{
            throw new AuthenticationException("User is not logged in this session");
        }
    }

    protected void printLoggedIn(){
        System.out.println(loggedInUsers);
    }

    protected void printSignedUp(){
        System.out.println(UsersCache);
    }

    
    /** 
     * Metodo che verifica la presenza di un utente nel file
     * @param username
     * @return boolean
     * @throws IOException
     */
    private boolean isUserInFile(String username) throws IOException {
        // Sincronizzazione sull'oggetto lock per garantire l'accesso sicuro alle risorse condivise
        //lock.readLock().lock();
        synchronized (lock) {
            try {
                // Leggi l'intero file in una stringa
                String content = new String(Files.readAllBytes(Paths.get(user_path)));
    
                // Controlla se la stringa è vuota
                if (content.isEmpty()) {
                    // Il file è vuoto, restituisci un valore predefinito o crea un array JSON vuoto
                    return false;
                }
    
                // Parsa la stringa JSON utilizzando JsonParser
                JsonArray jsonArray = JsonParser.parseString(content).getAsJsonArray();
    
                // Itera su ogni elemento dell'array JSON
                for (JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();
    
                    // Estrai il nome utente dall'oggetto JSON
                    String jsonUsername = jsonObject.get("username").getAsString();
    
                    // Verifica se l'utente corrente è quello cercato
                    if (jsonUsername.equals(username)) {
                        return true;
                    }
                }
            } finally {
                // Rilascia il lock di lettura
                //lock.readLock().unlock();
            }
        }
    
        return false;
    }
    
    /**
     * Metodo che preleva un utente dal file JSON degli utenti.
     *
     * @param username Nome dell'utente da cercare.
     * @return Oggetto User corrispondente o null se l'utente non è stato trovato.
     * @throws IOException In caso di errori durante la lettura del file JSON.
     */
    private User getUserFromFile(String username) throws IOException {
        // Sincronizzazione sull'oggetto lock per garantire l'accesso sicuro alle risorse condivise
        //lock.readLock().lock();
        synchronized (lock) {
            try (JsonReader reader = new JsonReader(new FileReader(user_path))) {
                // Parsa il contenuto del file JSON in un oggetto JsonElement
                JsonElement jsonElement = JsonParser.parseReader(reader);
    
                // Verifica se il JsonElement rappresenta un array JSON
                if (jsonElement.isJsonArray()) {
                    // Ottiene l'array JSON
                    JsonArray array = jsonElement.getAsJsonArray();
    
                    // Itera su ogni elemento dell'array
                    for (JsonElement element : array) {
                        // Ottiene l'oggetto JSON per ciascun elemento
                        JsonObject jsonObject = element.getAsJsonObject();
                        // Estrae il nome utente dall'oggetto JSON
                        String name = jsonObject.get("username").getAsString();
    
                        // Verifica se l'utente corrente è quello cercato
                        if (username.equals(name)) {
                            // Estrae le altre informazioni dell'utente solo se l'username corrisponde
                            String password = jsonObject.get("password").getAsString();
                            int number_review = jsonObject.get("number_review").getAsInt();
                            String badge = null;
                            Date badgeDate = null;
    
                            // Verifica se l'oggetto JSON contiene l'attributo "badge"
                            if (jsonObject.has("badge")) {
                                // Estrai le informazioni sul badge se presente
                                JsonObject badgeObject = jsonObject.getAsJsonObject("badge");
                                badge = badgeObject.get("level").getAsString();
                                // Estrae e converte la data del badge
                                String dateString = badgeObject.get("date").getAsString();
                                SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                                badgeDate = formatter.parse(dateString);
                            }
                            // Restituisce un nuovo oggetto User con le informazioni estratte
                            return new User(name, password, new Badge(Level.valueOf(badge), badgeDate), number_review);
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            finally{
                //lock.readUnlock().lock();
            }
            // Restituisce null se l'utente non è stato trovato
            return null;
        }
    }
    
    
    /** 
     * Metodo che salva gli utenti nel file
     * @param users_file
     */
    protected void saveUsersToFile(File users_file) {
        //System.out.println("DENTRO USER.");
        //lock.writeLock().lock();
        System.out.println("Update user");
        if(UsersCache.isEmpty() && loggedInUsers.isEmpty()){
            System.out.println("No data to save");
            return;
        }
        synchronized(lock){
            try {
                // Crea un oggetto Gson
                Gson gson = new GsonBuilder().setPrettyPrinting().create();;

                // Legge il file JSON
                try (Reader reader = new FileReader(users_file)) {
                    // Converte il file JSON in una lista di utenti
                    //List<User> users = gson.fromJson(reader, new TypeToken<List<User>>(){}.getType());

                    List<User> users;
                    User[] deserialized = gson.fromJson(reader, User[].class);
                    users = Arrays.asList(deserialized);
                    List<User> usersModifiable = new ArrayList<>(users);

                    // Aggiorna le informazioni degli utenti con quelle presenti nella cache
                    for (Map.Entry<String, User> entry : UsersCache.entrySet()) {
                        String username = entry.getKey();
                        User cachedUser = entry.getValue();

                        boolean found = false;
                        // Trova l'utente corrispondente nella lista
                        for (int i = 0; i < usersModifiable.size(); i++) {
                            //System.out.println("Dentro for user");
                            if (usersModifiable.get(i).getUsername().equals(username)) {
                                // Aggiorna le informazioni dell'utente con quelle presenti nella cache
                                usersModifiable.set(i, cachedUser);
                                found = true;
                                break;
                            }
                        }

                        // Se l'utente non è stato trovato nella lista, lo aggiunge
                        if (!found) {
                            System.out.println("Aggiungendo utente alla lista: " + username);
                            usersModifiable.add(cachedUser);
                        }
                    }
                    // Converte la lista di utenti aggiornata in una stringa JSON
                    String json = gson.toJson(usersModifiable);

                    // Scrive la stringa JSON nel file
                    try (FileWriter writer = new FileWriter(users_file)) {
                        //System.out.println("Scrivendo nel file JSON");
                        writer.write(json);
                    } catch (IOException e) {
                        System.out.println("Errore durante la scrittura nel file: " + e.getMessage());
                        e.printStackTrace();
                    }

                // Svuota la cache
                UsersCache.clear();
                } catch (IOException e) {
                    System.out.println("Errore durante la lettura del file: " + e.getMessage());
                    e.printStackTrace();
                }
            } finally {
                //lock.writeLock().unlock();
            }
        }
    }
}
