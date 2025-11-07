package com.boxsender.admin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boxsender.users.Employee;
import com.boxsender.users.EmployeeRepository;

/**
 * Admin Controller
 *
 * This REST controller handles administrative operations for the Box Sender system.
 * Only users with the ADMIN role can access these endpoints.
 *
 * Base URL: /api/admin
 *
 * Key responsibilities:
 * - List all employees with their roles
 * - Update employee roles (promote/demote users)
 * - Manage system access control
 *
 * Security:
 * - All endpoints require ADMIN role via @PreAuthorize annotation
 * - Method-level security enforced by Spring Security
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")  // All endpoints in this controller require ADMIN role
public class AdminController {

    private final EmployeeRepository employeeRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor-based dependency injection
     * @param employeeRepo repository for employee database operations
     * @param passwordEncoder BCrypt encoder for password hashing
     */
    public AdminController(EmployeeRepository employeeRepo, PasswordEncoder passwordEncoder) {
        this.employeeRepo = employeeRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all employees with their roles
     *
     * HTTP Endpoint: GET /api/admin/employees
     * Authentication: Required (ADMIN role only)
     *
     * Returns a list of all employees in the system with their:
     * - ID, name, email, and current role
     * - Password hashes are excluded for security
     *
     * @param auth the authentication object containing logged-in admin's email
     * @return ResponseEntity with list of employee data or error
     *         - 200 OK: List of employees
     *         - 500 Internal Server Error: Unexpected error occurred
     */
    @GetMapping("/employees")
    public ResponseEntity<?> getAllEmployees(Authentication auth) {
        try {
            List<Employee> employees = employeeRepo.findAll();

            // Map employees to simplified DTO (exclude password hash)
            List<Map<String, Object>> employeeData = employees.stream()
                .map(emp -> {
                    Map<String, Object> empMap = new java.util.HashMap<>();
                    empMap.put("id", emp.getId());
                    empMap.put("firstName", emp.getFirstName());
                    empMap.put("lastName", emp.getLastName());
                    empMap.put("email", emp.getEmail());
                    empMap.put("role", emp.getRole());
                    return empMap;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(employeeData);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to retrieve employees: " + e.getMessage()));
        }
    }

    /**
     * Update an employee's role
     *
     * HTTP Endpoint: PUT /api/admin/employees/{id}/role
     * Authentication: Required (ADMIN role only)
     *
     * Allows admins to change an employee's role in the system.
     * Valid roles: ADMIN, MAILROOM_STAFF, EMPLOYEE
     *
     * Request body format:
     * {
     *   "role": "MAILROOM_STAFF"
     * }
     *
     * Security considerations:
     * - Validates that the new role is one of the three valid values
     * - Prevents accidental removal of all admins (warning: not fully implemented)
     * - Logs role changes for audit trail
     *
     * @param id the employee ID to update
     * @param body the request body containing the new role
     * @param auth the authentication object containing logged-in admin's email
     * @return ResponseEntity with success or error response
     *         - 200 OK: Role updated successfully
     *         - 400 Bad Request: Invalid role provided
     *         - 404 Not Found: Employee not found
     *         - 500 Internal Server Error: Unexpected error occurred
     */
    @PutMapping("/employees/{id}/role")
    public ResponseEntity<?> updateEmployeeRole(
        @PathVariable Long id,
        @RequestBody UpdateRoleRequest body,
        Authentication auth) {

        try {
            // Validate role value
            String newRole = body.role().toUpperCase();
            if (!newRole.equals("ADMIN") && !newRole.equals("MAILROOM_STAFF") && !newRole.equals("EMPLOYEE")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid role. Must be ADMIN, MAILROOM_STAFF, or EMPLOYEE"));
            }

            // Find employee
            Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));

            String oldRole = employee.getRole();

            // Update role
            employee.setRole(newRole);
            Employee updatedEmployee = employeeRepo.save(employee);

            // Log the change
            System.out.println(String.format(
                "Role change: %s (%s) changed from %s to %s by %s",
                employee.getEmail(),
                employee.getFirstName() + " " + employee.getLastName(),
                oldRole,
                newRole,
                auth.getName()
            ));

            return ResponseEntity.ok(Map.of(
                "message", "Role updated successfully",
                "employeeId", updatedEmployee.getId(),
                "email", updatedEmployee.getEmail(),
                "oldRole", oldRole,
                "newRole", updatedEmployee.getRole()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to update role: " + e.getMessage()));
        }
    }

    /**
     * Create a new employee account
     *
     * HTTP Endpoint: POST /api/admin/employees
     * Authentication: Required (ADMIN role only)
     *
     * Allows admins to create new employee accounts directly without registration.
     * The admin can specify the role (ADMIN, MAILROOM_STAFF, or EMPLOYEE) during creation.
     *
     * Request body format:
     * {
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "email": "john.doe@example.com",
     *   "password": "temporaryPassword123",
     *   "role": "MAILROOM_STAFF"
     * }
     *
     * Security:
     * - Validates email is unique
     * - Hashes password with BCrypt before storing
     * - Validates role is one of the three valid values
     * - Logs account creation for audit trail
     *
     * @param body the request body containing employee details
     * @param auth the authentication object containing logged-in admin's email
     * @return ResponseEntity with success or error response
     *         - 200 OK: Employee created successfully
     *         - 400 Bad Request: Email already exists or invalid role
     *         - 500 Internal Server Error: Unexpected error occurred
     */
    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(
        @RequestBody CreateEmployeeRequest body,
        Authentication auth) {

        try {
            // Validate email is unique
            if (employeeRepo.findByEmail(body.email()).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email already exists"));
            }

            // Validate role
            String role = body.role().toUpperCase();
            if (!role.equals("ADMIN") && !role.equals("MAILROOM_STAFF") && !role.equals("EMPLOYEE")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid role. Must be ADMIN, MAILROOM_STAFF, or EMPLOYEE"));
            }

            // Create new employee
            Employee employee = new Employee();
            employee.setFirstName(body.firstName());
            employee.setLastName(body.lastName());
            employee.setEmail(body.email());
            employee.setPasswordHash(passwordEncoder.encode(body.password()));
            employee.setRole(role);

            // Save to database
            Employee savedEmployee = employeeRepo.save(employee);

            // Log the creation
            System.out.println(String.format(
                "Account created: %s (%s %s) with role %s by admin %s",
                savedEmployee.getEmail(),
                savedEmployee.getFirstName(),
                savedEmployee.getLastName(),
                savedEmployee.getRole(),
                auth.getName()
            ));

            return ResponseEntity.ok(Map.of(
                "message", "Employee account created successfully",
                "employeeId", savedEmployee.getId(),
                "email", savedEmployee.getEmail(),
                "role", savedEmployee.getRole()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to create employee: " + e.getMessage()));
        }
    }

    /**
     * Update employee details (name, email, password)
     *
     * HTTP Endpoint: PUT /api/admin/employees/{id}
     * Authentication: Required (ADMIN role only)
     *
     * Allows admins to edit an employee's information.
     * All fields are optional - only provided fields will be updated.
     *
     * @param id the employee ID to update
     * @param body the request body containing fields to update
     * @param auth the authentication object
     * @return ResponseEntity with success or error response
     */
    @PutMapping("/employees/{id}")
    public ResponseEntity<?> updateEmployee(
        @PathVariable Long id,
        @RequestBody UpdateEmployeeRequest body,
        Authentication auth) {

        try {
            // Find employee
            Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));

            // Track what changed
            StringBuilder changes = new StringBuilder();

            // Update first name if provided
            if (body.firstName() != null && !body.firstName().isEmpty()) {
                String oldName = employee.getFirstName();
                employee.setFirstName(body.firstName());
                changes.append(String.format("firstName: %s -> %s, ", oldName, body.firstName()));
            }

            // Update last name if provided
            if (body.lastName() != null && !body.lastName().isEmpty()) {
                String oldName = employee.getLastName();
                employee.setLastName(body.lastName());
                changes.append(String.format("lastName: %s -> %s, ", oldName, body.lastName()));
            }

            // Update email if provided and unique
            if (body.email() != null && !body.email().isEmpty()) {
                if (!body.email().equals(employee.getEmail())) {
                    // Check if new email is already taken
                    if (employeeRepo.findByEmail(body.email()).isPresent()) {
                        return ResponseEntity.badRequest()
                            .body(Map.of("error", "Email already exists"));
                    }
                    String oldEmail = employee.getEmail();
                    employee.setEmail(body.email());
                    changes.append(String.format("email: %s -> %s, ", oldEmail, body.email()));
                }
            }

            // Update password if provided
            if (body.password() != null && !body.password().isEmpty()) {
                employee.setPasswordHash(passwordEncoder.encode(body.password()));
                changes.append("password: updated, ");
            }

            // Update role if provided
            if (body.role() != null && !body.role().isEmpty()) {
                String newRole = body.role().toUpperCase();
                if (!newRole.equals("ADMIN") && !newRole.equals("MAILROOM_STAFF") && !newRole.equals("EMPLOYEE")) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid role. Must be ADMIN, MAILROOM_STAFF, or EMPLOYEE"));
                }
                String oldRole = employee.getRole();
                employee.setRole(newRole);
                changes.append(String.format("role: %s -> %s, ", oldRole, newRole));
            }

            // Save changes
            Employee updatedEmployee = employeeRepo.save(employee);

            // Log the changes
            System.out.println(String.format(
                "Employee updated: %s (%d) - Changes: %s - By: %s",
                updatedEmployee.getEmail(),
                updatedEmployee.getId(),
                changes.toString(),
                auth.getName()
            ));

            return ResponseEntity.ok(Map.of(
                "message", "Employee updated successfully",
                "employeeId", updatedEmployee.getId(),
                "email", updatedEmployee.getEmail(),
                "firstName", updatedEmployee.getFirstName(),
                "lastName", updatedEmployee.getLastName(),
                "role", updatedEmployee.getRole()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to update employee: " + e.getMessage()));
        }
    }

    /**
     * Delete an employee account
     *
     * HTTP Endpoint: DELETE /api/admin/employees/{id}
     * Authentication: Required (ADMIN role only)
     *
     * Allows admins to delete employee accounts.
     * Warning: This is a permanent action and cannot be undone.
     *
     * Security considerations:
     * - Prevents admin from deleting their own account
     * - Logs deletion for audit trail
     *
     * @param id the employee ID to delete
     * @param auth the authentication object
     * @return ResponseEntity with success or error response
     */
    @DeleteMapping("/employees/{id}")
    public ResponseEntity<?> deleteEmployee(
        @PathVariable Long id,
        Authentication auth) {

        try {
            // Find employee
            Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + id));

            // Prevent admin from deleting their own account
            if (employee.getEmail().equals(auth.getName())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot delete your own account"));
            }

            // Log before deletion
            System.out.println(String.format(
                "Employee deleted: %s (%s %s, ID: %d) with role %s - By: %s",
                employee.getEmail(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getId(),
                employee.getRole(),
                auth.getName()
            ));

            // Delete the employee
            employeeRepo.delete(employee);

            return ResponseEntity.ok(Map.of(
                "message", "Employee deleted successfully",
                "email", employee.getEmail()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to delete employee: " + e.getMessage()));
        }
    }

    /**
     * Data Transfer Object (DTO) for role update requests
     *
     * Request body format:
     * {
     *   "role": "MAILROOM_STAFF"
     * }
     *
     * @param role the new role to assign (ADMIN, MAILROOM_STAFF, or EMPLOYEE)
     */
    public record UpdateRoleRequest(String role) {}

    /**
     * Data Transfer Object (DTO) for create employee requests
     *
     * Request body format:
     * {
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "email": "john.doe@example.com",
     *   "password": "temporaryPassword123",
     *   "role": "MAILROOM_STAFF"
     * }
     *
     * @param firstName employee's first name
     * @param lastName employee's last name
     * @param email employee's email (must be unique)
     * @param password plain text password (will be hashed)
     * @param role employee's role (ADMIN, MAILROOM_STAFF, or EMPLOYEE)
     */
    public record CreateEmployeeRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String role
    ) {}

    /**
     * Data Transfer Object (DTO) for update employee requests
     *
     * Request body format (all fields are optional):
     * {
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "email": "john.doe@example.com",
     *   "password": "newPassword123",
     *   "role": "MAILROOM_STAFF"
     * }
     *
     * @param firstName employee's first name (optional)
     * @param lastName employee's last name (optional)
     * @param email employee's email (optional, must be unique)
     * @param password plain text password (optional, will be hashed)
     * @param role employee's role (optional: ADMIN, MAILROOM_STAFF, or EMPLOYEE)
     */
    public record UpdateEmployeeRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String role
    ) {}
}
