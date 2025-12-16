package bank;

import bank.exceptions.TransactionAttributeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    private Payment pPos;
    private Payment pNeg;

    @BeforeEach
    void init() throws TransactionAttributeException {
        // SESUAIKAN dengan konstruktor Payment kamu
        pPos = new Payment("01.01.2024", 100.0,
                "Gehalt", 0.02, 0.03);   // amount > 0
        pNeg = new Payment("02.01.2024", -50.0,
                "Miete", 0.02, 0.03);    // amount < 0
    }

    // ---------- 1) Konstruktor ----------

    @Test
    void constructor_setsFieldsCorrectly() {
        assertEquals("01.01.2024", pPos.getDate());
        assertEquals(100.0, pPos.getAmount());
        assertEquals("Gehalt", pPos.getDescription());
        assertEquals(0.02, pPos.getIncomingInterest());
        assertEquals(0.03, pPos.getOutgoingInterest());
    }

    // ---------- 2) Copy-Konstruktor ----------

    @Test
    void copyConstructor_createsEqualButDifferentObject() {
        Payment copy = new Payment(pPos);    // copy-konstruktor

        // isi sama
        assertEquals(pPos, copy);

        // tapi objek beda di memory
        assertNotSame(pPos, copy);
    }

    // ---------- 3) calculate() ----------

    /**
     * Asumsi rumus:
     *  - amount >= 0: amount * (1 - incomingInterest)
     */
    @ParameterizedTest
    @ValueSource(doubles = {0.0, 50.0, 100.0})
    void calculate_forPositiveAmount_usesIncomingInterest(double amount) throws TransactionAttributeException {
        double in = 0.10;
        double out = 0.20;

        Payment p = new Payment("01.01.2024", amount,
                "Positiv", in, out);

        double expected = amount * (1 - in);   // SESUAIKAN kalau rumus kamu beda
        assertEquals(expected, p.calculate(), 1e-6);
    }

    /**
     * Asumsi rumus:
     *  - amount < 0: amount * (1 + outgoingInterest)
     */
    @Test
    void calculate_forNegativeAmount_usesOutgoingInterest() throws TransactionAttributeException {
        double amount = -100.0;
        double in = 0.10;
        double out = 0.20;

        Payment p = new Payment("01.01.2024", amount,
                "Negativ", in, out);

        double expected = amount * (1 + out);  // SESUAIKAN kalau rumus kamu beda
        assertEquals(expected, p.calculate(), 1e-6);
    }

    // ---------- 4) equals() ----------

    @Test
    void equals_sameData_returnsTrue() throws TransactionAttributeException {
        Payment a = new Payment("01.01.2024", 100.0,
                "Gehalt", 0.02, 0.03);
        Payment b = new Payment("01.01.2024", 100.0,
                "Gehalt", 0.02, 0.03);

        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void equals_differentData_returnsFalse() throws TransactionAttributeException {
        Payment base = new Payment("01.01.2024", 100.0,
                "Gehalt", 0.02, 0.03);

        Payment otherDate = new Payment("02.01.2024", 100.0,
                "Gehalt", 0.02, 0.03);
        Payment otherAmount = new Payment("01.01.2024", 200.0,
                "Gehalt", 0.02, 0.03);
        Payment otherDesc = new Payment("01.01.2024", 100.0,
                "Bonus", 0.02, 0.03);

        assertNotEquals(base, otherDate);
        assertNotEquals(base, otherAmount);
        assertNotEquals(base, otherDesc);
    }

    // ---------- 5) toString() ----------

    @Test
    void toString_containsAmountOrDescription() {
        String s = pPos.toString();

        assertNotNull(s);
        assertFalse(s.isBlank());

        assertTrue(
                s.contains(String.valueOf(pPos.getAmount())) ||
                        s.contains(pPos.getDescription())
        );
    }

    // ---------- 6) Validation of interests (error branches) ----------

    @Test
    void constructor_incomingInterestNegative_throws() {
        assertThrows(
                TransactionAttributeException.class,
                () -> new Payment("01.01.2024", 100.0,
                        "Bad", -0.1, 0.03)  // incomingInterest < 0
        );
    }

    @Test
    void constructor_outgoingInterestTooHigh_throws() {
        assertThrows(
                TransactionAttributeException.class,
                () -> new Payment("01.01.2024", 100.0,
                        "Bad", 0.02, 1.5)   // outgoingInterest > 1 (misal)
        );
    }

    @Test
    void setIncomingInterest_invalid_throws() throws TransactionAttributeException {
        Payment p = new Payment("01.01.2024", 100.0, "ok", 0.1, 0.2);

        assertThrows(
                TransactionAttributeException.class,
                () -> p.setIncomingInterest(-0.3)
        );
    }

    @Test
    void setOutgoingInterest_valid_setsValue() throws TransactionAttributeException {
        Payment p = new Payment("01.01.2024", 100.0, "ok", 0.1, 0.2);

        p.setOutgoingInterest(0.5);
        assertEquals(0.5, p.getOutgoingInterest());
    }

    // ---------- 7) 3-arg constructor (tanpa interest eksplisit) ----------

    @Test
    void threeArgConstructor_createsPayment() throws TransactionAttributeException {
        Payment p = new Payment("10.10.2025", 42.0, "Test ohne Zinsen");

        assertEquals("10.10.2025", p.getDate());
        assertEquals(42.0, p.getAmount());
        assertEquals("Test ohne Zinsen", p.getDescription());
        // kita tidak cek interest di sini, cuma pastikan konstruktor jalan
    }

    // ---------- 8) hashCode dipanggil & stabil ----------

    /*@Test
    void hashCode_isStableForSameObject() throws TransactionAttributeException {
        Payment a = new Payment("01.01.2024", 100.0,
                "Gehalt", 0.02, 0.03);

        int h1 = a.hashCode();
        int h2 = a.hashCode();

        assertEquals(h1, h2);   // minimal kontrak: panggilan berulang ke objek yang sama → nilai sama
    }*/
}
