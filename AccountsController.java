import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import dao.*;
import mainmodels.*;

public class AccountsController {
    @FXML
    private TableView<Account> accountsTable;
    @FXML
    private TableColumn<Account, String> customerNameColumn;
    @FXML
    private TableColumn<Account, String> customerIdColumn;
    @FXML
    private TableColumn<Account, String> accountTypeColumn;
    @FXML
    private TableColumn<Account, Date> openingDateColumn;
    @FXML
    private TableColumn<Account, Double> balanceColumn;

    @FXML
    private void handleBack(ActionEvent event) {
        try {

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize() {
        customerNameColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getOwner() != null) {
                return new SimpleStringProperty(cellData.getValue().getOwner().getName());
            } else {
                return new SimpleStringProperty("Unknown");
            }
        });
        // 2. Customer ID Column: Get ID from Owner and convert Integer to String
        // This fixes the "SimpleIntegerProperty cannot be converted" error
        customerIdColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getOwner() != null) {
                return new SimpleStringProperty(
                        String.valueOf(cellData.getValue().getOwner().getCustomerID())
                );
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

        // 3. Account Type Column: Use the custom getAccountType method
        // This fixes the "setCellValueFactory cannot be applied" error
        accountTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getAccountType(cellData.getValue())
                )
        );
        // 4. Opening Date Column LocalDate conversion
        openingDateColumn.setCellValueFactory(cellData -> {
            // Check if it is the DTO which has the getter for OpeningDate
            if (cellData.getValue() instanceof AccountDAO.AccountDTO) {
                java.time.LocalDate ld = ((AccountDAO.AccountDTO) cellData.getValue()).getOpeningDate();
                if (ld != null) {
                    return new SimpleObjectProperty<>(java.sql.Date.valueOf(ld));
                }
            }
            return null;
        });
        // 5. Balance Column: Standard PropertyValueFactory works here
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        loadAllAccounts();
    }
    public void setAccountData(List<Account> accounts) {
        accountsTable.setItems(FXCollections.observableArrayList(accounts));
    }

    private void loadAllAccounts() {
        try {
            AccountDAO dao = new AccountDAO(DBConnection.getConnection());
            List<Account> list = dao.getAllAccounts();
            accountsTable.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleViewTransactions(ActionEvent event) {
        Account selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an account first.");
            alert.show();
            return;
        }

        try (java.sql.Connection conn = DBConnection.getConnection()) {
            TransactionDAO tDao = new TransactionDAO(conn);
            List<Transaction> txList = tDao.getTransactionsForAccount(selected.getAccountNumber());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Transactions.fxml"));
            Parent root = loader.load();

            TransactionsController ctrl = loader.getController();
            ctrl.setTransactionData(txList);

            Stage stage = new Stage();
            stage.setTitle("Transactions for Account: " + selected.getAccountNumber());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
