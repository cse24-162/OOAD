import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import dao.*;
import mainmodels.*;

import java.util.Date;
import java.util.List;

public class TransactionsController {

    @FXML
    private TableView<Transaction> transactionsTable;
    @FXML
    private TableColumn<Transaction, String> transactionIdColumn;
    @FXML
    private TableColumn<Transaction, Date> dateColumn;
    @FXML
    private TableColumn<Transaction, String> transactionTypeColumn;
    @FXML
    private TableColumn<Transaction, String> accTypeColumn;
    @FXML
    private TableColumn<Transaction, Double> amountColumn;

    @FXML
    private TableColumn<Transaction, Double> prebalanceColumn;

    @FXML
    private TableColumn<Transaction, Double> postbalanceColumn;

    @FXML
    public void initialize() {
        transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        accTypeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getAccount() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getAccount().getAccountType(cellData.getValue().getAccount())
                );
            }
            return new SimpleStringProperty("N/A");
        });
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        prebalanceColumn.setCellValueFactory(new PropertyValueFactory<>("preBalance"));
        postbalanceColumn.setCellValueFactory(new PropertyValueFactory<>("postBalance"));
    }


    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setTransactionData(List<Transaction> transactions) {
        transactionsTable.setItems(FXCollections.observableArrayList(transactions));
    }
}
