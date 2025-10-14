// Minimal helpers
function validateForm(form){
    if(!form.checkValidity()){
      form.classList.add('was-validated'); // Bootstrap’s custom validation styling
        return false;
    }
    return true;
    }


function showView(targetId){
    const sign = document.getElementById('view-signin');
    const reg  = document.getElementById('view-register');
    const show = (el) => { el.classList.remove('d-none'); };
    const hide = (el) => { el.classList.add('d-none'); };

    if(targetId === 'register'){ hide(sign); show(reg); }
    else { hide(reg); show(sign); }
}

    document.addEventListener('DOMContentLoaded', () => {

    document.getElementById('linkToRegister')?.addEventListener('click', (e)=>{ e.preventDefault(); showView('register'); });
    document.getElementById('linkToSignin')?.addEventListener('click',   (e)=>{ e.preventDefault(); showView('signin');   });

    // SIGN IN
    const loginForm = document.getElementById('loginForm');
    loginForm?.addEventListener('submit', async (e)=>{
        e.preventDefault();
        if(!validateForm(loginForm)) return;
    
        const body = {
        email:    loginForm.email.value.trim(),
        password: loginForm.password.value
        };

    alert(`Signed in as ${body.email} (demo)`);
    });

    // REGISTER
    const regForm = document.getElementById('registerForm');
    regForm?.addEventListener('submit', async (e)=>{
        e.preventDefault();

      // match confirmation first
        const p1 = document.getElementById('regPassword');
        const p2 = document.getElementById('regPassword2');
        if(p1.value !== p2.value){
        
        p2.setCustomValidity('Passwords do not match');
        }else{
        p2.setCustomValidity('');
        }

        if(!validateForm(regForm)) return;
        const body = {
        firstName: regForm.firstName.value.trim(),
        lastName:  regForm.lastName.value.trim(),
        email:     regForm.email.value.trim(),
        password:  regForm.password.value
        };

        alert(`Account created for ${body.firstName} ${body.lastName} (demo)`);
        showView('signin');
    });
});