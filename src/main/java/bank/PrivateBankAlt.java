package bank;

import java.io.IOException;
import java.util.List;

/**
 * Alternative implementation of {@link PrivateBank} that calculates the account
 * balance differently for {@link Transfer} transactions.
 * <p>
 * For transfers, the balance is adjusted based on sender/recipient instead of
 * using {@link Transaction#calculate()}.
 */
public class PrivateBankAlt extends PrivateBank {

    /**
     * Constructor with explicit directory name.
     *
     * @param name          bank name
     * @param inI           incoming interest
     * @param outI          outgoing interest
     * @param directoryName directory for JSON files
     * @throws IOException if loading existing accounts fails
     */
    public PrivateBankAlt(String name, double inI, double outI, String directoryName) throws IOException {
        super(name, inI, outI, directoryName);
    }

    /**
     * Constructor using the default directory {@code "accounts"}.
     *
     * @param name bank name
     * @param inI  incoming interest
     * @param outI outgoing interest
     * @throws IOException if loading existing accounts fails
     */
    public PrivateBankAlt(String name, double inI, double outI) throws IOException {
        this(name, inI, outI, "accounts");
    }

    /**
     * Variant 2: calculates the account balance using {@code instanceof} and
     * sender/recipient for {@link Transfer} objects.
     * <p>
     * Incoming/outgoing {@link Payment} objects still use {@link Payment#calculate()}.
     *
     * @param account account name
     * @return current account balance
     */
    @Override
    public double getAccountBalance(String account) {
        List<Transaction> list = getTransactions(account); // may be empty if account does not exist
        double sum = 0.0;
        for (int i = 0; i < list.size(); i++) {
            Transaction t = list.get(i);
            if (t instanceof Transfer) {
                Transfer tr = (Transfer) t;
                if (account.equals(tr.getSender()))          sum -= tr.getAmount();   // sent
                else if (account.equals(tr.getRecipient()))  sum += tr.getAmount();   // received
            } else {
                sum += t.calculate(); // Payment
            }
        }
        return sum;
    }
}
