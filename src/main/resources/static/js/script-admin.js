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

// Manejar clics en botones de aceptar/rechazar
document.querySelectorAll(".btn-aceptar, .btn-rechazar").forEach(btn => {
    btn.addEventListener("click", (e) => {
        e.target.parentElement.parentElement.remove();
    });
});