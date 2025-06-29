package com.example.proyekbasisdata.dtos;

import java.time.LocalDate;

public class CustomerHistory {
    private int id;
    private LocalDate orderDate;
    private double totalPrice;
    private String branchName;
    private String customerName;
    private String staffName;
    private String promoName;
    private String statusName;

    public CustomerHistory(int id, LocalDate orderDate, double totalPrice,
                           String branchName, String customerName, String staffName, String promoName, String statusName) {
        this.id = id;
        this.orderDate = orderDate;
        this.totalPrice = totalPrice;
        this.branchName = branchName;
        this.customerName = customerName;
        this.staffName = staffName;
        this.promoName = promoName;
        this.statusName = statusName;
    }

    // Getters
    public int getId() { return id; }
    public LocalDate getOrderDate() { return orderDate; }
    public double getTotalPrice() { return totalPrice; }
    public String getBranchName() { return branchName; }
    public String getCustomerName() { return customerName; }
    public String getStaffName() { return staffName; }
    public String getPromoName() { return promoName; }
    public String getStatusName() { return statusName; }
}
