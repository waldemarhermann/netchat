package de.thb.netchat.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.Button;

// Controller für den Startbildschirm: Klasse reagiert auf Klicks in der start_view.fxml.
// Dafür da, um Benutzer zum Login-Formular oder Registrierungs-Formular weiterzuleiten.
public class StartController {

    // FXML Referenzen.
    // Werden in JavaFx auto. gefüllt, weil sie in der FXML-Datei markiert sind.
    // Mit fx:id="loginButton".
    @FXML
    private Button loginButton;
    // Mit fx:id="registerButton".
    @FXML
    private Button registerButton;

    // Event Handler: Methoden werden aufgerufen, wenn in FXML 'onAction="#..."' steht.
    // onAction="#openLogin"
    @FXML
    public void openLogin() {
        // Wenn Login-Button geklickt wird, dann lade FXML-Datei für die LoginAnsicht.
        openWindow("/de/thb/netchat/client/login_view.fxml", "NetChat: Login");
    }
    // onAction="#openRegister"
    @FXML
    public void openRegister() {
        // Wenn Register-Button geklickt wird, dann lade FXML-Datei für die Registrierungsansicht.
        openWindow("/de/thb/netchat/client/register_view.fxml", "NetChat: Registrierung");
    }

    // Private Hilfsmethode, die den Szenenwechsel durchführt.
    private void openWindow(String fxml, String title) {
        try {
            // Loader (Lader) wird vorbereitet. FXMLLoader zuständig, um XML-Datei zu lesen und in
            // Java-Objekte zu verwandeln.
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));

            // Parsen der XML. Ergebnis ist eine fertig geladene Szene.
            Scene scene = new Scene(loader.load());

            // Aktuelles Fenster finden/holen. Fenster wird benötigt, um den Inhalt zu tauschen.
            // Mithilfe des loginButton wird das aktuelle Fenster ausgesucht.
            // FXUtil ist eigene erstellte Hilfsklasse.
            Stage stage = FXUtil.getStageFromNode(loginButton);

            // Inhalt wird getauscht: Alte Bild (start_view) gegen (login_view/register_view).
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            // Für den Fall, dass Datei nicht gefunden wird oder FXML fehlerhaft ist.
            e.printStackTrace();
        }
    }

}
