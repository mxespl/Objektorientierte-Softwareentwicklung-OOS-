import bank.*;
import bank.exceptions.*;

public class Main {
    public static void main(String[] args) throws Exception {
        PrivateBank bank = new PrivateBank("MyBank", 0.02, 0.03, "accounts");

        bank.createAccount("martha");
        bank.createAccount("john");

        System.out.println("START:");
        System.out.println("martha = " + bank.getAccountBalance("martha"));
        System.out.println("john   = " + bank.getAccountBalance("john"));

        Transaction t = new OutgoingTransfer("11.12.2025", 100.0, "test", "martha", "john");
        bank.addTransaction("martha", t);

        System.out.println("AFTER:");
        System.out.println("martha = " + bank.getAccountBalance("martha"));
        System.out.println("john   = " + bank.getAccountBalance("john"));

    }
}
