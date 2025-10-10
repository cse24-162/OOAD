import java.time.LocalDate;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
abstract class Account {
    protected int accountNumber;
    private Customer owner;
    protected String branch;
    protected LocalDate openingDate;
    protected double balance;

    public Account(int accNo, Customer owner, String branch, double balance) {
        this.accountNumber = accNo;
        this.owner = owner;
        this.branch = branch;
        this.balance = balance;
    }

    public Account(int accountNumber, String branch, Customer owner) {
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.owner = owner;
    }

    public int getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Customer getOwner() {
        return owner;
    }
    public void setOwner(Customer owner) {
        this.owner = owner;
    }

    public String getBranch() {
        return branch;
    }
    public void setBranch(String branch) {
        this.branch = branch;
    }

    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    //methods

    public void deposit(double amount) {
        this.balance += amount;
        System.out.println("Deposited " + amount + " to account " + this.accountNumber);
        System.out.println("Current balance is " + this.balance);
    }

    protected boolean amountValid(double amount) {
        if (amount <= balance)
            return true;
        else
            return false;
    }

}