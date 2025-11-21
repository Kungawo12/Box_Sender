// Search button click
document.getElementById('searchBtn').addEventListener('click', searchPackages);
// When user clicks "Search Packages" button → call searchPackages()


// Clear button click
document.getElementById('clearBtn').addEventListener('click', () => {
    // Clear all filter inputs
    document.getElementById('trackingFilter').value = '';
    document.getElementById('recipientFilter').value = '';
    document.getElementById('statusFilter').value = 'all';
    // Clear results table
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
    // Get filter values
    const tracking = document.getElementById('trackingFilter').value.trim();
    const recipientName = document.getElementById('recipientFilter').value.trim();
    const status = document.getElementById('statusFilter').value;

    try {
        // Build URL with query parameters
        let url = '/api/packages/search?';
        
        if (tracking) {
            url += `tracking=${encodeURIComponent(tracking)}&`;
        }
        
        if (recipientName) {
            url += `recipientName=${encodeURIComponent(recipientName)}&`;
        }
        
        url += `status=${status}`;

        console.log('Searching with URL:', url); // Debug log

        // Call API
        const packages = await api('GET', url);

        // Display results
        displayResults(packages);

    } catch (error) {
        alert('Search failed: ' + error.message);
        console.error('Search error:', error);
    }
}

// Display search results in table
function displayResults(packages) {
    const tbody = document.getElementById('resultsBody');
    const resultCount = document.getElementById('resultCount');

    // Show count
    resultCount.innerHTML = `Found <strong>${packages.length}</strong> package(s)`;

    // If no results
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

    // Build table rows
    tbody.innerHTML = packages.map(pkg => `
        <tr class="${pkg.status === 'picked_up' ? 'table-secondary' : ''}">
            <td>
                <strong>${pkg.trackingNumber}</strong>
            </td>
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
                ${pkg.status === 'received' ? 
                    `<button class="btn btn-sm btn-success" onclick="goToPickup('${pkg.trackingNumber}', '${pkg.recipientName}')">
                        Pick Up
                    </button>` : 
                    `<span class="text-muted">Already picked up</span>`}
            </td>
        </tr>
    `).join('');
}
// Redirect to pickup page with pre-filled data
function goToPickup(trackingNumber, recipientName) {
    // Encode data to pass via URL
    const params = new URLSearchParams({
        tracking: trackingNumber,
        recipient: recipientName
    });
    
    // Redirect to pickup page
    window.location.href = `/pickup.html?${params.toString()}`;
}


// Format date nicely
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {
        hour: '2-digit', 
        minute: '2-digit'
    });
}

// Search on Enter key in any filter box
document.getElementById('trackingFilter').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') searchPackages();
});

document.getElementById('recipientFilter').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') searchPackages();
});
