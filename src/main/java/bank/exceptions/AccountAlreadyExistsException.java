package bank.exceptions;

/** Thrown when attempting to create an account that already exists. */
public class AccountAlreadyExistsException extends Exception {
    /** @param message detail message */
    public AccountAlreadyExistsException(String message) {
        super(message);
    }
}
