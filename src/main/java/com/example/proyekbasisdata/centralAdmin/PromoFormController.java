package com.example.proyekbasisdata.centralAdmin;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import com.example.proyekbasisdata.dtos.Promo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class PromoFormController {
    @FXML
    private Label titleLabel;

    @FXML
    private TextField promoNameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField discountField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button saveButton;

    private Promo promoToEdit;

    public void setMode(String mode, Promo promo) {
        this.promoToEdit = promo;

        if ("EDIT".equalsIgnoreCase(mode) && promoToEdit != null) {
            titleLabel.setText("Edit Menu");
            if (promoToEdit != null) {
                promoNameField.setText(promoToEdit.getName());
                descriptionField.setText(promoToEdit.getDescription());
                discountField.setText(String.valueOf(promoToEdit.getDiscount()));
                startDatePicker.setValue(promoToEdit.getStartDate());
                endDatePicker.setValue(promoToEdit.getEndDate());
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
            String promoName = promoNameField.getText().trim();
            String description = descriptionField.getText().trim();
            String discountText = discountField.getText().trim();
            Date startDateText = Date.valueOf(startDatePicker.getValue());
            Date endDateText = Date.valueOf(endDatePicker.getValue());

            try (Connection connection = DataSourceManager.getDatabaseConnection()) {
                int price = Integer.parseInt(discountText);

                // Send to database
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO promos (name, description, discount, start_date, end_date) VALUES (?, ?, ?, ?, ?)");
                stmt.setString(1, promoName);
                stmt.setString(2, description);
                stmt.setInt(3, price);
                stmt.setDate(4, startDateText);
                stmt.setDate(5, endDateText);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showSuccessAlert("Promo Successfully added!");
                    clearForm();

                    // Load the menu page
                    HelloApplication app = HelloApplication.getApplicationInstance();
                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/promo.fxml"));
                    Scene scene = new Scene(loader.load());
                    app.getPrimaryStage().setScene(scene);
                    app.getPrimaryStage().sizeToScene();
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid discount value. Please enter a valid number.");
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
            String promoName = promoNameField.getText().trim();
            String description = descriptionField.getText().trim();
            String discountText = discountField.getText().trim();
            Date startDateText = Date.valueOf(startDatePicker.getValue());
            Date endDateText = Date.valueOf(endDatePicker.getValue());

            try (Connection connection = DataSourceManager.getDatabaseConnection()) {
                int price = Integer.parseInt(discountText);

                // Send to database
                PreparedStatement stmt = connection.prepareStatement("UPDATE promos SET name = ?, description = ?, discount = ?, start_date = ?, end_date = ? WHERE id = ?");
                stmt.setString(1, promoName);
                stmt.setString(2, description);
                stmt.setInt(3, price);
                stmt.setDate(4, startDateText);
                stmt.setDate(5, endDateText);
                stmt.setInt(6, promoToEdit.getId());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showSuccessAlert("Promo successfully updated!");
                    clearForm();

                    // Load the menu page
                    HelloApplication app = HelloApplication.getApplicationInstance();
                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/promo.fxml"));
                    Scene scene = new Scene(loader.load());
                    app.getPrimaryStage().setScene(scene);
                    app.getPrimaryStage().sizeToScene();
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid discount value. Please enter a valid number.");
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
        if (promoNameField.getText().trim().isEmpty()) {
            showErrorAlert("Nama menu tidak boleh kosong!");
            return false;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            showErrorAlert("Description cannot be empty!");
            return false;
        }

        if (discountField.getText().trim().isEmpty()) {
            showErrorAlert("Discount cannot be empty!");
            return false;
        }

        if (startDatePicker.getValue() == null) {
            showErrorAlert("Start date cannot be empty!");
            return false;
        }

        if (endDatePicker.getValue() == null) {
            showErrorAlert("End date cannot be empty!");
            return false;
        }

        return true;
    }

    private void clearForm() {
        promoNameField.setText("");
        descriptionField.setText("");
        discountField.setText("");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }

    // Handle cancel click
    @FXML
    public void onCancelClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/promo.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    // Handle dashboard page button click
    @FXML
    public void onDashboardPageClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
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
}
