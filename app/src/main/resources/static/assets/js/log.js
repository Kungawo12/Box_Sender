console.log('log.js is loading...');

// Wait for DOM to be ready before attaching form handler
document.addEventListener('DOMContentLoaded', () => {
    // Set up form submission handler
    handleForm('logForm', async (fd, form) => {
        // ========================================
        // STEP 1: Extract form data
        // ========================================
        
        // Get tracking number from form
        // const trackingNumber = (fd.get('trackingNumber') || '').toString().trim();
        // fd.get('trackingNumber')  → gets value from form field
        // || ''                      → if null/undefined, use empty string
        // .toString()                → convert to string
        // .trim()                    → remove whitespace from start/end

        const trackingNumber = (fd.get('trackingNumber') || '').toString().trim();
        const carrier        = (fd.get('carrier') || '').toString().trim();
        const recipientFirst = (fd.get('recipientFirstName') || '').toString().trim();
        const recipientLast  = (fd.get('recipientLastName') || '').toString().trim();
        const recipientEmail = (fd.get('recipientEmail') || '').toString().trim();
        const description    = (fd.get('description') || '').toString().trim(); 

        // STEP 2: Validate form data
        // Check tracking number
        if (!trackingNumber || trackingNumber.length < 5) 
            throw new Error('Tracking number looks too short');
        // This throws an error, which is caught by handleForm()
        // handleForm() will show alert() with the error message
        
        // Check carrier
        if (!carrier) 
            throw new Error('Carrier is required');
        
        // Check recipient info
        if (!recipientEmail || !recipientFirst || !recipientLast) 
            throw new Error('Recipient info is required');

        // STEP 3: Call API to log package
        const data = await api('POST', '/api/packages', {
            trackingNumber, 
            carrier, 
            description,
            recipientEmail, 
            recipientFirst,
            recipientLast
        });
        // This uses ES6 shorthand property syntax:
        // { trackingNumber: trackingNumber } → { trackingNumber }


        // STEP 4: Show success message
        const box = document.getElementById('logResult');
        // Build success message HTML
        const msg = ` Package logged successfully!<br>
                    <strong>Tracking:</strong> ${trackingNumber}<br>
                    <strong>Status:</strong> ${data?.status || 'received'}<br>
                    <strong>Recipient:</strong> ${recipientFirst} ${recipientLast}`;
        
        // Display message
        if (box) {
            box.innerHTML = `<div class="alert alert-success">${msg}</div>`; 
        } else {
            alert(msg);
        }

        // STEP 5: Reset form for next entry
        form.reset();

        console.log('=== FORM SUBMISSION COMPLETED ===');
    });

    // Clear button handler
    const clearBtn = document.getElementById('btnClear');
    const form = document.getElementById('logForm');
    const resultBox = document.getElementById('logResult');
    
    if (clearBtn && form) {
        clearBtn.addEventListener('click', () => {  
            form.reset();        //Clear form fields        
            if (resultBox) 
                resultBox.innerHTML = '';   //clear success message
        });
    }
    
});