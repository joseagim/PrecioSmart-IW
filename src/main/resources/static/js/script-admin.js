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
const acceptForm = document.querySelector("#accept-form");
const rejectForm = document.querySelector("#reject-form");

acceptForm.addEventListener("submit", (e) => {
    e.preventDefault(); // para que no se envíe
    go(acceptForm, "POST")
    .then(data => {
        
    })
})