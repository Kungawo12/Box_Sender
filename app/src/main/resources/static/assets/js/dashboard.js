/**
 * Dashboard JavaScript
 *
 * This script handles loading dashboard statistics and recent package activity.
 */

(function () {
    const tbody = document.getElementById('activityBody');

    /**
     * Load dashboard statistics
     */
    async function loadStats() {
        try {
            const response = await fetch('/api/dashboard/stats', {
                credentials: 'include',
                headers: { 'Accept': 'application/json' }
            });

            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = '/index.html';
                    return;
                }
                throw new Error('Failed to load statistics');
            }

            const stats = await response.json();

            // Update stat elements if they exist
            updateStatElement('totalPackages', stats.totalPackages);
            updateStatElement('pendingPickups', stats.pendingPickups);
            updateStatElement('pickedUpToday', stats.pickedUpToday);
            updateStatElement('overduePackages', stats.overduePackages);
            updateStatElement('pickupRate', stats.pickupRate);

        } catch (error) {
            console.error('Failed to load stats:', error);
        }
    }

    /**
     * Helper function to update stat elements
     */
    function updateStatElement(id, value) {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value;
        }
    }

    /**
     * Get status badge HTML
     */
    function statusBadge(status) {
        if (status === 'picked') {
            return '<span class="badge text-bg-success">Picked Up</span>';
        }
        return '<span class="badge text-bg-warning">Waiting</span>';
    }

    /**
     * Load recent package activity
     */
    async function loadActivity() {
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-secondary small">Loading…</td></tr>';

        try {
            const response = await fetch('/api/dashboard/recent', {
                credentials: 'include',
                headers: { 'Accept': 'application/json' }
            });

            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = '/index.html';
                    return;
                }
                throw new Error('Failed to load recent activity');
            }

            const packages = await response.json();

            if (!packages || packages.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-secondary small">No packages yet.</td></tr>';
                return;
            }

            // Build the table rows
            const rows = packages.map(pkg => {
                const when = new Date(pkg.createdAt).toLocaleString();
                const badge = statusBadge(pkg.status);
                const tracking = pkg.trackingNumber ? escapeHtml(pkg.trackingNumber) : 'N/A';
                const recipient = pkg.recipient
                    ? escapeHtml(`${pkg.recipient.firstName} ${pkg.recipient.lastName || ''}`)
                    : 'Unknown';
                const carrier = pkg.carrier ? escapeHtml(pkg.carrier) : 'N/A';

                return `
                <tr>
                    <td>${when}</td>
                    <td>${badge}</td>
                    <td><strong>${tracking}</strong></td>
                    <td>${recipient}</td>
                    <td>${carrier}</td>
                </tr>
                `;
            }).join('');

            tbody.innerHTML = rows;

        } catch (err) {
            tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center text-danger small">
                    Failed to load activity: ${err.message}
                </td>
            </tr>
            `;
        }
    }

    /**
     * Escape HTML to prevent XSS
     */
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * Check authentication and load user info
     */
    document.addEventListener('DOMContentLoaded', async () => {
        try {
            const res = await fetch('/api/auth/me', { credentials: 'include' });
            if (!res.ok) throw new Error('Not signed in');
            const me = await res.json();

            const firstNameElement = document.getElementById('first_name');
            if (firstNameElement) {
                firstNameElement.textContent = me.firstName;
            }

            // Load dashboard data
            loadStats();
            loadActivity();

        } catch {
            window.location.replace('/index.html');
        }
    });

    // Refresh activity every 30 seconds
    setInterval(loadActivity, 30000);
    // Refresh stats every 60 seconds
    setInterval(loadStats, 60000);
})();