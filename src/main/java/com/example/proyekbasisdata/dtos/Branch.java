package com.example.proyekbasisdata.dtos;

public class Branch {
    private int id;
    private String name;
    private String address;
    private String city;
    private int bracnchAdminId;

    public Branch(int id, String name, String address, String city, int bracnchAdminId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.bracnchAdminId = bracnchAdminId;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public int getBracnchAdminId() { return bracnchAdminId; }
}
