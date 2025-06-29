package com.example.proyekbasisdata.branchAdmin;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import com.example.proyekbasisdata.dtos.Catalog;
import com.example.proyekbasisdata.dtos.Category;
import com.example.proyekbasisdata.dtos.Menu;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MenuCatalogController {
    @FXML
    private GridPane categoryGridPane;

    @FXML
    private GridPane menuGridPane;

    private List<Menu> centralMenuList;
    private List<Catalog> catalogList;
    private List<Category> categoryList;
    private int currentCategoryId = -1;
    private int branchId;

    @FXML
    public void initialize() {
        HelloApplication app = HelloApplication.getApplicationInstance();
        this.branchId = getBranchIdByAdminId(app.getUserId());
        loadData();
    }

    private int getBranchIdByAdminId(int adminId) {
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT id FROM branch_admins WHERE id = ?");
            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to get branch ID: " + e.getMessage());
        }
        return -1;
    }

    private void loadData() {
        loadCategories();
        if (!categoryList.isEmpty()) {
            currentCategoryId = categoryList.get(0).getId();
            loadCentralMenus();
            loadCatalogs();
            displayCategories();
            displayMenus();
        } else {
            displayNoData();
        }
    }

    private void loadCategories() {
        categoryList = new ArrayList<>();
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM categories ORDER BY id");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Category category = new Category(id, name);
                categoryList.add(category);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void loadCentralMenus() {
        centralMenuList = new ArrayList<>();
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM menus WHERE category_id = ? ORDER BY id");
            stmt.setInt(1, currentCategoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String image = rs.getString("image");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String description = rs.getString("description");
                int category_id = rs.getInt("category_id");
                Menu menu = new Menu(id, image, name, description, price, category_id);
                centralMenuList.add(menu);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load central menus: " + e.getMessage());
        }
    }

    private void loadCatalogs() {
        catalogList = new ArrayList<>();
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT c.*, m.image, m.name, m.description, m.price, m.category_id " +
                            "FROM catalogs c JOIN menus m ON c.menu_id = m.id " +
                            "WHERE c.branch_id = ? AND m.category_id = ? ORDER BY c.id"
            );
            stmt.setInt(1, branchId);
            stmt.setInt(2, currentCategoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int stock = rs.getInt("stock");
                int branchId = rs.getInt("branch_id");
                int menuId = rs.getInt("menu_id");

                String image = rs.getString("image");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                int categoryId = rs.getInt("category_id");

                Catalog catalog = new Catalog(id, stock, branchId, menuId, image, name, description, price, categoryId);
                catalogList.add(catalog);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load catalogs: " + e.getMessage());
        }
    }

    private void displayCategories() {
        categoryGridPane.getChildren().clear();
        categoryGridPane.getColumnConstraints().clear();
        categoryGridPane.getRowConstraints().clear();

        if (categoryList.isEmpty()) return;

        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        column.setMinWidth(10.0);
        categoryGridPane.getColumnConstraints().add(column);

        RowConstraints row = new RowConstraints();
        row.setMinHeight(10.0);
        row.setVgrow(javafx.scene.layout.Priority.SOMETIMES);
        categoryGridPane.getRowConstraints().add(row);

        javafx.scene.layout.HBox categoryContainer = new javafx.scene.layout.HBox();
        categoryContainer.setSpacing(10);

        for (Category category : categoryList) {
            Button categoryButton = createCategoryButton(category);
            categoryContainer.getChildren().add(categoryButton);
        }

        categoryGridPane.add(categoryContainer, 0, 0);
    }

    private Button createCategoryButton(Category category) {
        Button button = new Button(category.getName());
        button.setAlignment(javafx.geometry.Pos.CENTER);
        button.setMaxHeight(Double.MAX_VALUE);
        button.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        button.setTextFill(javafx.scene.paint.Color.WHITE);
        button.setFont(Font.font("Poppins SemiBold", 16.0));
        button.setPadding(new Insets(0, 20, 0, 20));
        button.setCursor(Cursor.HAND);

        if (category.getId() == currentCategoryId) {
            button.setStyle("-fx-background-color: #2196F3; -fx-background-radius: 8px;");
        } else {
            button.setStyle("-fx-background-color: #64B5F6; -fx-background-radius: 8px;");
        }

        GridPane.setMargin(button, new Insets(3, 3, 3, 3));

        button.setOnAction(e -> {
            currentCategoryId = category.getId();
            loadCentralMenus();
            loadCatalogs();
            displayCategories();
            displayMenus();
        });

        return button;
    }

    private void displayMenus() {
        menuGridPane.getChildren().clear();
        menuGridPane.getColumnConstraints().clear();
        menuGridPane.getRowConstraints().clear();

        if (centralMenuList.isEmpty()) {
            displayNoMenuData();
            return;
        }

        for (int i = 0; i < 2; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
            column.setMinWidth(10.0);
            column.setPrefWidth(450.0);
            menuGridPane.getColumnConstraints().add(column);
        }

        int numRows = (int) Math.ceil((double) centralMenuList.size() / 2);

        for (int i = 0; i < numRows; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(10.0);
            row.setPrefHeight(230.0);
            row.setVgrow(javafx.scene.layout.Priority.SOMETIMES);
            menuGridPane.getRowConstraints().add(row);
        }

        for (int i = 0; i < centralMenuList.size(); i++) {
            int row = i / 2;
            int col = i % 2;

            Menu centralMenu = centralMenuList.get(i);
            Catalog catalog = findCatalogByMenuId(centralMenu.getId());

            AnchorPane menuPane = createMenuPane(centralMenu, catalog);
            menuGridPane.add(menuPane, col, row);
        }
    }

    private Catalog findCatalogByMenuId(int menuId) {
        return catalogList.stream()
                .filter(c -> c.getMenuId() == menuId)
                .findFirst()
                .orElse(null);
    }

    private AnchorPane createMenuPane(Menu centralMenu, Catalog catalog) {
        AnchorPane pane = new AnchorPane();
        boolean isInCatalog = catalog != null;

        if (isInCatalog) {
            pane.setStyle("-fx-background-color: #E8F5E8; -fx-background-radius: 15px; -fx-border-color: #4CAF50; -fx-border-width: 2px; -fx-border-radius: 15px;");
        } else {
            pane.setStyle("-fx-background-color: #f4d7a7; -fx-background-radius: 15px;");
        }

        pane.setPrefWidth(450.0);
        pane.setPrefHeight(220.0);
        GridPane.setMargin(pane, new Insets(5, 5, 5, 5));

        ImageView imageView = new ImageView();
        imageView.setFitHeight(150.0);
        imageView.setFitWidth(150.0);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);

        try {
            if (centralMenu.getImage() != null && !centralMenu.getImage().isEmpty()) {
                File file = new File("src/main/resources/com/example/proyekbasisdata/assets/" + centralMenu.getImage());
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                }
            } else {
                Image placeholder = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/proyekbasisdata/assets/placeholder-image.png")));
                imageView.setImage(placeholder);
            }
        } catch (Exception e) {
            Image placeholder = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/proyekbasisdata/assets/placeholder-image.png")));
            imageView.setImage(placeholder);
        }

        AnchorPane.setBottomAnchor(imageView, 35.0);
        AnchorPane.setLeftAnchor(imageView, 15.0);
        AnchorPane.setTopAnchor(imageView, 35.0);

        Label nameLabel = new Label(centralMenu.getName());
        nameLabel.setWrapText(true);
        nameLabel.setFont(Font.font("Poppins SemiBold", 20.0));
        AnchorPane.setLeftAnchor(nameLabel, 180.0);
        AnchorPane.setRightAnchor(nameLabel, 15.0);
        AnchorPane.setTopAnchor(nameLabel, 27.0);

        Label descLabel = new Label(centralMenu.getDescription());
        descLabel.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        descLabel.setPrefHeight(50.0);
        descLabel.setWrapText(true);
        descLabel.setFont(Font.font("Poppins Regular", 11.0));
        AnchorPane.setLeftAnchor(descLabel, 180.0);
        AnchorPane.setRightAnchor(descLabel, 15.0);
        AnchorPane.setTopAnchor(descLabel, 55.0);

        Label priceLabel = new Label(String.format("Rp. %.0f", centralMenu.getPrice()));
        priceLabel.setAlignment(javafx.geometry.Pos.CENTER);
        priceLabel.setStyle("-fx-text-fill: #2196F3;");
        priceLabel.setFont(Font.font("Poppins Bold", 22.0));
        AnchorPane.setBottomAnchor(priceLabel, 25.0);
        AnchorPane.setLeftAnchor(priceLabel, 180.0);

        if (isInCatalog) {
            Label stockLabel = new Label("Stock: " + catalog.getStock());
            stockLabel.setStyle(catalog.getStock() > 0 ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #F44336;");
            stockLabel.setFont(Font.font("Poppins SemiBold", 12.0));
            AnchorPane.setBottomAnchor(stockLabel, 5.0);
            AnchorPane.setLeftAnchor(stockLabel, 180.0);
            pane.getChildren().add(stockLabel);
        }

        if (isInCatalog) {
            Button editButton = createActionButton("#FF9800", "edit-icon.png");
            editButton.setOnAction(e -> onEditCatalogClick(catalog, centralMenu));
            AnchorPane.setBottomAnchor(editButton, 27.0);
            AnchorPane.setRightAnchor(editButton, 55.0);

            Button removeButton = createActionButton("#F44336", "delete-icon.png");
            removeButton.setOnAction(e -> onRemoveMenuFromCatalogClick(catalog));
            AnchorPane.setBottomAnchor(removeButton, 27.0);
            AnchorPane.setRightAnchor(removeButton, 15.0);

            pane.getChildren().addAll(editButton, removeButton);
        } else {
            Button addButton = createActionButton("#4CAF50", "add-icon.png");
            addButton.setOnAction(e -> onAddMenuToCatalogClick(centralMenu));
            AnchorPane.setBottomAnchor(addButton, 27.0);
            AnchorPane.setRightAnchor(addButton, 15.0);

            pane.getChildren().add(addButton);
        }

        pane.getChildren().addAll(imageView, nameLabel, descLabel, priceLabel);
        return pane;
    }

    private Button createActionButton(String color, String iconName) {
        Button button = new Button();
        button.setMaxHeight(30.0);
        button.setMaxWidth(32.0);
        button.setMinHeight(30.0);
        button.setMinWidth(32.0);
        button.setStyle("-fx-background-color: " + color + ";");
        button.setCursor(Cursor.HAND);

        ImageView icon = new ImageView();
        icon.setFitHeight(22.0);
        icon.setFitWidth(22.0);
        icon.setPickOnBounds(true);
        icon.setPreserveRatio(true);

        try {
            Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/proyekbasisdata/assets/" + iconName)));
            icon.setImage(iconImage);
        } catch (Exception e) {
            System.out.println("Icon not found: " + iconName);
        }

        button.setGraphic(icon);
        return button;
    }

    private void onAddMenuToCatalogClick(Menu menu) {
        // Show dialog to input initial stock
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Add Menu to Catalog");
        dialog.setHeaderText("Set initial stock for this menu:");
        dialog.setContentText("Stock:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int stock = Integer.parseInt(result.get());
                if (stock < 0) {
                    showErrorAlert("Invalid Input", "Stock cannot be negative.");
                    return;
                }

                Connection connection = DataSourceManager.getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO catalogs (stock, branch_id, menu_id) VALUES (?, ?, ?)"
                );
                stmt.setInt(1, stock);
                stmt.setInt(2, branchId);
                stmt.setInt(3, menu.getId());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showInfoAlert("Success", "Menu added to catalog successfully.");
                    loadCatalogs();
                    displayMenus();
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid Input", "Please enter a valid number for stock.");
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to add menu to catalog: " + e.getMessage());
            }
        }
    }

    private void onEditCatalogClick(Catalog catalog, Menu menu) {
        // Show dialog to edit stock
        TextInputDialog dialog = new TextInputDialog(String.valueOf(catalog.getStock()));
        dialog.setTitle("Edit Menu Stock");
        dialog.setHeaderText("Edit stock for: " + menu.getName());
        dialog.setContentText("Stock:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int newStock = Integer.parseInt(result.get());
                if (newStock < 0) {
                    showErrorAlert("Invalid Input", "Stock cannot be negative.");
                    return;
                }

                Connection connection = DataSourceManager.getDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(
                        "UPDATE catalogs SET stock = ? WHERE id = ?"
                );
                stmt.setInt(1, newStock);
                stmt.setInt(2, catalog.getId());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    showInfoAlert("Success", "Stock updated successfully.");
                    loadCatalogs();
                    displayMenus();
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid Input", "Please enter a valid number for stock.");
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to update stock: " + e.getMessage());
            }
        }
    }

    private void onRemoveMenuFromCatalogClick(Catalog catalog) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Remove Menu");
        confirmAlert.setHeaderText("Are you sure you want to remove this menu from catalog?");
        confirmAlert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            removeMenuFromCatalog(catalog.getId());
        }
    }

    private void removeMenuFromCatalog(int catalogId) {
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM catalogs WHERE id = ?");
            stmt.setInt(1, catalogId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showInfoAlert("Success", "Menu removed from catalog successfully.");
                loadCatalogs();
                displayMenus();
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to remove menu from catalog: " + e.getMessage());
        }
    }

    private void displayNoData() {
        categoryGridPane.getChildren().clear();
        menuGridPane.getChildren().clear();

        Label noCategoriesLabel = new Label("No categories available.");
        noCategoriesLabel.setFont(Font.font("Poppins Regular", 18.0));
        noCategoriesLabel.setStyle("-fx-text-fill: #666666;");

        AnchorPane categoryPane = new AnchorPane();
        categoryPane.getChildren().add(noCategoriesLabel);
        AnchorPane.setLeftAnchor(noCategoriesLabel, 20.0);
        AnchorPane.setTopAnchor(noCategoriesLabel, 10.0);

        categoryGridPane.add(categoryPane, 0, 0);
        displayNoMenuData();
    }

    private void displayNoMenuData() {
        Label noMenusLabel = new Label("No menus available for this category.");
        noMenusLabel.setFont(Font.font("Poppins Regular", 18.0));
        noMenusLabel.setStyle("-fx-text-fill: #666666;");

        AnchorPane menuPane = new AnchorPane();
        menuPane.getChildren().add(noMenusLabel);
        AnchorPane.setLeftAnchor(noMenusLabel, 20.0);
        AnchorPane.setTopAnchor(noMenusLabel, 20.0);

        menuGridPane.add(menuPane, 0, 0);
    }

    @FXML
    public void onDashboardClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("BranchAdminPage/Dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    @FXML
    public void onStaffManagementClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("BranchAdminPage/StaffManagement.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    @FXML
    public void onOrderHistoryClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("BranchAdminPage/OrderHistory.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    @FXML
    public void onDeliveryAssignmentClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("BranchAdminPage/DeliveryAssignment.fxml"));
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