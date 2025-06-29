package com.example.proyekbasisdata.Customer;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.dtos.MenuItem;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    private TableColumn<MenuItem, Integer> colQuantity;

    @FXML
    private TableColumn<MenuItem, Void> colAction;

    @FXML
    private Label labelTotal;

    @FXML
    private Button checkoutButton;

    private ObservableList<MenuItem> menuList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadBranchNames();

        branchComboBox.setOnAction(e -> {
            String branch = branchComboBox.getSelectionModel().getSelectedItem();
            if (branch != null) loadMenusByBranch(branch);
        });
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
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
                    updateTotal();
                });
                minusButton.setOnAction(e -> {
                    MenuItem item = getTableView().getItems().get(getIndex());
                    if (item.getQuantity() > 0) {
                        item.setQuantity(item.getQuantity() - 1);
                        menuTableView.refresh();
                        updateTotal();
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

    private void loadBranchNames() {
        String query = "SELECT name FROM branches";

        try (Connection conn = DataSourceManager.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            List<String> branches = new ArrayList<>();
            while (rs.next()) {
                branches.add(rs.getString("name"));
            }

            branchComboBox.setItems(FXCollections.observableArrayList(branches));

        } catch (SQLException e) {
            showAlert("Error loading branches", e.getMessage());
        }
    }

    private void loadMenusByBranch(String branchName) {
        menuList.clear();
        String sql = "SELECT m.name, m.description, m.price, 0 quantity FROM catalogs c " +
                "JOIN menus m ON c.menu_id = m.id " +
                "JOIN branches b ON c.branch_id = b.id WHERE b.name = ?";

        try (Connection conn = DataSourceManager.getDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, branchName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    menuList.add(new MenuItem(
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("quantity")
                    ));
                }
            }

            updateTotal();

        } catch (SQLException e) {
            showAlert("Error loading menus", e.getMessage());
        }
    }

    private void updateTotal() {
        double total = 0;
        for (MenuItem item : menuList) {
            total += item.getQuantity() * item.getPrice();
        }
        labelTotal.setText(String.format("Rp %, .0f", total));
    }

    @FXML
    public void checkoutButtonClick(ActionEvent event) {
        List<MenuItem> selectedItems = new ArrayList<>();
        for (MenuItem item : menuList) {
            if (item.getQuantity() > 0) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            showAlert("Tidak ada menu yang dipilih.", "Silakan pilih menu terlebih dahulu.");
            return;
        }

        String branchName = branchComboBox.getSelectionModel().getSelectedItem();
        if (branchName == null) {
            showAlert("Cabang belum dipilih", "Silakan pilih cabang.");
            return;
        }

        double totalPrice = 0;
        for (MenuItem item : selectedItems) {
            totalPrice += item.getQuantity() * item.getPrice();
        }

        String getBranchIdQuery = "SELECT id FROM branches WHERE name = ?";
        String getCatalogIdQuery = """
            SELECT c.id FROM catalogs c
            JOIN menus m ON c.menu_id = m.id
            JOIN branches b ON c.branch_id = b.id
            WHERE m.name = ? AND b.name = ?
        """;
        String insertOrderQuery = """
            INSERT INTO orders (order_date, total_price, customer_id, branch_id, status)
            VALUES (NOW(), ?, ?, ?, 'Preparing') RETURNING id
        """;
        String insertDetailQuery = """
            INSERT INTO order_details (quantity, catalog_id, order_id)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DataSourceManager.getDatabaseConnection()) {
            conn.setAutoCommit(false);
            int customerId = HelloApplication.getApplicationInstance().getUserId();

            int branchId;
            try (PreparedStatement stmt = conn.prepareStatement(getBranchIdQuery)) {
                stmt.setString(1, branchName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        branchId = rs.getInt("id");
                    } else {
                        showAlert("Cabang tidak ditemukan", "Terjadi kesalahan.");
                        return;
                    }
                }
            }

            int orderId;
            try (PreparedStatement stmt = conn.prepareStatement(insertOrderQuery)) {
                stmt.setDouble(1, totalPrice);
                stmt.setInt(2, customerId);
                stmt.setInt(3, branchId);
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    orderId = rs.getInt(1);
                }
            }

            for (MenuItem item : selectedItems) {
                int catalogId;
                try (PreparedStatement stmt = conn.prepareStatement(getCatalogIdQuery)) {
                    stmt.setString(1, item.getName());
                    stmt.setString(2, branchName);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            catalogId = rs.getInt("id");
                        } else {
                            showAlert("Menu tidak ditemukan", item.getName());
                            conn.rollback();
                            return;
                        }
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(insertDetailQuery)) {
                    stmt.setInt(1, item.getQuantity());
                    stmt.setInt(2, catalogId);
                    stmt.setInt(3, orderId);
                    stmt.executeUpdate();
                }
            }

            conn.commit(); // sukses transaksi

            openPaymentPage(orderId, totalPrice);
            menuList.clear();
            updateTotal();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Terjadi kesalahan", e.getMessage());
        }
    }

    private void openPaymentPage(int orderId, double totalPrice) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PaymentPage.fxml"));
        Scene scene = new Scene(loader.load());

        PaymentController controller = loader.getController();
        controller.setOrderDetails(orderId, totalPrice);

        Stage stage = new Stage();
        controller.setStage(stage); // untuk menutup nanti
        stage.setTitle("Pembayaran");
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informasi");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigasi tombol sidebar
    @FXML
    public void dasboardButtonClick(ActionEvent event) throws IOException {
        switchScene("Customer/CustomerDashboard.fxml");
    }

    @FXML
    public void historyButtonClick(ActionEvent event) throws IOException {
        switchScene("Customer/CustomerHistory.fxml");
    }

    @FXML
    public void onLogoutClick(ActionEvent event) throws IOException {
        switchScene("login-page.fxml");
    }

    private void switchScene(String fxmlPath) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }
}