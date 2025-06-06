package com.example.proyekbasisdata.centralAdmin;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class DashboardController {
    // Initialize variables for the dashboard
    @FXML
    private Label totalBranchesLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label totalOrdersLabel;

    @FXML
    private Label totalEarningsLabel;

    // Method to initialize the dashboard with data
    @FXML
    public void initialize() {
        displayTotalBranches();
        displayTotalOrders();
        displayTotalUsers();
        displayTotalEarnings();
    }

    public void displayTotalBranches() {
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM branches");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int count = rs.getInt(1);
                totalBranchesLabel.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load categories: " + e.getMessage());
        }
    }

    public void displayTotalOrders() {
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

    public void displayTotalUsers() {
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM customers");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int count = rs.getInt(1);
                totalUsersLabel.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load categories: " + e.getMessage());
        }
    }

    public void displayTotalEarnings() {
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        LocalDate lastDay = now.withDayOfMonth(now.lengthOfMonth());

        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT SUM(total_price) FROM orders WHERE order_date >= ? AND order_date <= ?");
            stmt.setDate(1, Date.valueOf(firstDay));
            stmt.setDate(2, Date.valueOf(lastDay));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                double totalEarnings = rs.getDouble(1);
                if (totalEarnings == Math.floor(totalEarnings)) {
                    totalEarningsLabel.setText(String.format("%.0f", totalEarnings));
                } else {
                    totalEarningsLabel.setText(String.format("%.2f", totalEarnings));
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load categories: " + e.getMessage());
        }
    }

    // Handle menu page button click
    @FXML
    public void onMenuPageClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/menu.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    // Handle promo page button click
    @FXML
    public void onPromoPageClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/promo.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    // Handle report page button click
    @FXML
    public void onReportPageClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/report.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    // Handle logout button click
    @FXML
    public void onLogoutClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-page.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    // Show error alert
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
