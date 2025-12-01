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
    private void onLoginClick() {
        // delegiert einfach auf die bestehende Methode
        login();
    }

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

            // Antwort vom Server lesen
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getSocket().getInputStream())
            );

            String response = in.readLine();
            Message rsp = gson.fromJson(response, Message.class);

            if ("error".equals(rsp.getType())) {
                showError(rsp.getText());
                connection.close();
                return;
            }

            // wenn login passt speichern u chat öffnen
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

            chatScene.getStylesheets().add(
                    getClass().getResource("/de/thb/netchat/client/app.css").toExternalForm()
            );


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

