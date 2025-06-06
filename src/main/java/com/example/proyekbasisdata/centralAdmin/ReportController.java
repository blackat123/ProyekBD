package com.example.proyekbasisdata.centralAdmin;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import com.example.proyekbasisdata.dtos.Branch;
import com.example.proyekbasisdata.dtos.CentralReport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportController {
    // FXML Components
    @FXML
    private GridPane branchGridPane;
    @FXML
    private TableView<CentralReport> reportTableView;
    @FXML
    private TableColumn<CentralReport, Integer> idColumn;
    @FXML
    private TableColumn<CentralReport, LocalDate> orderDateColumn;
    @FXML
    private TableColumn<CentralReport, Double> totalPriceColumn;
    @FXML
    private TableColumn<CentralReport, String> customerReviewColumn;
    @FXML
    private TableColumn<CentralReport, Integer> customerIdColumn;

    // Data variables
    private List<Branch> branchList;
    private ObservableList<CentralReport> reportList;
    private int currentBranchId = -1;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadData();
    }

    // Setup table columns
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        customerReviewColumn.setCellValueFactory(new PropertyValueFactory<>("customerReview"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        orderDateColumn.setCellFactory(column -> {
            return new TableCell<CentralReport, LocalDate>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(formatter));
                    }
                }
            };
        });

        totalPriceColumn.setCellFactory(column -> {
            return new TableCell<CentralReport, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("Rp %,.0f", item));
                    }
                }
            };
        });

        customerReviewColumn.setCellFactory(column -> {
            return new TableCell<CentralReport, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else if (item == null || item.trim().isEmpty()) {
                        setText("No review");
                    } else {
                        setText(item);
                    }
                }
            };
        });
    }

    // Load data from the database
    private void loadData() {
        loadBranches();
        if (!branchList.isEmpty()) {
            currentBranchId = branchList.getFirst().getId();
            loadReport();
            displayBranches();
            displayReport();
        } else {
            displayNoData();
        }
    }

    // Load branches from the database
    private void loadBranches() {
        branchList = new ArrayList<>();
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM branches ORDER BY id");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String address = rs.getString("address");
                String city = rs.getString("city");
                int branchAdminId = rs.getInt("branchadmin_id");
                Branch branch = new Branch(id, name, address, city, branchAdminId);
                branchList.add(branch);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load branches: " + e.getMessage());
        }
    }

    // Load reports based on the selected branch
    private void loadReport() {
        reportList = FXCollections.observableArrayList();
        try (Connection connection = DataSourceManager.getDatabaseConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM orders WHERE branch_id = ? ORDER BY order_date DESC, id DESC"
            );
            stmt.setInt(1, currentBranchId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate orderDate = rs.getDate("order_date").toLocalDate();
                double totalPrice = rs.getDouble("total_price");
                String customerReview = rs.getString("customer_review");
                int customerId = rs.getInt("customer_id");

                // Create CentralReport with proper data types
                CentralReport report = new CentralReport(id, orderDate, totalPrice, customerReview, customerId);
                reportList.add(report);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load reports: " + e.getMessage());
        }
    }

    // Display report data in the table
    private void displayReport() {
        reportTableView.setItems(reportList);

        // Show message if no data available
        if (reportList.isEmpty()) {
            reportTableView.setPlaceholder(new Label("No reports available for this branch"));
        }
    }

    // Display branches in the grid pane
    private void displayBranches() {
        branchGridPane.getChildren().clear();
        branchGridPane.getColumnConstraints().clear();
        branchGridPane.getRowConstraints().clear();

        if (branchList.isEmpty()) return;

        // Setup grid constraints
        ColumnConstraints column = new ColumnConstraints();
        column.setHgrow(javafx.scene.layout.Priority.SOMETIMES);
        column.setMinWidth(10.0);
        branchGridPane.getColumnConstraints().add(column);

        RowConstraints row = new RowConstraints();
        row.setMinHeight(10.0);
        row.setVgrow(javafx.scene.layout.Priority.SOMETIMES);
        branchGridPane.getRowConstraints().add(row);

        javafx.scene.layout.HBox branchContainer = new javafx.scene.layout.HBox();
        branchContainer.setSpacing(10);

        for (Branch branch : branchList) {
            Button branchButton = createBranchButton(branch);
            branchContainer.getChildren().add(branchButton);
        }

        branchGridPane.add(branchContainer, 0, 0);
    }

    // Display no data message
    private void displayNoData() {
        branchGridPane.getChildren().clear();

        Label noBranchesLabel = new Label("No branches available");
        noBranchesLabel.setFont(Font.font("Poppins Regular", 18.0));
        noBranchesLabel.setStyle("-fx-text-fill: #666666;");

        AnchorPane branchPane = new AnchorPane();
        branchPane.getChildren().add(noBranchesLabel);
        AnchorPane.setLeftAnchor(noBranchesLabel, 20.0);
        AnchorPane.setTopAnchor(noBranchesLabel, 10.0);

        branchGridPane.add(branchPane, 0, 0);

        // Clear table when no branches
        reportTableView.setItems(FXCollections.observableArrayList());
        reportTableView.setPlaceholder(new Label("No branches available"));
    }

    // Create branch button
    private Button createBranchButton(Branch branch) {
        Button button = new Button(branch.getName());
        button.setAlignment(javafx.geometry.Pos.CENTER);
        button.setMaxHeight(Double.MAX_VALUE);
        button.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        button.setTextFill(javafx.scene.paint.Color.WHITE);
        button.setFont(Font.font("Poppins SemiBold", 16.0));
        button.setPadding(new Insets(0, 20, 0, 20));
        button.setCursor(Cursor.HAND);

        // Set style based on whether this is the active branch
        if (branch.getId() == currentBranchId) {
            button.setStyle("-fx-background-color: #c29156; -fx-background-radius: 8px;");
        } else {
            button.setStyle("-fx-background-color: #e5b17a; -fx-background-radius: 8px;");
        }

        // Add margin
        GridPane.setMargin(button, new Insets(3, 3, 3, 3));

        // Add click event
        button.setOnAction(e -> {
            currentBranchId = branch.getId();
            loadReport();
            displayBranches(); // Refresh to update button styles
            displayReport();   // Update table with new data
        });

        return button;
    }

    // Navigation methods
    @FXML
    public void onDashboardPageClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    @FXML
    public void onMenuPageClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/menu.fxml"));
        Scene scene = new Scene(loader.load());
        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().sizeToScene();
    }

    @FXML
    public void onPromoPageClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("centralAdminPage/promo.fxml"));
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

    // Alert methods
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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