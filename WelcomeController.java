import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class WelcomeController {

    @FXML
    private void openLoginScreen(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("LoginSceen.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Orchid Bank - Login");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exitApp(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}