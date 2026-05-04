package com.university.fms.model;

public class Administrator extends Person {
    private String department;

    public Administrator(String personID, String name, String email, String password, String department) {
        super(personID, name, email, password, "ADMIN");
        this.department = department;
    }

    public String getDepartment() { return department; }

    @Override
    public String getDisplayRole() { return "Administrator"; }
}
