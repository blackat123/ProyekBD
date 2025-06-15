package com.example.proyekbasisdata.branchAdmin;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OrderHistoryController {

    public static class OrderHistory {
        private int id;
        private String customerName;
        private String customerAddress;
        private String customerPhone;
        private String orderType;
        private String status;
        private double totalPrice;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String orderItems;

        public OrderHistory(int id, String customerName, String customerAddress, String customerPhone,
                            String orderType, String status, double totalPrice, LocalDateTime createdAt,
                            LocalDateTime updatedAt, String orderItems) {
            this.id = id;
            this.customerName = customerName;
            this.customerAddress = customerAddress;
            this.customerPhone = customerPhone;
            this.orderType = orderType;
            this.status = status;
            this.totalPrice = totalPrice;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.orderItems = orderItems;
        }

        // Getters
        public int getId() { return id; }
        public String getCustomerName() { return customerName; }
        public String getCustomerAddress() { return customerAddress; }
        public String getCustomerPhone() { return customerPhone; }
        public String getOrderType() { return orderType; }
        public String getStatus() { return status; }
        public double getTotalPrice() { return totalPrice; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public String getOrderItems() { return orderItems; }

        public String getFormattedCreatedAt() {
            return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
        }

        public String getFormattedUpdatedAt() {
            return updatedAt != null ? updatedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
        }

        // Setters
        public void setStatus(String status) { this.status = status; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    @FXML
    private TableView<OrderHistory> orderTable;

    @FXML
    private TableColumn<OrderHistory, Integer> orderIdColumn;

    @FXML
    private TableColumn<OrderHistory, String> customerNameColumn;

    @FXML
    private TableColumn<OrderHistory, String> customerPhoneColumn;

    @FXML
    private TableColumn<OrderHistory, String> orderTypeColumn;

    @FXML
    private TableColumn<OrderHistory, String> statusColumn;

    @FXML
    private TableColumn<OrderHistory, Double> totalPriceColumn;

    @FXML
    private TableColumn<OrderHistory, String> createdAtColumn;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> orderTypeFilterComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button updateStatusButton;

    @FXML
    private Button viewDetailsButton;

    private ObservableList<OrderHistory> orderList;
    private int branchId;
    private OrderHistory selectedOrder;

    @FXML
    public void initialize() {
        HelloApplication app = HelloApplication.getApplicationInstance();
        this.branchId = getBranchIdByAdminId(app.getUserId());

        setupTableColumns();
        setupFilters();
        loadOrders();
        setupTableSelectionListener();

        updateStatusButton.setDisable(true);
        viewDetailsButton.setDisable(true);
    }

    private int getBranchIdByAdminId(int adminId) {
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement("SELECT branch_id FROM branch_admins WHERE id = ?");
            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("branch_id");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to get branch ID: " + e.getMessage());
        }
        return -1;
    }

    private void setupTableColumns() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        orderTypeColumn.setCellValueFactory(new PropertyValueFactory<>("orderType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("formattedCreatedAt"));

        // Format total price column
        totalPriceColumn.setCellFactory(column -> new TableCell<OrderHistory, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("Rp. %.0f", item));
                }
            }
        });

        // Format status column with colors
        statusColumn.setCellFactory(column -> new TableCell<OrderHistory, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "PENDING":
                            setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                            break;
                        case "CONFIRMED":
                            setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                            break;
                        case "PREPARING":
                            setStyle("-fx-text-fill: #9C27B0; -fx-font-weight: bold;");
                            break;
                        case "READY":
                            setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            break;
                        case "DELIVERED":
                            setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            break;
                        case "CANCELLED":
                            setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #666666;");
                    }
                }
            }
        });

        orderList = FXCollections.observableArrayList();
        orderTable.setItems(orderList);
    }

    private void setupFilters() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL", "PENDING", "CONFIRMED", "PREPARING", "READY", "DELIVERED", "CANCELLED"
        ));
        statusFilterComboBox.setValue("ALL");

        orderTypeFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL", "DINE_IN", "TAKEAWAY", "DELIVERY"
        ));
        orderTypeFilterComboBox.setValue("ALL");

        statusFilterComboBox.setOnAction(e -> loadOrders());
        orderTypeFilterComboBox.setOnAction(e -> loadOrders());
    }

    private void setupTableSelectionListener() {
        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedOrder = newSelection;
                updateStatusButton.setDisable(false);
                viewDetailsButton.setDisable(false);
            } else {
                selectedOrder = null;
                updateStatusButton.setDisable(true);
                viewDetailsButton.setDisable(true);
            }
        });
    }

    private void loadOrders() {
        orderList.clear();
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();

            String sql = """
                SELECT o.id, o.customer_name, o.customer_address, o.customer_phone, 
                       o.order_type, o.status, o.total_price, o.created_at, o.updated_at,
                       GROUP_CONCAT(CONCAT(oi.quantity, 'x ', m.name) SEPARATOR ', ') as order_items
                FROM orders o
                LEFT JOIN order_items oi ON o.id = oi.order_id
                LEFT JOIN menus m ON oi.menu_id = m.id
                WHERE o.branch_id = ?
                """;

            if (!"ALL".equals(statusFilterComboBox.getValue())) {
                sql += " AND o.status = ?";
            }

            if (!"ALL".equals(orderTypeFilterComboBox.getValue())) {
                sql += " AND o.order_type = ?";
            }

            if (searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                sql += " AND (o.customer_name LIKE ? OR o.customer_phone LIKE ? OR CAST(o.id AS CHAR) LIKE ?)";
            }

            sql += " GROUP BY o.id ORDER BY o.created_at DESC";

            PreparedStatement stmt = connection.prepareStatement(sql);
            int paramIndex = 1;
            stmt.setInt(paramIndex++, branchId);

            if (!"ALL".equals(statusFilterComboBox.getValue())) {
                stmt.setString(paramIndex++, statusFilterComboBox.getValue());
            }

            if (!"ALL".equals(orderTypeFilterComboBox.getValue())) {
                stmt.setString(paramIndex++, orderTypeFilterComboBox.getValue());
            }

            if (searchField.getText() != null && !searchField.getText().trim().isEmpty()) {
                String searchTerm = "%" + searchField.getText().trim() + "%";
                stmt.setString(paramIndex++, searchTerm);
                stmt.setString(paramIndex++, searchTerm);
                stmt.setString(paramIndex++, searchTerm);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                OrderHistory order = new OrderHistory(
                        rs.getInt("id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_address"),
                        rs.getString("customer_phone"),
                        rs.getString("order_type"),
                        rs.getString("status"),
                        rs.getDouble("total_price"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                        rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null,
                        rs.getString("order_items")
                );
                orderList.add(order);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load orders: " + e.getMessage());
        }
    }

    @FXML
    public void onSearchClick(ActionEvent event) {
        loadOrders();
    }

    @FXML
    public void onRefreshClick(ActionEvent event) {
        searchField.clear();
        statusFilterComboBox.setValue("ALL");
        orderTypeFilterComboBox.setValue("ALL");
        loadOrders();
    }

    @FXML
    public void onUpdateStatusClick(ActionEvent event) {
        if (selectedOrder == null) {
            showErrorAlert("Selection Error", "Please select an order to update.");
            return;
        }

        String[] availableStatuses;
        switch (selectedOrder.getStatus()) {
            case "PENDING":
                availableStatuses = new String[]{"CONFIRMED", "CANCELLED"};
                break;
            case "CONFIRMED":
                availableStatuses = new String[]{"PREPARING", "CANCELLED"};
                break;
            case "PREPARING":
                availableStatuses = new String[]{"READY"};
                break;
            case "READY":
                if ("DELIVERY".equals(selectedOrder.getOrderType())) {
                    availableStatuses = new String[]{"DELIVERED"};
                } else {
                    availableStatuses = new String[]{"DELIVERED"};
                }
                break;
            default:
                showInfoAlert("Status Update", "This order status cannot be changed.");
                return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(availableStatuses[0], availableStatuses);
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Update status for Order #" + selectedOrder.getId());
        dialog.setContentText("Select new status:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            updateOrderStatus(selectedOrder.getId(), result.get());
        }
    }

    private void updateOrderStatus(int orderId, String newStatus) {
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?"
            );
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showInfoAlert("Success", "Order status updated successfully.");

                // If status is DELIVERED and order type is DELIVERY, create delivery assignment if not exists
                if ("DELIVERED".equals(newStatus) && selectedOrder.getOrderType().equals("DELIVERY")) {
                    createDeliveryAssignmentIfNotExists(orderId);
                }

                loadOrders();
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update order status: " + e.getMessage());
        }
    }

    private void createDeliveryAssignmentIfNotExists(int orderId) {
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();

            PreparedStatement checkStmt = connection.prepareStatement(
                    "SELECT COUNT(*) FROM delivery_assignments WHERE order_id = ?"
            );
            checkStmt.setInt(1, orderId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                PreparedStatement insertStmt = connection.prepareStatement(
                        "INSERT INTO delivery_assignments (order_id, status, assigned_at) VALUES (?, 'PENDING', CURRENT_TIMESTAMP)"
                );
                insertStmt.setInt(1, orderId);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Failed to create delivery assignment: " + e.getMessage());
        }
    }

    @FXML
    public void onViewDetailsClick(ActionEvent event) {
        if (selectedOrder == null) {
            showErrorAlert("Selection Error", "Please select an order to view details.");
            return;
        }

        showOrderDetails(selectedOrder);
    }

    private void showOrderDetails(OrderHistory order) {
        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Order Details");
        detailsAlert.setHeaderText("Order #" + order.getId() + " Details");

        StringBuilder details = new StringBuilder();
        details.append("Customer Name: ").append(order.getCustomerName()).append("\n");
        details.append("Phone: ").append(order.getCustomerPhone()).append("\n");
        if (order.getCustomerAddress() != null && !order.getCustomerAddress().isEmpty()) {
            details.append("Address: ").append(order.getCustomerAddress()).append("\n");
        }
        details.append("Order Type: ").append(order.getOrderType()).append("\n");
        details.append("Status: ").append(order.getStatus()).append("\n");
        details.append("Total Price: Rp. ").append(String.format("%.0f", order.getTotalPrice())).append("\n");
        details.append("Created At: ").append(order.getFormattedCreatedAt()).append("\n");
        if (order.getUpdatedAt() != null) {
            details.append("Updated At: ").append(order.getFormattedUpdatedAt()).append("\n");
        }
        details.append("\nOrder Items:\n").append(order.getOrderItems() != null ? order.getOrderItems() : "No items found");

        detailsAlert.setContentText(details.toString());
        detailsAlert.showAndWait();
    }

    @FXML
    public void onDashboardClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("branchAdminPage/dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    @FXML
    public void onMenuCatalogClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("branchAdminPage/menu-catalog.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    @FXML
    public void onStaffManagementClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("branchAdminPage/staff-management.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    @FXML
    public void onDeliveryAssignmentClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("branchAdminPage/delivery-assignment.fxml"));
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