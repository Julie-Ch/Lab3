import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

/**
 * La classe HotelService gestisce le operazioni correlate agli hotel, inclusa la lettura/scrittura da file JSON,
 * l'aggiornamento delle classifiche e l'invio di notifiche tramite UDP.
 */
public class HotelService {

    private final ConcurrentHashMap<Integer, Hotel> hotelCache = new ConcurrentHashMap<>();
    //private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Object lock = new Object();
    private final ConcurrentHashMap<String, Hotel> rankCache = new ConcurrentHashMap<>();
    //private boolean firstUpdate = true;
    
    private final String hotel_file;
    private final String UDP_port;
    private final String UDP_addr;

    /**
     * Costruttore della classe HotelService.
     *
     * @param hotel_file   Percorso del file JSON contenente le informazioni sugli hotel.
     * @param UDP_addr     Indirizzo IP per l'invio di notifiche UDP.
     * @param UDP_port     Porta per l'invio di notifiche UDP.
     */
    public HotelService(String hotel_file, String UDP_addr, String UDP_port) {
        this.hotel_file = hotel_file;
        this.UDP_port = UDP_port;
        this.UDP_addr = UDP_addr;
    }

    /**
     * Invia un messaggio UDP a un indirizzo specifico e una porta specifica.
     *
     * @param message    Il messaggio da inviare.
     * @param ipAddress  L'indirizzo IP di destinazione.
     * @param port       La porta di destinazione.
     */
    private void sendUDPMessage(String message, String ipAddress, int port) {
        try {
            // Crea un nuovo socket Datagram
            DatagramSocket socket = new DatagramSocket();
            InetAddress group = InetAddress.getByName(ipAddress);
            // Converte il messaggio in un array di byte utilizzando l'UTF-8
            byte[] msg = message.getBytes(StandardCharsets.UTF_8);
            // Crea un pacchetto Datagram contenente il messaggio, l'indirizzo e la porta
            DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port); 
            // Invia il pacchetto Datagram attraverso il socket
            socket.send(packet);
            // Chiude il socket dopo l'invio del messaggio
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Legge e restituisce un oggetto Ratings a partire dal lettore JSON fornito.
     *
     * @param reader Il lettore JSON da cui leggere le valutazioni.
     * @return Un oggetto Ratings con le valutazioni lette.
     * @throws IOException Se si verificano errori durante la lettura dal lettore JSON.
     */
    private Ratings readRatings(JsonReader reader) throws IOException {
        // Crea un nuovo oggetto Ratings per immagazzinare le valutazioni
        Ratings tempRatings = new Ratings();
        // Inizia la lettura dell'oggetto JSON contenente le valutazioni
        reader.beginObject();
        while (reader.hasNext()) {
            // Legge il nome della valutazione (cleaning, position, services, quality)
            String ratingName = reader.nextName();
            // Legge il valore della valutazione
            float ratingValue = (float) reader.nextDouble();
            // Assegna il valore della valutazione all'elemento corrispondente
            switch (ratingName) {
                case "cleaning":
                    tempRatings.setCleaning(ratingValue);
                    break;
                case "position":
                    tempRatings.setPosition(ratingValue);
                    break;
                case "services":
                    tempRatings.setServices(ratingValue);
                    break;
                case "quality":
                    tempRatings.setQuality(ratingValue);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + ratingName);
            }
        }
        // Conclude la lettura dell'oggetto JSON delle valutazioni
        reader.endObject();
        // Restituisce l'oggetto Ratings completo con le valutazioni lette
        return tempRatings;
    }
    
