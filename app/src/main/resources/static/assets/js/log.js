console.log('🚀 log.js is loading...');

// Wait for DOM to be ready before attaching form handler
document.addEventListener('DOMContentLoaded', () => {
    handleForm('logForm', async (fd, form) => {
        
        
        const trackingNumber = (fd.get('trackingNumber') || '').toString().trim();
        const carrier        = (fd.get('carrier') || '').toString().trim();
        const recipientFirst = (fd.get('recipientFirstName') || '').toString().trim();
        const recipientLast  = (fd.get('recipientLastName') || '').toString().trim();
        const recipientEmail = (fd.get('recipientEmail') || '').toString().trim();
        const description    = (fd.get('description') || '').toString().trim(); 


        if (!trackingNumber || trackingNumber.length < 5) 
            throw new Error('Tracking number looks too short');
        
        if (!carrier) 
            throw new Error('Carrier is required');
        
        if (!recipientEmail || !recipientFirst || !recipientLast) 
            throw new Error('Recipient info is required');

        const data = await api('POST', '/api/packages', {
            trackingNumber,
            carrier,
            description,
            recipientEmail,
            recipientFirst,
            recipientLast
        });

        // Build success message with pickup code and email confirmation
        const box = document.getElementById('logResult');

        // Email status styling
        const emailIcon = data?.emailSent ? '✉️' : '⚠️';
        const emailClass = data?.emailSent ? 'text-success' : 'text-warning';

        const msg = `
            <div class="alert alert-success">
                <h5 class="alert-heading">✅ Package Logged Successfully!</h5>
                <hr>
                <div class="row">
                    <div class="col-md-6">
                        <p class="mb-1"><strong>Tracking Number:</strong></p>
                        <p class="mb-2" style="font-size: 1.1rem;">${trackingNumber}</p>

                        <p class="mb-1"><strong>Recipient:</strong></p>
                        <p class="mb-2">${recipientFirst} ${recipientLast}</p>

                        <p class="mb-1"><strong>Email:</strong></p>
                        <p class="mb-2">${recipientEmail}</p>
                    </div>
                    <div class="col-md-6">
                        <p class="mb-1"><strong>Pickup Code:</strong></p>
                        <div class="bg-success text-white p-3 rounded text-center mb-3" style="font-size: 1.5rem; letter-spacing: 3px; font-family: 'Courier New', monospace;">
                            ${data?.pickupCode || 'N/A'}
                        </div>

                        <p class="${emailClass} mb-0">
                            <strong>${emailIcon} ${data?.message || 'Package logged'}</strong>
                        </p>
                    </div>
                </div>
                ${!data?.emailSent ? '<div class="alert alert-warning mt-3 mb-0"><small>⚠️ Email failed to send. Please inform recipient of pickup code: <strong>' + (data?.pickupCode || '') + '</strong></small></div>' : ''}
            </div>
        `;

        if (box) {
            box.innerHTML = msg;
        } else {
            alert('Package logged! Pickup code: ' + (data?.pickupCode || 'N/A'));
        }

        form.reset();
        console.log('=== FORM SUBMISSION COMPLETED ===');
    });

    // Clear button handler
    const clearBtn = document.getElementById('btnClear');
    const form = document.getElementById('logForm');
    const resultBox = document.getElementById('logResult');
    
    if (clearBtn && form) {
        clearBtn.addEventListener('click', () => {  
            form.reset();                
            if (resultBox) 
                resultBox.innerHTML = '';
        });
    }
    
});