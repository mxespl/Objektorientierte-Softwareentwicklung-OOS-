package bank.exceptions;

/** Thrown when an operation targets an account that does not exist. */
public class AccountDoesNotExistException extends Exception {
    /** @param message detail message */
    public AccountDoesNotExistException(String message) {
        super(message);
    }
}
