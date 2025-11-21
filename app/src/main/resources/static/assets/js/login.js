/**
 * Login Page JavaScript - Updated with Role Support
 */

function validateForm(form) {
  if (!form.checkValidity()) {
    form.classList.add('was-validated');
    return false;
  }
  return true;
}

function showView(targetId) {
  const sign = document.getElementById('view-signin');
  const reg  = document.getElementById('view-register');

  const show = el => el.classList.remove('d-none');
  const hide = el => el.classList.add('d-none');

  if (targetId === 'register') {
    hide(sign);
    show(reg);
  } else {
    hide(reg);
    show(sign);
  }
}

async function apiPost(path, data) {
  const res = await fetch(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(data)
  });

  // Parse response body
  const text = await res.text();
  let responseData = null;
  try {
    responseData = text ? JSON.parse(text) : null;
  } catch {
    responseData = { error: text };
  }

  if (!res.ok) {
    const msg = responseData?.error || 'Request failed';
    throw new Error(msg);
  }
  
  return responseData;
}

document.addEventListener('DOMContentLoaded', () => {

  // --- VIEW SWITCHING ---
  document.getElementById('linkToRegister')?.addEventListener('click', (e) => {
    e.preventDefault();
    showView('register');
  });

  document.getElementById('linkToSignin')?.addEventListener('click', (e) => {
    e.preventDefault();
    showView('signin');
  });

  // --- SIGN IN FORM ---
  const loginForm = document.getElementById('loginForm');
  loginForm?.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!validateForm(loginForm)) return;

    // Get form values including role
    const email = loginForm.email.value.trim();
    const password = loginForm.password.value;
    const selectedRole = document.getElementById('loginRole').value;

    // Validate role selection
    if (!selectedRole) {
      alert('Please select your role.');
      return;
    }

    const body = {
      email: email,
      password: password
    };

    try {
      // Send login request
      const data = await apiPost('/api/auth/login', body);

      // Check if role matches
      if (data.role && data.role !== selectedRole) {
        const roleName = data.role === 'MAILROOM_STAFF' ? 'Mailroom Employee' : 'Employee';
        alert(`This account is registered as "${roleName}". Please select the correct role.`);
        return;
      }

      // Success - redirect to dashboard
      window.location.replace('/dashboard.html');

    } catch (err) {
      alert(err.message || 'Invalid email or password');
    }
  });

  // --- REGISTRATION FORM ---
  const regForm = document.getElementById('registerForm');
  regForm?.addEventListener('submit', async (e) => {
    e.preventDefault();

    // Password matching
    const p1 = document.getElementById('regPassword');
    const p2 = document.getElementById('regPassword2');
    p2.setCustomValidity(p1.value !== p2.value ? 'Passwords do not match' : '');

    if (!validateForm(regForm)) return;

    // Get role
    const selectedRole = document.getElementById('regRole').value;
    if (!selectedRole) {
      alert('Please select your role.');
      return;
    }

    const body = {
      role: selectedRole,
      firstName: regForm.firstName.value.trim(),
      lastName: regForm.lastName.value.trim(),
      email: regForm.email.value.trim(),
      password: regForm.password.value
    };

    try {
      await apiPost('/api/auth/register', body);
      
      alert('Account created successfully! Please sign in.');
      regForm.reset();
      showView('signin');

    } catch (err) {
      alert(err.message || 'Registration failed');
    }
  });
});