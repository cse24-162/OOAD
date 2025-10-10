import java.util.HashMap;
import java.util.Map;

public class Bank {
    String bankName;
    String bankBranch;
    Map<String,Customer> customers;
    Map<String,Account> accounts;

    public Bank(String bankName, String bankBranch) {
        this.bankName = bankName;
        this.bankBranch = bankBranch;
        customers = new HashMap<>();
        accounts = new HashMap<>();

    }
    public String getBankName() {
        return bankName;
    }
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    public String getBankBranch() {
        return bankBranch;
    }
    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }
    public Map<String, Customer> getCustomers() {
        return customers;
    }
    public void setCustomers(Map<String, Customer> customers) {
        this.customers = customers;
    }
    public Map<String, Account> getAccounts() {
        return accounts;
    }
    public void setAccounts(Map<String, Account> accounts) {
        this.accounts = accounts;
    }

}
