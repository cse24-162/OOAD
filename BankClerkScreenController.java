import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javafx.scene.control.*;
import javafx.stage.Stage;
import mainmodels.*;
import dao.*;

public class BankClerkScreenController {

    //  Tab 1: Bank Clerk Screen (View/Search)


    // Tab 2: Create Account FXML Fields
    @FXML private TextField name;
    @FXML private TextField contactNumber;
    @FXML private TextField openingBalance;
    @FXML private TextField address;
    @FXML private TextField branch;
    @FXML private TextField accountType; // Holds Account Typee

    // Company Customer Specific
    @FXML private TextField contactPerson;
    @FXML private TextField registrationNo;

    // Individual Customer Specific
    @FXML private TextField employerName;
    @FXML private TextField employeraddress;
    @FXML private DatePicker dateOfBirth; // Holds Date of Birth

    // Button
    @FXML private Button createAccountButton;
    @FXML private Button payInterestButton;
    @FXML private TextField searchField;

    private int userId;

    private final Bank bank = new Bank("Orchid Bank", "Main Branch");

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a Customer Name, ID, or Account Number.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            AccountDAO accountDAO = new AccountDAO(conn);
            CustomerDAO customerDAO = new CustomerDAO(conn);

            // 1. Try to parse as Number (Customer ID or Account ID)
            if (query.matches("\\d+")) {
                int id = Integer.parseInt(query);

                // Check fo account
                Account acc = accountDAO.getAccountByNumber(id);
                if (acc != null) {
                    openAccountsPage(Collections.singletonList(acc));
                    return;
                }

                // Check if it is a Customer ID
                Customer cust = customerDAO.getCustomerById(id);
                if (cust != null) {
                    openCustomersPage(Collections.singletonList(cust));
                    return;
                }
            }

            // 2. Treat as Customer Name (Search logic - simplified to exact match or fetch all and filter)
            List<Customer> allCustomers = customerDAO.getAllCustomers();
            List<Customer> matchingCustomers = new ArrayList<>();
            for(Customer c : allCustomers) {
                if(c.getName().toLowerCase().contains(query.toLowerCase())) {
                    matchingCustomers.add(c);
                }
            }

