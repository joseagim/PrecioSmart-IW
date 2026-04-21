// Mostrar/ocultar divs según el radio button seleccionado
const radioButtons = document.querySelectorAll('input[name="type"]');
const requestLists = {
    "0": document.querySelector("#pending-request-list"),
    
    "1": document.querySelector("#accepted-request-list"),
    "2": document.querySelector("#rejected-request-list")
};

function showRequestList(type) {
    Object.values(requestLists).forEach(list => {
        list.style.display = "none";
    });
    requestLists[type].style.display = "block";
}

radioButtons.forEach(radio => {
    radio.addEventListener("change", (e) => {
        showRequestList(e.target.value);
    });
});

// Mostrar la primera lista al cargar
showRequestList("0");

// Pillar los datos del formulario y mandar el AJAX
const acceptForms = document.querySelectorAll(".accept-form");
const rejectForms = document.querySelectorAll(".reject-form");

acceptForms.forEach(f => f.addEventListener("submit", handleRequestSubmit));
rejectForms.forEach(f => f.addEventListener("submit", handleRequestSubmit));

function handleRequestSubmit(e) {
    e.preventDefault();
    const form = e.target;
    const requestId = form.querySelector('input[name="id"]').value;
    const action = form.classList.contains('accept-form') ? 'aceptar' : 'rechazar';

    console.log(`Acción: ${action}, ID: ${requestId}`);

    go(f.action, "POST", new URLSearchParams(FormData(f)))
        .then(data => {
            
            const action = form.classList.contains('accept-form') ? 'aceptar' : 'rechazar';
        })
        .fail(data => {

        })
}