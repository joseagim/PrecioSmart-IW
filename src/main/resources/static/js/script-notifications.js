
const notifList = document.querySelector("#notification-list");

/**
 * Crea una tarjeta de notificación con el mismo formato que Thymeleaf
 * @param {object} data - Datos de la notificación {nombre, supermarket, precio, tipo, estado}
 */
function createNotificationCard(data) {
    if (!notifList) return;

    const supermarket = data.supermarket || "Supermercado";
    const nombre = data.nombre || "Producto";
    const precio = data.precio ?? "-";

    // Mapear tipo y estado a texto amigable
    const tipoTexto = {
        'ADD': 'Tu solicitud de añadir',
        'ADD_IN_SUPER': 'Tu solicitud de añadir en',
        'MODIFY': 'Tu solicitud de modificar'
    }[data.tipo] || 'Tu solicitud sobre';

    const estadoTexto = {
        'APPROVED': 'ha sido aceptada',
        'REJECTED': 'ha sido rechazada',
        'PENDING': 'está pendiente'
    }[data.estado] || 'está pendiente';

    const badgeClass = {
        'APPROVED': 'bg-success',
        'REJECTED': 'bg-danger',
        'PENDING': 'bg-secondary'
    }[data.estado] || 'bg-secondary';

    // Mensaje principal
    const mensaje = data.tipo === 'ADD_IN_SUPER'
        ? `${tipoTexto} ${nombre} en ${supermarket} ${estadoTexto}`
        : `${tipoTexto} ${nombre} ${estadoTexto}`;

    // Crear tarjeta
    const card = document.createElement("div");
    card.className = "notification rounded-4 border bg-white p-3 mb-3 d-flex align-items-center gap-3";

    card.innerHTML = `
        <img src="/img/supermarkets/${String(supermarket).toLowerCase()}.png"
            alt="Logo de ${supermarket}"
            style="width: 56px; height: 56px; object-fit: contain;">

        <div class="flex-grow-1 text-start">
            <div class="d-flex justify-content-between align-items-start">
                <div>
                    <h3 class="fs-6 mb-1 fw-semibold">
                        ${mensaje}
                    </h3>

                    <div class="small text-muted">
                        <span>${precio} €</span>
                        &middot;
                        <span>${supermarket}</span>
                    </div>
                </div>

                <div>
                    <span class="badge ${badgeClass}">
                        ${data.estado === 'APPROVED' ? 'Aceptada' : (data.estado === 'REJECTED' ? 'Rechazada' : 'Pendiente')}
                    </span>
                </div>
            </div>
        </div>
    `;

    notifList.appendChild(card);
}

/**
 * Maneja notificaciones que llegan vía WebSocket
 */
document.addEventListener("DOMContentLoaded", () => {
    if (!notifList) return;

    // Guardar manejador anterior si existe
    const manejadorAnterior = ws.receive || (() => { });

    ws.receive = (m) => {
        manejadorAnterior(m);

        // Si es una notificación de solicitud
        if (m.tipo === "request") {
            console.log("Nueva notificación recibida:", m);

            // Ocultar mensaje de "no hay notificaciones" si existe
            const emptyMsg = notifList.querySelector(".text-center.text-muted");
            if (emptyMsg) {
                emptyMsg.style.display = "none";
            }

            // Crear y agregar la tarjeta
            createNotificationCard({
                nombre: m.nombre,
                supermarket: m.supermarket,
                precio: m.precio,
                tipo: m.requestType || m.subtipo || "ADD",
                estado: m.resultado === "aceptada" ? "APPROVED" : (m.resultado === "rechazada" ? "REJECTED" : "PENDING")
            });
        }
    }
});

const markAsReadForms = document.querySelectorAll(".mark-as-read-form");

markAsReadForms.forEach((f) => f.addEventListener("submit", handleMarkAsReadSubmit));

function handleMarkAsReadSubmit(e) {
    e.preventDefault();
    const form = e.target;
    const notificationId = form.querySelector('input[name="id"]').value;

    go(form.action, "POST", new URLSearchParams(new FormData(form)))
        .then((response) => {
            const notificationItem = form.closest('.notification');
            if (notificationItem) {
                notificationItem.remove();
            }
        })
        .catch((error) => {
            let message = `No se pudo aceptar la solicitud`;
            try {
                const json = JSON.parse(error?.text || "{}");
                if (json.message) {
                    message = json.message;
                }
            } catch (_) {
                message = "Failed parsing json at notification";
            }
            console.log(message);
        }
        )
}