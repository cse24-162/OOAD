import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import dao.*;
import javafx.scene.*;
import javafx.stage.Stage;
import java.sql.*;

public class LoginController {
    public Button loginButton;
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                showAlert(Alert.AlertType.ERROR, "Database Error: Cannot connect to DB.");
                return;
            }
            UserDAO userDAO = new UserDAO(conn);
            UserDAO.LoginResult res = userDAO.login(username, password);
            if (res == null) {
                showAlert(Alert.AlertType.ERROR, "Login Failed: Invalid username/password");
                return;
            }

            if ("customer".equalsIgnoreCase(res.role)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerMainScreen.fxml"));
                Parent root = loader.load();
                // pass customer id to controller
                CustomerMainScreenController ctrl = loader.getController();
                ctrl.init(res.userId); // implement init(int userId) in controller
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Customer Main Screen");
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("BankClerkScreen.fxml"));
                Parent root = loader.load();
                BankClerkScreenController ctrl = loader.getController();
                ctrl.init(res.userId); // pass user id
                Stage stage = (Stage)  usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Bank Clerk Screen");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error, An error occurred: " + e.getMessage());
        }
    }



    public void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Login Status");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void openNewScene( Parent root, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }

}