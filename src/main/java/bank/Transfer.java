package bank;// damit die Klasse weiß, in welchem Paket er sich befindet

/**
 * Repräsentiert eine Überweisung (German: {@code Überweisung}) mit
 * {@code sender} und {@code recipient}. Keine Zinsen fallen an.
 *
 * Semantik von {@link #calculate()}: gibt den Basisbetrag unverändert zurück.
 *
 */
public class Transfer extends Transaction{
    //ALLES PRIVATEN ATTRIBUTEN
    /** Konto/Name des Senders. Darf {@code null} sein. */
    private String sender;
    /** Konto/Name des Empfängers. Darf {@code null} sein. */
    private String recipient;

    /**
     * Basis-Konstruktor.
     *
     * @param date        date {@code DD.MM.YYYY}
     * @param amount      base amount (≥ 0 deposit, &lt; 0 withdrawal)
     * @param description free-text description
     */
    public Transfer(String date, double amount, String description) {
       super(date,amount,description);
    }

    /**
     * Voll-Konstruktor.
     *
     * @param date        date {@code DD.MM.YYYY}
     * @param amount      base amount
     * @param description free-text description
     * @param sender      sender identifier (may be {@code null})
     * @param recipient   recipient identifier (may be {@code null})
     */
    public Transfer(String date, double amount, String description, String sender,
                    String recipient) {
        super(date, amount, description);   // reuse #1
        setSender(sender);
        setRecipient(recipient);
    }

    /**
     * Copy-Konstruktor.
     * @param other other transfer to copy
     */
    public Transfer(Transfer other) {
        super(other);
        this.sender = other.sender;
        this.recipient = other.recipient;

    }

    //ALLES GETTER
    /** @return sender (may be {@code null}) */
    public String getSender() {return sender;}
    /** @return recipient (may be {@code null}) */

    public String getRecipient() {return recipient;}


    //ALLES SETTER
    /**
     * Sets the base amount with a simple check: must be &gt; 0 (sample rule).
     * (Note: This is an example of subclass-specific restriction.)
     *
     * @param newAmount neue base amount
     * @Override fur anderung von set amount von transaction
     *
     */
    @Override
    public void setAmount(double newAmount) { // die Zahl kann nicht negativ, benutzen wir if
        if(newAmount <=0){
            //this.amount = 0; //Dies dient zum Zurücksetzen und Speichern der letzten Nummer, damit wir nichts übertragen.
            System.out.println("Fehler: Betrag muss > 0 sein");
        }else{
            super.setAmount(newAmount);
        } //todo : exception werfen anstatt fehlermeldung machen

    }

    /** @param newSender sender identifier (may be {@code null}) */
    public void setSender(String newSender) {this.sender = newSender;}
    /** @param newRecipient recipient identifier (may be {@code null}) */
    public void setRecipient(String newRecipient) {this.recipient = newRecipient;}


    /**
     * keine zinsen: returns the base amount unchanged.
     * @return amount as stored
     */
    @Override
    public double calculate() { return getAmount(); }

    /**
     * Value equality includes base fields (via {@code super.equals(o)})
     * and {@code sender}/{@code recipient}.
     *
     * @param o andere objekte
     * @return true iff base + sender + recipient are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if(o == null || getClass() != o.getClass()) return false;

        if(!super.equals(o)) return false;
        Transfer other = (Transfer) o;

        // sender (null-safe)
        String s1 = this.getSender();
        String s2 = other.getSender();
        if (s1 == null) {
            if (s2 != null) return false;
        } else {
            if (!s1.equals(s2)) return false;
        }

        // recipient (null-safe)
        String r1 = this.getRecipient();
        String r2 = other.getRecipient();
        if (r1 == null) {
            return r2 == null;          // both null → equal; else not equal
        } else {
            return r1.equals(r2);       // both non-null → compare text
        }

    }

    /**
     * Von Menschen lesbares Formular einschließlich berechnetem Betrag.
     * @return string like {@code Transfer{date=..., amount=..., ...}}
     */
    //PRINT ALLES
    @Override
    public String toString() {
        return "Transfer{date=" + getDate() +
                ", amount=" + calculate() +
                ", description=" + getDescription() +
                ", sender=" + sender +
                ", recipient=" + recipient + "}";
    }

}