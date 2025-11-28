package mainmodels;

public class ChequeAccount extends Account implements Withdraw{

    public ChequeAccount(int accountNumber,String branch,Customer owner,String employer){
        super(accountNumber,branch,owner);
    }
    @Override
    public void deposit(double amount) {
        super.deposit(amount);
    }
    @Override
    public void withdraw(double amount) {
        boolean valid = amountValid(amount);
        if (!valid){
            System.out.println("Insufficient funds.");
        }else {
            System.out.println("You have successfully withdrawn " + amount + ".");
            balance-=amount;
            System.out.println("New balance: " + balance);
        }
    }

}