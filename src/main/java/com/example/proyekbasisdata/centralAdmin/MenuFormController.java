package com.example.proyekbasisdata.centralAdmin;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import com.example.proyekbasisdata.dtos.Menu;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class MenuFormController {

    @FXML
    private ImageView imagePreview;

    @FXML
    private TextField imagePathField;

    @FXML
    private Button browseImageButton;

    @FXML
    private TextField menuNameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField priceField;

    @FXML
    private Button saveButton;

    @FXML
    private Label titleLabel;

    // Initialize variables for data handling
    private String selectedImagePath = "";
    private int categoryId;
    private Menu menuToEdit;

    @FXML
    private void initialize() {
        // Setup price field to only accept numbers
        priceField.textProperty().addListener((_, _, newValue) -> {
            if (!newValue.matches("\\d*")) {
                priceField.setText(newValue.replaceAll("\\D", ""));
            }
        });

        // Update image preview when path changes
        imagePathField.textProperty().addListener((_, _, newValue) -> {
            updateImagePreview(newValue);
        });
    }

    public void setMode(String mode, int categoryId, Menu menu) {
        this.categoryId = categoryId;
        this.menuToEdit = menu;

        if ("EDIT".equalsIgnoreCase(mode) && menuToEdit != null) {
            titleLabel.setText("Edit Menu");
            saveButton.setText("Update Menu");
            if (menuToEdit != null) {
                menuNameField.setText(menuToEdit.getName());
                descriptionField.setText(menuToEdit.getDescription());
                priceField.setText(String.valueOf(menuToEdit.getPrice()));

                if (menuToEdit.getImage() != null && !menuToEdit.getImage().isEmpty()) {
                    imagePathField.setText(menuToEdit.getImage());
                    updateImagePreview(menuToEdit.getImage());
                }
            }
            saveButton.setOnAction(e -> onUpdateMenuClick());
        } else {
            titleLabel.setText("Add Menu");
            saveButton.setText("Save Menu");
            saveButton.setOnAction(e -> onSaveMenuClick());
        }
    }

    @FXML
    private void onBrowseImageClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Menu Image");

        // Add extension filters
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Image files (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog
        Stage stage = (Stage) browseImageButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            imagePathField.setText(file.getName());
            updateImagePreview(selectedImagePath);
        }
    }

    private void updateImagePreview(String imagePath) {
        try {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                Image image;
                if (imagePath.startsWith("http")) {
                    // For URL images
                    image = new Image(imagePath);
                } else {
                    // For local file images
                    File file = new File(imagePath);
                    if (file.exists()) {
                        image = new Image(file.toURI().toString());
                    } else {
                        return;
                    }
                }
                imagePreview.setImage(image);
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
        }
    }

    @FXML
    private void onSaveMenuClick() {
        // Validate form inputs
        if (validateForm()) {
            // Get form data
            String imagePath = imagePathField.getText().trim();
            String menuName = menuNameField.getText().trim();
            String description = descriptionField.getText().trim();
            String priceText = priceField.getText().trim();

            try (Connection connection = DataSourceManager.getDatabaseConnection()) {
                double price = Double.parseDouble(priceText);

                // Send to database
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO menus (image, name, description, price, category_id) VALUES (?, ?, ?, ?, ?)");
                stmt.setString(1, imagePath);
                stmt.setString(2, menuName);
                stmt.setString(3, description);
                stmt.setDouble(4, price);
                stmt.setInt(5, this.categoryId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                        File source = new File(selectedImagePath);
                        File dest = new File("src/main/resources/com/example/proyekbasisdata/assets/" + imagePath);
                        Files.copy(source.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    showSuccessAlert("Menu successfully added!");
                    clearForm();

                    // Load the menu page
                    HelloApplication app = HelloApplication.getApplicationInstance();
                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/menu.fxml"));
                    Scene scene = new Scene(loader.load());
                    app.getPrimaryStage().setScene(scene);
                    app.getPrimaryStage().sizeToScene();
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Format harga tidak valid!");
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
            String imagePath = imagePathField.getText().trim();
            String menuName = menuNameField.getText().trim();
            String description = descriptionField.getText().trim();
            String priceText = priceField.getText().trim();

            try (Connection connection = DataSourceManager.getDatabaseConnection()) {
                double price = Double.parseDouble(priceText);

                // Send to database
                PreparedStatement stmt = connection.prepareStatement("UPDATE menus SET image = ?, name = ?, description = ?, price = ?, category_id = ? WHERE id = ?");
                stmt.setString(1, imagePath);
                stmt.setString(2, menuName);
                stmt.setString(3, description);
                stmt.setDouble(4, price);
                stmt.setInt(5, this.categoryId);
                stmt.setInt(6, this.menuToEdit.getId());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                        File source = new File(selectedImagePath);
                        File dest = new File("src/main/resources/com/example/proyekbasisdata/assets/" + imagePath);
                        Files.copy(source.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    showSuccessAlert("Menu successfully updated!");
                    clearForm();

                    // Load the menu page
                    HelloApplication app = HelloApplication.getApplicationInstance();
                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/menu.fxml"));
                    Scene scene = new Scene(loader.load());
                    app.getPrimaryStage().setScene(scene);
                    app.getPrimaryStage().sizeToScene();
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Format harga tidak valid!");
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
        if (menuNameField.getText().trim().isEmpty()) {
            showErrorAlert("Promo name cannot be empty!");
            return false;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            showErrorAlert("Description cannot be empty!");
            return false;
        }

        if (priceField.getText().trim().isEmpty()) {
            showErrorAlert("Price cannot be empty!");
            return false;
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                showErrorAlert("Price must be greater than 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Price must be a valid number!");
            return false;
        }

        return true;
    }

    private void clearForm() {
        imagePathField.setText("");
        menuNameField.setText("");
        descriptionField.setText("");
        priceField.setText("");

        // Reset image preview to placeholder
        try {
            Image placeholderImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../assets/placeholder-image.png")));
            imagePreview.setImage(placeholderImage);
        } catch (Exception e) {
            System.out.println("Could not load placeholder image");
        }

        selectedImagePath = "";
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

    // Handle cancel button click
    @FXML
    public void onCancelClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/menu.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
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