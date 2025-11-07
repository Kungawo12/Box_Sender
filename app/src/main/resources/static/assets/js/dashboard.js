(function () {
    const tbody = document.getElementById('activityBody');
    if (!tbody) return;

    // Format timestamp to readable format
    function fmt(timestamp) {
        if (!timestamp) return '-';
        const date = new Date(timestamp);
        
        // Format: "Nov 2, 2025 9:30 PM"
        const options = {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
            hour: 'numeric',
            minute: '2-digit',
            hour12: true
        };
        
        return date.toLocaleString('en-US', options);
    }

    // Create badge based on action
    function actionBadge(action) {
        const a = String(action || '').toUpperCase();
        if (a === 'PICKED_UP') {
            return '<span class=" text-success"> Picked Up</span>';
        }
        return '<span class=" text-danger"> Received</span>';
    }

    // Load activity from API
    async function loadActivity() {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-secondary py-4">Loading…</td></tr>';
        
        try {
            const events = await api('GET', '/api/activity?limit=100');

            if (!events || events.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" class="text-center text-secondary py-4">
                            No activity yet. Log your first package!
                        </td>
                    </tr>
                `;
                return;
            }

            // Build table rows
            const rows = events.map(e => {
                const when = fmt(e.when);
                const badge = actionBadge(e.action);
                const tracking = e.trackingNumber ? `<code class="text-primary">${e.trackingNumber}</code>` : '-';
                const recipient = e.recipient || '-';
                const details = e.details || '-';
                
                return `
                    <tr>
                        <td class="text-nowrap">${when}</td>
                        <td>${badge}</td>
                        <td>${tracking}</td>
                        <td><strong>${recipient}</strong></td>
                        <td class="text-muted small">${details}</td>
                    </tr>
                `;
            }).join('');

            tbody.innerHTML = rows;

        } catch (err) {
            console.error('Failed to load activity:', err);
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-danger py-4">
                        Failed to load activity: ${err.message}
                    </td>
                </tr>
            `;
        }
    }

    // Load user info
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

    // Initial load
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', loadActivity);
    } else {
        loadActivity();
    }

    // Auto-refresh every 30 seconds
    setInterval(loadActivity, 30000);
})();