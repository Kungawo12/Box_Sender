/**
 * API Utility Functions
 *
 * This file provides reusable utility functions for making API calls
 * to the backend Spring Boot server. It handles:
 * - Making HTTP requests with proper headers
 * - Session cookie management
 * - Error handling and response parsing
 * - Form submission handling
 * - Date formatting
 *
 * These functions are used across all frontend pages (login, dashboard, log, etc.)
 */

// Base URL for API calls (empty string means same origin)
// Could be changed to point to a different server if needed
const API_Base = "";

/**
 * API Call Function
 *
 * Makes HTTP requests to the backend API with JSON payloads.
 * This is the core function used for all API communication.
 *
 * Features:
 * - Supports GET, POST, PUT, DELETE methods
 * - Automatically includes session cookies (credentials: 'include')
 * - Handles JSON encoding/decoding
 * - Throws errors for non-2xx responses
 * - Parses error messages from server
 *
 * Session Management:
 * - credentials: 'include' sends session cookies with each request
 * - Spring Security uses these cookies to identify logged-in users
 * - Without this, user would need to re-authenticate every request
 *
 * Error Handling:
 * - If response is not OK (status 200-299), throws Error
 * - Extracts error message from response body if available
 * - Caller should use try/catch to handle errors
 *
 * Example Usage:
 * ```javascript
 * try {
 *   const data = await api('POST', '/api/packages', {
 *     trackingNumber: '123456',
 *     carrier: 'UPS'
 *   });
 *   console.log('Success:', data);
 * } catch (error) {
 *   alert('Error: ' + error.message);
 * }
 * ```
 *
 * @param {'GET'|'POST'|'PUT'|'DELETE'} method - HTTP method to use
 * @param {string} path - API endpoint path (e.g., '/api/packages')
 * @param {object?} body - Optional request body (will be JSON-encoded)
 * @returns {Promise<any>} Parsed JSON response data, or null if no body
 * @throws {Error} If response status is not 2xx
 */
async function api(method, path, body) {
    // Make HTTP request using Fetch API
    const res = await fetch(API_Base + path, {
        method,  // GET, POST, PUT, or DELETE
        headers: { 'Content-Type': 'application/json' },  // Tell server we're sending JSON
        credentials: 'include',  // Include session cookies (critical for authentication!)
        body: body ? JSON.stringify(body) : undefined  // Convert JS object to JSON string
    });

    // Parse response body
    let data = null;
    const text = await res.text();  // Get response as text first
    if (text) {
        try {
            data = JSON.parse(text);  // Try to parse as JSON
        } catch(_) {
            // If not JSON, leave as text (for error messages)
        }
    }

    // Check if request was successful (status 200-299)
    if (!res.ok) {
        // Extract error message from response
        // Try multiple formats: {error: "msg"}, {message: "msg"}, or status text
        const msg = data?.error || data?.message || `${res.status} ${res.statusText}`;
        throw new Error(msg);  // Throw error for caller to handle
    }

    return data;  // Return parsed JSON data
}

/**
 * Form Submission Handler
 *
 * Simplifies form handling by automatically:
 * - Preventing default form submission (page reload)
 * - Creating FormData object from form
 * - Calling custom handler function
 * - Catching and displaying errors
 *
 * This avoids repetitive try/catch blocks in every form handler.
 *
 * Example Usage:
 * ```javascript
 * handleForm('logForm', async (formData, form) => {
 *   const trackingNumber = formData.get('trackingNumber');
 *   await api('POST', '/api/packages', { trackingNumber });
 *   form.reset();  // Clear form on success
 * });
 * ```
 *
 * Error Handling:
 * - Catches all errors from the handler function
 * - Displays error message in browser alert
 * - Logs error to console for debugging
 * - Prevents unhandled promise rejections
 *
 * @param {string} formId - The ID of the form element in the HTML
 * @param {Function} handler - Async function that receives (FormData, formElement)
 *                             Should throw Error if submission fails
 */
function handleForm(formId, handler) {
    // Find the form element in the DOM
    const form = document.getElementById(formId);
    if (!form) {
        console.error(`Form with ID "${formId}" not found!`);
        return;
    }

    // Add submit event listener
    form.addEventListener('submit', async (e) => {
        // Prevent default form submission (which would reload the page)
        e.preventDefault();

        try {
            // Create FormData object from form inputs
            // FormData automatically collects all form fields
            const fd = new FormData(form);

            // Call the custom handler function with form data and form element
            await handler(fd, form);
        }
        catch (err) {
            // Catch any errors from the handler
            console.error('Form submission error:', err);
            alert(err?.message || 'Something went wrong');
        }
    });
}

/**
 * Date Formatter
 *
 * Converts ISO 8601 date strings from the server to user-friendly format.
 *
 * ISO 8601 Format (from database):
 * - Example: "2025-01-15T14:30:00"
 * - Hard to read for users
 * - Standard format for databases and APIs
 *
 * Formatted Output:
 * - Example: "1/15/2025, 2:30:00 PM"
 * - Uses browser's locale settings
 * - Automatically adjusts to user's timezone
 * - More readable and user-friendly
 *
 * Browser Compatibility:
 * - Uses toLocaleString() which works in all modern browsers
 * - Automatically formats based on user's language/region settings
 *
 * @param {string} iso - ISO 8601 date string (e.g., "2025-01-15T14:30:00")
 * @returns {string} Formatted date string in user's locale, or original string if invalid
 */
function fmt(iso) {
    // Handle empty/null input
    if (!iso) return '';

    // Parse ISO string to Date object
    const d = new Date(iso);

    // Check if date is valid (invalid dates become NaN)
    // If invalid, return original string unchanged
    return isNaN(d) ? String(iso) : d.toLocaleString();
}