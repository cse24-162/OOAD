import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import mainmodels.*;
import java.sql.*;
import java.util.List;
import dao.*;
import javafx.util.StringConverter;

public class CustomerMainScreenController {

    @FXML private TableView<Account> accountsTableView;
    @FXML private TableColumn<Account, Integer> accNumberColumn;
    @FXML private TableColumn<Account, String> accTypeColumn;
    @FXML private TableColumn<Account, Double> balanceColumn;

    // Profile Fields
    @FXML private TextField customerNameField;
    @FXML private TextField addressField;
    @FXML private TextField contactNumberField;
    @FXML private TextField customerIdField;

    // Transaction Fields
    @FXML private ComboBox<Account> accountSelector;
    @FXML private TextField amountField;
    @FXML private Label statusLabel;

    @FXML private TableView<Transaction> myTransactionsTable;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, Double> colAmount;
    @FXML private TableColumn<Transaction, Double> colBalance;

    private int userId;
    private int customerId;
    private CustomerDAO customerDAO;
    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;

    public void init(int userId) {
        this.userId = userId;
        try {
            Connection conn = DBConnection.getConnection();
            this.customerDAO = new CustomerDAO(conn);
            this.accountDAO = new AccountDAO(conn);
            this.transactionDAO = new TransactionDAO(conn);

            Customer cust = customerDAO.getCustomerByUserId(userId);
            if (cust == null) {
                statusLabel.setText("Error loading customer profile.");
                return;
            }
            this.customerId = cust.getCustomerID();
            loadProfileData(cust);
            loadAccountData();
            refreshTransactions();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfileData(Customer customer) {
        customerIdField.setText(String.valueOf(customer.getCustomerID()));
        customerNameField.setText(customer.getName());
        contactNumberField.setText(String.valueOf(customer.getContactNumber()));
        addressField.setText(customer.getAddress());
    }

    private void loadAccountData() throws SQLException {
        // Setup Table Columns
        accNumberColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        accTypeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAccountType(data.getValue())));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        // Fetch Accounts
        List<Account> accounts = accountDAO.getAccountsForCustomer(this.customerId);

        // Populate Table
        accountsTableView.getItems().setAll(accounts);

        // Populate Dropdown (ComboBox)
        accountSelector.getItems().setAll(accounts);

        // Format how the account looks in the dropdown
        accountSelector.setConverter(new StringConverter<Account>() {
            @Override
            public String toString(Account account) {
                if (account == null) return null;
                return account.getAccountNumber() + " - " + account.getAccountType(account) + " (Bal: " + account.getBalance() + ")";
            }

            @Override
            public Account fromString(String string) {
                return null; // Not needed for this usage
            }
        });
    }

    @FXML
    private void handleDeposit() {
        performTransaction("DEPOSIT");
    }

    @FXML
    private void handleWithdraw() {
        performTransaction("WITHDRAWAL");
    }

    private void performTransaction(String type) {
        Account selectedAccount = accountSelector.getValue();
        if (selectedAccount == null) {
            showAlert(Alert.AlertType.WARNING, "Please select an account.");
            return;
        }
        if ("WITHDRAWAL".equalsIgnoreCase(type)) {
            String accType = selectedAccount.getAccountType(selectedAccount);

            if (selectedAccount instanceof SavingsAccount || "Savings".equalsIgnoreCase(accType)) {
                showAlert(Alert.AlertType.ERROR, "Action Not Allowed\nSavings Accounts are deposit-only. You cannot withdraw funds.");
                return;
            }
        }

        String amountText = amountField.getText();
        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Please enter a valid positive amount.");
            return;
        }

        try {
            transactionDAO.performTransaction(customerId, selectedAccount.getAccountNumber(), userId, type, amount);

            showAlert(Alert.AlertType.INFORMATION, "Transaction Successful!");
            amountField.clear();
            loadAccountData();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Transaction Failed: " + e.getMessage());
        }
    }
    @FXML
    public void refreshTransactions() {
        try {
            colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
            colType.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
            colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
            colBalance.setCellValueFactory(new PropertyValueFactory<>("postBalance"));

            List<Transaction> myTx = transactionDAO.getTransactionsForCustomer(this.customerId);
            myTransactionsTable.getItems().setAll(myTx);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.show();
    }
}