
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
    document.addEventListener('DOMContentLoaded', async () => {
        try {
            const res = await fetch('/api/auth/me', { credentials: 'include' });
            if (!res.ok) throw new Error('Not signed in');
                const me = await res.json();
            document.getElementById('first_name').textContent = me.firstName;
            } catch {
                window.location.replace('/index.html');
        }
        });

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', loadActivity);
    } else {
        loadActivity();
    }
    setInterval(loadActivity, 30000);
})();