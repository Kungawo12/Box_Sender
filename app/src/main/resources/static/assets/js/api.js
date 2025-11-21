const API_Base = "";

/**
 * Make API calls with consistent error handling
 * 
 * @param {string} method - HTTP method (GET, POST, PUT, DELETE)
 * @param {string} url - API endpoint URL
 * @param {object} body - Request body (optional, for POST/PUT)
 * @returns {Promise} - Resolves to JSON response
 */
async function api(method, path, body) {
    //Configure request
    const res = await fetch(API_Base + path, {
        method,                             //GET,POST,PUT,DELETE
        headers: { 
            'Content-Type': 'application/json'      //tell server we're sending JSON
        },
        credentials: 'include',                        //include cookies(sessions)                 
        body: body ? JSON.stringify(body) : undefined       //convert JS object to JSON
    });

    //Parse response 
    let data = null;
    const text = await res.text();      //Get response as text first
    if (text) {
        try {
            data = JSON.parse(text);    // Try to parse as JSON
        } catch(_) {}           // If not JSON, data stays null
    }

    //Check if request was successful
    if (!res.ok) {
        //Extract error message
        const msg = data?.error || data?.message || `${res.status} ${res.statusText}`;
        throw new Error(msg);
    }
    return data;
}

/**
 * Helper to handle form submissions
 * @param {string} formId - The ID of the form element
 * @param {Function} handler - Async function that receives (FormData, form)
 */
function handleForm(formId, handler) {
    const form = document.getElementById(formId);

    //Safety check
    if (!form) {
        console.error(`Form with ID "${formId}" not found!`);
        return;
    }
    //Add submit listener
    form.addEventListener('submit', async (e) => {
        e.preventDefault();             // Stop default form submission
        try {
            const fd = new FormData(form);
            await handler(fd, form);
        } 
        catch (err) {
            console.error('Form submission error:', err);       // Extract form data
            alert(err?.message || 'Something went wrong');      // Call your handler
        }
    });
}

/**
 * Format ISO date string to human-readable format
 * @param {string} iso - ISO date string
 * @returns {string} Formatted date
 */
function fmt(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    return isNaN(d) ? String(iso) : d.toLocaleString();
}