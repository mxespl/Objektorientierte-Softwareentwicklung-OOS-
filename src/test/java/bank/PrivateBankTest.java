package bank;

import bank.exceptions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PrivateBank}.
 */
class PrivateBankTest {

    private static final String TEST_DIR = "testAccounts"; // name folder khusus buat tes

    private PrivateBank bank; // bank yang lagi kita test

    // Contoh Transaksi yang dipakai berulang di banyak test
    private Payment payIn;
    private Payment payOut;
    private IncomingTransfer inTransfer;
    private OutgoingTransfer outTransfer;

    // ---------- Helper: hapus semua file JSON di folder test ----------

    private void deleteTestFiles() {
        File dir = new File(TEST_DIR);
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.getName().endsWith(".json")) {
                // cuma hapus file .json
                f.delete();
            }
        }
    }

    // ---------- @BeforeEach / @AfterEach ----------

    @BeforeEach
    void init() throws IOException, TransactionAttributeException {
        // pastikan folder test bersih
        deleteTestFiles();

        // Bank dengan test-directory sendiri
        bank = new PrivateBank("MyBank", 0.02, 0.03, TEST_DIR);

        // Test-Transaktionen
        payIn = new Payment("01.01.2024", 100.0, "Gehalt", 0.02, 0.03);
        payOut = new Payment("02.01.2024", -50.0, "Miete", 0.02, 0.03);

        inTransfer = new IncomingTransfer(
                "03.01.2024", 200.0, "Bonus", "Chef", "Adam"
        );
        outTransfer = new OutgoingTransfer(
                "04.01.2024", 70.0, "Essen", "Adam", "Restaurant"
        );
    }

    @AfterEach
    void cleanup() {
        deleteTestFiles();
    }

    // ---------- 1) Konstruktor ----------

    @Test
    void constructor_setsFieldsAndCreatesDirectory() {
        assertEquals("MyBank", bank.getName());
        assertEquals(0.02, bank.getIncomingInterest());
        assertEquals(0.03, bank.getOutgoingInterest());

        File dir = new File(TEST_DIR);
        assertTrue(dir.exists(), "Test-Verzeichnis sollte existieren");
    }

    // ---------- 2) createAccount ----------

    @Test
    void createAccount_createsEmptyAccountAndPersists() throws Exception {
        bank.createAccount("Adam");

        // Konto existiert und ist leer
        List<Transaction> list = bank.getTransactions("Adam");
        assertNotNull(list);
        assertTrue(list.isEmpty());

        // Datei existiert
        File f = new File(TEST_DIR, "Konto Adam.json");
        assertTrue(f.exists(), "JSON-Datei für Konto Adam sollte existieren");
    }

    @Test
    void createAccount_duplicate_throwsException() throws Exception {
        bank.createAccount("Adam");

        assertThrows(
                AccountAlreadyExistsException.class,
                () -> bank.createAccount("Adam")
        );
    }

    // ---------- 3) addTransaction ----------

    @Test
    void addTransaction_valid_updatesBalanceAndPersists() throws Exception {
        bank.createAccount("Adam");

        bank.addTransaction("Adam", payIn);

        double expected = payIn.calculate();
        assertEquals(expected, bank.getAccountBalance("Adam"), 1e-6);

        File f = new File(TEST_DIR, "Konto Adam.json");
        assertTrue(f.exists(), "Konto-Datei sollte nach addTransaction existieren");
    }

    @Test
    void addTransaction_nonExistingAccount_throwsException() {
        assertThrows(
                AccountDoesNotExistException.class,
                () -> bank.addTransaction("Unbekannt", payIn)
        );
    }

    @Test
    void addTransaction_duplicateTransaction_throwsException() throws Exception {
        bank.createAccount("Adam");
        bank.addTransaction("Adam", payIn);

        assertThrows(
                TransactionAlreadyExistException.class,
                () -> bank.addTransaction("Adam", payIn)
        );
    }

    // ---------- 4) removeTransaction ----------

    @Test
    void removeTransaction_existing_removesAndPersists() throws Exception {
        bank.createAccount("Adam");
        bank.addTransaction("Adam", payIn);

        bank.removeTransaction("Adam", payIn);

        assertFalse(bank.containsTransaction("Adam", payIn));

        // Neue Bank über selben Ordner → sollte auch ohne Transaktion sein
        PrivateBank bank2 = new PrivateBank("MyBank", 0.02, 0.03, TEST_DIR);
        assertFalse(bank2.containsTransaction("Adam", payIn));
    }

    @Test
    void removeTransaction_nonExistingAccount_throwsException() {
        assertThrows(
                AccountDoesNotExistException.class,
                () -> bank.removeTransaction("Unbekannt", payIn)
        );
    }

    @Test
    void removeTransaction_nonExistingTransaction_throwsException() throws Exception {
        bank.createAccount("Adam");

        assertThrows(
                TransactionDoesNotExistException.class,
                () -> bank.removeTransaction("Adam", payIn)
        );
    }

    // ---------- 5) getAccountBalance mit gemischten Transaktionen ----------

    @Test
    void getAccountBalance_combinesPaymentAndTransfers() throws Exception {
        bank.createAccount("Adam");

        bank.addTransaction("Adam", payIn);       // + (100 mit Zinsen)
        bank.addTransaction("Adam", payOut);      // - (50 mit Zinsen)
        bank.addTransaction("Adam", inTransfer);  // + 200
        bank.addTransaction("Adam", outTransfer); // - 70

        double expected =
                payIn.calculate()
                        + payOut.calculate()
                        + inTransfer.calculate()
                        + outTransfer.calculate();

        assertEquals(expected, bank.getAccountBalance("Adam"), 1e-6);
    }

    // ---------- 6) getTransactionsSorted ----------

    @Test
    void getTransactionsSorted_sortsByCalculatedAmount() throws Exception {
        bank.createAccount("Adam");

        Payment small = new Payment("01.01.2024", 50.0, "small", 0.02, 0.03);
        Payment big = new Payment("01.01.2024", 200.0, "big", 0.02, 0.03);

        bank.addTransaction("Adam", big);
        bank.addTransaction("Adam", small);

        List<Transaction> asc = bank.getTransactionsSorted("Adam", true);
        List<Transaction> desc = bank.getTransactionsSorted("Adam", false);

        assertEquals(small, asc.get(0));
        assertEquals(big, asc.get(1));

        assertEquals(big, desc.get(0));
        assertEquals(small, desc.get(1));
    }

    // ---------- 7) getTransactionsByType ----------

    @Test
    void getTransactionsByType_filtersPositiveAndNegative() throws Exception {
        bank.createAccount("Adam");

        bank.addTransaction("Adam", payIn);  // calculate() >= 0
        bank.addTransaction("Adam", payOut); // calculate() < 0

        List<Transaction> positives = bank.getTransactionsByType("Adam", true);
        List<Transaction> negatives = bank.getTransactionsByType("Adam", false);

        assertTrue(positives.stream().allMatch(t -> t.calculate() >= 0));
        assertTrue(negatives.stream().allMatch(t -> t.calculate() < 0));
    }

    // ---------- 8) equals & toString ----------

    @Test
    void equals_sameState_returnsTrue() throws Exception {
        PrivateBank other = new PrivateBank("MyBank", 0.02, 0.03, TEST_DIR);
        assertEquals(bank, other);
    }

    @Test
    void equals_differentName_returnsFalse() throws Exception {
        PrivateBank other = new PrivateBank("OtherBank", 0.02, 0.03, TEST_DIR);
        assertNotEquals(bank, other);
    }

    @Test
    void toString_isNotEmpty() {
        String s = bank.toString();
        assertNotNull(s);
        assertFalse(s.isBlank());
    }

    // ---------- 9) deleteAccount & getAllAccounts ----------

    @Test
    void deleteAccount_existing_removesFromMapAndDeletesFile() throws Exception {
        bank.createAccount("Adam");
        bank.createAccount("Eve");

        assertTrue(bank.getAllAccounts().contains("Adam"));
        assertTrue(bank.getAllAccounts().contains("Eve"));

        File f = new File(TEST_DIR, "Konto Adam.json");
        assertTrue(f.exists());

        bank.deleteAccount("Adam");

        assertFalse(bank.getAllAccounts().contains("Adam"));
        assertFalse(f.exists());
    }

    @Test
    void deleteAccount_nonExisting_throws() {
        assertThrows(
                AccountDoesNotExistException.class,
                () -> bank.deleteAccount("UnknownDude")
        );
    }

    @Test
    void getAllAccounts_returnsAllCreatedAccounts() throws Exception {
        bank.createAccount("Adam");
        bank.createAccount("Martha");
        bank.createAccount("John");

        List<String> accounts = bank.getAllAccounts();

        assertTrue(accounts.contains("Adam"));
        assertTrue(accounts.contains("Martha"));
        assertTrue(accounts.contains("John"));
    }

    @Test
    void containsTransaction_falseForUnknownAccountOrTransaction() throws Exception {
        bank.createAccount("Adam");
        bank.addTransaction("Adam", payIn);

        // akun ada tapi transaksinya lain
        assertFalse(bank.containsTransaction("Adam", payOut));

        // akun tidak ada sama sekali → di sini diasumsikan method mengembalikan false
        assertFalse(bank.containsTransaction("Nope", payIn));
    }

    // ---------- 10) hashCode & copy constructor ----------

    @Test
    void hashCode_isStableForSameObject() {
        int h1 = bank.hashCode();
        int h2 = bank.hashCode();

        assertEquals(h1, h2); // minimal kontrak: panggilan berulang ke objek yang sama → hasil sama
    }

    @Test
    void copyConstructor_createsEqualButDifferentObject() throws Exception {
        bank.createAccount("Adam");
        bank.addTransaction("Adam", payIn);

        // asumsi kamu punya konstruktor: public PrivateBank(PrivateBank other)
        PrivateBank copy = new PrivateBank(bank);

        assertEquals(bank, copy);
        assertNotSame(bank, copy);
    }

    // ---------- 11) Setter für Name und Zinsen ----------

    @Test
    void setters_changeValuesCorrectly() {
        // awal
        assertEquals("MyBank", bank.getName());
        assertEquals(0.02, bank.getIncomingInterest());
        assertEquals(0.03, bank.getOutgoingInterest());

        bank.setName("NewBank");
        bank.setIncomingInterest(0.10);
        bank.setOutgoingInterest(0.20);

        assertEquals("NewBank", bank.getName());
        assertEquals(0.10, bank.getIncomingInterest());
        assertEquals(0.20, bank.getOutgoingInterest());
    }

    // ---------- 12) createAccount mit initialen Transaktionen ----------

    @Test
    void createAccount_withInitialTransactions_setsBankInterestAndPersists() throws Exception {
        // siapkan transaksi awal
        Payment p = new Payment("01.01.2024", 100.0, "Start", 0.5, 0.5);
        IncomingTransfer it = new IncomingTransfer(
                "02.01.2024", 200.0, "StartBonus", "Chef", "StartKonto"
        );

        List<Transaction> initial = List.of(p, it);

        bank.createAccount("StartKonto", initial);

        // akun harus ada
        List<Transaction> list = bank.getTransactions("StartKonto");
        assertEquals(2, list.size());

        // Payment di dalam akun harus pakai interest bank (0.02 / 0.03),
        // bukan 0.5 / 0.5 yang kita set dari luar
        Transaction t0 = list.get(0);
        assertTrue(t0 instanceof Payment);
        Payment stored = (Payment) t0;
        assertEquals(bank.getIncomingInterest(), stored.getIncomingInterest());
        assertEquals(bank.getOutgoingInterest(), stored.getOutgoingInterest());

        // file JSON juga harus ada
        File f = new File(TEST_DIR, "Konto StartKonto.json");
        assertTrue(f.exists());
    }

    @Test
    void createAccount_withInitialTransactions_duplicate_throws() {
        // dua kali transaksi yang sama di list awal
        List<Transaction> initial = List.of(payIn, payIn);

        assertThrows(
                TransactionAlreadyExistException.class,
                () -> bank.createAccount("DupKonto", initial)
        );
    }

    @Test
    void createAccount_doesNotThrow() {
        assertDoesNotThrow(() -> bank.createAccount("Adam"));
    }

}
