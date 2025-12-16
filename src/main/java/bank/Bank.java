package bank;

import bank.exceptions.*;

import java.io.IOException;
import java.util.List;

/**
 * Interface for a generic bank. Provides methods to handle the interaction between
 * accounts and transactions.
 */
public interface Bank {

    /**
     * Adds an empty account to the bank.
     *
     * @param account the account name to be added
     * @throws AccountAlreadyExistsException if the account already exists
     * @throws IOException                   if persisting the account fails
     */
    void createAccount(String account)
            throws AccountAlreadyExistsException, IOException;

    /**
     * Adds an account (with specified transactions) to the bank.
     * <p>
     * Important: duplicate transactions must not be added to the account.
     *
     * @param account      the account name to be added
     * @param transactions a list of already existing transactions which should be added to the newly created account
     * @throws AccountAlreadyExistsException    if the account already exists
     * @throws TransactionAlreadyExistException if a transaction in the list already exists in the account
     * @throws TransactionAttributeException    if the validation check for certain attributes fails
     * @throws IOException                      if persisting the account fails
     */
    void createAccount(String account, List<Transaction> transactions)
            throws AccountAlreadyExistsException,
            TransactionAlreadyExistException,
            TransactionAttributeException,
            IOException;

    /**
     * Adds a transaction to an already existing account.
     *
     * @param account     the account to which the transaction is added
     * @param transaction the transaction which should be added to the specified account
     * @throws TransactionAlreadyExistException if the transaction already exists in the account
     * @throws AccountDoesNotExistException     if the specified account does not exist
     * @throws TransactionAttributeException    if the validation check for certain attributes fails
     * @throws IOException                      if persisting the updated account fails
     */
    void addTransaction(String account, Transaction transaction)
            throws TransactionAlreadyExistException,
            AccountDoesNotExistException,
            TransactionAttributeException,
            IOException;

    /**
     * Removes a transaction from an account.
     * <p>
     * If the transaction does not exist, an exception is thrown.
     *
     * @param account     the account from which the transaction is removed
     * @param transaction the transaction which is removed from the specified account
     * @throws AccountDoesNotExistException     if the specified account does not exist
     * @throws TransactionDoesNotExistException if the transaction cannot be found in the account
     * @throws IOException                      if persisting the updated account fails
     */
    void removeTransaction(String account, Transaction transaction)
            throws AccountDoesNotExistException,
            TransactionDoesNotExistException,
            IOException;

    /**
     * Checks whether the specified transaction for a given account exists.
     *
     * @param account     the account in which the transaction is searched
     * @param transaction the transaction to search/look for
     * @return {@code true} if the transaction exists in the account, {@code false} otherwise
     */
    boolean containsTransaction(String account, Transaction transaction);

    /**
     * Calculates and returns the current account balance.
     *
     * @param account the selected account
     * @return the current account balance
     */
    double getAccountBalance(String account);

    /**
     * Returns a list of transactions for an account.
     *
     * @param account the selected account
     * @return the list of all transactions for the specified account (may be empty, never {@code null})
     */
    List<Transaction> getTransactions(String account);

    /**
     * Returns a sorted list (based on calculated amounts) of transactions for a specific account.
     * <p>
     * The list is sorted either in ascending or descending order.
     *
     * @param account the selected account
     * @param asc     {@code true} for ascending order, {@code false} for descending order
     * @return the sorted list of all transactions for the specified account (may be empty, never {@code null})
     */
    List<Transaction> getTransactionsSorted(String account, boolean asc);

    /**
     * Returns a list of either positive or negative transactions (based on calculated amounts).
     *
     * @param account  the selected account
     * @param positive {@code true} to list non-negative (>= 0) transactions,
     *                 {@code false} to list negative (&lt; 0) transactions
     * @return the list of all transactions matching the selected type (may be empty, never {@code null})
     */
    List<Transaction> getTransactionsByType(String account, boolean positive);



    void deleteAccount(String account) throws AccountDoesNotExistException, IOException;

    List<String> getAllAccounts();
}
