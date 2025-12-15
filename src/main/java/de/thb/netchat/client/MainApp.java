package de.thb.netchat.client;

import de.thb.netchat.model.Message;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Hauptklasse der JavaFX-Client-Anwendung
public class MainApp extends Application {

    // Wenn JavaFX-Laufzeitumgebung startet, wird leere Stage gebaut. Dieses wird hier übergeben.
    @Override
    public void start(Stage stage) throws Exception {

        // Lädt FXML-Datei für die Startansicht
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/de/thb/netchat/client/start_view.fxml")
        );

        // Szene mit geladenem Layout wird erstellt
        Scene scene = new Scene(loader.load());

        // Konfiguriert das Fenster (Titel und Inhalt+)
        stage.setTitle("NetChat – Login");
        stage.setScene(scene);

        // Zeigt das Fenster an
        stage.show();
    }

    // Wird aufgerufen, wenn die Anwendung beendet wird (z.B. Klick auf das 'X')
    @Override
    public void stop() {
        System.out.println("App wird beendet...");

        // Singleton Instanz des Managers wird aufgerufen, um auf offene Verbindung zuzugreifen
        ClientConnection clientConnection = ClientManager.getInstance().getConnection();
        String user = ClientManager.getInstance().getUsername();

        // Wenn aktive Verbindung und User existieren
        if (clientConnection != null && user != null) {
            try {

                // Sende dem Server eine exit-Nachricht
                clientConnection.send(new Message("exit", user, null, "App closed"));

                // Socket wird geschlossen
                clientConnection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
