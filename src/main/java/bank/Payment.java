package bank; // damit die Klasse weiß, in welchem Paket er sich befindet

import bank.exceptions.TransactionAttributeException;

/**
 * Repräsentiert Ein- und Auszahlungen mit Zinsen/Gebühren.
 */
public class Payment extends Transaction {

    /**
     * Anteil (0..1), der bei Einzahlung abgezogen wird.
     */
    private double incomingInterest;

    /**
     * Anteil (0..1), der bei Auszahlung abgezogen wird.
     */
    private double outgoingInterest;

    // ---------- Konstruktoren ----------

    /**
     * Basis-Konstruktor.
     *
     * @param date        Datum {@code DD.MM.YYYY}
     * @param amount      Basisbetrag (>= 0 Einzahlung, < 0 Auszahlung)
     * @param description Freitext
     */
    public Payment(String date, double amount, String description) {
        super(date, amount, description);
    }

    /**
     * Voll-Konstruktor.
     *
     * @param date             Datum {@code DD.MM.YYYY}
     * @param amount           Basisbetrag
     * @param description      Freitext
     * @param incomingInterest Anteil [0..1] für Einzahlung
     * @param outgoingInterest Anteil [0..1] für Auszahlung
     *
     * @throws TransactionAttributeException falls ein Zins außerhalb [0,1] liegt
     */
    public Payment(String date,
                   double amount,
                   String description,
                   double incomingInterest,
                   double outgoingInterest) throws TransactionAttributeException {
        super(date, amount, description);
        // Validierung passiert in den Settern
        setIncomingInterest(incomingInterest);
        setOutgoingInterest(outgoingInterest);
    }

    /**
     * Copy-Konstruktor.
     *
     * @param other anderes Payment-Objekt, das kopiert werden soll
     */
    public Payment(Payment other) {
        super(other);
        this.incomingInterest = other.incomingInterest;
        this.outgoingInterest = other.outgoingInterest;
    }

    // ---------- Getter ----------

    /** @return incoming interest in [0,1] */
    public double getIncomingInterest() {
        return incomingInterest;
    }

    /** @return outgoing interest in [0,1] */
    public double getOutgoingInterest() {
        return outgoingInterest;
    }

    // ---------- Setter ----------

    /**
     * Setzt den Habenzins. Muss im Bereich [0,1] liegen.
     *
     * @param newIncomingInterest Wert in [0,1]
     * @throws TransactionAttributeException falls außerhalb [0,1]
     */
    public void setIncomingInterest(double newIncomingInterest)
            throws TransactionAttributeException {
        if (newIncomingInterest < 0 || newIncomingInterest > 1) {
            throw new TransactionAttributeException("Incoming interest must be in [0,1]");
        }
        this.incomingInterest = newIncomingInterest;
    }

    /**
     * Setzt den Sollzins. Muss im Bereich [0,1] liegen.
     *
     * @param newOutgoingInterest Wert in [0,1]
     * @throws TransactionAttributeException falls außerhalb [0,1]
     */
    public void setOutgoingInterest(double newOutgoingInterest)
            throws TransactionAttributeException {
        if (newOutgoingInterest < 0 || newOutgoingInterest > 1) {
            throw new TransactionAttributeException("Outgoing interest must be in [0,1]");
        }
        this.outgoingInterest = newOutgoingInterest;
    }

    // ---------- Fachlogik ----------

    /**
     * Berechnet den effektiven Betrag nach Zinsen.
     * Verändert nicht den gespeicherten {@code amount}.
     *
     * @return effektiver Betrag nach Zinsen
     */
    @Override
    public double calculate() {
        double a = super.getAmount();
        if (a >= 0) {
            // Einzahlung: Zins wird abgezogen
            return a * (1.0 - getIncomingInterest());
        } else {
            // Auszahlung: Gebühr/Zins wird aufgeschlagen
            return a * (1.0 + getOutgoingInterest());
        }
    }

    // ---------- equals / toString ----------

    /**
     * Wertgleichheit: Basisfelder (Transaction) + beide Zinsfelder.
     *
     * @param o anderes Objekt
     * @return true genau dann, wenn Basis + Zinsen gleich sind
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Payment other = (Payment) o;

        if (Double.compare(this.getIncomingInterest(), other.getIncomingInterest()) != 0)
            return false;

        return Double.compare(this.getOutgoingInterest(), other.getOutgoingInterest()) == 0;
    }

    /**
     * Von Menschen lesbare Darstellung einschließlich berechnetem Betrag und Zinsen.
     *
     * @return String wie {@code Payment{date=..., amount=..., ...}}
     */
    @Override
    public String toString() {
        return "Payment{date=" + getDate()
                + ", amount=" + calculate()
                + ", description=" + getDescription()
                + ", incomingInterest=" + getIncomingInterest()
                + ", outgoingInterest=" + getOutgoingInterest()
                + "}";
    }
}
