/**
 * Pickup Package JavaScript
 *
 * This script handles the package pickup functionality.
 * It allows employees to mark packages as picked up by entering
 * the tracking number and signature/pickup information.
 */

document.addEventListener('DOMContentLoaded', () => {
    const pickupForm = document.getElementById('pickupForm');
    const messageElement = document.getElementById('msg');

    /**
     * Handle the pickup form submission
     */
    pickupForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Get form values
        const trackingNumber = document.getElementById('pt').value.trim();
        const pickupCode = document.getElementById('pc').value.trim().toUpperCase();
        const pickedUpBy = document.getElementById('pb').value.trim();
        const staff = document.getElementById('ps').value.trim();

        // Validate pickup code format (6 alphanumeric characters)
        if (pickupCode.length !== 6) {
            showMessage('Pickup code must be exactly 6 characters', 'error');
            return;
        }

        // Build signature string from form inputs
        const signature = pickedUpBy || 'Recipient';
        const notes = staff ? `Staff notes: ${staff}` : '';

        try {
            // Step 1: Find package by tracking number
            const searchResponse = await fetch(
                `/api/packages/search?trackingNumber=${encodeURIComponent(trackingNumber)}`,
                {
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json'
                    }
                }
            );

            if (!searchResponse.ok) {
                if (searchResponse.status === 401) {
                    window.location.href = '/index.html';
                    return;
                }
                throw new Error('Failed to search for package');
            }

            const packages = await searchResponse.json();

            // Check if package was found
            if (!packages || packages.length === 0) {
                showMessage('Package not found. Please check the tracking number.', 'error');
                return;
            }

            // Get the first matching package
            const packageData = packages[0];

            // Step 2: Mark package as picked up with verification code
            const pickupResponse = await fetch(`/api/packages/${packageData.id}/pickup`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    signature: signature,
                    notes: notes,
                    pickupCode: pickupCode
                })
            });

            if (!pickupResponse.ok) {
                const errorData = await pickupResponse.json();
                throw new Error(errorData.error || 'Failed to mark package as picked up');
            }

            const result = await pickupResponse.json();

            // Show success message
            showMessage(
                `Success! Package ${result.trackingNumber} marked as picked up.`,
                'success'
            );

            // Clear form
            pickupForm.reset();

        } catch (error) {
            console.error('Pickup error:', error);
            showMessage(error.message || 'An error occurred during pickup', 'error');
        }
    });

    /**
     * Display a message to the user
     * @param {string} message - The message to display
     * @param {string} type - 'success' or 'error'
     */
    function showMessage(message, type) {
        messageElement.textContent = message;
        messageElement.className = type === 'success'
            ? 'alert alert-success mt-3'
            : 'alert alert-danger mt-3';

        // Auto-hide success messages after 5 seconds
        if (type === 'success') {
            setTimeout(() => {
                messageElement.textContent = '';
                messageElement.className = 'small-muted mt-2';
            }, 5000);
        }
    }
});
