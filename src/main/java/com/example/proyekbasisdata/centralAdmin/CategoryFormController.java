package com.example.proyekbasisdata.centralAdmin;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import com.example.proyekbasisdata.dtos.Category;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CategoryFormController {
    // Initialize variables for UI components
    @FXML
    private Label titleLabel;

    @FXML
    private TextField nameField;

    @FXML
    private Button saveButton;

    // Initialize variables for data handling
    private Category categoryToEdit;

    // Handle the mode of the form
    public void setMode(String mode, Category category) {
        this.categoryToEdit = category;

        if ("EDIT".equalsIgnoreCase(mode) && categoryToEdit != null) {
            titleLabel.setText("Edit Menu");
            if (categoryToEdit != null) {
                nameField.setText(categoryToEdit.getName());
            }
            saveButton.setOnAction(e -> onUpdateMenuClick());
        } else {
            titleLabel.setText("Add Menu");
            saveButton.setOnAction(e -> onSaveMenuClick());
        }
    }

    @FXML
    private void onSaveMenuClick() {
        // Validate form inputs
        if (validateForm()) {
            // Get form data
            String name = nameField.getText().trim();

            try (Connection connection = DataSourceManager.getDatabaseConnection()) {
                // Send to database
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO categories (name) VALUES (?)");
                stmt.setString(1, name);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showSuccessAlert("Category successfully added!");
                    nameField.setText("");

                    // Load the menu page
                    HelloApplication app = HelloApplication.getApplicationInstance();
                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/menu.fxml"));
                    Scene scene = new Scene(loader.load());
                    app.getPrimaryStage().setScene(scene);
                    app.getPrimaryStage().sizeToScene();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Database Connection Failed");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    private void onUpdateMenuClick() {
        // Validate form inputs
        if (validateForm()) {
            // Get form data
            String name = nameField.getText().trim();

            try (Connection connection = DataSourceManager.getDatabaseConnection()) {
                // Send to database
                PreparedStatement stmt = connection.prepareStatement("UPDATE categories SET name = ? WHERE id = ?");
                stmt.setString(1, name);
                stmt.setInt(2, categoryToEdit.getId());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showSuccessAlert("Category successfully updated!");
                    nameField.setText("");

                    // Load the menu page
                    HelloApplication app = HelloApplication.getApplicationInstance();
                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/menu.fxml"));
                    Scene scene = new Scene(loader.load());
                    app.getPrimaryStage().setScene(scene);
                    app.getPrimaryStage().sizeToScene();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Database Connection Failed");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            showErrorAlert("Name field cannot be empty.");
            return false;
        }
        return true;
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Menu Data successfully saved");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Please check your input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Handle the navigation to different pages
    @FXML
    public void onDashboardPageClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/dashboard.fxml"));
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
}
