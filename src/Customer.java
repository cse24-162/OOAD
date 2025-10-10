import javax.swing.*;
import java.util.*;

abstract class Customer {
    public String name;
    public long contactNumber;
    protected String address;
    private List<Account> accounts;
    public String customerID;

    public Customer(String name, long contactNumber, String address) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.address = address;
        this.accounts = new ArrayList<>();
    }


    public String getCustomerID() {
        return customerID;
    }
    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getContactNumber() {
        return contactNumber;
    }
    public void setContactNumber(long contactNumber) {
        this.contactNumber = contactNumber;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public List<Account> getAccounts() {
        return accounts;
    }
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
    public boolean addAccount(Account account) {
        int beforeAccountSize = accounts.size();
        this.accounts.add(account);
        if (accounts.size() > beforeAccountSize)
            return true;
        else
            return false;
    }
    public void removeAccount(Account account) {
        this.accounts.remove(account);
    }
    public void addAccounts(List<Account> accounts) {
        this.accounts.addAll(accounts);
    }

    public void showAccounts() {
        for (Account account : accounts) {
            System.out.println(account);
        }
    }


}

