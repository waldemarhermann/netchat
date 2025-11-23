package de.thb.netchat.client;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LoginController {

    @FXML
    private TextField tfUsername;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private Button btnLogin;

    private final Gson gson = new Gson();

    @FXML
    public void login() {

        String username = tfUsername.getText();
        String password = tfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Bitte Benutzername und Passwort eingeben.");
            return;
        }

        try {
            ClientConnection connection = new ClientConnection("localhost", 9999);
            connection.connect();

            // Login senden
            Message loginMsg = new Message(
                    "login",
                    username,
                    null,
                    password
            );

            connection.send(loginMsg);

            // Warten auf Antwort (BLOCKIERT NICHT die UI – extra Thread!)
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getSocket().getInputStream())
            );

            String response = in.readLine();
            Message rsp = new Gson().fromJson(response, Message.class);

            if (rsp.getType().equals("error")) {
                showError(rsp.getText());
                connection.close();
                return;
            }

            // Login OK → speichern & Chat öffnen
            ClientManager.getInstance().setUsername(username);
            ClientManager.getInstance().setConnection(connection);

            openChatWindow(connection, username);

        } catch (Exception e) {
            showError("Login fehlgeschlagen: " + e.getMessage());
        }
    }


    private void openChatWindow(ClientConnection connection, String username) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/de/thb/netchat/client/chat_view.fxml")
            );

            Scene chatScene = new Scene(loader.load());

            ChatController controller = loader.getController();
            controller.init(connection, username);

            Stage stage = FXUtil.getStageFromNode(btnLogin);
            stage.setScene(chatScene);
            stage.setTitle("NetChat – Chat");
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
