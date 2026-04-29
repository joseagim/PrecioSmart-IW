
const classNameNotifCards = "notification d-flex flex-column p-4 mt-3 mb-3";
const notifList = document.querySelector("#notification-list");


function createRejectCard(name,description,date){
const rejectCard=document.createElement("div");
rejectCard.className=classNameNotifCards;
rejectCard.innerHTML=`<div class="d-flex flex-row justify-content-center align-items-center text-center">
                            <h4 class="mb-0 d-flex flex-row">
                                <b class="d-flex flex-row">Solicitud <h4 class="ms-2" style="color: red;">rechazada</h4></b>
                            </h4>
                        </div>

                        <div class="d-flex justify-content-start align-items-start mt-4">
                            <ul class="list-unstyled d-flex flex-column align-items-start gap-3">
                                <li class="pb-2">
                                    <h5 class="mb-0">
                                        <b class="text-black">Nombre: </b>
                                        <span>${name}</span>
                                    </h5>
                                </li>

                                <li class="pb-2">
                                    <h5 class="mb-0">
                                        <b class="text-black">Fecha: </b>
                                        <span>${date}</span>
                                    </h5>
                                </li>

                                <li>
                                    <h5 class="mb-0">
                                        <b class="text-black">Motivo: </b>
                                        <span>${description}</span>
                                    </h5>
                                </li>
                            </ul>
                        </div>`;
notifList.appendChild(rejectCard);
}

function createAcceptCard(name,date){
const acceptCard=document.createElement("div");
acceptCard.className=classNameNotifCards;
acceptCard.innerHTML=`<div class="d-flex flex-row justify-content-center align-items-center text-center">
                            <h4 class="mb-0 d-flex flex-row">
                                <b class="d-flex flex-row">Solicitud<h4 class="ms-2" style="color: green;">aceptada</h3></b>
                            </h4>
                        </div>

                        <div class="d-flex justify-content-start align-items-start mt-4">
                            <ul class="list-unstyled d-flex flex-column align-items-start gap-3">
                                <li class="pb-2">
                                    <h5 class="mb-0">
                                        <b class="text-black">Nombre: </b>
                                        <span>${name}</span>
                                    </h5>
                                </li>

                                <li class="pb-2">
                                    <h5 class="mb-0">
                                        <b class="text-black">Fecha: </b>
                                        <span>${date}</span>
                                    </h5>
                                </li>
                            </ul>
                        </div>`;
notifList.appendChild(acceptCard);
}

document.addEventListener("DOMContentLoaded", () => {
    const manejadorAnterior = ws.receive;
    ws.receive = (m) => {
        manejadorAnterior(m);
        if (m.tipo === "request") {
            console.log("esto funciona");
            if(m.resultado=="aceptada"){
                createAcceptCard(m.nombre,m.fecha);
            }else{
                createRejectCard(m.nombre,m.motivo,m.fecha);
            }
        }
    }
});
