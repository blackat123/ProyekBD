package com.example.proyekbasisdata.dtos;

public  class MenuItem {
    private String name;
    private String description;
    private double price;
    private int quantity;

    public MenuItem(String name, String description, double price, int quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}