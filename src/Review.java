import com.google.gson.Gson;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Classe che rappresenta una recensione nel sistema HOTELIER.
 */
public class Review implements Serializable {
    private Ratings ratings;
    private float rate;
    private Date date;
    private String user;

    /**
     * Costruttore della classe Review.
     *
     * @param user    Nome dell'utente che ha scritto la recensione.
     * @param hotel   Nome dell'hotel oggetto della recensione.
     * @param rate   Punteggio sintetico assegnato alla recensione.
     * @param ratings Oggetto Ratings contenente i punteggi dettagliati.
     */
    public Review(String user, String hotel, float rate, Ratings ratings) {
        this.user = user;
        this.ratings = ratings;
        this.rate = rate;
        this.date = new Date();
    }

    /**
     * Costruttore vuoto di default per la classe Review.
     */
    public Review() {
    }

    /**
     * Restituisce il punteggio sintetico della recensione.
     *
     * @return Punteggio sintetico.
     */
    public float getrate() {
        return this.rate;
    }

    /**
     * Restituisce l'oggetto Ratings contenente i punteggi dettagliati.
     *
     * @return Oggetto Ratings.
     */
    public Ratings getRatings() {
        return this.ratings;
    }

    /**
     * Restituisce la data in cui è stata creata la recensione.
     *
     * @return Data della recensione.
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Restituisce il nome dell'utente che ha scritto la recensione.
     *
     * @return Nome dell'utente.
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Imposta il nome dell'utente che ha scritto la recensione.
     *
     * @param user Nome dell'utente.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Imposta il punteggio sintetico della recensione.
     *
     * @param rate Punteggio sintetico.
     */
    public void setrate(float rate) {
        this.rate = rate;
    }

    /**
     * Imposta la data della recensione.
     *
     * @param date Data della recensione.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Imposta gli oggetti Ratings contenente i punteggi dettagliati.
     *
     * @param ratings Oggetto Ratings.
     */
    public void setRatings(Ratings ratings) {
        this.ratings = ratings;
    }

    /**
     * Override del metodo toString per ottenere una rappresentazione JSON dell'oggetto.
     *
     * @return Rappresentazione JSON della recensione.
     */
    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Restituisce una rappresentazione formattata della recensione sotto forma di stringa.
     *
     * @return Una stringa formattata con la recensione.
     */
    public String printPretty() {
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String man = "\uD83D\uDC64";
        String clock = "\uD83D\uDD52";
        String star = "\u2B50";      
        String graph = "\uD83D\uDCCA";
   
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append("| ").append(man).append(" User: ").append(user).append("\n")
                .append("| ").append(clock).append(" Date: ").append(dateFormat.format(date)).append("\n")
                .append("| ").append(star).append(" Overall Rating: ").append(rate).append("\n")
                .append("| ").append(graph).append(" Ratings \n").append(ratings.prettyPrint());
        return stringBuilder.toString();
    }
}
