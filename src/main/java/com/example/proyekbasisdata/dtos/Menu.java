package com.example.proyekbasisdata.dtos;

public class Menu {
    private int id;
    private String image;
    private String name;
    private String description;
    private double price;
    private int categoryId;

    public Menu(int id, String image, String name, String description, double price, int categoryId) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
    }

    // Getters
    public int getId() { return id; }
    public String getImage() { return image; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getCategoryId() { return categoryId; }
}
