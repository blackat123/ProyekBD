package com.example.proyekbasisdata.branchAdmin;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import com.example.proyekbasisdata.dtos.Catalog;
import com.example.proyekbasisdata.dtos.Menu;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class BranchMenuFormController {
    @FXML
    private ImageView menuImageView;

    @FXML
    private Label menuNameLabel;

    @FXML
    private Label menuDescriptionLabel;

    @FXML
    private Label originalPriceLabel;

    @FXML
    private TextField customPriceField;

    @FXML
    private CheckBox availabilityCheckBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Catalog branchMenu;
    private Menu centralMenu;
    private int branchId;

    @FXML
    public void initialize() {
        HelloApplication app = HelloApplication.getApplicationInstance();
        this.branchId = getBranchIdByAdminId(app.getUserId());

        customPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                customPriceField.setText(oldValue);
            }
        });
    }

    public void initData(Catalog branchMenu, Menu centralMenu) {
        this.branchMenu = branchMenu;
        this.centralMenu = centralMenu;

        populateFields();
    }

    private void populateFields() {
        if (centralMenu != null) {
            try {
                if (centralMenu.getImage() != null && !centralMenu.getImage().isEmpty()) {
                    File file = new File("src/main/resources/com/example/proyekbasisdata/assets/" + centralMenu.getImage());
                    if (file.exists()) {
                        menuImageView.setImage(new Image(file.toURI().toString()));
                    }
                } else {
                    Image placeholder = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/proyekbasisdata/assets/placeholder-image.png")));
                    menuImageView.setImage(placeholder);
                }
            } catch (Exception e) {
                Image placeholder = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/proyekbasisdata/assets/placeholder-image.png")));
                menuImageView.setImage(placeholder);
            }

            menuNameLabel.setText(centralMenu.getName());
            menuDescriptionLabel.setText(centralMenu.getDescription());
        }
    }

    private int getBranchIdByAdminId(int adminId) {
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT id FROM branch_admins WHERE id = ?");
            stmt.setInt(1, adminId);
            var rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to get branch ID: " + e.getMessage());
        }
        return -1;
    }

    @FXML
    public void onCancelClick(ActionEvent event) {
        navigateBackToMenuCatalog();
    }

    private void navigateBackToMenuCatalog() {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("BranchAdminPage/MenuCatalog.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setScene(scene);
            app.getPrimaryStage().sizeToScene();
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to return to menu catalog: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}