import javafx.fxml.*;
import javafx.scene.control.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javafx.scene.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        try {
            if (username.equals("") || password.equals("")) {
                showAlert(Alert.AlertType.WARNING, "Please enter both username and password.");
                return;
            }
            FXMLLoader loader;
            Parent root;

            if (username.equalsIgnoreCase("customer") && password.equals("1234")) {
                loader = new FXMLLoader(getClass().getResource("customerMainScreen.fxml"));
                root = loader.load();
                openNewScene(event, root, "Customer Dashboard");

            } else if (username.equalsIgnoreCase("employee") && password.equals("4321")) {
                loader = new FXMLLoader(getClass().getResource("bankClerkScreen.fxml"));
                root = loader.load();
                openNewScene(event, root, "Bank Clerk Dashboard");

            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "An error occured while logging in.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Login Status");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openNewScene(ActionEvent event, Parent root, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }
}