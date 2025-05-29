package com.example.proyekbasisdata.centralAdmin;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import com.example.proyekbasisdata.dtos.Category;
import com.example.proyekbasisdata.dtos.Menu;
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

public class MenuController {
    // Initialize variables
    @FXML
    private GridPane categoryGridPane;

    @FXML
    private GridPane menuGridPane;

    private List<Menu> menuList;
    private List<Category> categoryList;
    private int currentCategoryId = -1;

    // Initialize the controller
    @FXML
    public void initialize() {
        loadData();
    }

    // Load data from the database
    private void loadData() {
        loadCategories();
        if (!categoryList.isEmpty()) {
            currentCategoryId = categoryList.get(0).getId();
            loadMenus();
            displayCategories();
            displayMenus();
        } else {
            displayNoData();
        }
    }

    // Load categories from the database
    private void loadCategories() {
        categoryList = new ArrayList<>();
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
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

    // Load menus based on the selected category
    private void loadMenus() {
        menuList = new ArrayList<>();
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
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
                menuList.add(menu);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load menus: " + e.getMessage());
        }
    }

    // Display categories in the grid pane
    private void displayCategories() {
        categoryGridPane.getChildren().clear();
        categoryGridPane.getColumnConstraints().clear();
        categoryGridPane.getRowConstraints().clear();

        if (categoryList.isEmpty()) return;

        // Setup grid constraints
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

    // Create category button
    private Button createCategoryButton(Category category) {
        Button button = new Button(category.getName());
        button.setAlignment(javafx.geometry.Pos.CENTER);
        button.setMaxHeight(Double.MAX_VALUE);
        button.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        button.setTextFill(javafx.scene.paint.Color.WHITE);
        button.setFont(Font.font("Poppins SemiBold", 16.0));
        button.setPadding(new Insets(0, 20, 0, 20));
        button.setCursor(Cursor.HAND);

        // Set style based on whether this is the active category
        if (category.getId() == currentCategoryId) {
            button.setStyle("-fx-background-color: #c29156; -fx-background-radius: 8px;");
        } else {
            button.setStyle("-fx-background-color: #e5b17a; -fx-background-radius: 8px;");
        }

        // Add margin
        GridPane.setMargin(button, new Insets(3, 3, 3, 3));

        // Add click event
        button.setOnAction(e -> {
            currentCategoryId = category.getId();
            loadMenus();
            displayCategories();
            displayMenus();
        });

        return button;
    }

    // Display menus in the grid pane
    private void displayMenus() {
        menuGridPane.getChildren().clear();
        menuGridPane.getColumnConstraints().clear();
        menuGridPane.getRowConstraints().clear();

        if (menuList.isEmpty()) {
            displayNoMenuData();
            return;
        }

        // Setup grid constraints for 2 columns
        for (int i = 0; i < 2; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
            column.setMinWidth(10.0);
            column.setPrefWidth(450.0);
            menuGridPane.getColumnConstraints().add(column);
        }

        int numRows = (int) Math.ceil((double) menuList.size() / 2);

        // Add rows for menus
        for (int i = 0; i < numRows; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(10.0);
            row.setPrefHeight(210.0);
            row.setVgrow(javafx.scene.layout.Priority.SOMETIMES);
            menuGridPane.getRowConstraints().add(row);
        }

        // Add menus to grid in 2-column layout
        for (int i = 0; i < menuList.size(); i++) {
            int row = i / 2;
            int col = i % 2;

            AnchorPane menuPane = createMenuPane(menuList.get(i));
            menuGridPane.add(menuPane, col, row);
        }
    }

    // Create menu pane
    private AnchorPane createMenuPane(Menu menu) {
        AnchorPane pane = new AnchorPane();
        pane.setStyle("-fx-background-color: #f4d7a7; -fx-background-radius: 15px;");
        pane.setPrefWidth(450.0);
        pane.setPrefHeight(200.0);
        GridPane.setMargin(pane, new Insets(5, 5, 5, 5));

        // Menu image
        ImageView imageView = new ImageView();
        imageView.setFitHeight(150.0);
        imageView.setFitWidth(150.0);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);

        try {
            if (menu.getImage() != null && !menu.getImage().isEmpty()) {
                File file = new File("src/main/resources/com/example/proyekbasisdata/assets/" + menu.getImage());
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

        AnchorPane.setBottomAnchor(imageView, 25.0);
        AnchorPane.setLeftAnchor(imageView, 15.0);
        AnchorPane.setTopAnchor(imageView, 25.0);

        // Menu name
        Label nameLabel = new Label(menu.getName());
        nameLabel.setWrapText(true);
        nameLabel.setFont(Font.font("Poppins SemiBold", 24.0));
        AnchorPane.setLeftAnchor(nameLabel, 180.0);
        AnchorPane.setRightAnchor(nameLabel, 15.0);
        AnchorPane.setTopAnchor(nameLabel, 27.0);

        // Menu description
        Label descLabel = new Label(menu.getDescription());
        descLabel.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        descLabel.setPrefHeight(65.0);
        descLabel.setWrapText(true);
        descLabel.setFont(Font.font("Poppins Regular", 12.0));
        AnchorPane.setLeftAnchor(descLabel, 180.0);
        AnchorPane.setRightAnchor(descLabel, 15.0);
        AnchorPane.setTopAnchor(descLabel, 65.0);

        // Price label
        Label priceLabel = new Label(String.format("Rp. %.0f", menu.getPrice()));
        priceLabel.setAlignment(javafx.geometry.Pos.CENTER);
        priceLabel.setStyle("-fx-text-fill: #a16e34;");
        priceLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        priceLabel.setTextFill(javafx.scene.paint.Color.valueOf("#a16e34"));
        priceLabel.setFont(Font.font("Poppins Bold", 26.0));
        AnchorPane.setBottomAnchor(priceLabel, 24.0);
        AnchorPane.setLeftAnchor(priceLabel, 180.0);

        // Edit button
        Button editButton = new Button();
        editButton.setMaxHeight(30.0);
        editButton.setMaxWidth(32.0);
        editButton.setMinHeight(30.0);
        editButton.setMinWidth(32.0);
        editButton.setStyle("-fx-background-color: #66ce7d;");
        editButton.setCursor(Cursor.HAND);
        editButton.setOnAction(e -> onEditMenuClick(menu));

        ImageView editIcon = new ImageView();
        editIcon.setFitHeight(22.0);
        editIcon.setFitWidth(22.0);
        editIcon.setPickOnBounds(true);
        editIcon.setPreserveRatio(true);
        try {
            Image editImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/proyekbasisdata/assets/edit-icon.png")));
            editIcon.setImage(editImage);
        } catch (Exception e) {
            System.out.println("Edit icon not found");
        }
        editButton.setGraphic(editIcon);

        AnchorPane.setBottomAnchor(editButton, 27.0);
        AnchorPane.setRightAnchor(editButton, 55.0);

        // Delete button
        Button deleteButton = new Button();
        deleteButton.setMaxHeight(30.0);
        deleteButton.setMaxWidth(32.0);
        deleteButton.setMinHeight(30.0);
        deleteButton.setMinWidth(32.0);
        deleteButton.setStyle("-fx-background-color: #ff6d6d;");
        deleteButton.setCursor(Cursor.HAND);
        deleteButton.setOnAction(e -> onDeleteMenuClick(menu));

        ImageView deleteIcon = new ImageView();
        deleteIcon.setFitHeight(22.0);
        deleteIcon.setFitWidth(22.0);
        deleteIcon.setPickOnBounds(true);
        deleteIcon.setPreserveRatio(true);
        try {
            Image deleteImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/proyekbasisdata/assets/delete-icon.png")));
            deleteIcon.setImage(deleteImage);
        } catch (Exception e) {
            System.out.println("Delete icon not found");
        }
        deleteButton.setGraphic(deleteIcon);

        AnchorPane.setBottomAnchor(deleteButton, 27.0);
        AnchorPane.setRightAnchor(deleteButton, 15.0);

        // Add all elements to pane
        pane.getChildren().addAll(imageView, nameLabel, descLabel, priceLabel, editButton, deleteButton);

        return pane;
    }

    // Display no data message
    private void displayNoData() {
        categoryGridPane.getChildren().clear();
        menuGridPane.getChildren().clear();

        Label noCategoriesLabel = new Label("No categories available. Please add a category first.");
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

    // Handle add category button click
    @FXML
    private void onAddCategoryClick() throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/category-form.fxml"));
        Scene scene = new Scene(loader.load());
        CategoryFormController controller = loader.getController();
        controller.setMode("ADD", null);
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    // Handle edit category button click
    @FXML
    private void onEditCategoryClick() throws IOException {
        if (currentCategoryId == -1) {
            showWarningAlert("No Category Selected", "Please select a category to edit.");
            return;
        }

        // Find current category
        Category currentCategory = categoryList.stream()
                .filter(c -> c.getId() == currentCategoryId)
                .findFirst()
                .orElse(null);

        if (currentCategory != null) {
            System.out.println("Edit Category: " + currentCategory.getName());
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/category-form.fxml"));
            Scene scene = new Scene(loader.load());
            CategoryFormController controller = loader.getController();
            controller.setMode("EDIT", currentCategory);
            app.getPrimaryStage().setScene(scene);
            app.getPrimaryStage().sizeToScene();
        }
    }

    // Handle delete category button click
    @FXML
    private void onDeleteCategoryClick() {
        if (currentCategoryId == -1) {
            showWarningAlert("No Category Selected", "Please select a category to delete.");
            return;
        }

        // Find current category
        Category currentCategory = categoryList.stream()
                .filter(c -> c.getId() == currentCategoryId)
                .findFirst()
                .orElse(null);

        if (currentCategory != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Category");
            confirmAlert.setHeaderText("Are you sure you want to delete this category?");
            confirmAlert.setContentText("Category: " + currentCategory.getName() + "\nThis action cannot be undone.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                deleteCategory(currentCategory.getId());
            }
        }
    }

    // Delete category from the database
    private void deleteCategory(int categoryId) {
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM categories WHERE id = ?");
            stmt.setInt(1, categoryId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                showInfoAlert("Success", "Category deleted successfully.");
                loadData(); // Refresh the display
            } else {
                showErrorAlert("Delete Failed", "Failed to delete category.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to delete category: " + e.getMessage());
        }
    }

    // Handle add menu button click
    @FXML
    private void onAddMenuClick() {
        if (currentCategoryId == -1) {
            showWarningAlert("No Category Selected", "Please select a category first.");
            return;
        }

        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/menu-form.fxml"));
            Scene scene = new Scene(loader.load());
            MenuFormController controller = loader.getController();
            controller.setMode("ADD", currentCategoryId, null);
            app.getPrimaryStage().setScene(scene);
            app.getPrimaryStage().sizeToScene();
            System.out.println("Add Menu clicked, opening menu form for category ID: " + currentCategoryId);
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open menu form: " + e.getMessage());
        }
    }

    // Handle edit menu button click
    @FXML
    private void onEditMenuClick(Menu menu) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/menu-form.fxml"));
            Scene scene = new Scene(loader.load());
            MenuFormController controller = loader.getController();
            controller.setMode("EDIT", currentCategoryId, menu);
            app.getPrimaryStage().setScene(scene);
            app.getPrimaryStage().sizeToScene();
            System.out.println("Edit Menu clicked for menu ID: " + menu.getId());
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open menu form: " + e.getMessage());
        }
    }

