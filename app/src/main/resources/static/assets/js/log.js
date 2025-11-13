console.log('log.js is loading...');

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
        
        
        const box = document.getElementById('logResult');
        const msg = ` Package logged successfully!<br>
                     <strong>Tracking:</strong> ${trackingNumber}<br>
                     <strong>Status:</strong> ${data?.status || 'received'}<br>
                     <strong>Recipient:</strong> ${recipientFirst} ${recipientLast}<br>
                     <strong>Email:</strong> ${recipientEmail}`;

        if (box) {
            box.innerHTML = `<div class="alert alert-success">${msg}</div>`; 
        } else {
            alert(msg);
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