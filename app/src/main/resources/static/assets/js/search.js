/**
 * Advanced Package Search JavaScript
 *
 * This script provides comprehensive package search functionality with:
 * - Multi-field searching (tracking, carrier, description, recipient name/email)
 * - Status filtering
 * - Dynamic sorting by any column
 * - Apply button to trigger search
 * - Enter key support for quick searching
 * - Result count display
 */

document.addEventListener('DOMContentLoaded', () => {
    // Get all form elements
    const trackingNumberInput = document.getElementById('trackingNumber');
    const carrierInput = document.getElementById('carrier');
    const descriptionInput = document.getElementById('description');
    const recipientNameInput = document.getElementById('recipientName');
    const recipientEmailInput = document.getElementById('recipientEmail');
    const statusSelect = document.getElementById('status');
    const sortBySelect = document.getElementById('sortBy');
    const sortOrderBtn = document.getElementById('sortOrderBtn');
    const sortOrderIcon = document.getElementById('sortOrderIcon');
    const applyBtn = document.getElementById('applyBtn');
    const clearBtn = document.getElementById('clearBtn');
    const tbody = document.getElementById('tbody');
    const resultCount = document.getElementById('resultCount');

    // Track current sort order
    let currentSortOrder = 'desc';

    // Load all packages on page load
    performSearch();

    // Apply button triggers the search
    applyBtn.addEventListener('click', performSearch);

    // Allow Enter key in input fields to trigger search
    const inputFields = [trackingNumberInput, carrierInput, descriptionInput,
                         recipientNameInput, recipientEmailInput];
    inputFields.forEach(input => {
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
    });

    // Status and sort changes apply immediately for better UX
    statusSelect.addEventListener('change', performSearch);
    sortBySelect.addEventListener('change', performSearch);

    // Toggle sort order when button clicked
    sortOrderBtn.addEventListener('click', () => {
        currentSortOrder = currentSortOrder === 'desc' ? 'asc' : 'desc';
        sortOrderIcon.textContent = currentSortOrder === 'desc' ? '↓' : '↑';
        performSearch();
    });

    // Clear all filters
    clearBtn.addEventListener('click', () => {
        trackingNumberInput.value = '';
        carrierInput.value = '';
        descriptionInput.value = '';
        recipientNameInput.value = '';
        recipientEmailInput.value = '';
        statusSelect.value = '';
        sortBySelect.value = 'createdAt';
        currentSortOrder = 'desc';
        sortOrderIcon.textContent = '↓';
        performSearch();
    });

    /**
     * Perform search based on current filter values
     */
    async function performSearch() {
        try {
            // Build query parameters
            const params = new URLSearchParams();

            // Add search filters (only if they have values)
            const trackingNumber = trackingNumberInput.value.trim();
            const carrier = carrierInput.value.trim();
            const description = descriptionInput.value.trim();
            const recipientName = recipientNameInput.value.trim();
            const recipientEmail = recipientEmailInput.value.trim();
            const status = statusSelect.value;
            const sortBy = sortBySelect.value;

            if (trackingNumber) params.append('trackingNumber', trackingNumber);
            if (carrier) params.append('carrier', carrier);
            if (description) params.append('description', description);

            // For recipient name, we'll search both first and last name
            // The backend supports separate fields, but we provide a single input
            if (recipientName) {
                params.append('recipientFirstName', recipientName);
                params.append('recipientLastName', recipientName);
            }

            if (recipientEmail) params.append('recipientEmail', recipientEmail);
            if (status) params.append('status', status);

            // Add sorting parameters
            params.append('sortBy', sortBy);
            params.append('sortOrder', currentSortOrder);

            // Make API request
            const url = `/api/packages/search?${params.toString()}`;
            const response = await fetch(url, {
                credentials: 'include',
                headers: {
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                if (response.status === 401) {
                    window.location.href = '/index.html';
                    return;
                }
                throw new Error('Failed to search packages');
            }

            const packages = await response.json();
            displayPackages(packages);

        } catch (error) {
            console.error('Search error:', error);
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-danger py-4">
                        Error loading packages: ${escapeHtml(error.message)}
                    </td>
                </tr>
            `;
            resultCount.textContent = '0';
        }
    }

    /**
     * Display packages in the table
     * @param {Array} packages - Array of package objects
     */
    function displayPackages(packages) {
        // Update result count
        resultCount.textContent = packages.length;

        if (!packages || packages.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        No packages found matching your search criteria
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = packages.map(pkg => {
            const recipientName = pkg.recipient
                ? `${pkg.recipient.firstName} ${pkg.recipient.lastName || ''}`.trim()
                : 'Unknown';

            const recipientEmail = pkg.recipient ? pkg.recipient.email : '';

            const description = pkg.description || 'No description';

            const loggedAt = pkg.createdAt
                ? formatDateTime(pkg.createdAt)
                : 'N/A';

            const pickupAt = pkg.status === 'picked' && pkg.updatedAt
                ? formatDateTime(pkg.updatedAt)
                : '-';

            const statusBadge = pkg.status === 'picked'
                ? '<span class="badge bg-success">Picked Up</span>'
                : '<span class="badge bg-warning text-dark">Waiting</span>';

            return `
                <tr>
                    <td class="text-nowrap">
                        <strong>${escapeHtml(pkg.trackingNumber || 'N/A')}</strong>
                    </td>
                    <td>${escapeHtml(pkg.carrier || 'N/A')}</td>
                    <td>
                        <small>${escapeHtml(description)}</small>
                    </td>
                    <td>
                        <div>${escapeHtml(recipientName)}</div>
                        <small class="text-muted">${escapeHtml(recipientEmail)}</small>
                    </td>
                    <td>${statusBadge}</td>
                    <td class="text-nowrap">
                        <small>${loggedAt}</small>
                    </td>
                    <td class="text-nowrap">
                        <small>${pickupAt}</small>
                    </td>
                </tr>
            `;
        }).join('');
    }

    /**
     * Format date/time for display
     * @param {string} dateString - ISO date string
     * @returns {string} Formatted date/time
     */
    function formatDateTime(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        // Show relative time for recent packages
        if (diffMins < 1) return 'Just now';
        if (diffMins < 60) return `${diffMins} min ago`;
        if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
        if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;

        // Otherwise show formatted date
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined,
            hour: 'numeric',
            minute: '2-digit'
        });
    }

    /**
     * Escape HTML to prevent XSS attacks
     * @param {string} text - Text to escape
     * @returns {string} Escaped text
     */
    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
});
