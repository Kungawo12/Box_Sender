// assets/js/login.js

// --- helpers ---
function validateForm(form) {
    if (!form.checkValidity()) {
      form.classList.add('was-validated'); // Bootstrap styling
      return false;
    }
    return true;
  }
  
  function showView(targetId) {
    const sign = document.getElementById('view-signin');
    const reg  = document.getElementById('view-register');
    const show = el => el.classList.remove('d-none');
    const hide = el => el.classList.add('d-none');
    if (targetId === 'register') { hide(sign); show(reg); }
    else                         { hide(reg); show(sign); }
  }
  
  // POST JSON helper
  async function apiPost(path, data) {
    const res = await fetch(path, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',           // send/receive cookies with fetch
      body: JSON.stringify(data)
    });
  
    if (!res.ok) {
      let msg = 'Request failed';
      try {
        const text = await res.text();
        if (text) msg = text;
      } catch {}
      throw new Error(msg);
    }
    return res;
  }
  
  document.addEventListener('DOMContentLoaded', () => {
    // swap between Sign in <-> Register
    document.getElementById('linkToRegister')?.addEventListener('click', (e)=>{
      e.preventDefault(); showView('register');
    });
    document.getElementById('linkToSignin')?.addEventListener('click', (e)=>{
      e.preventDefault(); showView('signin');
    });
  
    // --- SIGN IN ---
    const loginForm = document.getElementById('loginForm');
    loginForm?.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!validateForm(loginForm)) return;
  
      const body = {
        email:    loginForm.email.value.trim(),
        password: loginForm.password.value
      };
  
      try {
        await apiPost('/api/auth/login', body);
        window.location.replace('/dashboard.html');
      } catch (err) {
        alert(err.message);
      }
    });
  
    // --- REGISTER ---
    const regForm = document.getElementById('registerForm');
    regForm?.addEventListener('submit', async (e) => {
      e.preventDefault();
  
      const p1 = document.getElementById('regPassword');
      const p2 = document.getElementById('regPassword2');
      p2.setCustomValidity(p1.value !== p2.value ? 'Passwords do not match' : '');
      if (!validateForm(regForm)) return;
  
      const body = {
        firstName: regForm.firstName.value.trim(),
        lastName:  regForm.lastName.value.trim(),
        email:     regForm.email.value.trim(),
        password:  regForm.password.value
      };
  
      try {
        // server registers AND auto-logs in
        await apiPost('/api/auth/register', body);
        window.location.replace('/dashboard.html');
      } catch (err) {
        alert(err.message);
        showView('signin');
      }
    });
  });