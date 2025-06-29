package com.example.proyekbasisdata.Customer;

import com.example.proyekbasisdata.HelloApplication;
import com.example.proyekbasisdata.datasources.DataSourceManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

public class PaymentController {

    @FXML
    private ComboBox<String> paymentMethodComboBox;

    @FXML
    private Button confirmButton;

    @FXML
    private Label totalLabel;

    private int orderId;
    private double totalPrice;
    private Stage paymentStage; // menyimpan stage payment window

    public void setOrderDetails(int orderId, double totalPrice) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        totalLabel.setText(String.format("Total: Rp %.2f", totalPrice));
    }

    public void setStage(Stage stage) {
        this.paymentStage = stage;
    }

    @FXML
    public void initialize() {
        paymentMethodComboBox.getItems().addAll("Cash", "Credit Card", "QRIS", "Bank Transfer");
    }

    @FXML
    void handleConfirmPayment(ActionEvent event) {
        String method = paymentMethodComboBox.getValue();
        if (method == null || method.isEmpty()) {
            showAlert("Pilih metode pembayaran terlebih dahulu.");
            return;
        }

        try (Connection conn = DataSourceManager.getDatabaseConnection()) {
            // Masukkan ke tabel payments
            String sql = "INSERT INTO payments (payment_method, payment_date, status, order_id) VALUES (?, ?, 'Paid', ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, method);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, orderId);
            stmt.executeUpdate();

            // Update status order jadi "Completed"
            String updateOrder = "UPDATE orders SET status = 'Completed' WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateOrder);
            updateStmt.setInt(1, orderId);
            updateStmt.executeUpdate();

            showAlert("Pembayaran berhasil!");

            if (paymentStage != null) {
                paymentStage.close(); // Tutup jendela pembayaran
            }

            // Kembali ke dashboard
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("Customer/CustomerDashboard.fxml"));
            Scene dashboardScene = new Scene(loader.load());
            app.getPrimaryStage().setScene(dashboardScene);
            app.getPrimaryStage().sizeToScene();

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Terjadi kesalahan saat memproses pembayaran.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informasi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}