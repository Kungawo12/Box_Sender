\/**
 * Login Page JavaScript
 *
 * Handles user authentication for the Box Sender application:
 * - Sign in (login) for existing users
 * - Registration for new users
 * - View switching between sign in and register forms
 * - Form validation using Bootstrap
 * - Password matching validation
 *
 * This page is public (no authentication required).
 * After successful login or registration, users are redirected to the dashboard.
 */

/**
 * Form Validation Function
 *
 * Uses HTML5 form validation (required, email, minlength, etc.)
 * combined with Bootstrap's validation styling.
 *
 * HTML5 Validation Features:
 * - required attribute: field must have value
 * - type="email": must be valid email format
 * - minlength attribute: minimum character count
 * - pattern attribute: regex validation
 *
 * Bootstrap Styling:
 * - 'was-validated' class triggers Bootstrap validation styles
 * - Shows green checkmarks for valid fields
 * - Shows red X and error messages for invalid fields
 *
 * @param {HTMLFormElement} form - The form element to validate
 * @returns {boolean} true if form is valid, false if invalid
 */
function validateForm(form) {
    // checkValidity() is built-in HTML5 method
    // Returns true if all form fields pass validation
    if (!form.checkValidity()) {
      // Add Bootstrap class to show validation styling
      form.classList.add('was-validated');
      return false;
    }
    return true;
}

/**
 * View Switcher Function
 *
 * Toggles between sign in and register views on the same page.
 * Uses Bootstrap's 'd-none' class to show/hide content.
 *
 * Why single page with two views?
 * - Better user experience (no page reload)
 * - Faster switching between forms
 * - Maintains form state if user switches by accident
 *
 * @param {string} targetId - Which view to show: 'register' or 'signin'
 */
function showView(targetId) {
    // Get both view containers
    const sign = document.getElementById('view-signin');
    const reg  = document.getElementById('view-register');

    // Helper functions to show/hide using Bootstrap classes
    const show = el => el.classList.remove('d-none');  // d-none = display: none
    const hide = el => el.classList.add('d-none');

    // Show requested view, hide the other
    if (targetId === 'register') {
      hide(sign);
      show(reg);
    } else {
      hide(reg);
      show(sign);
    }
}

/**
 * API POST Helper Function
 *
 * Makes POST requests to authentication endpoints.
 * Similar to api() function but simpler for login/register.
 *
 * Critical Feature:
 * - credentials: 'include' sends/receives cookies
 * - These cookies contain the session ID after login
 * - Without this, Spring Security won't recognize the user as logged in
 *
 * Error Handling:
 * - Extracts error message from response
 * - Throws Error for caller to catch and display
 *
 * @param {string} path - API endpoint (e.g., '/api/auth/login')
 * @param {object} data - Request body to JSON-encode
 * @returns {Promise<Response>} The fetch Response object
 * @throws {Error} If response status is not 2xx
 */
async function apiPost(path, data) {
    // Make HTTP POST request
    const res = await fetch(path, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',  // Include cookies for session management
      body: JSON.stringify(data)  // Convert JS object to JSON
    });

    // Check if request failed (status not 200-299)
    if (!res.ok) {
      let msg = 'Request failed';
      try {
        // Try to get error message from response body
        const text = await res.text();
        if (text) msg = text;
      } catch {}
      throw new Error(msg);
    }
    return res;
}
  
/**
 * Page Initialization
 *
 * Runs when the DOM is fully loaded (all HTML parsed and ready).
 * Sets up event listeners for:
 * - View switching links
 * - Sign in form submission
 * - Registration form submission
 *
 * DOMContentLoaded Event:
 * - Fires when HTML is fully loaded and parsed
 * - Doesn't wait for stylesheets, images, or subframes
 * - Perfect timing to attach event listeners to DOM elements
 */
document.addEventListener('DOMContentLoaded', () => {

    // --- VIEW SWITCHING ---
    // Set up links to switch between sign in and register views

    // "Create account" link - switches to register view
    document.getElementById('linkToRegister')?.addEventListener('click', (e)=>{
      e.preventDefault();  // Prevent link from navigating
      showView('register');
    });

    // "Sign in instead" link - switches back to sign in view
    document.getElementById('linkToSignin')?.addEventListener('click', (e)=>{
      e.preventDefault();  // Prevent link from navigating
      showView('signin');
    });

    // --- SIGN IN FORM ---
    /**
     * Sign In Form Handler
     *
     * Process:
     * 1. Validate form fields (email format, required fields)
     * 2. Send credentials to /api/auth/login
     * 3. If successful, redirect to dashboard
     * 4. If failed, show error message
     *
     * Security Notes:
     * - Password is sent over HTTPS (encrypted in transit)
     * - Server hashes password with BCrypt before comparing
     * - Session cookie is set by server on successful login
     * - trim() removes whitespace from email (common user mistake)
     */
    const loginForm = document.getElementById('loginForm');
    loginForm?.addEventListener('submit', async (e) => {
      e.preventDefault();  // Prevent form from submitting normally (page reload)

      // Validate form before submitting
        if (!validateForm(loginForm)) return;

      // Prepare request body with login credentials
        const body = {
        email:    loginForm.email.value.trim(),  // Remove whitespace
        password: loginForm.password.value  // Don't trim passwords (might be intentional)
        };

        try {
        // Send login request to server
        await apiPost('/api/auth/login', body);

        // On success, redirect to dashboard
        // replace() instead of href prevents back button from returning to login
        window.location.replace('/dashboard.html');
      } catch (err) {
        // On failure, show error message
        alert(err.message);
      }
    });

    // --- REGISTRATION FORM ---
    /**
     * Registration Form Handler
     *
     * Process:
     * 1. Validate password matching
     * 2. Validate all form fields
     * 3. Send registration data to /api/auth/register
     * 4. Server creates account AND automatically logs user in
     * 5. Redirect to dashboard
     *
     * Password Matching:
     * - Uses HTML5 setCustomValidity() for custom validation
     * - Shows Bootstrap validation styling if passwords don't match
     * - Must be checked before form.checkValidity()
     *
     * Auto-Login Feature:
     * - Server automatically logs user in after registration
     * - No need for separate login request
     * - Better user experience (one less step)
     */
    const regForm = document.getElementById('registerForm');
    regForm?.addEventListener('submit', async (e) => {
      e.preventDefault();  // Prevent default form submission

      // Custom validation: Check if passwords match
        const p1 = document.getElementById('regPassword');
        const p2 = document.getElementById('regPassword2');
      // setCustomValidity() sets custom error message
      // Empty string '' means field is valid
        p2.setCustomValidity(p1.value !== p2.value ? 'Passwords do not match' : '');

      // Validate entire form (including custom password match validation)
        if (!validateForm(regForm)) return;

      // Prepare registration data
        const body = {
        firstName: regForm.firstName.value.trim(),
        lastName:  regForm.lastName.value.trim(),
        email:     regForm.email.value.trim(),
        password:  regForm.password.value  // Don't trim password
    };

    try {
        // Send registration request
        // Server registers AND auto-logs in user
        await apiPost('/api/auth/register', body);

        // On success, redirect to dashboard
        // User is already logged in thanks to auto-login feature
        window.location.replace('/dashboard.html');
    } catch (err) {
        // On failure, show error and switch back to sign in view
        alert(err.message);
        showView('signin');
    }
    });
});