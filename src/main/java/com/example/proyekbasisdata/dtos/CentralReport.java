package com.example.proyekbasisdata.dtos;

import java.time.LocalDate;

public class CentralReport {
    private int id;
    private LocalDate orderDate;
    private double totalPrice;
    private String customerReview;
    private int customerId;

    public CentralReport(int id, LocalDate orderDate, double totalPrice, String customerReview, int customerId) {
        this.id = id;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.customerReview = customerReview;
        this.customerId = customerId;
    }

    // Getters
    public int getId() { return id;}
    public LocalDate getOrderDate() { return orderDate; }
    public double getTotalPrice() { return totalPrice; }
    public String getCustomerReview() { return customerReview; }
    public int getCustomerId() { return customerId; }
}
