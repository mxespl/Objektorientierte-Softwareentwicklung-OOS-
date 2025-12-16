package bank.exceptions;

/** Thrown when a duplicate transaction is added to an account. */
public class TransactionAlreadyExistException extends Exception {
    /** @param message detail message */
    public TransactionAlreadyExistException(String message) {
        super(message);
    }
}
