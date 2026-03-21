const registerForm = document.querySelector(".form-signin");

if (registerForm) {
    registerForm.onsubmit = function (event) {
        const pass = document.getElementById("password").value;
        const confirm = document.getElementById("confirmPassword").value;
        if (pass !== confirm) {
            event.preventDefault();
            alert("Las contrasenas no coinciden");
        }
    };
}
