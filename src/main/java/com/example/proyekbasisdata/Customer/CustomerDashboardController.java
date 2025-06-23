package com.example.proyekbasisdata.Customer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import java.io.IOException;

public class CustomerDashboardController {

    @FXML
    private Button orderButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button logoutButton;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        loadView("order-view.fxml");

        orderButton.setOnAction(e -> loadView("order-view.fxml"));
        historyButton.setOnAction(e -> loadView("history-view.fxml"));
        logoutButton.setOnAction(this::handleLogout);
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proyekbasisdata/login-page.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

            // Close current window
            ((Stage) logoutButton.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
