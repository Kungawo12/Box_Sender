(function () {
  'use strict';

  // DOM Elements
  const viewSignin = document.getElementById('view-signin');
  const viewRegister = document.getElementById('view-register');
  const linkToRegister = document.getElementById('linkToRegister');
  const linkToSignin = document.getElementById('linkToSignin');
  
  const loginForm = document.getElementById('loginForm');
  const registerForm = document.getElementById('registerForm');
  const loginError = document.getElementById('loginError');
  const registerError = document.getElementById('registerError');
  const registerSuccess = document.getElementById('registerSuccess');

  // Show/hide security key field based on account type
  const typeEmployee = document.getElementById('typeEmployee');
  const typeMailroom = document.getElementById('typeMailroom');
  const securityKeyField = document.getElementById('securityKeyField');
  const securityKeyInput = document.getElementById('securityKey');

  if (typeEmployee && typeMailroom) {
      typeEmployee.addEventListener('change', () => {
          securityKeyField.classList.add('d-none');
          securityKeyInput.removeAttribute('required');
      });

      typeMailroom.addEventListener('change', () => {
          securityKeyField.classList.remove('d-none');
          securityKeyInput.setAttribute('required', 'required');
      });
  }

  // View Switching
  if (linkToRegister) {
      linkToRegister.addEventListener('click', (e) => {
          e.preventDefault();
          viewSignin.classList.add('d-none');
          viewRegister.classList.remove('d-none');
      });
  }

  if (linkToSignin) {
      linkToSignin.addEventListener('click', (e) => {
          e.preventDefault();
          viewRegister.classList.add('d-none');
          viewSignin.classList.remove('d-none');
      });
  }

  // API Helper
  async function api(method, path, body) {
      const res = await fetch(path, {
          method: method,
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: body ? JSON.stringify(body) : undefined
      });

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

  // LOGIN FORM - No role selection needed
  if (loginForm) {
      loginForm.addEventListener('submit', async (e) => {
          e.preventDefault();
          
          if (loginError) loginError.classList.add('d-none');

          try {
              const email = document.getElementById('loginEmail').value.trim();
              const password = document.getElementById('loginPassword').value;

              if (!email || !password) {
                  throw new Error('Please enter email and password');
              }

              // Call login API (no role needed - backend determines from database)
              await api('POST', '/api/auth/login', {
                  email: email,
                  password: password
              });

              // Success - redirect to dashboard
              window.location.replace('/dashboard.html');

          } catch (error) {
              if (loginError) {
                  loginError.textContent = error.message || 'Invalid email or password';
                  loginError.classList.remove('d-none');
              }
          }
      });
  }

  // REGISTER FORM - With security key validation
  if (registerForm) {
      registerForm.addEventListener('submit', async (e) => {
          e.preventDefault();
          
          if (registerError) registerError.classList.add('d-none');
          if (registerSuccess) registerSuccess.classList.add('d-none');

          try {
              // Get form values
              const accountType = document.querySelector('input[name="accountType"]:checked').value;
              const securityKey = document.getElementById('securityKey').value;
              const firstName = document.getElementById('firstName').value.trim();
              const lastName = document.getElementById('lastName').value.trim();
              const email = document.getElementById('regEmail').value.trim();
              const password = document.getElementById('regPassword').value;
              const password2 = document.getElementById('regPassword2').value;

              // Validation
              if (!firstName || !lastName) {
                  throw new Error('Please enter your full name');
              }
              if (!email) {
                  throw new Error('Please enter your email');
              }
              if (password.length < 6) {
                  throw new Error('Password must be at least 6 characters');
              }
              if (password !== password2) {
                  throw new Error('Passwords do not match');
              }

              // Validate security key for mailroom staff
              if (accountType === 'MAILROOM_STAFF' && !securityKey) {
                  throw new Error('Security key is required for mailroom staff registration');
              }

              // Call register API
              await api('POST', '/api/auth/register', {
                  role: accountType,
                  securityKey: securityKey || null,
                  firstName: firstName,
                  lastName: lastName,
                  email: email,
                  password: password
              });

              // Success
              if (registerSuccess) {
                  registerSuccess.textContent = 'Account created successfully! You can now sign in.';
                  registerSuccess.classList.remove('d-none');
              }
              registerForm.reset();

              // Switch to login view after 2 seconds
              setTimeout(() => {
                  viewRegister.classList.add('d-none');
                  viewSignin.classList.remove('d-none');
                  if (registerSuccess) registerSuccess.classList.add('d-none');
              }, 2000);

          } catch (error) {
              if (registerError) {
                  registerError.textContent = error.message || 'Registration failed';
                  registerError.classList.remove('d-none');
              }
          }
      });
  }

})();