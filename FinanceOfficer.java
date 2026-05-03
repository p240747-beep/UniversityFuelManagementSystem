package com.university.fms.model;

public class FinanceOfficer extends Person {
    private double budgetLimit;

    public FinanceOfficer(String personID, String name, String email, String password, double budgetLimit) {
        super(personID, name, email, password, "FINANCE_OFFICER");
        this.budgetLimit = budgetLimit;
    }

    public double getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(double budgetLimit) { this.budgetLimit = budgetLimit; }

    @Override
    public String getDisplayRole() { return "Finance Officer"; }
}
