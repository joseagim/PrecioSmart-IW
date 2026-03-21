const form = document.getElementById("request-form");
const errorBox = document.getElementById("error-box");

function isValidEAN(value) {
    return /^\d{8,13}$/.test(value);
}

form.addEventListener("submit", function (e) {
    e.preventDefault();

    const name = document.getElementById("name").value.trim();
    const brand = document.getElementById("brand").value.trim();
    const price = Number(document.getElementById("price").value);
    const supermarket = document.getElementById("supermarket").value;
    const ean = document.getElementById("ean").value.trim();
    const photoInput = document.getElementById("photo");
    const hasPhoto = photoInput.files && photoInput.files.length > 0;

    if (!name || !brand || !supermarket || !isValidEAN(ean) || Number.isNaN(price) || price <= 0 || !hasPhoto) {
        errorBox.classList.remove("d-none");
        return;
    }

    errorBox.classList.add("d-none");

    const formData = new FormData();
    formData.append("name", name);
    formData.append("brand", brand);
    formData.append("price", price);
    formData.append("supermarket", supermarket);
    formData.append("ean", ean);
    formData.append("photo", photoInput.files[0]);

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