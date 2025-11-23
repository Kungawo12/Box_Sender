(function() {
    let currentUserRole = null;

    // Load user info and role
    async function loadUserInfo() {
        try {
            const res = await fetch('/api/auth/me', { credentials: 'include' });
            if (!res.ok) {
                window.location.replace('/index.html');
                return;
            }
            
            const user = await res.json();
            currentUserRole = user.role;
            
            // Update role badge
            const roleBadge = document.getElementById('userRole');
            if (roleBadge) {
                if (currentUserRole === 'MAILROOM_STAFF') {
                    roleBadge.textContent = 'Mailroom Staff';
                    roleBadge.className = 'badge bg-success';
                } else {
                    roleBadge.textContent = 'Employee';
                    roleBadge.className = 'badge bg-info';
                    
                    // Hide navigation buttons for employees
                    const staffNav = document.getElementById('staffNav');
                    if (staffNav) {
                        staffNav.style.display = 'none';
                    }
                }
            }
            
        } catch (error) {
            console.error('Failed to load user:', error);
            window.location.replace('/index.html');
        }
    }

    // Search button click
    document.getElementById('searchBtn').addEventListener('click', searchPackages);

    // Clear button click
    document.getElementById('clearBtn').addEventListener('click', () => {
        document.getElementById('trackingFilter').value = '';
        document.getElementById('recipientFilter').value = '';
        document.getElementById('statusFilter').value = 'all';
        document.getElementById('resultsBody').innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted py-4">
                    Filters cleared. Click "Search Packages" to see all packages.
                </td>
            </tr>
        `;
        document.getElementById('resultCount').innerHTML = '';
    });

    // Search function
    async function searchPackages() {
        const tracking = document.getElementById('trackingFilter').value.trim();
        const recipientName = document.getElementById('recipientFilter').value.trim();
        const status = document.getElementById('statusFilter').value;

        try {
            let url = '/api/packages/search?';
            
            if (tracking) {
                url += `tracking=${encodeURIComponent(tracking)}&`;
            }
            
            if (recipientName) {
                url += `recipient=${encodeURIComponent(recipientName)}&`;
            }
            
            url += `status=${status}`;

            const packages = await api('GET', url);
            displayResults(packages);

        } catch (error) {
            alert('Search failed: ' + error.message);
            console.error('Search error:', error);
        }
    }

    // Display search results
    function displayResults(packages) {
        const tbody = document.getElementById('resultsBody');
        const resultCount = document.getElementById('resultCount');

        resultCount.innerHTML = `Found <strong>${packages.length}</strong> package(s)`;

        if (packages.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-warning py-4">
                        <strong>No packages found</strong><br>
                        <small>Try different search filters</small>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = packages.map(pkg => `
            <tr class="${pkg.status === 'picked_up' ? 'table-secondary' : ''}">
                <td><strong>${pkg.trackingNumber}</strong></td>
                <td>${pkg.carrier}</td>
                <td>
                    <strong>${pkg.recipientName}</strong><br>
                    <small class="text-muted">${pkg.recipientEmail}</small>
                </td>
                <td>
                    ${pkg.status === 'received' ? 
                        '<span class="badge bg-success">Ready for Pickup</span>' : 
                        '<span class="badge bg-secondary">Picked Up</span>'}
                </td>
                <td>
                    ${formatDate(pkg.createdAt)}
                    ${pkg.pickedUpAt ? `<br><small class="text-muted">Picked: ${formatDate(pkg.pickedUpAt)}</small>` : ''}
                </td>
                <td>
                    ${getActionButton(pkg)}
                </td>
            </tr>
        `).join('');
    }

    // Get action button based on role and status
    function getActionButton(pkg) {
        if (currentUserRole === 'MAILROOM_STAFF') {
            // Mailroom staff sees "Pick Up" button
            if (pkg.status === 'received') {
                return `<button class="btn btn-sm btn-success" onclick="window.goToPickup('${pkg.trackingNumber}', '${pkg.recipientName}')">
                    Pick Up
                </button>`;
            } else {
                return '<span class="text-muted">Already picked up</span>';
            }
        } else {
            // Employees see "Details" button
            return `<button class="btn btn-sm btn-info" onclick="window.showDetails(${pkg.id})">
                Details
            </button>`;
        }
    }

    // Show package details in modal (for employees)
    window.showDetails = async function(packageId) {
        try {
            // Get package details
            const pkg = await api('GET', `/api/packages/${packageId}`);
            
            // Populate modal
            document.getElementById('detailTracking').textContent = pkg.trackingNumber || '-';
            document.getElementById('detailCarrier').textContent = pkg.carrier || '-';
            document.getElementById('detailRecipientName').textContent = pkg.recipientName || '-';
            document.getElementById('detailRecipientEmail').textContent = pkg.recipientEmail || '-';
            document.getElementById('detailDescription').textContent = pkg.description || 'No description';
            document.getElementById('detailCreatedAt').textContent = formatDate(pkg.createdAt);
            document.getElementById('detailLoggedBy').textContent = pkg.loggedBy || '-';
            
            // Status
            const statusDiv = document.getElementById('detailStatus');
            if (pkg.status === 'received') {
                statusDiv.innerHTML = '<span class="badge bg-success">Ready for Pickup</span>';
            } else {
                statusDiv.innerHTML = '<span class="badge bg-secondary">Picked Up</span>';
            }
            
            // Pickup details (if picked up)
            const pickupSection = document.getElementById('pickupDetailsSection');
            if (pkg.status === 'picked_up' && pkg.pickedUpAt) {
                pickupSection.style.display = 'block';
                document.getElementById('detailPickedUpAt').textContent = formatDate(pkg.pickedUpAt);
                document.getElementById('detailPickedUpBy').textContent = pkg.pickedUpBy || '-';
                document.getElementById('detailSignature').textContent = pkg.signature || '-';
            } else {
                pickupSection.style.display = 'none';
            }
            
            // Show modal
            const modal = new bootstrap.Modal(document.getElementById('detailsModal'));
            modal.show();
            
        } catch (error) {
            alert('Failed to load package details: ' + error.message);
        }
    };

    // Redirect to pickup page (for mailroom staff)
    window.goToPickup = function(trackingNumber, recipientName) {
        const params = new URLSearchParams({
            tracking: trackingNumber,
            recipient: recipientName
        });
        window.location.href = `/pickup.html?${params.toString()}`;
    };

    // Format date
    function formatDate(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {
            hour: '2-digit', 
            minute: '2-digit'
        });
    }

    // Search on Enter key
    document.getElementById('trackingFilter').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') searchPackages();
    });

    document.getElementById('recipientFilter').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') searchPackages();
    });

    // Load user info on page load
    document.addEventListener('DOMContentLoaded', loadUserInfo);
})();