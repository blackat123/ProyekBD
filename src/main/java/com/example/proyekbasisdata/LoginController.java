package com.example.proyekbasisdata;


import com.example.proyekbasisdata.datasources.DataSourceManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    // Initialize variables for UI components
    @FXML
    private TextField email_input;

    @FXML
    private TextField password_input;

    @FXML
    private ChoiceBox<String> role_input;

    private int userId;

    public int getUserId() {
        return userId;
    }

    // Initialize choice box options
    @FXML
    public void initialize() {
        // Add options to the role choice box
        role_input.getItems().addAll("Customer", "Branch Admin", "Central Admin");
        // Set default selection
        role_input.setValue("Customer");
    }

    // Verify credentials method
    public boolean verifyCredentials(String email, String password, String role) throws SQLException {
        // Check the role
        Connection connection = DataSourceManager.getDatabaseConnection();
        PreparedStatement stmt;

        switch (role) {
            case "Customer" -> {
                stmt = connection.prepareStatement("SELECT * FROM customers WHERE email = ?");
            }
            case "Branch Admin" -> {
                stmt = connection.prepareStatement("SELECT * FROM branch_admins WHERE email = ?");
            }
            case "Central Admin" -> {
                stmt = connection.prepareStatement("SELECT * FROM central_admins WHERE email = ?");
            }
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        }

         try (connection) {
             // Create a prepared statement to prevent SQL injection
             stmt.setString(1, email);
                System.out.println(role + " in");

             // Execute the query
             ResultSet rs = stmt.executeQuery();

             if (rs.next()) {
                 // User found, check the password
                 String dbPassword = rs.getString("password");

                 if (dbPassword.equals(password)) {
                     this.userId = rs.getInt("id");
                     return true; // Credentials are valid
                 }
             }
         }

        return false;
    }

    @FXML
    public void onLoginClick(ActionEvent event) {
        // Get the username and password from the text fields
        String email = email_input.getText();
        String password = password_input.getText();
        String role = role_input.getValue();

        // Verify the credentials
        try {
            if (verifyCredentials(email, password, role)) {
                HelloApplication app = HelloApplication.getApplicationInstance();
                FXMLLoader loader = null;
                app.setUserId(this.userId);// Store the user_id in HelloApplication

                switch (role) {
                    case "Customer" -> {
                        // Load the customer scene
                        loader = new FXMLLoader(HelloApplication.class.getResource("signup-page.fxml"));
                    }
                    case "Branch Admin" -> {
                        // Load the branch admin scene
                        loader = new FXMLLoader(HelloApplication.class.getResource("signup-page.fxml"));
                    }
                    case "Central Admin" -> {
                        // Load the central admin scene
                        loader = new FXMLLoader(HelloApplication.class.getResource("signup-page.fxml"));
                    }
                }
                assert loader != null;
                Scene scene = new Scene(loader.load());
                app.getPrimaryStage().setScene(scene);
                app.getPrimaryStage().sizeToScene();
            } else {
                // Show an error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText("Invalid Credentials");
                alert.setContentText("Please check your username, password, and role.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Database Connection Failed");
            alert.setContentText("Could not connect to the database. Please try again later.");
            alert.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
