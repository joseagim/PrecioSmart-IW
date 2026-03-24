const form = document.getElementById("add-to-cart-form");
const registroFeedback = document.getElementById("registroFeedback");
const buyButton = document.getElementById("buy-button");

if (form) {
    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const formData = new FormData(form);
    
        const url = form.getAttribute("action");
        const method = (form.getAttribute("method") || "POST").toUpperCase();

        if (buyButton) {
            buyButton.disabled = true;
        }

        go(url, method, formData, {})
            .then(response => {
                const json = typeof response === "string" ? JSON.parse(response) : response;

                if (!registroFeedback) {
                    return;
                }

                if (json && json.error) {
                    registroFeedback.querySelector("p").textContent = json.error;
                    registroFeedback.classList.remove("d-none", "alert-success");
                    registroFeedback.classList.add("alert-danger");
                } else {
                    const successMessage = (json && json.success) ? json.success : "Producto anadido correctamente";
                   
                    registroFeedback.querySelector("p").textContent = successMessage;
                    registroFeedback.classList.remove("d-none", "alert-danger");
                    registroFeedback.classList.add("alert-success");
                }
            })
            .catch(() => {
                if (!registroFeedback) {
                    return;
                }
                registroFeedback.querySelector("p").textContent = "Error al anadir el producto";
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






