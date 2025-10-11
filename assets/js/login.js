handleForm('loginForm', async (fd) =>{
    const email = (fd.get('email') || "").toString().trim();
    const password =(fd.get('password')|| "").toString();

    if(!email || !password) throw new Error('Please enter email and password');

    await api('POST','/api/auth/login', { email, password });
    
    location.href ="dashboard.html"
})