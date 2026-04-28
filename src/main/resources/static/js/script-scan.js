// Añadimos el parámetro 'inputElement'
function subirYEscanear(inputElement) {
    const loading = document.getElementById('loading');
    
    // Si no nos pasan el input (por si acaso), lo buscamos, pero ahora usamos inputElement
    const fileInput = inputElement;
    
    if (!fileInput || fileInput.files.length === 0) return;

    const file = fileInput.files[0];
    loading.style.display = 'block';

    const img = new Image();
    const objectUrl = URL.createObjectURL(file);
    
    img.onload = function() {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        const MAX_WIDTH = 800;
        let width = img.width;
        let height = img.height;

        if (width > MAX_WIDTH) {
            height *= MAX_WIDTH / width;
            width = MAX_WIDTH;
        }

        canvas.width = width;
        canvas.height = height;
        ctx.drawImage(img, 0, 0, width, height);

        canvas.toBlob((blob) => {
            const formData = new FormData();
            formData.append('imagen', blob, 'scan.jpg');

            fetch('/api/lector/leer', {
                method: 'POST',
                body: formData
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { 
                        throw new Error(err.error || 'No se pudo reconocer el código'); 
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data.redirectUrl) {
                    window.location.href = data.redirectUrl;
                }
            })
            .catch(error => {
                alert("Error: " + error.message);
                console.error(error);
            })
            .finally(() => {
                loading.style.display = 'none';
                fileInput.value = ''; // Limpiamos el input que disparó el evento
                URL.revokeObjectURL(objectUrl);
            });
        }, 'image/jpeg', 0.7);
    };

    img.src = objectUrl;
}