const form = document.getElementById("request-form");
const errorBox = document.getElementById("error-box");
const successBox = document.getElementById("success-box");
const requestList = document.getElementById("request-list");
const typeRadios = document.querySelectorAll("input[name='type']");

function showMessage(box, message) {
    box.textContent = message;
    box.classList.remove("d-none");
}

function setMode(type) {

    const addOnly = document.querySelectorAll('.add-only');
    const isAdd = type === '0';
    addOnly.forEach(el => {
        if (isAdd) {
            el.classList.remove('d-none');
        } else {
            el.classList.add('d-none');
        }
    });

    const nameInput = document.getElementById("name");
    const brandInput = document.getElementById("brand");
    const quantityInput = document.getElementById("quantity");
    const photoInput = document.getElementById("photo");

    if (nameInput) nameInput.required = isAdd;
    if (brandInput) brandInput.required = isAdd;
    if (quantityInput) quantityInput.required = isAdd;
    if (photoInput) photoInput.required = isAdd;
}

typeRadios.forEach(r => r.addEventListener('change', e => setMode(e.target.value)));
setMode(document.querySelector("input[name='type']:checked")?.value || "0");



function isValidEAN(value) {
    return /^\d{8,13}$/.test(value);
}

form.addEventListener("submit", function (e) {
    e.preventDefault();


    const selectedType = document.querySelector("input[name='type']:checked");

    const name = document.querySelector("#name").value.trim();
    const brand = document.getElementById("brand").value.trim();
    const quantity = document.getElementById("quantity").value.trim();
    const price = Number(document.getElementById("price").value);
    const supermarket = document.getElementById("supermarket").value;
    const ean = document.getElementById("ean").value.trim();
    const photoInput = document.getElementById("photo");
    const hasPhoto = photoInput.files && photoInput.files.length > 0;

    // Validacion por tipo

    if (selectedType.value === "0") {
        if (!name || !brand || !quantity || !supermarket || !isValidEAN(ean) || Number.isNaN(price) || price <= 0 || !hasPhoto) {
            showMessage(errorBox, "Revisa los campos del formulario.");
            e.preventDefault();
            return;
        }
    } else { // modify: solo ean, price, supermarket
        if (!supermarket || !isValidEAN(ean) || Number.isNaN(price) || price <= 0) {
            showMessage(errorBox, "Revisa los campos del formulario.");
            e.preventDefault();
            return;
        }
    }

    errorBox.classList.add("d-none");
    successBox.classList.add("d-none");

    const formData = new FormData();
    formData.append("type", selectedType.value);
    if (selectedType.value === '0') {
        formData.append("name", name);
        formData.append("brand", brand);
        formData.append("quantity", quantity);
        if (hasPhoto) formData.append("photo", photoInput.files[0]);
    }
    formData.append("price", price);
    formData.append("supermarket", supermarket);
    formData.append("ean", ean);


    fetch("/user/request", {
        method: "POST",
        body: formData
    })

        .then(response => {
            return response.json()
                .then(data => ({ ok: response.ok, data }))
                .catch(() => ({ ok: response.ok, data: { status: "error", message: "Respuesta no valida del servidor" } }));
        })
        .then(({ ok, data }) => {
            if (!ok || data.status !== "ok") {
                showMessage(errorBox, data.message || "Revisa los campos del formulario.");
                return;
            }

            window.location.replace("/user/request?success=true");
           
        })
        .catch(error => {
            console.error("Error:", error);
            showMessage(errorBox, error.message || "Revisa los campos del formulario.");
        });

});