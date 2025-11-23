package de.thb.netchat.client;

import de.thb.netchat.model.Message;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/de/thb/netchat/client/start_view.fxml")
        );


        Scene scene = new Scene(loader.load());
        stage.setTitle("NetChat – Login");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        System.out.println("App wird beendet...");
        ClientConnection conn = ClientManager.getInstance().getConnection();
        String user = ClientManager.getInstance().getUsername();

        if (conn != null && user != null) {
            try {
                // Sende Exit Nachricht
                conn.send(new Message("exit", user, null, "App closed"));
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
