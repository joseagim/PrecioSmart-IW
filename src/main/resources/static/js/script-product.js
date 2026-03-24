const ini = document.getElementById("cantidad");
if (ini) {
    ini.value = 1;
}

const botonCompra = document.querySelector("#buy-button");

if (botonCompra) {
    botonCompra.addEventListener("click", function() {
        const cantidad = parseInt(document.getElementById("cantidad").value) || 1;
        const productId = this.getAttribute("data-product-id");
        const url = `/user/cart/add/${productId}`;
        const method = "POST";

        // Usamos FormData para enviar el valor de cantidad y aprovechar CSRF hidden input
        const formData = new FormData();
        formData.append('cantidad', cantidad);

        // El helper go acepta FormData y un headers vacío para no forzar JSON
        go(url, method, formData, {} ).then(response => {
            // si el backend devuelve JSON con redirección o success
            if (response && response.redirect) {
                window.location.href = response.redirect;
            } else {
                // por defecto recargar la página para reflejar cambios en carrito
                window.location.reload();
            }
        }).catch(err => console.error('Error al añadir al carrito:', err));
    });
}



