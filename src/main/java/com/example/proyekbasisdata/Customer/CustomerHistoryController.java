package com.example.proyekbasisdata.Customer;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import com.example.proyekbasisdata.dtos.CustomerHistory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomerHistoryController {

    @FXML private TableView<CustomerHistory> customerHistoryTableView;
    @FXML private TableColumn<CustomerHistory, Integer> idColumn;
    @FXML private TableColumn<CustomerHistory, LocalDate> orderDateColumn;
    @FXML private TableColumn<CustomerHistory, Double> totalPriceColumn;
    @FXML private TableColumn<CustomerHistory, String> branchColumn;
    @FXML private TableColumn<CustomerHistory, String> customerColumn;
    @FXML private TableColumn<CustomerHistory, String> staffColumn;
    @FXML private TableColumn<CustomerHistory, String> promoColumn;
    @FXML private TableColumn<CustomerHistory, String> statusColumn;
    @FXML private ComboBox<String> statusFilterComboBox;


    private ObservableList<CustomerHistory> customerHistoryList;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCustomerHistory();
        setupFilterComboBox();
    }

    private void setupFilterComboBox() {
        statusFilterComboBox.getItems().addAll("All", "Completed", "Preparing", "Ready to Deliver");
        statusFilterComboBox.setValue("All"); // default

        statusFilterComboBox.setOnAction(event -> {
            String selected = statusFilterComboBox.getValue();
            if (selected.equals("All")) {
                customerHistoryTableView.setItems(customerHistoryList);
            } else {
                ObservableList<CustomerHistory> filteredList = customerHistoryList.filtered(
                        ch -> ch.getStatusName().equalsIgnoreCase(selected)
                );
                customerHistoryTableView.setItems(filteredList);
            }
        });
    }


    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        branchColumn.setCellValueFactory(new PropertyValueFactory<>("branchName"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        staffColumn.setCellValueFactory(new PropertyValueFactory<>("staffName"));
        promoColumn.setCellValueFactory(new PropertyValueFactory<>("promoName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusName"));

        orderDateColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });

        totalPriceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("Rp %,.0f", item));
            }
        });
    }

    private void loadCustomerHistory() {
        customerHistoryList = FXCollections.observableArrayList();

        String sql = """
            SELECT o.id, o.order_date, o.total_price,
                   b.name AS branch_name,
                   c.name AS cust_name,
                   s.name AS staff_name,
                   p.name AS promo_name,
                   o.status AS status_name
            FROM orders o
            LEFT JOIN branches b ON o.branch_id = b.id
            LEFT JOIN customers c ON o.customer_id = c.id
            LEFT JOIN deliveries d ON o.delivery_id = d.id 
            LEFT JOIN staffs s ON d.staff_id = s.id
            LEFT JOIN promos p ON o.promo_id = p.id
            ORDER BY o.id
        """;

        try (Connection connection = DataSourceManager.getDatabaseConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                CustomerHistory ch = new CustomerHistory(
                        rs.getInt("id"),
                        rs.getDate("order_date").toLocalDate(),
                        rs.getDouble("total_price"),
                        rs.getString("branch_name"),
                        rs.getString("cust_name"),
                        rs.getString("staff_name"),
                        rs.getString("promo_name"),
                        rs.getString("status_name")
                );
                customerHistoryList.add(ch);
            }

            customerHistoryTableView.setItems(customerHistoryList);
            customerHistoryTableView.setPlaceholder(new Label("No order history available"));

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load customer history: " + e.getMessage());
        }
    }

    @FXML
    public void orderButtonClick(ActionEvent actionEvent)  throws IOException {
        switchScene("Customer/CustomerOrder.fxml");
    }

    public void historyButtonClick(ActionEvent actionEvent) {
    }
    @FXML
    public void dasboardButtonClick(ActionEvent actionEvent) throws IOException {
        switchScene("Customer/CustomerDashboard.fxml");
        System.out.println("Tekan ke dashboard");
    }
    @FXML
    public void onLogoutClick(ActionEvent event) throws IOException {
        switchScene("login-page.fxml");
        System.out.println("Tekanke login");
    }

    private void switchScene(String fxmlPath) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    // Alerts
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
