handleForm('logForm', async (fd, form) => {
    const trackingNumber = (fd.get('trackingNumber') || '').toString().trim();
    const carrier        = (fd.get('carrier') || '').toString().trim();
    const recipientFirst = (fd.get('recipientFirst') || '').toString().trim();
    const recipientLast  = (fd.get('recipientLast') || '').toString().trim();
    const recipientEmail = (fd.get('recipientEmail') || '').toString().trim();
    const description    = (fd.get('description') || '').toString().trim(); 

    if (!trackingNumber || trackingNumber.length < 5) 
    throw new Error('Tracking number looks too short');
    
    if (!carrier) 
    throw new Error('Carrier is required');
    
    if (!recipientEmail || !recipientFirst || !recipientLast) 
    throw new Error('Recipient info is required');

    const data = await api('POST', '/api/packages', {
        trackingNumber, carrier, description,
        recipientEmail, recipientFirst, recipientLast
    });
    
    const box = document.getElementById('logResult');
    const msg = `Package logged. Tracking: ${trackingNumber}. Pickup code: ${data?.pickupCode || '(unknown)'}`;
    if (box) 
        box.innerHTML = `<div class="alert alert-success">${msg}</div>`; 
    else 
    alert(msg);

    form.reset();
});

document.addEventListener('DOMContentLoaded', () => {
    const clearBtn = document.getElementById('btnClear');
    const form = document.getElementById('logForm');
    const resultBox = document.getElementById('logResult');
    if (!clearBtn || !form) return; // 

    clearBtn.addEventListener('click', () => {  
        form.reset();                
    if (resultBox) 
        resultBox.innerHTML = '';
    });
});
