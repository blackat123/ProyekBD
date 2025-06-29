package com.example.proyekbasisdata.branchAdmin;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import com.example.proyekbasisdata.dtos.Staff;
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
import java.util.Optional;

public class DeliveryAssignmentController {

    public static class DeliveryAssignment {
        private int id;
        private int orderId;
        private String customerName;
        private String customerAddress;
        private String customerPhone;
        private String assignedStaff;
        private String status;
        private LocalDateTime assignedAt;
        private LocalDateTime deliveredAt;
        private double totalAmount;

        public DeliveryAssignment(int id, int orderId, String customerName, String customerAddress,
                                  String customerPhone, String assignedStaff, String status,
                                  LocalDateTime assignedAt, double totalAmount) {
            this.id = id;
            this.orderId = orderId;
            this.customerName = customerName;
            this.customerAddress = customerAddress;
            this.customerPhone = customerPhone;
            this.assignedStaff = assignedStaff;
            this.status = status;
            this.assignedAt = assignedAt;
            this.deliveredAt = deliveredAt;
            this.totalAmount = totalAmount;
        }

        // Getters
        public int getId() { return id; }
        public int getOrderId() { return orderId; }
        public String getCustomerName() { return customerName; }
        public String getCustomerAddress() { return customerAddress; }
        public String getCustomerPhone() { return customerPhone; }
        public String getAssignedStaff() { return assignedStaff; }
        public String getStatus() { return status; }
        public LocalDateTime getAssignedAt() { return assignedAt; }
        public LocalDateTime getDeliveredAt() { return deliveredAt; }
        public double getTotalAmount() { return totalAmount; }

        public void setStatus(String status) { this.status = status; }
        public void setAssignedStaff(String assignedStaff) { this.assignedStaff = assignedStaff; }
        public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    }

    @FXML
    private TableView<DeliveryAssignment> deliveryTable;

    @FXML
    private TableColumn<DeliveryAssignment, Integer> orderIdColumn;

    @FXML
    private TableColumn<DeliveryAssignment, String> customerNameColumn;

    @FXML
    private TableColumn<DeliveryAssignment, String> customerAddressColumn;

    @FXML
    private TableColumn<DeliveryAssignment, String> customerPhoneColumn;

    @FXML
    private TableColumn<DeliveryAssignment, String> assignedStaffColumn;

    @FXML
    private TableColumn<DeliveryAssignment, String> statusColumn;

    @FXML
    private TableColumn<DeliveryAssignment, Double> totalAmountColumn;

    @FXML
    private ComboBox<Staff> deliveryStaffComboBox;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private Button assignButton;

    @FXML
    private Button updateStatusButton;

    @FXML
    private Button refreshButton;

    private ObservableList<DeliveryAssignment> deliveryList;
    private ObservableList<Staff> deliveryStaffList;
    private int branchId;
    private DeliveryAssignment selectedDelivery;

