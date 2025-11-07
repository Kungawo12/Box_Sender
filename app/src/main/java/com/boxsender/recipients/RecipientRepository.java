package com.boxsender.recipients;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Recipient Repository Interface
 *
 * This interface provides data access operations for the Recipient entity.
 * Extends JpaRepository to inherit standard database operations automatically.
 *
 * Spring Data JPA implements this interface at runtime - no manual implementation needed.
 *
 * Inherited CRUD operations from JpaRepository:
 * - save(Recipient) - Insert or update a recipient
 * - findById(Long) - Find recipient by ID
 * - findAll() - Get all recipients
 * - deleteById(Long) - Delete recipient by ID
 * - existsById(Long) - Check if recipient exists
 * - and many more...
 *
 * Custom finder methods use Spring Data JPA naming conventions
 * to automatically generate SQL queries.
 */
public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    /**
     * Finds a recipient by their email address
     * Used when logging packages to look up or verify the recipient
     *
     * Spring generates query: SELECT * FROM recipients WHERE email = ?
     *
     * @param email the email address to search for
     * @return Optional containing the Recipient if found, or empty if not found
     *         Using Optional is a best practice to handle the case where no recipient exists
     */
    Optional<Recipient> findByEmail(String email);
}