// On page load, check if data was passed from search page
window.addEventListener('DOMContentLoaded', () => {
    // Get URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const tracking = urlParams.get('tracking');
    const recipient = urlParams.get('recipient');

    // If data exists, pre-fill the form
    if (tracking && recipient) {
        document.getElementById('trackingNumber').value = tracking;
        document.getElementById('pickedUpBy').value = recipient;
        
        // Show package info box
        document.getElementById('displayTracking').textContent = tracking;
        document.getElementById('displayRecipient').textContent = recipient;
        document.getElementById('packageInfo').style.display = 'block';
        
        // Focus on signature field
        document.getElementById('signature').focus();
    } else {
        // If accessed directly, make fields editable
        document.getElementById('trackingNumber').removeAttribute('readonly');
        document.getElementById('pickedUpBy').removeAttribute('readonly');
    }
});



// Handle pickup form submission
handleForm('pickupForm', async (formData) => {
    const trackingNumber = formData.get('trackingNumber').trim();
    const pickedUpBy = formData.get('pickedUpBy').trim();
    const signature = formData.get('signature').trim();

    // Validate
    if (!trackingNumber) {
        showMessage('Please enter tracking number', 'danger');
        return;
    }

    if (!pickedUpBy) {
        showMessage('Please enter your name', 'danger');
        return;
    }

    if (!signature) {
        showMessage('Please provide your signature', 'danger');
        return;
    }

    // Verify signature matches name (optional but good practice)
    if (signature.toLowerCase() !== pickedUpBy.toLowerCase()) {
        if (!confirm('Signature doesn\'t match your name. Continue anyway?')) {
            return;
        }
    }

    try {
        // Call API
        const response = await api('POST', '/api/packages/pickup', {
            trackingNumber: trackingNumber,
            pickedUpBy: pickedUpBy,
            signature: signature
        });

        // Success!
        showMessage(
            ` Package picked up successfully!<br>
            <strong>Tracking:</strong> ${response.trackingNumber}<br>
            <strong>Picked up by:</strong> ${response.pickedUpBy}<br>
            <strong>Time:</strong> ${new Date(response.pickedUpAt).toLocaleString()}`,
            'success'
        );

        // Clear form
        document.getElementById('pickupForm').reset();

    } catch (error) {
        showMessage(error.message, 'danger');
    }
});

// Show message to user
function showMessage(message, type) {
    const messageArea = document.getElementById('messageArea');
    messageArea.innerHTML = `
        <div class="alert alert-${type}" role="alert">
            ${message}
        </div>
    `;
}