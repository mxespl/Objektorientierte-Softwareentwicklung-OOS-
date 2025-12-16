package bank.exceptions;

/** Thrown when a transaction cannot be found in the selected account. */
public class TransactionDoesNotExistException extends Exception {
    /** @param message detail message */
    public TransactionDoesNotExistException(String message) {
        super(message);
    }
}
