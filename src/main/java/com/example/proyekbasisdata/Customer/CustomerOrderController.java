package com.example.proyekbasisdata.Customer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

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

    private final ObservableList<MenuItem> menuList = FXCollections.observableArrayList();
    private final Map<String, Integer> branchMap = new HashMap<>();

    @FXML
    public void initialize() {
        setupTable();
        loadBranches();
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        colAction.setCellFactory(new Callback<TableColumn<MenuItem, Void>, TableCell<MenuItem, Void>>() {
            @Override
            public TableCell<MenuItem, Void> call(final TableColumn<MenuItem, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("+");

                    {
                        btn.setOnAction(event -> {
                            MenuItem item = getTableView().getItems().get(getIndex());
                            showAlert("Tambah Pesanan", "Menu ditambahkan: " + item.getName());
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                };
            }
        });

        menuTableView.setItems(menuList);
    }

    @FXML
    private void onBranchSelected() {
        String selectedBranch = branchComboBox.getValue();
        if (selectedBranch == null || !branchMap.containsKey(selectedBranch)) {
            return;
        }
        int branchId = branchMap.get(selectedBranch);
        loadMenusByBranch(branchId);
    }

    private void loadBranches() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM branches");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                branchComboBox.getItems().add(name);
                branchMap.put(name, id);
            }
        } catch (SQLException e) {
            showAlert("Error", "Gagal memuat cabang: " + e.getMessage());
        }
    }

    private void loadMenusByBranch(int branchId) {
        menuList.clear();
        String query = "SELECT m.name, m.description, m.price FROM catalogs c " +
                "JOIN menus m ON c.menu_id = m.id WHERE c.branch_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                menuList.add(new MenuItem(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            showAlert("Error", "Gagal memuat menu: " + e.getMessage());
        }
    }

    @FXML
    private void onCheckout() {
        showAlert("Checkout", "Fitur checkout berhasil dijalankan.");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/your_database", "your_user", "your_password");
    }

    public static class MenuItem {
        private final String name;
        private final String description;
        private final Double price;

        public MenuItem(String name, String description, Double price) {
            this.name = name;
            this.description = description;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Double getPrice() {
            return price;
        }
    }
}
