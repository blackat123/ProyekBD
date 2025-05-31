package com.example.proyekbasisdata.dtos;

public class BranchMenu {
    private int id;
    private int branchId;
    private int menuId;
    private boolean isAvailable;
    private double customPrice;

    private String image;
    private String name;
    private String description;
    private double originalPrice;
    private int categoryId;

    public BranchMenu(int id, int branchId, int menuId, boolean isAvailable, double customPrice,
                      String image, String name, String description, double originalPrice, int categoryId) {
        this.id = id;
        this.branchId = branchId;
        this.menuId = menuId;
        this.isAvailable = isAvailable;
        this.customPrice = customPrice;
        this.image = image;
        this.name = name;
        this.description = description;
        this.originalPrice = originalPrice;
        this.categoryId = categoryId;
    }

    public int getId() { return id; }
    public int getBranchId() { return branchId; }
    public int getMenuId() { return menuId; }
    public boolean isAvailable() { return isAvailable; }
    public double getCustomPrice() { return customPrice; }
    public String getImage() { return image; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getOriginalPrice() { return originalPrice; }
    public int getCategoryId() { return categoryId; }

    public void setAvailable(boolean available) { isAvailable = available; }
    public void setCustomPrice(double customPrice) { this.customPrice = customPrice; }
}