const form = document.getElementById("request-form");
const errorBox = document.getElementById("error-box");
const typeRadios = document.querySelectorAll("input[name='type']");


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
    const photoInput = document.getElementById("photo");

    if (nameInput) nameInput.required = isAdd;
    if (brandInput) brandInput.required = isAdd;
    if (photoInput) photoInput.required = isAdd;
}

typeRadios.forEach(r => r.addEventListener('change', e => setMode(e.target.value)));


function isValidEAN(value) {
    return /^\d{8,13}$/.test(value);
}

form.addEventListener("submit", function (e) {
    e.preventDefault();


    const selectedType = document.querySelector("input[name='type']:checked");

  
    
    const name = document.getElementById("name").value.trim();
    const brand = document.getElementById("brand").value.trim();
    const price = Number(document.getElementById("price").value);
    const supermarket = document.getElementById("supermarket").value;
    const ean = document.getElementById("ean").value.trim();
    const photoInput = document.getElementById("photo");
    const hasPhoto = photoInput.files && photoInput.files.length > 0;

    // Validacion por tipo

    if (selectedType.value === "0") {
        if (!name || !brand || !supermarket || !isValidEAN(ean) || Number.isNaN(price) || price <= 0 || !hasPhoto) {
            errorBox.classList.remove("d-none");
            e.preventDefault();
            return;
        }
    } else { // modify: solo ean, price, supermarket
        if (!supermarket || !isValidEAN(ean) || Number.isNaN(price) || price <= 0) {
            errorBox.classList.remove("d-none");
            e.preventDefault();
            return;
        }
    }

    errorBox.classList.add("d-none");

    const formData = new FormData();
    formData.append("type", selectedType.value);
    if (selectedType.value === '0') {
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