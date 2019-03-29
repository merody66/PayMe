package com.example.user.payme.Objects;

/**
 * Users class to contain the following user information:
 *  - Name
 *  - PayLah! Number
 *  - Email Address
 */
public class User {
    public String name;
    public String paylahNumber;
    public String email;

    public User() {
        // Default constructor needed
    }

    public User(String name, String paylahNumber, String email) {
        this.name = name;
        this.paylahNumber = paylahNumber;
        this.email = email;
    }
}
