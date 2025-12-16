package bank.exceptions;

/** Thrown when transaction attributes are invalid (e.g., interest or amount). */
public class TransactionAttributeException extends Exception {
    /** @param message detail message */
    public TransactionAttributeException(String message) {
        super(message);
    }
}
