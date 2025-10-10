public class InvestmentAccount extends Account implements PayInterest, Withdraw {

    double INTEREST_RATE = 0.05;
    double MIN_INITIAL_DEPOSIT = 500.00;
    double initialDeposit;

    public InvestmentAccount(int accountNumber,String branch,Customer owner, double initialDeposit) {
        super(accountNumber,branch,owner);
        if (initialDeposit >= MIN_INITIAL_DEPOSIT) {
            this.balance = initialDeposit;
            System.out.println("First deposit amount is " + initialDeposit);
        }else{
            System.out.println("Deposited amount required to open an acoount id P500, account not created");
        }

    }
    @Override
    public void withdraw(double amount) {
        if (amount > balance) {
            System.out.println("Insufficient funds to withdraw");
        }else {
            this.balance = balance - amount;
            System.out.println(amount + " withdrawn");
            System.out.println("Current balance is " + this.balance);
        }
    }
    @Override
    public void deposit(double amount) {
        super.deposit(amount);
    }
    //interest
    @Override
    public double calculateInterest() {
        double interestAmount = balance*INTEREST_RATE;
        return interestAmount;
    }
    @Override
    public void payInterest() {
        double interestAmount = calculateInterest();
        balance = balance + interestAmount;
        System.out.println(interestAmount + " payed " + balance);
    }
}
