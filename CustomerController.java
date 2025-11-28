import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import mainmodels.Customer;
import javafx.event.ActionEvent;
import dao.*;

import java.sql.SQLException;
import java.util.List;

public class CustomerController {
    @FXML
    private TableView<Customer> tableView;
    @FXML
    private TableColumn<Customer, String> customerNameColumn;

    @FXML
    private TableColumn<Customer, String> customerIdColumn;

    @FXML
    private TableColumn<Customer, Long> contactNumberColumn;

    @FXML
    private TableColumn<Customer, String> addressColumn;
    private CustomerDAO customerDAO;
    @FXML
    public void initialize() {
        customerDAO = new CustomerDAO(DBConnection.getConnection());

        customerNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        customerIdColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCustomerID())));
        contactNumberColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getContactNumber()));
        addressColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));

        loadCustomers();
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
    public void loadCustomers() {
        try {
            CustomerDAO dao = new CustomerDAO(DBConnection.getConnection());
            List<Customer> list = dao.getAllCustomers();
            tableView.getItems().setAll(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void handleRefresh(ActionEvent actionEvent) {
        loadCustomers();
    }
    public void setCustomerData(List<Customer> customers) {
        tableView.setItems(javafx.collections.FXCollections.observableArrayList(customers));
    }
}
