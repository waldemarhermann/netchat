package de.thb.netchat.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.Button;

public class StartController {

    @FXML
    public void openLogin() {
        openWindow("/de/thb/netchat/client/login_view.fxml", "NetChat – Login");
    }

    @FXML
    public void openRegister() {
        openWindow("/de/thb/netchat/client/register_view.fxml", "NetChat – Registrierung");
    }

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;


    private void openWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load());

            // Bühne holen über den Button
            Stage stage = FXUtil.getStageFromNode(loginButton);

            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