    // Handle delete menu button click
    @FXML
    private void onDeleteMenuClick(Menu menu) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Menu");
        confirmAlert.setHeaderText("Are you sure you want to delete this menu?");
        confirmAlert.setContentText("Menu: " + menu.getName() + "\nThis action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteMenu(menu.getId());
        }
    }

    private void deleteMenu(int menuId) {
        String imageName = null;
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM menus WHERE id = ?");
            PreparedStatement selectStmt = connection.prepareStatement("SELECT image FROM menus WHERE id = ?");
            stmt.setInt(1, menuId);
            selectStmt.setInt(1, menuId);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                imageName = rs.getString("image");
            }
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                if (imageName != null && !imageName.isEmpty() && !imageName.equals("placeholder-image.png")) {
                    File imageFile = new File("src/main/resources/com/example/proyekbasisdata/assets/" + imageName);
                    if (imageFile.exists()) {
                        if (!imageFile.delete()) {
                            System.out.println("Failed to delete image file: " + imageFile.getAbsolutePath());
                        }
                    }
                }
                showInfoAlert("Success", "Menu deleted successfully.");
                loadMenus(); // Refresh the menus
                displayMenus();
            } else {
                showErrorAlert("Delete Failed", "Failed to delete menu.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to delete menu: " + e.getMessage());
        }
    }

    // Handle dashboard page button click
    @FXML
    private void onDashboardPageClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    // Handle promo page button click
    @FXML
    private void onPromoPageClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/promo.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    // Handle report page button click
    @FXML
    private void onReportPageClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/report.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    // Handle logout button click
    @FXML
    private void onLogoutClick(ActionEvent event) throws IOException {
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

    // Show warning alert
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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