    /**
     * Legge una recensione dal lettore JSON fornito e restituisce un oggetto Review corrispondente.
     *
     * @param reader        Il lettore JSON da cui leggere la recensione.
     * @return              Un oggetto Review con le informazioni lette.
     * @throws IOException  Se si verificano errori durante la lettura dal lettore JSON.
     * @throws ParseException Se si verificano errori durante la conversione della data.
     */
    private Review readReview(JsonReader reader) throws IOException, ParseException {

        Review tempReview = new Review();
        // Inizia a leggere l'oggetto JSON
        reader.beginObject();  
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy, h:mm:ss a");
        // Itera attraverso gli elementi dell'oggetto JSON
        while (reader.hasNext()) {
            String name = reader.nextName();
            // Switch basato sul nome dell'elemento corrente
            switch (name) {
                // Se l'elemento è "user", imposta il nome utente nella review
                case "user":
                    tempReview.setUser(reader.nextString());
                    break;      
                // Se l'elemento è "rate", imposta il valore del rate nella review
                case "rate":
                    tempReview.setrate((float) reader.nextDouble());
                    break; 
                // Se l'elemento è "ratings", legge e imposta le valutazioni
                case "ratings":
                    reader.beginObject();
                    float cleaning = 0, position = 0, services = 0, quality = 0;
                    while (reader.hasNext()) {
                        // Ottiene il nome e il valore della valutazione
                        String ratingName = reader.nextName();
                        float ratingValue = ((float) reader.nextDouble());
                        // Switch basato sul nome del rating
                        switch (ratingName) {
                            case "cleaning":
                                cleaning = ratingValue;
                                break;
                            case "position":
                                position = ratingValue;
                                break;
                            case "services":
                                services = ratingValue;
                                break;
                            case "quality":
                                quality = ratingValue;
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + ratingName);
                        }
                    }
                    reader.endObject();
                    // Crea un oggetto Ratings con le valutazioni lette
                    Ratings ratings = new Ratings(cleaning, position, services, quality);
                    // Imposta le valutazioni nella review
                    tempReview.setRatings(ratings);
                    break;
                // Se l'elemento è "date", legge e imposta la data nella review
                case "date":
                    tempReview.setDate(formatter.parse(reader.nextString()));
                    break;
                // Se l'elemento non è riconosciuto, salta il suo valore
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject(); 
        // Restituisce l'oggetto Review completato
        return tempReview;
    }
    

    /**
     * Legge un hotel dal lettore JSON fornito e restituisce un oggetto Hotel corrispondente.
     *
     * @param reader        Il lettore JSON da cui leggere l'hotel.
     * @return              Un oggetto Hotel con le informazioni lette.
     * @throws IOException  Se si verificano errori durante la lettura dal lettore JSON.
     * @throws ParseException Se si verificano errori durante la conversione della data.
     */
    private Hotel readHotel(JsonReader reader) throws IOException, ParseException {

        Hotel tempHotel = new Hotel();
    
        // Itera attraverso gli elementi dell'oggetto JSON
        while (reader.hasNext()) {
            String name = reader.nextName();
            // Switch basato sul nome dell'elemento corrente
            switch (name) {
                case "id":
                    tempHotel.setId(reader.nextInt());
                    break;
                case "name":
                    tempHotel.setName(reader.nextString());
                    break;
                case "description":
                    tempHotel.setDescription(reader.nextString());
                    break;
                case "city":
                    tempHotel.setCity(reader.nextString());
                    break;
                case "phone":
                    tempHotel.setPhone(reader.nextString());
                    break;
                case "rate":
                    tempHotel.setRate((float) reader.nextDouble());
                    break;
                case "score":
                    tempHotel.setScore((float) reader.nextDouble());
                    break;
                case "ratings":
                    Ratings ratings = readRatings(reader);
                    tempHotel.setRatings(ratings);
                    break;
                case "Number_reviews":
                    tempHotel.setNumber_reviews(reader.nextInt());
                    break;
                case "services":
                    List<String> services = new ArrayList<>();
                    reader.beginArray();
                    while (reader.hasNext()) { // Legge ogni elemento dell'array di servizi
                        services.add(reader.nextString());
                    }
                    reader.endArray();
                    tempHotel.setServices(services);
                    break;
                case "reviews":
                    reader.beginArray();
                    while (reader.hasNext()) {
                        // Legge la review
                        Review review = readReview(reader);
                        tempHotel.setReview(review);
                    }
                    reader.endArray();
                    break;
                // Se l'elemento non è riconosciuto, salta il suo valore
                default:
                    reader.skipValue();
                    break;
            }
        }
        // Restituisce l'oggetto Hotel completato
        return tempHotel;
    }
    

     /**
     * Cerca tutti gli hotel in una determinata città leggendo il file JSON degli hotel.
     *
     * @param city  La città di cui cercare gli hotel.
     * @return      Una lista di hotel nella città specificata.
     * @throws IOException  Se si verificano errori durante la lettura del file JSON.
     */
    protected List<Hotel> searchAllHotels(String city) throws IOException {
        // Lista per memorizzare gli hotel corrispondenti alla città
        List<Hotel> hotel_list = new ArrayList<>();
        boolean cityFound = false;
    
        // Utilizza un blocco sincronizzato per garantire la correttezza in contesti multithreading
        synchronized(lock) {
            try (JsonReader reader = new JsonReader(new FileReader(hotel_file))) {
                // Inizia a leggere l'array di hotel nel file JSON
                reader.beginArray();
                while (reader.hasNext()) {
                    reader.beginObject();
                    // Legge l'oggetto hotel corrente e lo inserisce nella lista
                    Hotel tempHotel = readHotel(reader);
                    reader.endObject();
                    // Verifica se l'hotel appartiene alla città specificata
                    if (tempHotel.getCity().toLowerCase(Locale.ROOT).equals((city).toLowerCase(Locale.ROOT))) {
                        // Aggiunge l'hotel alla lista
                        hotel_list.add(tempHotel);
                        // Imposta il flag per indicare che la città è stata trovata
                        cityFound = true;
                    } else if (cityFound) {
                        // Se la città è stata trovata e l'hotel successivo non appartiene alla città, termina la lettura
                        reader.close();
                        return hotel_list;
                    }
                }
                reader.endArray();
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                // Rilascia eventuali risorse o lock nel blocco finally
                //lock.readLock().unlock();
            }
        }
        // Restituisce la lista di hotel corrispondenti alla città
        return hotel_list;
    }

    /**
     * Trova un hotel per nome e città nella cache o leggendolo dal file JSON degli hotel.
     *
     * @param hotelName  Il nome dell'hotel.
     * @param city       La città dell'hotel.
     * @return           L'oggetto Hotel corrispondente o null se non trovato.
     * @throws IOException  Se si verificano errori durante la lettura del file JSON.
     */
    protected Hotel findHotelByNameAndCity(String hotelName, String city) {
        for (Hotel hotel : hotelCache.values()) {
            if (hotel.getName().equalsIgnoreCase(hotelName) && hotel.getCity().equalsIgnoreCase(city)) {
                return hotel;
            }
        }
        return null; // Ritorna null se nessun hotel corrisponde al nome e alla città forniti
    }

    /**
    *  Trova un hotel per nome e città nella cache o leggendolo dal file JSON degli hotel.
    * @param hotelName  Il nome dell'hotel.
    * @param city       La città dell'hotel.
    * @return           L'oggetto Hotel corrispondente o null se non trovato.
    * @throws IOException  Se si verificano errori durante la lettura del file JSON.
    */
    protected Hotel searchHotel(String hotelName, String city) throws IOException {
        // Utilizza un blocco sincronizzato per garantire la correttezza in contesti multithreading
        synchronized(lock) {
            try {
                // Cerca l'hotel nella cache
                Hotel cachedHotel = findHotelByNameAndCity(hotelName, city);
                if (cachedHotel != null) {
                    return cachedHotel;
                }
                // Apre il lettore JSON dal file
                try (JsonReader reader = new JsonReader(new FileReader(hotel_file))) {
                    Hotel tempHotel = null;
                    // Inizia a leggere l'array di hotel nel file JSON
                    reader.beginArray();
                    // Itera attraverso gli oggetti hotel nel file JSON
                    while (reader.hasNext()) {
                        reader.beginObject();
                        // Legge l'oggetto hotel corrente e lo inserisce nella cache
                        Hotel hotel = readHotel(reader);
                        reader.endObject();
                        // Verifica se l'hotel corrente corrisponde al nome e alla città cercati
                        if (hotel.getName().equalsIgnoreCase(hotelName) && hotel.getCity().equalsIgnoreCase(city)) {
                            tempHotel = hotel;
                            break;
                        }
                    }
                    // Se l'hotel non è stato trovato, termina la lettura
                    if (tempHotel == null) {
                        reader.endArray();
                        return null;
                    }
                    // Inserisce l'hotel trovato nella cache
                    hotelCache.put(tempHotel.getId(), tempHotel);
                    // Restituisce l'hotel trovato
                    return tempHotel;
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
            } finally {
                // Rilascia eventuali risorse o lock nel blocco finally
                //lock.readLock().unlock();
            }
        }
        // Se non viene trovato alcun hotel, restituisce null
        return null;
    }

    /**
     * Aggiorna il file JSON degli hotel e la cache delle classifiche.
     */
    protected void updateJsonFileAndRankCache() {
        // StringBuilder per registrare eventuali cambiamenti nelle classifiche degli hotel
        StringBuilder changes = new StringBuilder();
        // Timestamp di inizio aggiornamento
        System.out.println("Update started at: " + LocalDateTime.now());

        // Blocco sincronizzato per garantire l'accesso sicuro alle risorse condivise
        synchronized (lock) {
            // Debug: Stampa la cache degli hotel prima dell'aggiornamento
            System.out.println("hotel cache: " + hotelCache);
            try {
                // Crea un oggetto Gson per la manipolazione dei dati JSON
                Gson gson = new GsonBuilder()
                        .setDateFormat("MMM dd, yyyy, h:mm:ss a").setPrettyPrinting().create();
                // Legge file e converte in una lista di oggetti Hotel
                Reader reader = new FileReader(hotel_file);
                List<Hotel> hotels = null;

                //hotels = gson.fromJson(reader, new TypeToken<List<Hotel>>() {}.getType());

                Hotel[] deserialized = gson.fromJson(reader, Hotel[].class);
                hotels = Arrays.asList(deserialized);
 
                reader.close();

                // Ottiene gli ID degli hotel presenti nella cache
                Set<Integer> cachedHotelIds = hotelCache.keySet();

                // Aggiunge alla cache gli hotel presenti nel file ma non nella cache
                for (Hotel hotel : hotels) {
                    if (!cachedHotelIds.contains(hotel.getId())) {
                        hotelCache.put(hotel.getId(), hotel);
                    }
                }

                // Aggiorna le informazioni degli hotel nel file con quelle presenti nella cache
                for (Map.Entry<Integer, Hotel> entry : hotelCache.entrySet()) {
                    int id = entry.getKey();
                    Hotel cachedHotel = entry.getValue();

                    for (int i = 0; i < hotels.size(); i++) {
                        if (hotels.get(i).getId() == id) {
                            hotels.set(i, cachedHotel);
                            break;
                        }
                    }
                }

                // Calcola del punteggio aggiornato per ogni hotel
                for (Hotel hotel : hotels) {
                    hotel.calculateScore();
                }
                // Raggruppa gli hotel per città e ordina per punteggio
                Map<String, PriorityQueue<Hotel>> cityToHotelsMap = new HashMap<>();
                for (Hotel hotel : hotels) {
                    cityToHotelsMap.putIfAbsent(hotel.getCity(), new PriorityQueue<>(Comparator.comparing(Hotel::getScore).reversed()));
                    cityToHotelsMap.get(hotel.getCity()).add(hotel);
                }

                // Crea una mappa contenente gli hotel con il punteggio più alto per ogni città
                Map<String, Hotel> cityToTopHotelMap = new HashMap<>();
                for (Map.Entry<String, PriorityQueue<Hotel>> entry : cityToHotelsMap.entrySet()) {
                    String city = entry.getKey();
                    PriorityQueue<Hotel> cityHotels = entry.getValue();
                    cityToTopHotelMap.put(city, cityHotels.peek());
                }

                // Verifica le modifiche nelle classifiche e registra i cambiamenti
                for (Map.Entry<String, Hotel> entry : cityToTopHotelMap.entrySet()) {
                    String city = entry.getKey();
                    Hotel newHotel = entry.getValue();

                    if (newHotel != null && (!rankCache.containsKey(city) || rankCache.get(city).getId() != newHotel.getId())) {
                        changes.append("\n1st ranked Hotel in ").append(city).append(" is now ").append(newHotel.getName());
                    }
                }

                // Aggiorna la cache delle classifiche
                rankCache.clear();
                rankCache.putAll(cityToTopHotelMap);

                // Converte la lista di hotel aggiornata in una stringa JSON
                String json = gson.toJson(hotels);

                // Scrive la stringa JSON nel file
                try (FileWriter writer = new FileWriter(hotel_file)) {
                    writer.write(json);
                } catch (IOException e) {
                    System.out.println("Error writing to file: " + e.getMessage());
                    e.printStackTrace();
                }

                // Svuota della cache degli hotel
                hotelCache.clear();

            } catch (IOException e) {
                System.out.println("Error reading file: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Timestamp di fine aggiornamento
                System.out.println("Update ended at: " + LocalDateTime.now());
                // Invia una notifica tramite UDP se ci sono stati cambiamenti nelle classifiche
                if (changes.length() >0) {
                    sendUDPMessage(changes.toString(), UDP_addr, Integer.parseInt(UDP_port));
                }
            }
        }
    }

    /**
     * Scrive una recensione di un hotel, aggiornando il punteggio e la cache degli hotel.
     *
     * @param user    L'utente che scrive la recensione.
     * @param hotel   L'hotel per cui viene scritta la recensione.
     * @param review  La recensione scritta.
     * @throws IOException  Se si verificano errori durante la scrittura del file JSON.
     */
    protected void writeReview(User user, Hotel hotel, Review review) throws IOException{

        // Setta i vari parametri della recensione
        hotel.setRate(review.getrate());
        hotel.setRatings(review.getRatings());
        hotel.setReview(review);
        hotel.setNumber_reviews();
        // Mette l'hotel nella cache
        hotelCache.put(hotel.getId(), hotel);
        System.err.println(hotelCache);
    }
}

