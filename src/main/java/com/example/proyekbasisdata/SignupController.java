package com.example.proyekbasisdata;

import com.example.proyekbasisdata.datasources.DataSourceManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;


public class SignupController {
    // Initialize variables for UI components
    @FXML
    private TextField name_input;

    @FXML
    private TextField email_input;

    @FXML
    private PasswordField password_input;

    @FXML
    private TextField phoneNumber_input;

    @FXML
    private TextField address_input;

    // Handle signup button click
    @FXML
    public void onSignupClick(ActionEvent event) {
        // Get the input values
        String name = name_input.getText();
        String email = email_input.getText();
        String password = password_input.getText();
        String phoneNumber = phoneNumber_input.getText();
        String address = address_input.getText();
        Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());


        // Validate inputs (basic validation)
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty() || address.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Signup Failed");
            alert.setHeaderText("Invalid Credentials");
            alert.setContentText("Please fill in all fields.");
            alert.showAndWait();
            return;
        }

        // Send data to database
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO customers (name, email, password, phone_number, address, created_at) VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, phoneNumber);
            stmt.setString(5, address);
            stmt.setTimestamp(6, currentTimestamp);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Clear the input fields after successful signup
                name_input.clear();
                email_input.clear();
                password_input.clear();
                phoneNumber_input.clear();
                address_input.clear();

                // Load the login page
                HelloApplication app = HelloApplication.getApplicationInstance();
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-page.fxml"));
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

    // Handle login page click
    @FXML
    public void onLoginPageClick(ActionEvent event) throws IOException {
        // Load the registration page
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-page.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }
}
