import java.time.LocalDateTime;

public class Transaction {
    String transactionId;
    LocalDateTime date;
    String transactionType;
    double amount;
    Account account;
    double preBalance;
    double postBalance;

    Transaction(String transactionId, LocalDateTime date, String transactionType, double amount,
                Account account, double preBalance, double postBalance) {
        this.transactionId = transactionId;
        this.date = date;
        this.transactionType = transactionType;
        this.amount = amount;
        this.account = account;
        this.preBalance = preBalance;
        this.postBalance = postBalance;

    }
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    public String getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public Account getAccount() {
        return account;
    }
    public void setAccount(Account account) {
        this.account = account;
    }
    public double getPreBalance() {
        return preBalance;
    }
    public void setPreBalance(double preBalance) {
        this.preBalance = preBalance;
    }
    public double getPostBalance() {
        return postBalance;
    }
    public void setPostBalance(double postBalance) {
        this.postBalance = postBalance;
    }
    @Override
    public String toString() {
        return "Transaction{" + "transactionId=" + transactionId + ", date=" + date
                + ", transactionType=" + transactionType + ", amount=" + amount
                + ", account=" + account + ", preBalance=" + preBalance
                + ", postBalance=" + postBalance + '}';
    }

}
