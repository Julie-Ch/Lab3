import java.io.Serializable;

/**
 * Classe che rappresenta i punteggi dettagliati di una recensione nel sistema HOTELIER.
 */
public class Ratings implements Serializable {
    private float cleaning;
    private float position;
    private float services;
    private float quality;

    /**
     * Costruttore vuoto di default per la classe Ratings.
     */
    public Ratings() {
    }

    /**
     * Costruttore della classe Ratings.
     *
     * @param cleaning Punteggio per la pulizia.
     * @param position Punteggio per la posizione.
     * @param services Punteggio per i servizi.
     * @param quality  Punteggio per la qualità.
     */
    public Ratings(float cleaning, float position, float services, float quality) {
        this.cleaning = cleaning;
        this.position = position;
        this.services = services;
        this.quality = quality;
    }

    /**
     * Restituisce il punteggio per la pulizia.
     *
     * @return Punteggio per la pulizia.
     */
    public float getCleaning() {
        return cleaning;
    }

    /**
     * Imposta il punteggio per la pulizia.
     *
     * @param cleaning Punteggio per la pulizia.
     */
    public void setCleaning(float cleaning) {
        this.cleaning = cleaning;
    }

    /**
     * Restituisce il punteggio per la posizione.
     *
     * @return Punteggio per la posizione.
     */
    public float getPosition() {
        return position;
    }

    /**
     * Imposta il punteggio per la posizione.
     *
     * @param position Punteggio per la posizione.
     */
    public void setPosition(float position) {
        this.position = position;
    }

    /**
     * Restituisce il punteggio per i servizi.
     *
     * @return Punteggio per i servizi.
     */
    public float getServices() {
        return services;
    }

    /**
     * Imposta il punteggio per i servizi.
     *
     * @param services Punteggio per i servizi.
     */
    public void setServices(float services) {
        this.services = services;
    }

    /**
     * Restituisce il punteggio per la qualità.
     *
     * @return Punteggio per la qualità.
     */
    public float getQuality() {
        return quality;
    }

    /**
     * Imposta il punteggio per la qualità.
     *
     * @param quality Punteggio per la qualità.
     */
    public void setQuality(float quality) {
        this.quality = quality;
    }

    /**
     * Override del metodo toString per ottenere una rappresentazione testuale degli oggetti Ratings.
     *
     * @return Stringa rappresentante gli oggetti Ratings.
     */
    @Override
    public String toString() {
        return "Ratings{" +
                "cleaning=" + cleaning +
                ", position=" + position +
                ", services=" + services +
                ", quality=" + quality +
                '}';
    }

    /**
     * Restituisce una rappresentazione formattata delle valutazioni sotto forma di stringa.
     *
     * @return Una stringa formattata con le valutazioni.
     */

    public String prettyPrint() {
        StringBuilder stringBuilder = new StringBuilder();

        String soap = "\uD83E\uDDFC";
        String pin = "\uD83D\uDCCD";
        String sofa = "\uD83D\uDECB";
        String hundred = "\uD83D\uDCAF";
        
        stringBuilder.append("| " + soap + " Cleaning: " +  this.cleaning + "\n");
        stringBuilder.append("| " + pin + " Position: " +  this.position + "\n");
        stringBuilder.append("| " + sofa + " Services: " +  this.services + "\n");
        stringBuilder.append("| " + hundred + " Quality: " +  this.quality + "\n| -------------------------------");
    
        return stringBuilder.toString();
    }
    
}
