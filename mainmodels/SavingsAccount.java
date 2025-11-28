package mainmodels;
public class SavingsAccount extends Account implements PayInterest  {
    double INTEREST_RATE = 0.0005;

    public SavingsAccount(int accountNumber,String branch, double balance,Customer owner) {
        super(accountNumber,branch,owner);
        this.balance = balance;

    }
    @Override
    public void deposit(double amount) {
        super.deposit(amount);
        System.out.println("Savings account deposit amount is "+amount);
    }

    @Override
    public void payInterest() {
        double interestAmount = calculateInterest();
        balance = balance + interestAmount;
        System.out.println("Savings account balance is "+balance);
    }

    @Override
    public double calculateInterest() {
        double interestAmount = balance*INTEREST_RATE;
        return interestAmount;
    }

}
