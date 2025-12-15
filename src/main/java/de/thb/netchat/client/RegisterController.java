package de.thb.netchat.client;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

// Controller für die Registrierungsmaske. Klasse erfasst Benutzerdaten (Name, E-Mail, Password)
// und sendet an den Server. Server verarbeitet dann den Befehl, schreibt in die DB.
// Wichtig: Aufgebaute Verbindung nur temporär, da ClientConnection connect nirgendwo gespeichert wird.
// --> Server verarbeitet den Befehl, schreibt in die DB und wenn Verbindung abreißt, dann stirbt der Server-Thread.
public class RegisterController {

    // FXML Bindings: Diese Variablen werden mit Textfeldern aus der register_view.fxml verknüpft.
    @FXML private TextField tfUsername;
    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;

    // Wird ausgeführt, sobald Register-Button geklickt wird.
    @FXML
    public void register() {

        // Daten werden aus der GUI gelesen.
        String username = tfUsername.getText();
        String email = tfEmail.getText();
        String password = tfPassword.getText();

        // Validierung: Sind alle Felder ausgefüllt?
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Bitte alle Felder ausfüllen.");
            return; // Methode wird abgebrochen.
        }

        try {
            // Temporäre Verbindung wird aufgebaut. ClientConnection bereitet vor.
            ClientConnection connection = new ClientConnection("localhost", 9999);
            // Socket wird geöffnet. Auf Serverseite in ChatServer.java wird serverSocket.accept(); ausgelöst.
            connection.connect();

            // Server erwartet beim Typ register die Email und das Password im Body.
            Message msg = new Message(
                    "register",
                    username,
                    null, // Server liest Empfänger nicht bei Register.
                    email + "||" + password // Werden mit || getrennt.
            );

            // absenden
            connection.send(msg);

            // Szenenwechsel: Auf Antwort vom Server wird nicht gewartet, sofortiger Wechsel zum Login-Screen.
            // Nachtrag: Verbindung connection wird hier lokal vergessen. Vom Garbage Collector aufgeräumt.
            openLoginWindow();

        } catch (Exception e) {
            // Falls Server nicht erreichbar ist.
            showError("Registrierung fehlgeschlagen: " + e.getMessage());
        }
    }

    // Private Hilfsmethode: Zum Login-Screen wechseln.
    private void openLoginWindow() {
        try {
            // FXML laden
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/de/thb/netchat/client/login_view.fxml")
            );

            Scene scene = new Scene(loader.load());

            // Aktuelle stage wird über Textfeld tfUsername geholt.
            Stage stage = (Stage) tfUsername.getScene().getWindow();

            // Szene wird getauscht.
            stage.setScene(scene);
            stage.setTitle("NetChat – Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Popup-Fenster mit Fehlermeldung wird angezeigt.
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
