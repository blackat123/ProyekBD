package com.example.proyekbasisdata.dtos;

public class Catalog {
    private int id;
    private int stock;
    private int branchId;
    private int menuId;

    private String menuImage;
    private String menuName;
    private String menuDescription;
    private double menuPrice;
    private int categoryId;

    public Catalog(int id, int stock, int branchId, int menuId, String menuImage,
                   String menuName, String menuDescription, double menuPrice, int categoryId) {
        this.id = id;
        this.stock = stock;
        this.branchId = branchId;
        this.menuId = menuId;
        this.menuImage = menuImage;
        this.menuName = menuName;
        this.menuDescription = menuDescription;
        this.menuPrice = menuPrice;
        this.categoryId = categoryId;
    }

    public int getId() { return id; }
    public int getStock() { return stock; }
    public int getBranchId() { return branchId; }
    public int getMenuId() { return menuId; }
    public String getMenuImage() { return menuImage; }
    public String getMenuName() { return menuName; }
    public String getMenuDescription() { return menuDescription; }
    public double getMenuPrice() { return menuPrice; }
    public int getCategoryId() { return categoryId; }

    public void setStock(int stock) { this.stock = stock; }
}
