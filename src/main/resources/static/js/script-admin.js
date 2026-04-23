// Pillar los datos del formulario y mandar el AJAX
const acceptForms = document.querySelectorAll(".accept-form");
const rejectForms = document.querySelectorAll(".reject-form");
const errorBox = document.getElementById("error-box");
const successBox = document.getElementById("success-box");

acceptForms.forEach(f => f.addEventListener("submit", handleRequestSubmit));
rejectForms.forEach(f => f.addEventListener("submit", handleRequestSubmit));

function handleRequestSubmit(e) {
    e.preventDefault();
    const form = e.target;
    const requestId = form.querySelector('input[name="id"]').value;
    const action = form.classList.contains('accept-form') ? 'aceptar' : 'rechazar';

    if (errorBox) {
        errorBox.classList.add("d-none");
        errorBox.textContent = "";
    }
    if (successBox) {
        successBox.classList.add("d-none");
        successBox.textContent = "";
    }

    go(form.action, "POST", new URLSearchParams(new FormData(form)))
        .then((response) => {
            const requestItem = form.closest('.request-item');
            if (requestItem) {
                requestItem.remove();
            }

            successBox.textContent = response?.message;
            successBox.classList.remove("d-none");


        })
        .catch((error) => {
            let message = `No se pudo aceptar la solicitud`;
            try {
                const json = JSON.parse(error?.text || "{}"); 
                if (json.message) {
                    message = json.message;
                }
            } catch (_) {
                // Mantener mensaje por defecto si no hay JSON valido
            }


            errorBox.textContent = message;
            errorBox.classList.remove("d-none");

        })
}