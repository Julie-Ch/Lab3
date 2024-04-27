import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Classe che rappresenta un hotel con informazioni come ID, nome, descrizione, città, telefono, servizi, tasso, valutazioni e recensioni.
 */
public class Hotel implements Serializable{

    private int id;
    private String name;
    private String description;
    private String city;
    private String phone;
    private List<String> services;
    private float rate;
    private Ratings ratings;
    private ArrayList<Review> reviews;
    private int Number_reviews;
    private float score;

    /** Pesi utilizzati per il calcolo dello score */
    private static final float WEIGHT_QUALITY = 0.4f;
    private static final float WEIGHT_QUANTITY = 0.3f;
    private static final float WEIGHT_ACTUALITY = 0.3f;

    /**
     * Costruttore di default che inizializza le liste e le variabili di tipo numerico.
     */
    public Hotel() {
        this.services = new ArrayList<>();
        this.reviews = new ArrayList<>();
        this.ratings = new Ratings(0.0F, 0.0F, 0.0F, 0.0F);
        this.Number_reviews = 0;
        this.score = 0;
    }

    /**
     * Costruttore con parametri per inizializzare un oggetto Hotel con valori specificati.
     *
     * @param id          ID dell'hotel.
     * @param name        Nome dell'hotel.
     * @param description Descrizione dell'hotel.
     * @param city        Città in cui si trova l'hotel.
     * @param phone       Numero di telefono dell'hotel.
     * @param services    Lista dei servizi dell'hotel.
     * @param rate        Tasso dell'hotel.
     * @param ratings     Valutazioni dell'hotel.
     */
    public Hotel(int id, String name, String description, String city, String phone, List<String> services, float rate, Ratings ratings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.phone = phone;
        this.services = services;
        this.rate = rate;
        this.ratings = ratings;
    }

    // Metodi getter e setter per ogni campo

    /**
     * Restituisce l'ID dell'hotel.
     *
     * @return ID dell'hotel.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID dell'hotel.
     *
     * @param id Nuovo ID dell'hotel.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il nome dell'hotel.
     *
     * @return Nome dell'hotel.
     */
    public String getName() {
        return name;
    }

    /**
     * Imposta il nome dell'hotel.
     *
     * @param name Nuovo nome dell'hotel.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Restituisce la descrizione dell'hotel.
     *
     * @return Descrizione dell'hotel.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Imposta la descrizione dell'hotel.
     *
     * @param description Nuova descrizione dell'hotel.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Restituisce la città in cui si trova l'hotel.
     *
     * @return Città dell'hotel.
     */
    public String getCity() {
        return city;
    }

    /**
     * Imposta la città in cui si trova l'hotel.
     *
     * @param city Nuova città dell'hotel.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Restituisce il numero di telefono dell'hotel.
     *
     * @return Numero di telefono dell'hotel.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Imposta il numero di telefono dell'hotel.
     *
     * @param phone Nuovo numero di telefono dell'hotel.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Restituisce le valutazioni dell'hotel.
     *
     * @return Valutazioni dell'hotel.
     */
    public Ratings getRatings() {
        return ratings;
    }

    /**
     * Imposta le valutazioni dell'hotel.
     *
     * @param ratings Nuove valutazioni dell'hotel.
     */
    public void setRatings(Ratings ratings) {
        // Aggiorna le valutazioni medie
        if (this.ratings.getCleaning() == 0) this.ratings.setCleaning(ratings.getCleaning());
        else this.ratings.setCleaning((ratings.getCleaning() + this.ratings.getCleaning()) / 2);
    
        if (this.ratings.getPosition() == 0) this.ratings.setPosition(ratings.getPosition());
        else this.ratings.setPosition((ratings.getPosition() + this.ratings.getPosition()) / 2);
    
        if (this.ratings.getServices() == 0) this.ratings.setServices(ratings.getServices());
        else this.ratings.setServices((ratings.getServices() + this.ratings.getServices()) / 2);
    
        if (this.ratings.getQuality() == 0) this.ratings.setQuality(ratings.getQuality());
        else this.ratings.setQuality((ratings.getQuality() + this.ratings.getQuality()) / 2);
    }
    

    /**
     * Aggiunge una nuova recensione all'hotel.
     *
     * @param review Nuova recensione da aggiungere.
     */
    public void setReview(Review review) {
        this.reviews.add(review);
    }

    /**
     * Restituisce la lista delle recensioni dell'hotel.
     *
     * @return Lista delle recensioni dell'hotel.
     */
    public ArrayList<Review> getReviews() {
        return this.reviews;
    }

    /**
     * Imposta la lista dei servizi dell'hotel.
     *
     * @param services Nuova lista dei servizi dell'hotel.
     */
    public void setServices(List<String> services) {
        this.services = services;
    }

    /**
     * Restituisce il tasso dell'hotel.
     *
     * @return Tasso dell'hotel.
     */
    public float getRate() {
        return this.rate;
    }

    /**
     * Restituisce la lista dei servizi dell'hotel.
     *
     * @return La lista dei servizi dell'hotel.
     */
    public List<String> getServices() {
        return services;
}

    /**
     * Imposta il tasso dell'hotel.
     *
     * @param rate Nuovo tasso dell'hotel.
     */
    public void setRate(float rate) {
        if (this.rate == 0) this.rate = rate;
        else this.rate = Math.round((this.rate + rate) / 2.0f);
    }

    /**
     * Imposta il punteggio dell'hotel.
     *
     * @param score Nuovo punteggio dell'hotel.
     */
    public void setScore(float score) {
        this.score = score;
    }

    /**
     * Restituisce il punteggio dell'hotel.
     *
     * @return Punteggio dell'hotel.
     */
    public float getScore() {
        return this.score;
    }

