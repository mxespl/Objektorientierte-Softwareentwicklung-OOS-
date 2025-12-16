package bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Transfer}, {@link IncomingTransfer}, {@link OutgoingTransfer}.
 */
class TransferTest {

    private Transfer transfer;
    private IncomingTransfer incoming;
    private OutgoingTransfer outgoing;

    @BeforeEach
    void init() {
        transfer = new Transfer(
                "01.01.2024",
                100.0,
                "Gift",
                "Alice",
                "Bob"
        );

        incoming = new IncomingTransfer(
                "02.01.2024",
                200.0,
                "Salary",
                "Company",
                "Alice"
        );

        outgoing = new OutgoingTransfer(
                "03.01.2024",
                50.0,
                "Rent",
                "Alice",
                "Landlord"
        );
    }

    // ---------- 1) Konstruktor Transfer (voll) ----------

    @Test
    void constructor_setsAllFieldsCorrectly() {
        assertEquals("01.01.2024", transfer.getDate());
        assertEquals(100.0, transfer.getAmount());
        assertEquals("Gift", transfer.getDescription());
        assertEquals("Alice", transfer.getSender());
        assertEquals("Bob", transfer.getRecipient());
    }

    // ---------- 1b) 3-arg Konstruktor (ohne sender/recipient) ----------

    @Test
    void threeArgConstructor_setsBaseFieldsAndNullParties() {
        Transfer t = new Transfer("10.10.2025", 42.0, "Test");

        assertEquals("10.10.2025", t.getDate());
        assertEquals(42.0, t.getAmount());
        assertEquals("Test", t.getDescription());
        assertNull(t.getSender());
        assertNull(t.getRecipient());
    }

    // ---------- 2) Copy-Konstruktor Transfer ----------

    @Test
    void copyConstructor_createsEqualButDifferentObject() {
        Transfer copy = new Transfer(transfer);  // copy-konstruktor

        assertEquals(transfer, copy);      // isi sama
        assertNotSame(transfer, copy);     // objek beda di memory
    }

    // ---------- 3) calculate() ----------

    /**
     * Asumsi rumus:
     *   Transfer / IncomingTransfer / OutgoingTransfer TIDAK punya interest,
     *   jadi calculate() hanya mengembalikan amount.
     */
    @ParameterizedTest
    @ValueSource(doubles = { -200.0, -50.0, 0.0, 50.0, 123.45 })
    void calculate_forTransfer_returnsAmount(double amount) {
        Transfer t = new Transfer(
                "01.01.2024",
                amount,
                "Test",
                "A",
                "B"
        );

        assertEquals(amount, t.calculate(), 1e-6);
    }

    @Test
    void calculate_forIncomingTransfer_returnsAmount() {
        double amount = incoming.getAmount();
        assertEquals(amount, incoming.calculate(), 1e-6);
    }

    @Test
    void calculate_forOutgoingTransfer_returnsNegativeAmount() {
        double amount = outgoing.getAmount();

        // karena implementasi kamu mengembalikan -amount
        double expected = -amount;

        assertEquals(expected, outgoing.calculate(), 1e-6);
    }

    // ---------- 3b) setAmount (override di Transfer) ----------

    @Test
    void setAmount_withValidPositiveValue_updatesAmount() {
        Transfer t = new Transfer(
                "01.01.2024",
                10.0,
                "Start",
                "A",
                "B"
        );

        t.setAmount(99.0);  // > 0 → harus lewat ke super.setAmount
        assertEquals(99.0, t.getAmount());
    }

    @Test
    void setAmount_withNonPositiveValue_doesNotChangeAmount() {
        Transfer t = new Transfer(
                "01.01.2024",
                10.0,
                "Start",
                "A",
                "B"
        );

        t.setAmount(0.0);   // <= 0 → menurut implementasi kamu: hanya print error, tidak ubah amount
        assertEquals(10.0, t.getAmount());

        t.setAmount(-5.0);
        assertEquals(10.0, t.getAmount());
    }

    // ---------- 4) equals() ----------

    @Test
    void equals_sameData_returnsTrue() {
        Transfer t1 = new Transfer(
                "01.01.2024",
                100.0,
                "Gift",
                "Alice",
                "Bob"
        );
        Transfer t2 = new Transfer(
                "01.01.2024",
                100.0,
                "Gift",
                "Alice",
                "Bob"
        );

        assertEquals(t1, t2);
        assertEquals(t2, t1);
    }

    @Test
    void equals_differentData_returnsFalse() {
        Transfer base = new Transfer(
                "01.01.2024",
                100.0,
                "Gift",
                "Alice",
                "Bob"
        );

        Transfer otherDate = new Transfer(
                "02.01.2024",
                100.0,
                "Gift",
                "Alice",
                "Bob"
        );

        Transfer otherAmount = new Transfer(
                "01.01.2024",
                200.0,
                "Gift",
                "Alice",
                "Bob"
        );

        Transfer otherSender = new Transfer(
                "01.01.2024",
                100.0,
                "Gift",
                "Charlie",
                "Bob"
        );

        Transfer otherRecipient = new Transfer(
                "01.01.2024",
                100.0,
                "Gift",
                "Alice",
                "Dave"
        );

        assertNotEquals(base, otherDate);
        assertNotEquals(base, otherAmount);
        assertNotEquals(base, otherSender);
        assertNotEquals(base, otherRecipient);
    }

    // ---------- 5) toString() ----------

    @Test
    void toString_containsImportantInfo() {
        String s = transfer.toString();

        assertTrue(s.contains("01.01.2024"));
        assertTrue(s.contains("100"));
        assertTrue(s.contains("Gift"));
        assertTrue(s.contains("Alice"));
        assertTrue(s.contains("Bob"));
    }

    // ---------- 6) hashCode minimal (stabil untuk objek yang sama) ----------

    @Test
    void hashCode_isStableForSameObject() {
        int h1 = transfer.hashCode();
        int h2 = transfer.hashCode();

        assertEquals(h1, h2);
    }
}
