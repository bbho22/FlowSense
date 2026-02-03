package com.example.flowsense;

public class User {
    private String firstName;
    private String secondName;
    private String email;
    private String role;
    private boolean isActive;

    public User() {} // Needed for Firebase

    public User(String firstName, String secondName, String email, String role, boolean isActive) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }

    public String getFirstName() { return firstName; }
    public String getSecondName() { return secondName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean getIsActive() { return isActive; }
}
