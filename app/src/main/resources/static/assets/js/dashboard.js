(function () {
    const tbody = document.getElementById('activityBody');
    const userRole = document.getElementById('userRole');
    const firstName = document.getElementById('first_name');

    // Format timestamp
    function fmt(timestamp) {
        if (!timestamp) return '-';
        const date = new Date(timestamp);
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

    // Action badge
    function actionBadge(action) {
        const a = String(action || '').toUpperCase();
        if (a === 'PICKED_UP') {
            return '<span class="text-success">Picked Up</span>';
        }
        return '<span class="text-danger">Received</span>';
    }

    // Load activity
    async function loadActivity() {
        if (!tbody) return;
        
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

            const rows = events.map(e => {
                return `
                    <tr>
                        <td class="text-nowrap">${fmt(e.when)}</td>
                        <td>${actionBadge(e.action)}</td>
                        <td>${e.trackingNumber ? `<code class="text-primary">${e.trackingNumber}</code>` : '-'}</td>
                        <td><strong>${e.recipient || '-'}</strong></td>
                        <td class="text-muted small">${e.details || '-'}</td>
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

    // Update UI based on role
    function updateUIForRole(role) {
        // Update role badge
        if (userRole) {
            if (role === 'MAILROOM_STAFF') {
                userRole.textContent = 'Mailroom Staff';
                userRole.className = 'badge bg-success me-2';
            } else {
                userRole.textContent = 'Employee';
                userRole.className = 'badge bg-info me-2';
            }
        }

        // For employees: hide Log Package, Pickup, and Reports cards
        if (role === 'EMPLOYEE') {
            const quickActionsSection = document.querySelector('.row.g-3');
            if (!quickActionsSection) return;

            const cards = quickActionsSection.querySelectorAll('.col-md-4');
            cards.forEach((card, index) => {
                // Hide cards: 0=Log, 1=Pickup, 3=Reports (keep 2=Search)
                if (index === 0 || index === 1 || index === 3) {
                    card.style.display = 'none';
                } else if (index === 2) {
                    // Make Search card bigger
                    card.classList.remove('col-md-4');
                    card.classList.add('col-md-8', 'mx-auto');
                }
            });

            // Add info message if not already there
            if (!document.querySelector('.alert-info')) {
                const infoDiv = document.createElement('div');
                infoDiv.className = 'col-12 mt-3';
                infoDiv.innerHTML = `
                    <div class="alert alert-info mb-0">
                        <strong>Employee Access:</strong> You can search for packages. 
                        Contact mailroom staff to log new packages, process pickups, or generate reports.
                    </div>
                `;
                quickActionsSection.appendChild(infoDiv);
            }
        }
        // For mailroom staff: show all cards (do nothing, they're already visible)
    }

    // Load user info on page load
    document.addEventListener('DOMContentLoaded', async () => {
        try {
            const res = await fetch('/api/auth/me', { credentials: 'include' });
            if (!res.ok) throw new Error('Not signed in');
            
            const me = await res.json();
            
            // Display name
            if (firstName) {
                firstName.textContent = me.firstName;
            }
            
            // Update UI based on role
            updateUIForRole(me.role);
            
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

    // Auto-refresh
    setInterval(loadActivity, 30000);
})();