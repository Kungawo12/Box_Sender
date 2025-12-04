(function() {
    async function loadUserInfo() {
        try {
            const res = await fetch('/api/auth/me', { credentials: 'include' });
            if (!res.ok) {
                window.location.replace('/index.html');
                return;
            }
            
            const user = await res.json();
            
            const roleBadge = document.getElementById('userRole');
            if (roleBadge) {
                if (user.role === 'MAILROOM_STAFF') {
                    roleBadge.textContent = 'Mailroom Staff';
                    roleBadge.className = 'badge bg-success';
                } else {
                    roleBadge.textContent = 'Employee';
                    roleBadge.className = 'badge bg-info';
                }
            }
            
        } catch (error) {
            console.error('Failed to load user:', error);
            window.location.replace('/index.html');
        }
    }

    function setDefaultDates() {
        const today = new Date();
        const lastMonth = new Date();
        lastMonth.setDate(today.getDate() - 30);

        document.getElementById('endDate').valueAsDate = today;
        document.getElementById('startDate').valueAsDate = lastMonth;
    }

    window.downloadReport = async function(format) {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        const loadingIndicator = document.getElementById('loadingIndicator');
        const errorMessage = document.getElementById('errorMessage');

        loadingIndicator.style.display = 'none';
        errorMessage.style.display = 'none';

        try {
            loadingIndicator.style.display = 'block';

            let url = `/api/reports/summary/${format}?`;
            if (startDate) url += `startDate=${startDate}&`;
            if (endDate) url += `endDate=${endDate}`;

            console.log('Fetching report from:', url);

            const response = await fetch(url, {
                method: 'GET',
                credentials: 'include'
            });

            console.log('Response status:', response.status);
            console.log('Response headers:', [...response.headers.entries()]);

            if (!response.ok) {
                // Try to get error message from response
                const contentType = response.headers.get('content-type');
                let errorMsg = 'Failed to generate report';
                
                if (contentType && contentType.includes('application/json')) {
                    const errorData = await response.json();
                    errorMsg = errorData.error || errorData.message || errorMsg;
                    console.error('Error data:', errorData);
                } else {
                    const errorText = await response.text();
                    console.error('Error text:', errorText);
                    errorMsg = errorText || errorMsg;
                }
                
                throw new Error(errorMsg);
            }

            const contentDisposition = response.headers.get('Content-Disposition');
            let filename = `Package_Summary.${format === 'pdf' ? 'pdf' : 'xlsx'}`;
            if (contentDisposition) {
                const filenameMatch = contentDisposition.match(/filename="?([^"]+)"?/);
                if (filenameMatch) {
                    filename = filenameMatch[1];
                }
            }

            const blob = await response.blob();
            console.log('Blob size:', blob.size, 'bytes');
            
            const downloadUrl = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = downloadUrl;
            link.download = filename;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(downloadUrl);

            loadingIndicator.style.display = 'none';

        } catch (error) {
            console.error('Report generation failed:', error);
            loadingIndicator.style.display = 'none';
            errorMessage.textContent = 'Failed to generate report: ' + error.message;
            errorMessage.style.display = 'block';
        }
    };

    document.addEventListener('DOMContentLoaded', () => {
        loadUserInfo();
        setDefaultDates();
    });
})();