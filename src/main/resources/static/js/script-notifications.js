/*
const manejadorAnterior = ws.receive
ws.receive = (m) => {
    manejadorAnterior(m);

    if (m.tipo === "request") {
        console.log("peticion recibida correctamente");
    }
}
*/
const markAsReadForms = document.querySelectorAll(".mark-as-read-form");

markAsReadForms.forEach((f) => f.addEventListener("submit", handleMarkAsReadSubmit));

function handleMarkAsReadSubmit(e) {
    e.preventDefault();
    const form = e.target;

    go(form.action, "POST", new URLSearchParams(new FormData(form)))
        .then(response => {
            console.log("Has leido la noti");
            const notificationItem = form.closest('.notification-item');
            console.log(notificationItem);
            // eliminar el div de la noti y restar 1 al contador de no leídas
            if (notificationItem) {
                notificationItem.remove();
                let p = document.querySelector(".notification-item");

                if (!p || p == null) {
                    p = document.getElementById("error-box");
                    p.classList.remove("d-none")
                }
                p = document.querySelector("#nav-unread");
                if (p && p.textContent > 0) {
                    p.textContent = p.textContent - 1;
                }
            }
        })
        .catch((error) => {
            let message = `No se pudo aceptar la solicitud`;
            try {
                const json = JSON.parse(error.text || "{}");
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