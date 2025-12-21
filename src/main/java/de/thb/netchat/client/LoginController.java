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

// Aufgaben: 1. Nimmt Benutzerdaten entgegen. 2. Baut Verbindung zum Server auf (Handshake).
// 3. Sendet Login-Anfrage und wartet sofort auf die Antwort (synchron).
// 4. Bei Erfolg: Die Verbindung im ClientManager speichern und zum Chat wechseln.
public class LoginController {

    @FXML
    private TextField tfUsername;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private Button btnLogin;

    // Übersetzer für JSON
    private final Gson gson = new Gson();

    // Wird ausgeführt, wenn auf login-Button geclickt wird.
    @FXML
    public void login() {

        // Eingaben auslesen.
        String username = tfUsername.getText();
        String password = tfPassword.getText();

        // Validierung: Alles ausgefüllt?
        if (username.isEmpty() || password.isEmpty()) {
            showError("Bitte Benutzername und Passwort eingeben.");
            return;
        }

        try {
            // Verbindung herstellen: Hier wird ClientConnection erstellt. ABER: Noch nicht die
            // dauerhafte Verbindung, falls Login fehlschlägt.
            ClientConnection connection = new ClientConnection("localhost", 9999);
            connection.connect(); // Baut den Socket auf (TCP Handshake)

            // Login-Nachricht wird vorbereitet
            // Typ "login" sagt dem Server, dass er diese Daten prüfen soll.
            Message loginMsg = new Message(
                    "login",
                    username,
                    null,
                    password // --> wird hier noch in Klartext übertragen
            );

            // Absenden.
            connection.send(loginMsg);

            // Auf Antwort warten (Synchron)
            // BufferedReader wird gebaut, um Antwort des Servers entgegen zu nehmen.
            // Wichtig: Hier wird kein Listener-Thread genutzt, weil erst weitergemacht wird, wenn
            // Server grünes Licht gibt.
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getSocket().getInputStream())
            );

            // Hier wartet (blockiert kurzzeitig) das Programm kurz, bis Daten kommen.
            String response = in.readLine();
            Message rsp = new Gson().fromJson(response, Message.class);

            // Antwort wid geprüft.
            if (rsp.getType().equals("error")) {
                showError(rsp.getText());
                connection.close();
                return;
            }

            // Erfolg: Offene, funktionierende Verbindung wird im CLientManager (Singleton) gespeichert.
            // Damit kann diese im nächsten Fenster (ChatController) wieder verwendet werden.
            ClientManager.getInstance().setUsername(username);
            ClientManager.getInstance().setConnection(connection);

            // Szenenwechsel zum Chat.
            openChatWindow(connection, username);

        } catch (Exception e) {
            showError("Login fehlgeschlagen: " + e.getMessage());
        }
    }


    // Lädt das Fenster und übergibt die Verbindung.
    private void openChatWindow(ClientConnection connection, String username) {
        try {
            // FXML laden.
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/de/thb/netchat/client/chat_view.fxml")
            );

            // Grafische Struktur wird fertig geladen.
            Scene chatScene = new Scene(loader.load());

            // ChatController holen & Daten übergeben
            // ChatController wird hier geholt, den der loader erstellt hat.
            // Mit init() werden die Daten (Verbindung, Username) von hier zum Chat übergeben.
            ChatController controller = loader.getController();
            controller.init(connection, username);

            // Fensterinhalt austauschen.
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
