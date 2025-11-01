/**
 * Admin Panel JavaScript
 *
 * This script handles the admin panel functionality for managing employee roles.
 * Only accessible to users with ADMIN role.
 */

document.addEventListener('DOMContentLoaded', async () => {
    // Check if user is admin
    try {
        const res = await fetch('/api/auth/me', { credentials: 'include' });
        if (!res.ok) throw new Error('Not signed in');
        const me = await res.json();

        // Redirect non-admins to dashboard
        if (me.role !== 'ADMIN') {
            alert('Access denied. Admin role required.');
            window.location.href = '/dashboard.html';
            return;
        }

        // Load employees if user is admin
        loadEmployees();

        // Setup create employee form handler
        setupCreateEmployeeForm();

    } catch (error) {
        window.location.href = '/index.html';
    }
});

/**
 * Setup create employee form submission handler
 */
function setupCreateEmployeeForm() {
    const form = document.getElementById('createEmployeeForm');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Get form values
        const firstName = document.getElementById('newFirstName').value.trim();
        const lastName = document.getElementById('newLastName').value.trim();
        const email = document.getElementById('newEmail').value.trim();
        const password = document.getElementById('newPassword').value;
        const role = document.getElementById('newRole').value;

        // Validate
        if (!firstName || !lastName || !email || !password || !role) {
            showMessage('Please fill in all fields', 'error');
            return;
        }

        if (password.length < 6) {
            showMessage('Password must be at least 6 characters', 'error');
            return;
        }

        try {
            const response = await fetch('/api/admin/employees', {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    firstName,
                    lastName,
                    email,
                    password,
                    role
                })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.error || 'Failed to create employee');
            }

            // Show success message
            showMessage(`Successfully created account for ${email} with role ${role}`, 'success');

            // Clear form
            form.reset();

            // Reload employee list
            loadEmployees();

        } catch (error) {
            console.error('Create employee error:', error);
            showMessage(error.message || 'Failed to create employee', 'error');
        }
    });
}

/**
 * Load all employees and display them in the table
 */
async function loadEmployees() {
    const tbody = document.getElementById('employeeTableBody');

    try {
        const response = await fetch('/api/admin/employees', {
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                window.location.href = '/index.html';
                return;
            }
            throw new Error('Failed to load employees');
        }

        const employees = await response.json();

        if (!employees || employees.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No employees found</td></tr>';
            return;
        }

        // Build table rows
        tbody.innerHTML = employees.map(emp => {
            const roleBadge = getRoleBadge(emp.role);

            return `
                <tr>
                    <td><strong>${escapeHtml(emp.firstName)} ${escapeHtml(emp.lastName)}</strong></td>
                    <td>${escapeHtml(emp.email)}</td>
                    <td>${roleBadge}</td>
                    <td>
                        <div class="btn-group btn-group-sm" role="group">
                            <button type="button" class="btn btn-outline-primary" onclick='editEmployee(${JSON.stringify(emp)})' title="Edit">
                                Edit
                            </button>
                            <button type="button" class="btn btn-outline-danger" onclick="deleteEmployee(${emp.id}, '${escapeHtml(emp.email)}')" title="Delete">
                                Delete
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

    } catch (error) {
        console.error('Load employees error:', error);
        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center text-danger">
                    Error loading employees: ${escapeHtml(error.message)}
                </td>
            </tr>
        `;
    }
}

/**
 * Get badge HTML for a role
 * @param {string} role - The role name
 * @returns {string} Bootstrap badge HTML
 */
function getRoleBadge(role) {
    switch (role) {
        case 'ADMIN':
            return '<span class="badge bg-danger">Admin</span>';
        case 'MAILROOM_STAFF':
            return '<span class="badge bg-primary">Mailroom Staff</span>';
        case 'EMPLOYEE':
            return '<span class="badge bg-secondary">Employee</span>';
        default:
            return `<span class="badge bg-dark">${escapeHtml(role)}</span>`;
    }
}

/**
 * Display a message to the user
 * @param {string} message - The message to display
 * @param {string} type - 'success' or 'error'
 */
function showMessage(message, type) {
    const messageBox = document.getElementById('messageBox');
    messageBox.innerHTML = `
        <div class="alert alert-${type === 'success' ? 'success' : 'danger'} alert-dismissible fade show" role="alert">
            ${escapeHtml(message)}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;

    // Auto-hide success messages after 5 seconds
    if (type === 'success') {
        setTimeout(() => {
            messageBox.innerHTML = '';
        }, 5000);
    }
}

/**
 * Open edit employee modal
 * @param {Object} employee - The employee object to edit
 */
window.editEmployee = function(employee) {
    // Populate form with employee data
    document.getElementById('editEmployeeId').value = employee.id;
    document.getElementById('editFirstName').value = employee.firstName;
    document.getElementById('editLastName').value = employee.lastName;
    document.getElementById('editEmail').value = employee.email;
    document.getElementById('editPassword').value = ''; // Clear password field
    document.getElementById('editRole').value = employee.role;

    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('editEmployeeModal'));
    modal.show();
};

/**
 * Save employee edit from modal
 */
window.saveEmployeeEdit = async function() {
    const employeeId = document.getElementById('editEmployeeId').value;
    const firstName = document.getElementById('editFirstName').value.trim();
    const lastName = document.getElementById('editLastName').value.trim();
    const email = document.getElementById('editEmail').value.trim();
    const password = document.getElementById('editPassword').value; // Can be empty
    const role = document.getElementById('editRole').value;

    // Validate required fields
    if (!firstName || !lastName || !email || !role) {
        showMessage('Please fill in all required fields', 'error');
        return;
    }

    // Validate password if provided
    if (password && password.length < 6) {
        showMessage('Password must be at least 6 characters', 'error');
        return;
    }

    try {
        // Build request body (only include password if provided)
        const body = {
            firstName,
            lastName,
            email,
            role
        };

        if (password) {
            body.password = password;
        }

        const response = await fetch(`/api/admin/employees/${employeeId}`, {
            method: 'PUT',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(body)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || 'Failed to update employee');
        }

        // Close modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('editEmployeeModal'));
        modal.hide();

        // Show success message
        showMessage(`Successfully updated employee: ${email}`, 'success');

        // Reload employee list
        loadEmployees();

    } catch (error) {
        console.error('Update employee error:', error);
        showMessage(error.message || 'Failed to update employee', 'error');
    }
};

/**
 * Delete an employee account
 * @param {number} employeeId - The employee ID to delete
 * @param {string} email - The employee's email (for display)
 */
window.deleteEmployee = async function(employeeId, email) {
    const confirmed = confirm(`Are you sure you want to delete the account for ${email}?\n\nThis action cannot be undone!`);
    if (!confirmed) return;

    try {
        const response = await fetch(`/api/admin/employees/${employeeId}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Accept': 'application/json'
            }
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || 'Failed to delete employee');
        }

        // Show success message
        showMessage(`Successfully deleted account: ${email}`, 'success');

        // Reload employee list
        loadEmployees();

    } catch (error) {
        console.error('Delete employee error:', error);
        showMessage(error.message || 'Failed to delete employee', 'error');
    }
};

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
