const API_Base = "";

/**
 * Call your API with JSON. Throws Error on non-2xx.
 * @param {'GET'|'POST'|'PUT'|'DELETE'} method
 * @param {string} path - e.g. '/api/packages'
 * @param {object?} body - JS object to be JSON-encoded
 * @returns {Promise<any>} parsed JSON response (or null)
 */
    async function api(method, path, body) {
    const res = await fetch(API_Base + path, {
        method,
        headers: { 'Content-Type': 'application/json' },
      // send/receive cookies for your Spring session (same-origin OK; cross-origin needs CORS)
        credentials: 'include',                              
        body: body ? JSON.stringify(body) : undefined
    })

let data = null;
const text = await res.text();
if (text) {
    try {data = JSON.parse(text);}
    catch(_) {}

if (!res.ok) {
    const msg = data?.error || data?.message || `${res.status} ${res.statusText}`;
    throw new Error(msg);
    }
    return data;
}
function handleForm(formId, handler) {
    const form = document.getElementById(formId);
    if (!form) return;
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
    try {
        const fd = new FormData(form);
        await handler(fd, form);
    } 
    catch (err) {
        alert(err?.message || 'Something went wrong');
    }
    });
}
    function fmt(iso) {
        if (!iso) return '';
        const d = new Date(iso);
        return isNaN(d) ? String(iso) : d.toLocaleString();    // human-friendly local time.
    }
}