package com.example.proyekbasisdata.Customer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class CustomerHistoryController {
    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private TableView<OrderHistory> historyTableView;

    @FXML
    private TableColumn<OrderHistory, Integer> colOrderId;

    @FXML
    private TableColumn<OrderHistory, String> colBranch;

    @FXML
    private TableColumn<OrderHistory, String> colMenus;

    @FXML
    private TableColumn<OrderHistory, String> colStatus;

    @FXML
    private TableColumn<OrderHistory, Double> colTotal;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button orderButton;

    @FXML
    private Button historyButton;

    @FXML
    private Button logoutButton;

    private final ObservableList<OrderHistory> orderList = FXCollections.observableArrayList();

    private int customerId = 1; // Ganti dengan ID customer yang login

    @FXML
    public void initialize() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBranch.setCellValueFactory(new PropertyValueFactory<>("branch"));
        colMenus.setCellValueFactory(new PropertyValueFactory<>("menus"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        statusFilterComboBox.getItems().addAll("Semua", "Preparing", "Cooking", "Ready to Deliver", "Completed");
        statusFilterComboBox.setValue("Semua");
        statusFilterComboBox.setOnAction(e -> loadOrderHistory());

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        orderList.clear();
        String statusFilter = statusFilterComboBox.getValue();

        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/proyekbd", "postgres", "password");
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT o.id, b.name AS branch_name, o.status, o.total_price, " +
                             "string_agg(m.name || ' x' || od.quantity, ', ') AS menu_list " +
                             "FROM orders o " +
                             "JOIN branches b ON o.branch_id = b.id " +
                             "JOIN order_details od ON o.id = od.order_id " +
                             "JOIN catalogs c ON od.catalog_id = c.id " +
                             "JOIN menus m ON c.menu_id = m.id " +
                             "WHERE o.customer_id = ? " +
                             ("Semua".equals(statusFilter) ? "" : " AND o.status = ? ") +
                             "GROUP BY o.id, b.name, o.status, o.total_price")
        ) {
            stmt.setInt(1, customerId);
            if (!"Semua".equals(statusFilter)) {
                stmt.setString(2, statusFilter);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderHistory order = new OrderHistory(
                        rs.getInt("id"),
                        rs.getString("branch_name"),
                        rs.getString("menu_list"),
                        rs.getString("status"),
                        rs.getDouble("total_price")
                );
                orderList.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        historyTableView.setItems(orderList);
    }

    @FXML
    private void onLogout(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/proyekbasisdata/Login/login.fxml"));
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void onOrder(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/proyekbasisdata/Customer/CustomerOrder.fxml"));
        Stage stage = (Stage) orderButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void onDashboard(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/proyekbasisdata/Customer/DashboardCustomer.fxml"));
        Stage stage = (Stage) dashboardButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    public static class OrderHistory {
        private final int id;
        private final String branch;
        private final String menus;
        private final String status;
        private final double total;

        public OrderHistory(int id, String branch, String menus, String status, double total) {
            this.id = id;
            this.branch = branch;
            this.menus = menus;
            this.status = status;
            this.total = total;
        }

        public int getId() { return id; }
        public String getBranch() { return branch; }
        public String getMenus() { return menus; }
        public String getStatus() { return status; }
        public double getTotal() { return total; }
    }
}
