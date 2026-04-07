const form = document.getElementById("request-form");
const errorBox = document.getElementById("error-box");
const modeRadios = document.querySelectorAll('input[name="mode"]');

function setMode(mode) {
    const addOnly = document.querySelectorAll('.add-only');
    addOnly.forEach(el => {
        if (mode === 'add') {
            el.classList.remove('d-none');
        } else {
            el.classList.add('d-none');
        }
    });
}

modeRadios.forEach(r => r.addEventListener('change', e => setMode(e.target.value)));
// inicializar
if (modeRadios && modeRadios.length > 0) {
    const initial = Array.from(modeRadios).find(r => r.checked) || modeRadios[0];
    setMode(initial.value);
}

function isValidEAN(value) {
    return /^\d{8,13}$/.test(value);
}

form.addEventListener("submit", function (e) {
    e.preventDefault();

    const mode = document.querySelector('input[name="mode"]:checked').value;
    const name = document.getElementById("name").value.trim();
    const brand = document.getElementById("brand").value.trim();
    const price = Number(document.getElementById("price").value);
    const supermarket = document.getElementById("supermarket").value;
    const ean = document.getElementById("ean").value.trim();
    const photoInput = document.getElementById("photo");
    const hasPhoto = photoInput.files && photoInput.files.length > 0;

    // Validación por modo
    if (mode === 'add') {
        if (!name || !brand || !supermarket || !isValidEAN(ean) || Number.isNaN(price) || price <= 0 || !hasPhoto) {
            errorBox.classList.remove("d-none");
            return;
        }
    } else { // modify: solo ean, price, supermarket
        if (!supermarket || !isValidEAN(ean) || Number.isNaN(price) || price <= 0) {
            errorBox.classList.remove("d-none");
            return;
        }
    }

    errorBox.classList.add("d-none");

    const formData = new FormData();
    formData.append("mode", mode);
    if (mode === 'add') {
        formData.append("name", name);
        formData.append("brand", brand);
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
            if (!response.ok) {
                throw new Error("Network response was not ok");
            }
            return response.json();
        })
        .then(data => {
            if (data.status !== "ok") {
                throw new Error(data.message || "No se pudo guardar la solicitud");
            }
            window.location.href = "/user/request?success=true";
        })
        .catch(error => {
            console.error("Error:", error);
            errorBox.classList.remove("d-none");
        });
});