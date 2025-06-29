package com.example.proyekbasisdata.Customer;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerDashboardController {

    @FXML
    private Label totalOrdersLabel;
    @FXML
    private ListView<String> favoriteBranchListView;
    @FXML
    private Button orderButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button LogoutButton;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        displayTotalOrder();
        displayFavBranch();
    }
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void displayTotalOrder() {
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM orders");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int count = rs.getInt(1);
                totalOrdersLabel.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load categories: " + e.getMessage());
        }
    }
    public void displayFavBranch() {
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            String sql = """
            SELECT b.name
            FROM branches b, orders o
            WHERE b.id = o.branch_id
            GROUP BY b.name
            HAVING COUNT(*) = (
                SELECT MAX(jumlah)
                FROM (
                    SELECT COUNT(*) AS jumlah
                    FROM orders
                    GROUP BY branch_id
                ) AS subquery
            )
            """;
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> hasil = FXCollections.observableArrayList();
            while (rs.next()) {
                hasil.add(rs.getString(1));
            }
            favoriteBranchListView.setItems(hasil);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load favorite branches: " + e.getMessage());
        }
    }

    @FXML
    public void orderButtonClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("Customer/CustomerOrder.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }
    @FXML
    public void historyButtonClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        System.out.println("Menuju ke history button");
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("Customer/CustomerHistory.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }
    @FXML
    public void onLogoutClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-page.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }
}
