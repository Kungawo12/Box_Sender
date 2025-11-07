package com.boxsender.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Employee Entity Class
 *
 * This class represents an employee in the Box Sender system who can log packages
 * and manage package deliveries. It is mapped to the "employees" table in the database.
 *
 * Employees authenticate using their email and password to access the system.
 * Once logged in, they can register incoming packages and assign them to recipients.
 *
 * Key features:
 * - Uses JPA (Jakarta Persistence API) for ORM (Object-Relational Mapping)
 * - Stores encrypted passwords using BCrypt hashing for security
 * - Each employee has a unique email address for authentication
 */
@Entity  // Marks this class as a JPA entity (database table)
@Table(name = "employees")  // Specifies the table name in the database
public class Employee {

    // Primary key field - auto-generated unique identifier for each employee
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment in database
    private Long id;

    // Employee's first name - required field, max 100 characters
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    // Employee's last name - required field, max 100 characters
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    // Employee's email - used for login authentication
    // Must be unique across all employees and is required
    @Column(nullable = false, unique = true, length = 200)
    private String email;

    // Hashed password using BCrypt algorithm
    // Stored as a hash for security - never store plain text passwords
    // Length 225 to accommodate BCrypt hash length
    @Column(name = "password_hash", nullable = false, length = 225)
    private String passwordHash;

    // Role of the employee in the system
    // Values: "ADMIN" (full access + role management), "MAILROOM_STAFF" (full operational access), "EMPLOYEE" (limited access)
    // Default value is "EMPLOYEE" for security (least privilege principle)
    @Column(nullable = false, length = 50)
    private String role = "EMPLOYEE";

    // --- Getter and Setter Methods ---
    // These methods provide controlled access to private fields
    // Following JavaBean conventions for JPA and Spring Framework

    /**
     * Gets the unique identifier of the employee
     * @return the employee's ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the employee
     * @param id the employee's ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the employee's first name
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the employee's first name
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the employee's last name
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the employee's last name
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the employee's email address (used for login)
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the employee's email address
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the BCrypt hashed password
     * @return the password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash (should be BCrypt encrypted)
     * @param passwordHash the hashed password to set
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Gets the employee's role in the system
     * @return the role (ADMIN, MAILROOM_STAFF, or EMPLOYEE)
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the employee's role in the system
     * @param role the role to set (ADMIN, MAILROOM_STAFF, or EMPLOYEE)
     */
    public void setRole(String role) {
        this.role = role;
    }
}