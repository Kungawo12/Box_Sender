package com.boxsender.users;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  // Spring automatically implements this method!
    // Method name → SQL query
  Optional<Employee> findByEmail(String email);
  // Generated SQL:
    // SELECT * FROM employees WHERE email = ?
}
/*Available methods (from JpaRepository): 
* Find all employees
* List<Employee> employees = employeeRepo.findAll();

* Find by ID
* Optional<Employee> emp = employeeRepo.findById(1L);

* Save (INSERT or UPDATE)
* Employee saved = employeeRepo.save(emp);

* Delete
* employeeRepo.deleteById(1L);

* Count
* long count = employeeRepo.count();

* Check existence
* boolean exists = employeeRepo.existsById(1L);
*/
