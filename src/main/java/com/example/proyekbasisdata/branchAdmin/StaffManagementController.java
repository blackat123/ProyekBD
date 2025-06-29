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
import java.util.Optional;

public class StaffManagementController {
    @FXML
    private TableView<Staff> staffTable;

    @FXML
    private TableColumn<Staff, Integer> idColumn;

    @FXML
    private TableColumn<Staff, String> nameColumn;

    @FXML
    private TableColumn<Staff, String> usernameColumn;

    @FXML
    private TableColumn<Staff, String> phoneColumn;

    @FXML
    private TableColumn<Staff, String> roleColumn;

    @FXML
    private TableColumn<Staff, String> statusColumn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Button addButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button clearButton;

    private ObservableList<Staff> staffList;
    private int branchId;
    private Staff selectedStaff;

    @FXML
    public void initialize() {
        HelloApplication app = HelloApplication.getApplicationInstance();
        this.branchId = getBranchIdByAdminId(app.getUserId());

        setupTableColumns();
        setupComboBoxes();
        loadStaffData();
        setupTableSelectionListener();

        updateButton.setDisable(true);
        deleteButton.setDisable(true);
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
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        staffList = FXCollections.observableArrayList();
        staffTable.setItems(staffList);
    }

    private void setupComboBoxes() {
        roleComboBox.setItems(FXCollections.observableArrayList("KITCHEN_STAFF", "DELIVERY_STAFF", "CASHIER"));
        statusComboBox.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));

        roleComboBox.setValue("KITCHEN_STAFF");
        statusComboBox.setValue("ACTIVE");
    }

    private void setupTableSelectionListener() {
        staffTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedStaff = newSelection;
                populateFields(newSelection);
                updateButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                selectedStaff = null;
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }

    private void populateFields(Staff staff) {
        nameField.setText(staff.getName());
        passwordField.clear();
    }

    private void loadStaffData() {
        staffList.clear();
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM staffs WHERE branch_id = ? ORDER BY id DESC"
            );
            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Staff staff = new Staff(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("Position"),
                        rs.getInt("branch_id")
                );
                staffList.add(staff);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load staff data: " + e.getMessage());
        }
    }

    @FXML
    public void onAddStaffClick(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            Connection connection = DataSourceManager.getDatabaseConnection();

            PreparedStatement checkStmt = connection.prepareStatement("SELECT id FROM staffs WHERE username = ?");
            checkStmt.setString(1, usernameField.getText().trim());
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next()) {
                showErrorAlert("Validation Error", "Username already exists. Please choose a different username.");
                return;
            }

            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO staffs (name, username, password, phone, role, status, branch_id) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            stmt.setString(1, nameField.getText().trim());
            stmt.setString(2, usernameField.getText().trim());
            stmt.setString(3, passwordField.getText()); // In real app, hash this password
            stmt.setString(4, phoneField.getText().trim());
            stmt.setString(5, roleComboBox.getValue());
            stmt.setString(6, statusComboBox.getValue());
            stmt.setInt(7, branchId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showInfoAlert("Success", "Staff member added successfully.");
                loadStaffData();
                clearFields();
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add staff: " + e.getMessage());
        }
    }

    @FXML
    public void onUpdateStaffClick(ActionEvent event) {
        if (selectedStaff == null) {
            showErrorAlert("Selection Error", "Please select a staff member to update.");
            return;
        }

        if (!validateFields()) {
            return;
        }

        try {
            Connection connection = DataSourceManager.getDatabaseConnection();

            PreparedStatement checkStmt = connection.prepareStatement("SELECT id FROM staffs WHERE username = ? AND id != ?");
            checkStmt.setString(1, usernameField.getText().trim());
            checkStmt.setInt(2, selectedStaff.getId());
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next()) {
                showErrorAlert("Validation Error", "Username already exists. Please choose a different username.");
                return;
            }

            String sql = "UPDATE staff SET name = ?, username = ?, phone = ?, role = ?, status = ?";
            if (!passwordField.getText().isEmpty()) {
                sql += ", password = ?";
            }
            sql += " WHERE id = ?";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, nameField.getText().trim());
            stmt.setString(2, usernameField.getText().trim());
            stmt.setString(3, phoneField.getText().trim());
            stmt.setString(4, roleComboBox.getValue());
            stmt.setString(5, statusComboBox.getValue());

            if (!passwordField.getText().isEmpty()) {
                stmt.setString(6, passwordField.getText());
                stmt.setInt(7, selectedStaff.getId());
            } else {
                stmt.setInt(6, selectedStaff.getId());
            }

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showInfoAlert("Success", "Staff member updated successfully.");
                loadStaffData();
                clearFields();
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update staff: " + e.getMessage());
        }
    }

    @FXML
    public void onDeleteStaffClick(ActionEvent event) {
        if (selectedStaff == null) {
            showErrorAlert("Selection Error", "Please select a staff member to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Staff");
        confirmAlert.setHeaderText("Are you sure you want to delete this staff member?");
        confirmAlert.setContentText("Staff: " + selectedStaff.getName() + "\nThis action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteStaff(selectedStaff.getId());
        }
    }

    private void deleteStaff(int staffId) {
        try {
            Connection connection = DataSourceManager.getDatabaseConnection();
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM staffs WHERE id = ?");
            stmt.setInt(1, staffId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showInfoAlert("Success", "Staff member deleted successfully.");
                loadStaffData();
                clearFields();
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to delete staff: " + e.getMessage());
        }
    }

    @FXML
    public void onClearFieldsClick(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        nameField.clear();
        usernameField.clear();
        passwordField.clear();
        phoneField.clear();
        roleComboBox.setValue("KITCHEN_STAFF");
        statusComboBox.setValue("ACTIVE");
        staffTable.getSelectionModel().clearSelection();
        selectedStaff = null;
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Name is required.");
            return false;
        }
        if (usernameField.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Username is required.");
            return false;
        }
        if (selectedStaff == null && passwordField.getText().isEmpty()) {
            showErrorAlert("Validation Error", "Password is required for new staff.");
            return false;
        }
        if (phoneField.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Phone number is required.");
            return false;
        }
        if (roleComboBox.getValue() == null) {
            showErrorAlert("Validation Error", "Role is required.");
            return false;
        }
        if (statusComboBox.getValue() == null) {
            showErrorAlert("Validation Error", "Status is required.");
            return false;
        }
        return true;
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