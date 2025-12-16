package bank;

/**
 * Abstrakte Oberklasse
 * Subklassen : Transfer und Payment
 * **/
public abstract class Transaction implements CalculateBill {
    /**
     * Datum im Format
     */
    protected String date;
    /**
     * Aus sicht des BankKontos
     * Positive Werte: Einzahlung, Negative Werte: Auszahlung
     */
    protected double amount;
    /**
     * Freie text für Transaktionen
     */
    protected String description;

    /**
     * Initialisiert eine Transaktion mit Datum, Betrag und Beschreibung.
     *
     * @param date        das Datum, format {@code DD.MM.YYYY}
     * @param amount      die Hohe (Einzahlung ≥ 0, Auszahlung < 0)
     * @param description Freie text
     */
    public Transaction(String date, double amount, String description){
        this.date = date;
        this.amount = amount;
        this.description = description;
    }

    /**
     * Kopie von Objekten
     *
     * @param other kopieren andreren
     *
     */
    public Transaction(Transaction other){
        this.date = other.date;
        this.amount = other.amount;
        this.description = other.description;
    }

    /** @return date (DD.MM.YYYY}) */
    public String getDate() {return date;}

    /** @return amount (positive = Einzahlung, negative = Auszahlung) */
    public double getAmount() {return amount;}

    /** @return description freie text */
    public String getDescription() {return description;}

    //SETTER

    /**
     * Set das datum
     * @param date datum*/

    public void setDate(String date) {this.date = date;}

    /**
     * Set das amount
     * Subklassen können andern mit override wenn braucht
     * @param amount amount */
    public void setAmount(double amount) {this.amount = amount;}

    /**
     * Set das Description
     * @param description freietext */
    public void setDescription(String description) {this.description = description;}


    /**
     * Wertbasierte Gleichheit auf date, amount , description
     *
     * @param o andere Objekt
     * @return genau dann, wenn alle Basisfelder gleich sind*/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                               // (1) same reference
        if (o == null || getClass() != o.getClass()) return false; // (2) null / same class?

        Transaction other = (Transaction) o;                      // (3) safe downcast

        // (4) amount
        if (Double.compare(this.getAmount(), other.getAmount()) != 0) {
            return false;
        }

        // (5) date (null-safe)
        String d1 = this.getDate();
        String d2 = other.getDate();
        if (d1 == null) {
            if (d2 != null) {
                return false;
            }
        } else {
            if (!d1.equals(d2)) {
                return false;
            }
        }

        // (6) description (null-safe) — return in if/else form
        String s1 = this.getDescription();
        String s2 = other.getDescription();
        if (s1 == null) {
            return s2 == null;          // both null → true, else false
        } else {
            return s1.equals(s2);       // compare content
        }
    }


    /**
     * Gibt eine für Menschen lesbare Darstellung mit dem berechneten Betrag zurück.
     * Subklassen dürfen dieses Format überschreiben/erweitern.
     *
     * @return string */
    @Override
    public String toString() {
        double amount = calculate();
        return "Transaction{date=" + getDate()
                + ", amount=" + amount
                + ", description=" + getDescription()
                + "}";
    }


}
