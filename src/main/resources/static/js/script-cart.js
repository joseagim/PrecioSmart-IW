function actualizarCantidad(cartId, itemId, action) {
    const token = document.querySelector('input[name="_csrf"]').value;
    const header = "X-CSRF-TOKEN";

    const formData = new FormData();
    formData.append("cartId", cartId);
    formData.append("action", action);

    if (itemId !== null && itemId !== undefined) {
        formData.append("itemId", itemId);
    }

    fetch("/user/cart/update", {
        method: "POST",
        headers: {
            [header]: token
        },
        body: formData
    })
    .then(response => {
        if (!response.ok) throw new Error('Error en la red');
        return response.text(); 
    })
    .then(htmlFragment => {
        document.getElementById("tablaProductos").innerHTML = htmlFragment;
    })
    .catch(error => console.error('Error:', error));
}