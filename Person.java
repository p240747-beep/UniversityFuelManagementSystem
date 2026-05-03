package com.university.fms.model;

/**
 * Abstract base class representing a person in the system.
 * Demonstrates INHERITANCE relationship from the class diagram.
 */
public abstract class Person {
    protected String personID;
    protected String name;
    protected String email;
    protected String password;
    protected String role; // "ADMIN", "DRIVER", "FLEET_MANAGER", "FINANCE_OFFICER", "PUMP_OPERATOR"

    public Person(String personID, String name, String email, String password, String role) {
        this.personID = personID;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getPersonID() { return personID; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    public abstract String getDisplayRole();

    @Override
    public String toString() {
        return name + " (" + getDisplayRole() + ")";
    }
}
