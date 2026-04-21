const form = document.getElementById("add-to-cart-form");
const registroFeedback = document.getElementById("registroFeedback");
const buyButton = document.getElementById("buy-button");

if (form) {
    form.addEventListener("submit", function (e) {
        e.preventDefault();

        // If there is no cart selector, this submit creates the first cart.
        // Reload after success so Thymeleaf renders the new carts list and select.
        const hadNoCartSelector = !document.getElementById("cartId");

        const formData = new FormData(form);

        if (buyButton) {
            buyButton.disabled = true;
        }

        go(form.action, "POST", formData)
            .then(response => {
                const successMessage = response.success || "Producto anadido correctamente";
                registroFeedback.querySelector("p").textContent = successMessage;
                registroFeedback.classList.remove("d-none", "alert-danger");
                registroFeedback.classList.add("alert-success");

                if (hadNoCartSelector) {
                    window.location.reload();
                }
            })
            .catch(error => {
                const json = JSON.parse(error.text);

                registroFeedback.querySelector("p").textContent = json.error || "Error al anadir el producto";
                registroFeedback.classList.remove("d-none", "alert-success");
                registroFeedback.classList.add("alert-danger");
            })
            .finally(() => {
                if (buyButton) {
                    buyButton.disabled = false;
                }
            });
    });
}






