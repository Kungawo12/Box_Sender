package com.boxsender.users;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Employee Repository Interface
 *
 * This interface provides data access operations for the Employee entity.
 * It extends JpaRepository, which provides standard CRUD (Create, Read, Update, Delete)
 * operations automatically without needing to write implementation code.
 *
 * Spring Data JPA automatically implements this interface at runtime.
 *
 * What JpaRepository provides automatically:
 * - save(Employee) - Insert or update an employee
 * - findById(Long) - Find employee by ID
 * - findAll() - Get all employees
 * - deleteById(Long) - Delete employee by ID
 * - count() - Count total employees
 * - and many more...
 *
 * Custom Query Methods:
 * Spring Data JPA generates queries based on method names.
 * Method naming follows conventions that Spring interprets automatically.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  /**
   * Finds an employee by their email address
   * Used during login authentication to look up the user
   *
   * Method naming convention: findBy + FieldName
   * Spring automatically generates: SELECT * FROM employees WHERE email = ?
   *
   * @param email the email address to search for
   * @return Optional containing the Employee if found, or empty Optional if not found
   *         Optional prevents NullPointerException and makes it clear the value might not exist
   */
  Optional<Employee> findByEmail(String email);
}