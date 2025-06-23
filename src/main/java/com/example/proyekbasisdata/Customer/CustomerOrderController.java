package com.example.proyekbasisdata.Customer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.sql.*;

public class CustomerOrderController {

    @FXML
    private ComboBox<String> branchComboBox;

    @FXML
    private TableView<MenuItem> menuTableView;

    @FXML
    private TableColumn<MenuItem, String> colName;

    @FXML
    private TableColumn<MenuItem, String> colDescription;

    @FXML
    private TableColumn<MenuItem, Double> colPrice;

    @FXML
    private TableColumn<MenuItem, Void> colAction;

    @FXML
    private Button checkoutButton;

    private ObservableList<MenuItem> menuList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadBranches();

        branchComboBox.setOnAction(e -> {
            String branch = branchComboBox.getSelectionModel().getSelectedItem();
            if (branch != null) loadMenusByBranch(branch);
        });
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colAction.setCellFactory(getActionCellFactory());
        menuTableView.setItems(menuList);
    }

    private Callback<TableColumn<MenuItem, Void>, TableCell<MenuItem, Void>> getActionCellFactory() {
        return col -> new TableCell<>() {
            private final Button addButton = new Button("+");
            private final Button minusButton = new Button("-");
            private final HBox hBox = new HBox(5, addButton, minusButton);

            {
                addButton.setOnAction(e -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    item.setQuantity(item.getQuantity() + 1);
                    menuTableView.refresh();
                });
                minusButton.setOnAction(e -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    if (item.getQuantity() > 0) {
                        item.setQuantity(item.getQuantity() - 1);
                        menuTableView.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hBox);
            }
        };
    }

    private void loadBranches() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name FROM branches");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                branchComboBox.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            showAlert("Error loading branches", e.getMessage());
        }
    }

    private void loadMenusByBranch(String branchName) {
        menuList.clear();
        String sql = "SELECT m.name, m.description, m.price FROM catalogs c JOIN menus m ON c.menu_id = m.id JOIN branches b ON c.branch_id = b.id WHERE b.name = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, branchName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                menuList.add(new MenuItem(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        0
                ));
            }
        } catch (SQLException e) {
            showAlert("Error loading menu", e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/your_database", "your_user", "your_password");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    public static class MenuItem {
        private String name;
        private String description;
        private double price;
        private int quantity;

        public MenuItem(String name, String description, double price, int quantity) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
