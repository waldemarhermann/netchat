package de.thb.netchat.client;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField tfUsername;
    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;

    private final Gson gson = new Gson();

    @FXML
    public void register() {

        String username = tfUsername.getText();
        String email = tfEmail.getText();
        String password = tfPassword.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Bitte alle Felder ausfüllen.");
            return;
        }

        try {
            ClientConnection connection = new ClientConnection("localhost", 9999);
            connection.connect();

            Message msg = new Message(
                    "register",
                    username,
                    null,
                    email + "||" + password
            );

            connection.send(msg);

            // direkt zum Login
            openLoginWindow();

        } catch (Exception e) {
            showError("Registrierung fehlgeschlagen: " + e.getMessage());
        }
    }

    private void openLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/de/thb/netchat/client/login_view.fxml")
            );

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) tfUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("NetChat – Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
