package bank;

import bank.exceptions.*;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Implementation of a {@link Bank} that stores accounts and their transactions
 * in an in-memory {@link Map} and additionally persists them as JSON files
 * using Gson.
 * <p>
 * Key = account name ({@link String}), value = list of {@link Transaction}s.
 * Each account is stored in a separate JSON file in the configured directory.
 */
public class PrivateBank implements Bank {

    /** Name of the bank. */
    private String name;

    /** Global incoming interest (0..1) used for {@link Payment} objects. */
    private double incomingInterest;  // 0..1

    /** Global outgoing interest (0..1) used for {@link Payment} objects. */
    private double outgoingInterest;  // 0..1

    /** Directory in which the JSON files for the accounts are stored. */
    private String directoryName;

    /**
     * Mapping from account name to list of transactions.
     * The list stores the transactions in the order in which they were added.
     */
    private final Map<String, List<Transaction>> accountsToTransactions =
            new HashMap<>();

    /**
     * Gson instance used for serializing/deserializing {@link Transaction}
     * objects with the {@link TransactionAdapter}.
     */
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Transaction.class, new TransactionAdapter())
            .setPrettyPrinting()
            .create();

    /** Type token representing {@code List<Transaction>} for Gson. */
    private final Type transactionListType =
            new TypeToken<List<Transaction>>() {}.getType();

    // ---------- Constructors ----------

    /**
     * Creates a new bank with the given name, interest values and directory
     * for the account JSON files.
     *
     * @param name              bank name
     * @param incomingInterest  incoming interest in the range [0, 1]
     * @param outgoingInterest  outgoing interest in the range [0, 1]
     * @param directoryName     directory in which the account files are stored
     * @throws IOException if loading existing accounts from the file system fails
     */
    public PrivateBank(String name, double incomingInterest, double outgoingInterest,
                       String directoryName) throws IOException {
        this.name = name;
        this.incomingInterest = incomingInterest;
        this.outgoingInterest = outgoingInterest;
        this.directoryName = directoryName;

        File dir = new File(directoryName); // class File itu dari java.io , new File(directoryName) itu ngebuat path bukan folder
        if (!dir.exists()) {
            dir.mkdirs(); //ngebuat filenya kalo filenya gada mkdir itu bikin 1 sedangkan mkdirs bs lebih , ini method2 dari java.io
        }

        // load already persisted accounts (if any)
        readAccounts();
    }

    /**
     * Copy constructor. Creates a deep copy of the given bank.
     *
     * @param other other bank whose state is copied
     */
    public PrivateBank(PrivateBank other) {
        this.name = other.name;
        this.incomingInterest = other.incomingInterest;
        this.outgoingInterest = other.outgoingInterest;
        this.directoryName = other.directoryName;

        // deep copy of the map
        for (Map.Entry<String, List<Transaction>> e : other.accountsToTransactions.entrySet()) {
            this.accountsToTransactions.put(e.getKey(), new ArrayList<>(e.getValue()));
        }
    }

    // ---------- Getters / Setters ----------

    /** @return bank name */
    public String getName() { return name; }

    /** @return incoming interest in the range [0, 1] */
    public double getIncomingInterest() { return incomingInterest; }

    /** @return outgoing interest in the range [0, 1] */
    public double getOutgoingInterest() { return outgoingInterest; }

    /** @param newName new bank name */
    public void setName(String newName) { this.name = newName; }

    /** @param v new incoming interest in the range [0, 1] */
    public void setIncomingInterest(double v) { this.incomingInterest = v; }

    /** @param v new outgoing interest in the range [0, 1] */
    public void setOutgoingInterest(double v) { this.outgoingInterest = v; }

    // ---------- Object methods ----------

    /**
     * Returns a compact textual representation of this bank.
     *
     * @return a string containing name, interest rates and the number of accounts
     */
    @Override
    public String toString() {
        return "Name : " + getName()
                + "\nIncoming Interest : " + getIncomingInterest()
                + "\nOutgoing Interest : " + getOutgoingInterest()
                + "\nAccounts: " + accountsToTransactions.size();
    }

    /**
     * Two {@code PrivateBank} objects are equal if name, interest rates and the
     * mapping account name → transaction list (including contents and order)
     * are equal.
     *
     * @param o the object to compare with
     * @return {@code true} if equal, otherwise {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        PrivateBank pb = (PrivateBank) o;
        if (Double.compare(pb.incomingInterest, incomingInterest) != 0) return false;
        if (Double.compare(pb.outgoingInterest, outgoingInterest) != 0) return false;
        if (name == null) { if (pb.name != null) return false; }
        else if (!name.equals(pb.name)) return false;
        return accountsToTransactions.equals(pb.accountsToTransactions);
    }

    // ---------- Persistence helpers ----------

    /**
     * Reads all existing account files from {@code directoryName} and fills
     * {@link #accountsToTransactions}.
     * <p>
     * The expected file name format is {@code "Konto <account>.json"}.
     *
     * @throws IOException if reading from the file system fails
     */
    private void readAccounts() throws IOException {
        accountsToTransactions.clear();

        File dir = new File(directoryName);
        if (!dir.exists()) {
            return; // nothing to load
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            String fileName = file.getName();        // e.g. "Konto Adam.json"
            if (!fileName.startsWith("Konto ") || !fileName.endsWith(".json")) { //dapetin tengah nya
                continue;
            }
                String account = fileName.substring("Konto ".length(),
                        fileName.length() - ".json".length());

                Reader reader = new FileReader(file); // FileReader = alat untuk membaca teks dari file.
                List<Transaction> list = gson.fromJson(reader, transactionListType);
                if (list == null) list = new ArrayList<>();
                accountsToTransactions.put(account, list);
                reader.close();
        }
    }

    /**
     * Writes the transactions of the given account into a JSON file in
     * {@link #directoryName}.
     * <p>
     * The file name has the format {@code "Konto <account>.json"}.
     *
     * @param account the account name whose transactions should be persisted
     * @throws IOException if writing the JSON file fails
     */
    private void writeAccount(String account) throws IOException {
        List<Transaction> list = accountsToTransactions.get(account);
        if (list == null) {
            return;
        }

        File dir = new File(directoryName);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "Konto " + account + ".json");

        Writer writer = new FileWriter(file);
        gson.toJson(list, transactionListType, writer);
        writer.close();

    }

    // ---------- Bank API ----------

    /**
     * Creates an empty account.
     *
     * @param account account name
     * @throws AccountAlreadyExistsException if the account already exists
     * @throws IOException                   if persisting the account fails
     */
    @Override
    public void createAccount(String account)
            throws AccountAlreadyExistsException, IOException {

        if (accountsToTransactions.containsKey(account)) {
            throw new AccountAlreadyExistsException("Account exists: " + account);
        }
        accountsToTransactions.put(account, new ArrayList<>());
        writeAccount(account);
    }

    /**
     * Creates an account and adopts an initial list of transactions.
     * <p>
     * Duplicate transactions in the initial list are not allowed. For
     * {@link Payment} objects, the global bank interest values are applied.
     *
     * @param account      account name
     * @param transactions initial transactions (validated and copied)
     * @throws AccountAlreadyExistsException    if the account already exists
     * @throws TransactionAlreadyExistException if duplicate transactions occur within the initial list
     * @throws TransactionAttributeException    if validation checks for certain attributes fail
     * @throws IOException                      if persisting the account fails
     */
    @Override
    public void createAccount(String account, List<Transaction> transactions)
            throws AccountAlreadyExistsException,
            TransactionAlreadyExistException,
            TransactionAttributeException,
            IOException {

        if (accountsToTransactions.containsKey(account)) {
            throw new AccountAlreadyExistsException("Account exists: " + account);
        }

        ArrayList<Transaction> copy = new ArrayList<>();
        if (transactions != null) {
            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);

                // prevent duplicates in the initial list
                if (copy.contains(t)) {
                    throw new TransactionAlreadyExistException(
                            "Duplicate transaction in initial list: " + t);
                }

                // Payment: validate interest [0,1] and apply bank interest
                if (t instanceof Payment) {
                    if (incomingInterest < 0 || incomingInterest > 1 ||
                            outgoingInterest < 0 || outgoingInterest > 1) {
                        throw new TransactionAttributeException("Interest must be in [0,1]");
                    }
                    Payment p = (Payment) t;
                    p.setIncomingInterest(this.incomingInterest);
                    p.setOutgoingInterest(this.outgoingInterest);
                }

                // Transfer: validate amount (>= 0)
                if (t instanceof Transfer) {
                    Transfer tr = (Transfer) t;
                    if (tr.getAmount() < 0) {
                        throw new TransactionAttributeException("Transfer amount must be >= 0");
                    }
                }

                copy.add(t);
            }
        }
        accountsToTransactions.put(account, copy);
        writeAccount(account);
    }

    /**
     * Adds a transaction to an existing account.
     * <p>
     * For {@link Payment} objects the bank interest values are applied, for
     * {@link Transfer} objects the amount must be non-negative.
     *
     * @param account the account name
     * @param t       the transaction to add
     * @throws TransactionAlreadyExistException if the transaction already exists in the account
     * @throws AccountDoesNotExistException     if the account does not exist
     * @throws TransactionAttributeException    if attribute checks fail
     * @throws IOException                      if persisting the updated account fails
     */
    @Override
    public void addTransaction(String account, Transaction t)
            throws TransactionAlreadyExistException, AccountDoesNotExistException, IOException {

        if (!accountsToTransactions.containsKey(account)) {
            throw new AccountDoesNotExistException("Account does not exist: " + account);
        }

        List<Transaction> list = accountsToTransactions.get(account);
        if (list.contains(t)) {
            throw new TransactionAlreadyExistException("Transaction already exists");
        }

        list.add(t);
        writeAccount(account);   // tulis JSON utama

        // ---- logic extra khusus Transfer ----
        if (t instanceof OutgoingTransfer ot) {
            String other = ot.getRecipient();
            if (accountsToTransactions.containsKey(other)) {
                IncomingTransfer it = new IncomingTransfer(
                        ot.getDate(), ot.getAmount(), ot.getDescription(),
                        ot.getSender(), ot.getRecipient()
                );
                accountsToTransactions.get(other).add(it);
                writeAccount(other);
            }
        } else if (t instanceof IncomingTransfer it) {
            String other = it.getSender();
            if (accountsToTransactions.containsKey(other)) {
                OutgoingTransfer ot = new OutgoingTransfer(
                        it.getDate(), it.getAmount(), it.getDescription(),
                        it.getSender(), it.getRecipient()
                );
                accountsToTransactions.get(other).add(ot);
                writeAccount(other);
            }
        }
    }


    /**
     * Removes a transaction from an account.
     *
     * @param account account name
     * @param t       transaction to remove
     * @throws AccountDoesNotExistException     if the account does not exist
     * @throws TransactionDoesNotExistException if the transaction is not found in the account
     * @throws IOException                      if persisting the updated account fails
     */
    @Override
    public void removeTransaction(String account, Transaction t)
            throws AccountDoesNotExistException,
            TransactionDoesNotExistException,
            IOException {

        List<Transaction> list = accountsToTransactions.get(account);
        if (list == null) throw new AccountDoesNotExistException("No such account: " + account);

        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(t)) { idx = i; break; }
        }

        if (idx == -1) {
            throw new TransactionDoesNotExistException("Transaction not found in account: " + account);
        }
        list.remove(idx);
        writeAccount(account);
    }

    /**
     * Checks whether a transaction exists in an account.
     *
     * @param account account name
     * @param t       transaction to search
     * @return {@code true} if the transaction exists, {@code false} otherwise
     */
    @Override
    public boolean containsTransaction(String account, Transaction t) {
        List<Transaction> list = accountsToTransactions.get(account);
        if (list == null) return false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(t)) return true;
        }
        return false;
    }

    /**
     * Calculates the current account balance as the sum of {@link Transaction#calculate()}
     * of all transactions. For non-existing accounts, {@code 0.0} is returned.
     *
     * @param account account name
     * @return current account balance
     */
    @Override
    public double getAccountBalance(String account) {
        List<Transaction> list = accountsToTransactions.get(account);
        if (list == null) return 0.0;
        double sum = 0.0;
        for (int i = 0; i < list.size(); i++) sum += list.get(i).calculate();
        return sum;
    }

    /**
     * Returns a defensive copy of the transactions of an account.
     * <p>
     * For non-existing accounts an empty list is returned.
     *
     * @param account account name
     * @return copy of the transaction list (never {@code null})
     */
    @Override
    public List<Transaction> getTransactions(String account) {
        List<Transaction> list = accountsToTransactions.get(account);
        if (list == null) return new ArrayList<>();
        return new ArrayList<>(list);
    }

    /**
     * Returns a copy of the transactions sorted by their calculated value.
     *
     * @param account account name
     * @param asc     {@code true} for ascending order, {@code false} for descending order
     * @return sorted copy of the transaction list (may be empty)
     */
    @Override
    public List<Transaction> getTransactionsSorted(String account, boolean asc) {
        List<Transaction> list = new ArrayList<>(getTransactions(account));
        Comparator<Transaction> cmp = Comparator.comparingDouble(Transaction::calculate);
        if (!asc) {
            cmp = cmp.reversed();
        }
        list.sort(cmp);
        return list;
    }

    /**
     * Returns a filtered copy of the transactions by sign of {@link Transaction#calculate()}.
     *
     * @param account  account name
     * @param positive {@code true} for non-negative (&gt;= 0) transactions,
     *                 {@code false} for negative (&lt; 0) transactions
     * @return filtered copy of the transaction list (may be empty)
     */
    @Override
    public List<Transaction> getTransactionsByType(String account, boolean positive) {
        List<Transaction> list = accountsToTransactions.get(account);
        List<Transaction> result = new ArrayList<>();
        if (list == null) return result;

        for (int i = 0; i < list.size(); i++) {
            double val = list.get(i).calculate();
            if (positive) {
                if (val >= 0) result.add(list.get(i));
            } else {
                if (val < 0) result.add(list.get(i));
            }
        }
        return result;
    }

    @Override
    public List<String> getAllAccounts() {
        List<String> accounts = new ArrayList<>(accountsToTransactions.keySet());
        Collections.sort(accounts); // opsional, biar rapi di GUI
        return accounts;
    }

    @Override
    public void deleteAccount(String account) throws AccountDoesNotExistException, IOException {
        if (account == null || account.isBlank()) {
            throw new AccountDoesNotExistException("Account name is empty.");
        }
        if (!accountsToTransactions.containsKey(account)) {
            throw new AccountDoesNotExistException("Account does not exist: " + account);
        }

        accountsToTransactions.remove(account);

        Path file = Paths.get(directoryName, "Konto " + account + ".json"); // <-- INI bedanya
        Files.deleteIfExists(file);
    }

}
