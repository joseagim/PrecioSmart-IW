const notificationsList = document.querySelector("#notification-list");
const btnAceptar = document.querySelector("#btn-aceptar");
const btnRechazar = document.querySelector("#btn-rechazar");

const className = "border border-1 rounded-3 mt-3 mb-3 text-start notification";

let numSolicitudes = 1;

if (btnAceptar) {
    btnAceptar.addEventListener("click", () => {
        const div = document.createElement("div");
        div.className = className;
        div.innerHTML = `
            <div>
                <p>✅ Tu solicitud (#${numSolicitudes}) ha sido aceptada</p>
            </div>
        `;
        notificationsList.appendChild(div);
        numSolicitudes++;
    });
}

if (btnRechazar) {
    btnRechazar.addEventListener("click", () => {
        const div = document.createElement("div");
        div.className = className;
        div.innerHTML = `
            <div>
                <p>❌ Tu solicitud (#${numSolicitudes}) ha sido rechazada</p>
            </div>
        `;
        notificationsList.appendChild(div);
        numSolicitudes++;
    });
}
