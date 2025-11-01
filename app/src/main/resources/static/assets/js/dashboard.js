/**
 * Dashboard JavaScript
 *
 * This script handles loading dashboard statistics and recent package activity.
 */

(function () {
    const tbody = document.getElementById('activityBody');

    // Store current packages for export
    let currentActivityPackages = [];

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
            currentActivityPackages = packages; // Store for export

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

            // Role-based UI control
            // Hide "Log Package" card for regular EMPLOYEE role
            // Show "Admin Panel" card for ADMIN role
            const logPackageCard = document.getElementById('logPackageCard');
            const adminPanelCard = document.getElementById('adminPanelCard');

            // Only ADMIN and MAILROOM_STAFF can log packages
            if (me.role === 'EMPLOYEE') {
                if (logPackageCard) {
                    logPackageCard.style.display = 'none';
                }
            }

            // Only ADMIN can access admin panel
            if (me.role === 'ADMIN') {
                if (adminPanelCard) {
                    adminPanelCard.style.display = 'block';
                }
            } else {
                if (adminPanelCard) {
                    adminPanelCard.style.display = 'none';
                }
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

    /**
     * Export Recent Activity to CSV
     */
    window.exportActivityToCSV = function() {
        if (!currentActivityPackages || currentActivityPackages.length === 0) {
            alert('No activity to export');
            return;
        }

        // CSV header
        const headers = ['Date/Time', 'Status', 'Tracking Number', 'Recipient', 'Carrier'];

        // CSV rows
        const rows = currentActivityPackages.map(pkg => {
            const when = pkg.createdAt ? new Date(pkg.createdAt).toLocaleString() : 'N/A';
            const status = pkg.status === 'picked' ? 'Picked Up' : 'Waiting';
            const tracking = pkg.trackingNumber || 'N/A';
            const recipient = pkg.recipient
                ? `${pkg.recipient.firstName} ${pkg.recipient.lastName || ''}`.trim()
                : 'Unknown';
            const carrier = pkg.carrier || 'N/A';

            return [when, status, tracking, recipient, carrier]
                .map(field => `"${String(field).replace(/"/g, '""')}"`).join(',');
        });

        // Combine header and rows
        const csv = [headers.join(','), ...rows].join('\n');

        // Create download
        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', `recent_activity_${new Date().toISOString().split('T')[0]}.csv`);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    /**
     * Export Recent Activity to PDF
     */
    window.exportActivityToPDF = function() {
        if (!currentActivityPackages || currentActivityPackages.length === 0) {
            alert('No activity to export');
            return;
        }

        // Create printable HTML content
        let content = `
            <!DOCTYPE html>
            <html>
            <head>
                <title>Recent Package Activity</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    h1 { color: #0d6efd; font-size: 24px; margin-bottom: 10px; }
                    .meta { color: #666; font-size: 12px; margin-bottom: 20px; }
                    table { width: 100%; border-collapse: collapse; font-size: 11px; }
                    th { background-color: #0d6efd; color: white; padding: 8px; text-align: left; border: 1px solid #ddd; }
                    td { padding: 6px 8px; border: 1px solid #ddd; }
                    tr:nth-child(even) { background-color: #f8f9fa; }
                    .badge { display: inline-block; padding: 3px 8px; border-radius: 3px; font-size: 10px; font-weight: bold; }
                    .badge-success { background-color: #198754; color: white; }
                    .badge-warning { background-color: #ffc107; color: black; }
                </style>
            </head>
            <body>
                <h1>Box Sender - Recent Package Activity</h1>
                <div class="meta">
                    Generated: ${new Date().toLocaleString()}<br>
                    Total Records: ${currentActivityPackages.length}
                </div>
                <table>
                    <thead>
                        <tr>
                            <th>Date/Time</th>
                            <th>Status</th>
                            <th>Tracking Number</th>
                            <th>Recipient</th>
                            <th>Carrier</th>
                        </tr>
                    </thead>
                    <tbody>
        `;

        currentActivityPackages.forEach(pkg => {
            const when = pkg.createdAt ? new Date(pkg.createdAt).toLocaleString() : 'N/A';
            const statusBadge = pkg.status === 'picked'
                ? '<span class="badge badge-success">Picked Up</span>'
                : '<span class="badge badge-warning">Waiting</span>';
            const tracking = pkg.trackingNumber ? escapeHtml(pkg.trackingNumber) : 'N/A';
            const recipient = pkg.recipient
                ? escapeHtml(`${pkg.recipient.firstName} ${pkg.recipient.lastName || ''}`.trim())
                : 'Unknown';
            const carrier = pkg.carrier ? escapeHtml(pkg.carrier) : 'N/A';

            content += `
                <tr>
                    <td>${when}</td>
                    <td>${statusBadge}</td>
                    <td><strong>${tracking}</strong></td>
                    <td>${recipient}</td>
                    <td>${carrier}</td>
                </tr>
            `;
        });

        content += `
                    </tbody>
                </table>
            </body>
            </html>
        `;

        // Open print dialog in new window
        const printWindow = window.open('', '_blank');
        printWindow.document.write(content);
        printWindow.document.close();
        printWindow.focus();

        // Wait for content to load, then trigger print
        setTimeout(() => {
            printWindow.print();
        }, 250);
    };
})();