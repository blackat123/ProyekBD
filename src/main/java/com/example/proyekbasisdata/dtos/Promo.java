package com.example.proyekbasisdata.dtos;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Promo {
    private int id;
    private String name;
    private String description;
    private int discount;
    private LocalDate startDate;
    private LocalDate endDate;

    public Promo(int id, String name, String description, int discount, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.discount = discount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDiscount() { return discount; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}
