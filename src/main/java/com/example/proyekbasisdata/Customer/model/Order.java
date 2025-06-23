package com.example.proyekbasisdata.Customer.model;

public class Order {
    private int id;
    private String customerName;
    private String branch;
    private String product;
    private String status;
    private double totalAmount;

    public Order(int id, String customerName, String branch, String product, String status, double totalAmount) {
        this.id = id;
        this.customerName = customerName;
        this.branch = branch;
        this.product = product;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getBranch() {
        return branch;
    }

    public String getProduct() {
        return product;
    }

    public String getStatus() {
        return status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

