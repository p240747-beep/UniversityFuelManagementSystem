package com.university.fms.model;

public class FleetManager extends Person {
    private String managerID;

    public FleetManager(String personID, String name, String email, String password, String managerID) {
        super(personID, name, email, password, "FLEET_MANAGER");
        this.managerID = managerID;
    }

    public String getManagerID() { return managerID; }

    @Override
    public String getDisplayRole() { return "Fleet Manager"; }
}