    @FXML
    public void initialize() {
        HelloApplication app = HelloApplication.getApplicationInstance();
        this.branchId = getBranchIdByAdminId(app.getUserId());

        setupTableColumns();
        setupComboBoxes();
        loadDeliveryStaff();
        loadDeliveryAssignments();
        setupTableSelectionListener();

        assignButton.setDisable(true);
        updateStatusButton.setDisable(true);
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

    private void setupTableColumns() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerAddressColumn.setCellValueFactory(new PropertyValueFactory<>("customerAddress"));
        customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        assignedStaffColumn.setCellValueFactory(new PropertyValueFactory<>("assignedStaff"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // Format total amount column
        totalAmountColumn.setCellFactory(column -> new TableCell<DeliveryAssignment, Double>() {
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

        deliveryList = FXCollections.observableArrayList();
        deliveryTable.setItems(deliveryList);
    }

    private void setupComboBoxes() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "ALL", "PENDING", "ASSIGNED", "IN_DELIVERY", "DELIVERED", "CANCELLED"
        ));
        statusFilterComboBox.setValue("ALL");

        statusFilterComboBox.setOnAction(e -> loadDeliveryAssignments());

        deliveryStaffList = FXCollections.observableArrayList();
        deliveryStaffComboBox.setItems(deliveryStaffList);
    }

    private void setupTableSelectionListener() {
        deliveryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedDelivery = newSelection;
                assignButton.setDisable(false);
                updateStatusButton.setDisable(false);

                // Set current assigned staff in combo box
                Staff currentStaff = deliveryStaffList.stream()
                        .filter(s -> s.getName().equals(newSelection.getAssignedStaff()))
                        .findFirst()
                        .orElse(null);
                deliveryStaffComboBox.setValue(currentStaff);
            } else {
                selectedDelivery = null;
                assignButton.setDisable(true);
                updateStatusButton.setDisable(true);
                deliveryStaffComboBox.setValue(null);
            }
        });
    }

    private void loadDeliveryStaff() {
        deliveryStaffList.clear();
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM staffs WHERE branch_id = ? AND position = 'DELIVERIES' ORDER BY name"
            );
            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Staff staff = new Staff(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("phone"),
                        rs.getString("position"),
                        rs.getString("status"),
                        rs.getInt("branch_id")
                );
                deliveryStaffList.add(staff);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load delivery staff: " + e.getMessage());
        }
    }

    private void loadDeliveryAssignments() {
        deliveryList.clear();
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();

            String sql = """
                        SELECT da.id, da.staff_id, da.status, da.delivery_time,
                               o.customer_id, o.total_price,
                               s.name as staff_name
                        FROM deliveries da
                        JOIN orders o ON da.id = o.id
                        LEFT JOIN staffs s ON da.staff_id = s.id
                        WHERE o.branch_id = ?
                """;

            if (!"ALL".equals(statusFilterComboBox.getValue())) {
                sql += " AND da.status = ?";
            }
            

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, branchId);

            if (!"ALL".equals(statusFilterComboBox.getValue())) {
                stmt.setString(2, statusFilterComboBox.getValue());
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DeliveryAssignment assignment = new DeliveryAssignment(
                        rs.getInt("id"),
                        rs.getInt("orders_id"),
                        rs.getString("customers_name"),
                        rs.getString("customers_address"),
                        rs.getString("customers_phone"),
                        rs.getString("staffs_name") != null ? rs.getString("staffs_name") : "Not Assigned",
                        rs.getString("status"),
                        rs.getTimestamp("delivered_at") != null ? rs.getTimestamp("delivered_at").toLocalDateTime() : null,
                        rs.getDouble("total_price")
                );
                deliveryList.add(assignment);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load delivery assignments: " + e.getMessage());
        }
    }

    @FXML
    public void onAssignStaffClick(ActionEvent event) {
        if (selectedDelivery == null) {
            showErrorAlert("Selection Error", "Please select a delivery assignment.");
            return;
        }

        Staff selectedStaff = deliveryStaffComboBox.getValue();
        if (selectedStaff == null) {
            showErrorAlert("Selection Error", "Please select a delivery staff member.");
            return;
        }

        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE deliveries SET staffs_id = ?, status = 'ASSIGNED' WHERE id = ?"
            );
            stmt.setInt(1, selectedStaff.getId());
            stmt.setInt(2, selectedDelivery.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showInfoAlert("Success", "Delivery staff assigned successfully.");
                loadDeliveryAssignments();
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to assign delivery staff: " + e.getMessage());
        }
    }

    @FXML
    public void onUpdateStatusClick(ActionEvent event) {
        if (selectedDelivery == null) {
            showErrorAlert("Selection Error", "Please select a delivery assignment.");
            return;
        }

        // Show status update dialog
        ChoiceDialog<String> dialog = new ChoiceDialog<>("IN_DELIVERY",
                "ASSIGNED", "IN_DELIVERY", "DELIVERED", "CANCELLED");
        dialog.setTitle("Update Delivery Status");
        dialog.setHeaderText("Update status for Order #" + selectedDelivery.getOrderId());
        dialog.setContentText("Select new status:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            updateDeliveryStatus(selectedDelivery.getId(), result.get());
        }
    }

    private void updateDeliveryStatus(int deliveryId, String newStatus) {
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            String sql = "UPDATE deliveries SET status = ?";

            if ("DELIVERED".equals(newStatus)) {
                sql += ", delivered_at = CURRENT_TIMESTAMP";
            }

            sql += " WHERE id = ?";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setInt(2, deliveryId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showInfoAlert("Success", "Delivery status updated successfully.");
                loadDeliveryAssignments();
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update delivery status: " + e.getMessage());
        }
    }

    @FXML
    public void onRefreshClick(ActionEvent event) {
        loadDeliveryStaff();
        loadDeliveryAssignments();
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
    public void onMenuCatalogClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("BranchAdminPage/MenuCatalog.fxml"));
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