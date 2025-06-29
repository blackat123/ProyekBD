package com.example.proyekbasisdata.dtos;

public class Staff {
    private int id;
    private String name;
    private String Position;

    private int branch_id;

    public Staff(int branchId, String position, String name, int id) {
        this.branch_id = branchId;
        this.Position = position;
        this.name = name;
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setbranch_id(int branch_id) {
        this.branch_id = branch_id;
    }
}