    /**
     * Restituisce il numero di recensioni dell'hotel.
     *
     * @return Numero di recensioni dell'hotel.
     */
    public int getNumber_reviews() {
        return this.Number_reviews;
    }

    /**
     * Imposta il numero di recensioni dell'hotel.
     *
     * @param number Nuovo numero di recensioni dell'hotel.
     */
    public void setNumber_reviews(int number) {
        this.Number_reviews = number;
    }

    /**
     * Incrementa il numero di recensioni dell'hotel.
     */
    public void setNumber_reviews() {
        this.Number_reviews++;
    }

    /**
     * Calcola il punteggio complessivo dell'hotel in base alle recensioni.
     */
    public void calculateScore() {

        ArrayList<Review> reviews = this.getReviews();

        float qualityScore = 0;
        float quantityScore = reviews.size(); //oppure this.number_reviews
        float actualityScore = 0;

        Date now = new Date();
        long currentTime = now.getTime();

        for (Review review : reviews) {
            qualityScore += review.getrate();

            Date reviewDate = review.getDate();
            long reviewTime = reviewDate.getTime();

            // Calcola la differenza in minuti tra la data della recensione e oggi
            long diffInMinutes = TimeUnit.MINUTES.convert(currentTime - reviewTime, TimeUnit.MILLISECONDS);

            // Calcola il punteggio di attualità in base alla differenza di minuti
            actualityScore += 1.0f - ((float) diffInMinutes / (365 * 24 * 60));  // Converti giorni in minuti
        }


        if (!reviews.isEmpty()) {
            qualityScore /= reviews.size();
            actualityScore /= reviews.size();
        }

        // Calcola lo score come somma pesata
        float score = WEIGHT_QUALITY * qualityScore + WEIGHT_QUANTITY * quantityScore + WEIGHT_ACTUALITY * actualityScore;

        this.setScore(score);
    }

    /**
     * Restituisce una rappresentazione formattata dell'hotel con informazioni dettagliate
     * @return Una stringa con la rappresentazione formattata dell'hotel.
     */
    public String printPretty() {
        StringBuilder stringBuilder = new StringBuilder();

        String hotel = "\uD83C\uDFE8";  // Hotel
        String redphone = "\u260E\uFE0F";  // Telefono rosso
        String pin = "\uD83D\uDCCD";    // Pin rosso
        String tick = "\u2705";         // Tick verde
        String star = "\u2B50";         // Stella
        String speechBubble = "\uD83D\uDCAC";  // Fumetto
        String graph = "\uD83D\uDCCA";
        String boldTextStart = "\u001B[1m";
        String boldTextEnd = "\u001B[0m";
        
        stringBuilder.append(hotel).append(" ").append(boldTextStart).append(name).append(boldTextEnd).append("\n")
                .append("| ")
                .append(description).append("\n")
                .append("| -----------------------------\n")
                .append("| ")
                .append(pin).append(" ").append(city).append("\n")
                .append("| -----------------------------\n")
                .append("| ")
                .append(redphone).append("  ").append(phone).append("\n")
                .append("| -----------------------------\n")
                .append("| ")
                .append(tick).append(boldTextStart).append(" Services\n").append(boldTextEnd);
    
        for (String service : services) {
            stringBuilder.append("| ").append(service).append("\n");
        }
    
        stringBuilder.append("| -----------------------------\n")
                .append("| ")
                .append(star).append(boldTextStart).append(" Overall Rating ").append(boldTextEnd).append(rate).append("\n")
                .append("| -----------------------------\n")
                .append("| ").append(graph).append(boldTextStart).append(" Ratings").append(boldTextEnd).append("\n").append(ratings.prettyPrint()).append("\n");
        stringBuilder.append("| ")
                .append(speechBubble).append(boldTextStart).append(" Reviews").append(boldTextEnd);
    
        if (reviews.isEmpty()) {
            stringBuilder.append("\n| No reviews yet");
        } else {
            for (Review review : reviews) {
                stringBuilder.append(review.printPretty());
            }
        }
    
        return stringBuilder.toString();
    }
    
    /**
     * Verifica se l'oggetto Hotel corrente è diverso da un altro oggetto Hotel in base allo score.
     *
     * @param otherHotel Altro oggetto Hotel da confrontare.
     * @return true se l'oggetto corrente è diverso dall'altro oggetto, altrimenti false.
     */
    public boolean hasChanged(Hotel otherHotel) {
        // Confronta gli attributi significativi
        return this.getScore() != otherHotel.getScore() || !this.getRatings().equals(otherHotel.getRatings());
    }


    /**
     * Override del metodo toString per ottenere una rappresentazione JSON dell'hotel.
     *
     * @return Stringa con la rappresentazione JSON dell'hotel.
     */
    @Override
    public String toString() {
        return "{\n" +
                "\t\"id\": " + id + ",\n" +
                "\t\"name\": \"" + name + "\",\n" +
                "\t\"description\": \"" + description + "\",\n" +
                "\t\"city\": \"" + city + "\",\n" +
                "\t\"phone\": \"" + phone + "\",\n" +
                "\t\"services\": " + services.toString() + ",\n" +
                "\t\"rate\": " + rate + ",\n" +
                "\t\"ratings: " + ratings.toString() + "\n" +
                "\t\"reviews: " + reviews.toString() + "\n" +
                "}";
    }

    /**
     * Override del metodo equals per confrontare due oggetti Hotel.
     *
     * @param obj Oggetto da confrontare con l'hotel corrente.
     * @return true se gli oggetti Hotel sono uguali, false altrimenti.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Hotel hotel = (Hotel) obj;
        return Objects.equals(name, hotel.name) &&
                Objects.equals(city, hotel.city) &&
                score == hotel.score;
    }
}
