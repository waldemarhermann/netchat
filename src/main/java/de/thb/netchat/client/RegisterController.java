package de.thb.netchat.client;

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

    @FXML private Button btnRegister;
    @FXML private Label errorLabel;

    @FXML
    private void onRegisterClick() {
        register();
    }

    @FXML
    public void register() {

        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }

        String username = safe(tfUsername);
        String email = safe(tfEmail);
        String password = tfPassword != null ? tfPassword.getText().trim() : "";

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showInlineOrAlert("Bitte alle Felder ausfüllen.");
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

            connection.close();

            openLoginWindow();

        } catch (Exception e) {
            showInlineOrAlert("Registrierung fehlgeschlagen: " + e.getMessage());
        }
    }

    private void openLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/de/thb/netchat/client/login_view.fxml")
            );

            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(
                    getClass().getResource("/de/thb/netchat/client/app.css").toExternalForm()
            );

            Stage stage = (Stage) tfUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("NetChat – Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Fehler", "Konnte Login-View nicht öffnen: " + e.getMessage());
        }
    }

    private void showInlineOrAlert(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        } else {
            showAlert("Fehler", msg);
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String safe(TextField tf) {
        return tf != null && tf.getText() != null ? tf.getText().trim() : "";
    }
}
