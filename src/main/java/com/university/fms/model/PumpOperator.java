package com.university.fms.model;

public class PumpOperator extends Person {
    private String pumpStationID;

    public PumpOperator(String personID, String name, String email, String password, String pumpStationID) {
        super(personID, name, email, password, "PUMP_OPERATOR");
        this.pumpStationID = pumpStationID;
    }

    public String getPumpStationID() { return pumpStationID; }

    @Override
    public String getDisplayRole() { return "Pump Operator"; }
}
