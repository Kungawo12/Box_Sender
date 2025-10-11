// assets/js/dashboard.js
// Loads recent activity rows into <tbody id="activityBody"> on dashboard.html.
// Requires: assets/js/api.js (provides api() and fmt()).

(function () {
    const tbody = document.getElementById('activityBody');
    if (!tbody) 
    return; 

    function actionBadge(action) {
    const a = String(action || '').toUpperCase();
    if (a === 'PICKED_UP')  
    return '<span class="badge text-bg-success">Picked Up</span>';
    return '<span class="badge text-bg-primary">Received</span>';
    }

    async function loadActivity() {
    tbody.innerHTML = '<tr><td colspan="5" class="text-center text-secondary small">Loading…</td></tr>';
    try {
        // Ask Spring Boot for the latest events
        // Expected JSON like:
        // [{ whenAt: "2025-10-10T15:12:00Z", action: "RECEIVED"|"PICKED_UP",
        //    trackingNumber: "1Z...", recipient: "Jamie Rivera", details: "Carrier: UPS" }]
        const events = await api('GET', '/api/activity?limit=100');

        if (!events || events.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-secondary small">No activity yet.</td></tr>';
            return;
        }

        events.sort((a, b) => new Date(b.whenAt || b.when) - new Date(a.whenAt || a.when));

        // Build the table rows
        const rows = events.map(e => {
            const when = fmt(e.whenAt || e.when); // fmt() prints a user-friendly local date/time
            const badge = actionBadge(e.action);
            const tracking = e.trackingNumber ? `<strong>${e.trackingNumber}</strong>` : '';
            const recipient = e.recipient || '';
            const details = e.details || '';
            return `
            <tr>
                <td>${when}</td>
                <td>${badge}</td>
                <td>${tracking}</td>
                <td>${recipient}</td>
                <td>${details}</td>
            </tr>
            `;
        }).join('');

        tbody.innerHTML = rows;
        } catch (err) {
        // If the request fails (e.g., 401 unauthorized or server error), show a friendly message
        tbody.innerHTML = `
        <tr>
            <td colspan="5" class="text-center text-danger small">
                Failed to load activity: ${err.message}
            </td>
        </tr>
        `;
    }
    }
  
    // Run once after the HTML is parsed.
    // Since you include scripts with `defer`, the DOM is parsed before this runs; if not, DOMContentLoaded ensures safety.  [oai_citation:0‡MDN Web Docs](https://developer.mozilla.org/en-US/docs/Web/API/Document/DOMContentLoaded_event?utm_source=chatgpt.com)
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', loadActivity);
    } else {
      loadActivity();
    }
  
    // Optional: auto-refresh every 30 seconds so the feed stays current. You can delete this if not needed.  [oai_citation:1‡MDN Web Docs](https://developer.mozilla.org/en-US/docs/Web/API/Window/setInterval?utm_source=chatgpt.com)
    setInterval(loadActivity, 30000);
  })();