            if(!matchingCustomers.isEmpty()) {
                openCustomersPage(matchingCustomers);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Search Results", "No accounts or customers found matching: " + query);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }
    private void openAccountsPage(List<Account> data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("allAccountPage.fxml"));
            Parent root = loader.load();

            AccountsController controller = loader.getController();
            controller.setAccountData(data);

            Stage stage = new Stage();
            stage.setTitle("Search Results - Accounts");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Accounts Page.");
        }
    }
    private void openCustomersPage(List<Customer> data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("allCustomersPage.fxml"));
            Parent root = loader.load();

            CustomerController controller = loader.getController();
            controller.setCustomerData(data);

            Stage stage = new Stage();
            stage.setTitle("Search Results - Customers");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Customers Page.");
        }
    }
    //               CREATE ACCOUNT LOGI

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        try {
            // 1. Validate Mandatory Text Fields
            if (name.getText().isEmpty() || address.getText().isEmpty() || accountType.getText().isEmpty() || branch.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill in Name, Address, Branch, and Account Type.");
                return;
            }

            // 2. Validate Numeric Fields (Prevent crash on empty input)
            if (contactNumber.getText().isEmpty() || openingBalance.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Contact Number and Opening Balance are required.");
                return;
            }

            // 3. Parse Numbers safely
            long cContact;
            double cBal;
            try {
                cContact = Long.parseLong(contactNumber.getText());
                cBal = Double.parseDouble(openingBalance.getText());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Contact Number must be a whole number and Balance must be a valid amount.");
                return;
            }

            // 4. Gather Data
            String cName = name.getText();
            String cAddress = address.getText();
            String cType = accountType.getText().toLowerCase().trim();
            String cBranch = branch.getText();

            // 5. Determine Customer Type
            Customer newCustomer = null;
            String typeDB = "";

            if (dateOfBirth.getValue() != null) {
                // --- INDIVIDUAL CUSTOMER ---
                java.sql.Date dob = java.sql.Date.valueOf(dateOfBirth.getValue());
                newCustomer = new IndividualCustomer(cName, cContact, cAddress, dob, employerName.getText(), employeraddress.getText());
                typeDB = "individual";
            } else if (!registrationNo.getText().isEmpty()) {
                // --- COMPANY CUSTOMER ---
                try {
                    long reg = Long.parseLong(registrationNo.getText());
                    newCustomer = new CompanyCustomer(cName, reg, contactPerson.getText(), cName, cContact, cAddress);
                    typeDB = "company";
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "Registration Number must be a numeric value.");
                    return;
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "You must enter a Date of Birth (for Individual) OR Registration No (for Company).");
                return;
            }

            // 6. Perform Database Operations
            // Get a fresh connection to ensure it's open
            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null || conn.isClosed()) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Connection to database failed.");
                    return;
                }

                // Create Customer
                CustomerDAO custDAO = new CustomerDAO(conn);
                // Note: Ensure createCustomer in CustomerDAO is updated to use the instance connection if possible,
                // or that passing 'conn' to constructor updates the static field correctly.
                int custId = custDAO.createCustomer(newCustomer, typeDB, this.userId);

                if (custId == -1) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to generate Customer ID.");
                    return;
                }
                newCustomer.setCustomerID(custId);

                // Create Account
                AccountDAO accDAO = new AccountDAO(conn);
                int accNo = accDAO.createAccount(custId, cType, cBal);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Account Successfully Created!\nAccount Number: " + accNo);
                clearFormFields();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "System Error", "An error occurred: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFormFields() {
        name.clear();
        contactNumber.clear();
        openingBalance.clear();
        address.clear();
        branch.clear();
        accountType.clear();
        contactPerson.clear();
        registrationNo.clear();
        employerName.clear();
        employeraddress.clear();
        dateOfBirth.setValue(null);
    }

    public void init(int userId) {
        this.userId = userId;
    }

    @FXML
    private void handleViewAllAccounts(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("allAccountPage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("All Accounts");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewAllCustomers(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("allCustomersPage.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("All Customers");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void payInterest(ActionEvent actionEvent) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Connection Error", "Failed to connect to the database.");
                return;
            }

            AccountDAO accountDAO = new AccountDAO(conn);

            // Delegate all interest calculation and database operations to the DAO
            int paidCount = accountDAO.payInterest(this.userId);

            if (paidCount > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Interest Payment Complete",
                        "Successfully processed interest for " + paidCount + " eligible accounts.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Interest Payment",
                        "No interest-bearing accounts were found or processed.");
            }

        } catch (SQLException e) {
            // Handle SQL exceptions (e.g., connection lost, bad query)
            showAlert(Alert.AlertType.ERROR, "Database Error", "Interest payment failed due to SQL error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void searchByAccountNumber(int accountNumber) {
        try (Connection conn = DBConnection.getConnection()) {
            AccountDAO accountDAO = new AccountDAO(conn);
            Account account = accountDAO.getAccountByNumber(accountNumber);

            if (account != null) {
                // Get customer information for this account
                CustomerDAO customerDAO = new CustomerDAO(conn);
                Customer customer = customerDAO.getCustomerByAccountNumber(accountNumber);

                StringBuilder result = new StringBuilder();
                result.append("Account Number: ").append(account.getAccountNumber()).append("\n");
                result.append("Account Type: ").append(account.getAccountType(account)).append("\n");
                result.append("Balance: $").append(String.format("%.2f", account.getBalance())).append("\n");

                if (customer != null) {
                    result.append("\nCustomer Information:\n");
                    result.append("Name: ").append(customer.getName()).append("\n");
                    result.append("Customer ID: ").append(customer.getCustomerID()).append("\n");
                    result.append("Type: ").append(customer instanceof IndividualCustomer ? "Individual" : "Company").append("\n");
                }

                showSearchResults("Account Found", result.toString());
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Not Found", "No account found with number: " + accountNumber);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to search for account: " + e.getMessage());
        }
    }
    private void showSearchResults(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Search Results");
        alert.setContentText(content);
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(400, 300);
        alert.showAndWait();
    }

    public void handleViewAllTransactions(ActionEvent event) {
        try (Connection conn = DBConnection.getConnection()) {
            TransactionDAO dao = new TransactionDAO(conn);
            List<Transaction> list = dao.getAllTransactions();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Transactions.fxml"));
            Parent root = loader.load();

            TransactionsController ctrl = loader.getController();
            ctrl.setTransactionData(list);

            Stage stage = new Stage();
            stage.setTitle("All System Transactions");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load transactions.");
        }

    }
}


