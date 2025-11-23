package de.thb.netchat.client;

import javafx.scene.Node;
import javafx.stage.Stage;

public class FXUtil {

    public static Stage getStageFromNode(Node node) {
        return (Stage) node.getScene().getWindow();
    }
}
