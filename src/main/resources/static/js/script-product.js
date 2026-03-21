const ini = document.getElementById("cantidad");
if (ini) {
    ini.value = 1;
}

const botonCompra = document.querySelector("#buy-button");
if (botonCompra) {
    botonCompra.addEventListener("click", () => {
        const cantidad = document.getElementById("cantidad").value;
        alert("Vas a añadir " + String(cantidad) + " productos al carrito.");
    });
}
