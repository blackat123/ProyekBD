package com.example.proyekbasisdata.centralAdmin;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import com.example.proyekbasisdata.dtos.Promo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PromoController {
    // Initialize variables for UI components
    @FXML
    private GridPane promoGridPane;

    // Initialize for data handling
    private List<Promo> promoList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Initialize the controller
    @FXML
    public void initialize() {
        loadPromoData();
        if (!promoList.isEmpty()) {
            displayPromos();
        } else {
            displayNoData();
        }
    }

    // Load promo data from the database
    private void loadPromoData() {
        promoList = new ArrayList<>();
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM promos ORDER BY id");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                int discount = rs.getInt("discount");
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                Promo promo = new Promo(id, name, description, discount, startDate, endDate);
                promoList.add(promo);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void displayPromos() {
        promoGridPane.getChildren().clear();
        promoGridPane.getColumnConstraints().clear();
        promoGridPane.getRowConstraints().clear();

        if (promoList.isEmpty()) {
            return;
        }

        // Setup grid constraints for 2 columns
        for (int i = 0; i < 2; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
            column.setMinWidth(10.0);
            column.setPrefWidth(450.0);
            promoGridPane.getColumnConstraints().add(column);
        }

        int numRows = (int) Math.ceil((double) promoList.size() / 2);

        // Add rows for menus
        for (int i = 0; i < numRows; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(10.0);
            row.setPrefHeight(210.0);
            row.setVgrow(javafx.scene.layout.Priority.SOMETIMES);
            promoGridPane.getRowConstraints().add(row);
        }

        // Add menus to grid in 2-column layout
        for (int i = 0; i < promoList.size(); i++) {
            int row = i / 2;
            int col = i % 2;

            AnchorPane menuPane = createPromoCard(promoList.get(i));
            promoGridPane.add(menuPane, col, row);
        }
    }

    // Display message when no promos are available
    private void displayNoData() {
        promoGridPane.getChildren().clear();

        Label noPromoData = new Label("No promos available. Please add a promo first.");
        noPromoData.setFont(Font.font("Poppins Regular", 18.0));
        noPromoData.setStyle("-fx-text-fill: #666666;");

        AnchorPane promoPane = new AnchorPane();
        promoPane.getChildren().add(noPromoData);
        AnchorPane.setLeftAnchor(noPromoData, 20.0);
        AnchorPane.setTopAnchor(noPromoData, 10.0);

        promoGridPane.add(promoPane, 0, 0);
    }

    // Create a promo card for display
    private AnchorPane createPromoCard(Promo promo) {
        AnchorPane card = new AnchorPane();
        card.setStyle("-fx-background-color: #f4d7a7; -fx-background-radius: 15px;");
        card.setPrefWidth(450.0);
        card.setPrefHeight(200.0);
        GridPane.setMargin(card, new Insets(5, 5, 5, 5));

        // Edit button
        Button editButton = new Button();
        editButton.setMaxHeight(30);
        editButton.setMaxWidth(32);
        editButton.setMinHeight(30);
        editButton.setMinWidth(32);
        editButton.setStyle("-fx-background-color: #66ce7d;");
        editButton.setCursor(Cursor.HAND);
        editButton.setOnAction(e -> onEditPromoClick(promo));

        ImageView editIcon = new ImageView();
        try {
            Image editImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/proyekbasisdata/assets/edit-icon.png")));
            editIcon.setImage(editImage);
        } catch (Exception ex) {
            System.out.println("Edit icon not found");
        }
        editIcon.setFitHeight(22);
        editIcon.setFitWidth(22);
        editIcon.setPreserveRatio(true);
        editButton.setGraphic(editIcon);

        AnchorPane.setBottomAnchor(editButton, 27.0);
        AnchorPane.setRightAnchor(editButton, 55.0);

        // Delete button
        Button deleteButton = new Button();
        deleteButton.setMaxHeight(30);
        deleteButton.setMaxWidth(32);
        deleteButton.setMinHeight(30);
        deleteButton.setMinWidth(32);
        deleteButton.setStyle("-fx-background-color: #ff6d6d;");
        deleteButton.setCursor(Cursor.HAND);
        deleteButton.setOnAction(e -> onDeletePromoClick(promo));

        ImageView deleteIcon = new ImageView();
        try {
            Image deleteImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/proyekbasisdata/assets/delete-icon.png")));
            deleteIcon.setImage(deleteImage);
        } catch (Exception ex) {
            System.out.println("Delete icon not found");
        }
        deleteIcon.setFitHeight(22);
        deleteIcon.setFitWidth(22);
        deleteIcon.setPreserveRatio(true);
        deleteButton.setGraphic(deleteIcon);

        AnchorPane.setBottomAnchor(deleteButton, 27.0);
        AnchorPane.setRightAnchor(deleteButton, 15.0);

        // Promo title
        Label titleLabel = new Label(promo.getName());
        titleLabel.setFont(Font.font("Poppins SemiBold", 24));
        titleLabel.setWrapText(true);
        AnchorPane.setLeftAnchor(titleLabel, 162.0);
        AnchorPane.setRightAnchor(titleLabel, 12.0);
        AnchorPane.setTopAnchor(titleLabel, 27.0);

        // Promo description
        Label descriptionLabel = new Label(promo.getDescription());
        descriptionLabel.setFont(Font.font("Poppins Regular", 12));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefHeight(65);
        descriptionLabel.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        AnchorPane.setLeftAnchor(descriptionLabel, 162.0);
        AnchorPane.setRightAnchor(descriptionLabel, 12.0);
        AnchorPane.setTopAnchor(descriptionLabel, 65.0);

        // Start date label
        Label startDateLabelText = new Label("Start Date :");
        startDateLabelText.setFont(Font.font("Poppins Medium", 14));
        AnchorPane.setBottomAnchor(startDateLabelText, 49.0);
        AnchorPane.setLeftAnchor(startDateLabelText, 162.0);

        // End date label
        Label endDateLabelText = new Label("End Date :");
        endDateLabelText.setFont(Font.font("Poppins Medium", 14));
        AnchorPane.setBottomAnchor(endDateLabelText, 27.0);
        AnchorPane.setLeftAnchor(endDateLabelText, 162.0);

        // Discount percentage
        Label discountLabel = new Label(promo.getDiscount() + "%");
        discountLabel.setFont(Font.font("Poppins Black", 50));
        discountLabel.setStyle("-fx-text-fill: #a16e34;");
        discountLabel.setAlignment(javafx.geometry.Pos.CENTER);
        AnchorPane.setBottomAnchor(discountLabel, 0.0);
        AnchorPane.setLeftAnchor(discountLabel, 0.0);
        AnchorPane.setRightAnchor(discountLabel, 300.8);
        AnchorPane.setTopAnchor(discountLabel, 0.0);

        // Vertical separator line
        Label separatorLine = new Label();
        separatorLine.setPrefWidth(2);
        separatorLine.setStyle("-fx-border-color: #d7944c; -fx-border-style: segments(7,12) line-cap round; -fx-border-width: 0 3 0 0;");
        AnchorPane.setBottomAnchor(separatorLine, 1.0);
        AnchorPane.setLeftAnchor(separatorLine, 140.0);
        AnchorPane.setTopAnchor(separatorLine, 2.0);

        // Start date value
        Label startDateValue = new Label(promo.getStartDate().format(dateFormatter));
        startDateValue.setFont(Font.font("Poppins Medium", 14));
        AnchorPane.setBottomAnchor(startDateValue, 49.0);
        AnchorPane.setLeftAnchor(startDateValue, 249.0);

        // End date value
        Label endDateValue = new Label(promo.getEndDate().format(dateFormatter));
        endDateValue.setFont(Font.font("Poppins Medium", 14));
        AnchorPane.setBottomAnchor(endDateValue, 27.0);
        AnchorPane.setLeftAnchor(endDateValue, 242.0);

        // Add all components to the card
        card.getChildren().addAll(
                editButton, deleteButton, titleLabel, descriptionLabel,
                startDateLabelText, endDateLabelText, discountLabel,
                separatorLine, startDateValue, endDateValue
        );

        return card;
    }

    // Add a new promo
    @FXML
    public void onAddPromoClick() throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/promo-form.fxml"));
        Scene scene = new Scene(loader.load());
        PromoFormController controller = loader.getController();
        controller.setMode("ADD", null);
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    // Edit an existing promo
    private void onEditPromoClick(Promo promo){
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/promo-form.fxml"));
            Scene scene = new Scene(loader.load());
            PromoFormController controller = loader.getController();
            controller.setMode("EDIT", promo);
            app.getPrimaryStage().setScene(scene);
            app.getPrimaryStage().sizeToScene();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Handle promo deletion
    private void onDeletePromoClick(Promo promo) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Promo");
        confirmAlert.setHeaderText("Are you sure you want to delete this promo?");
        confirmAlert.setContentText("Promo: " + promo.getName());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deletePromo(promo.getId());
        }
    }

    // Delete promo by ID
    private void deletePromo(int promoId) {
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM promos WHERE id = ?");
            stmt.setInt(1, promoId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                showInfoAlert("Success", "Promo has been successfully deleted.");
                loadPromoData();
                displayPromos();
            } else {
                showErrorAlert("Deletion Failed", "Failed to delete the promo. Please try again.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to delete promo: " + e.getMessage());
        }
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

    // Show error alert
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Show information alert
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
