# PrecioSmart - Plataforma Inteligente de Comparación de Precios

**PrecioSmart** es una aplicación web diseñada para ayudar a optimizar su gasto en supermercados mediante la comparación de precios y características de productos de supermercado.

Utiliza el código de barras (EAN) como identificador único para garantizar búsquedas precisas y comparaciones justas.



## 🚀 Funcionalidades principales

* **Identificación de productos**: Uso del código de barras (EAN) para localizar productos de forma rápida y sin errores.
* **Consulta accesible**: Búsqueda mediante escaneo con la cámara del dispositivo móvil o introducción manual del código numérico.
* **Análisis comparativo**: Visualización de precios en distintos supermercados y comparaciones según precio y otros filtros como macros de los productos.
* **Carrito inteligente**: Gestión de cestas de la compra que permiten mezclar productos de distintas cadenas para que se pueda ver en qué supermercado sale más barato comprar todo el carrito.
* **Actualización de datos**: Los usuarios pueden csolicitar agregar nuevos productos o modificar los ya existentes con la intención de actualizar el precio si este ha cambiado.



## 💻 Vistas de la aplicación

La aplicación dispone de las siguientes interfaces de usuario:

1.  **Vista principal**: Página de inicio con información sobre la aplicación y acceso directo a las herramientas de búsqueda y escaneo.
2.  **Vista de búsqueda**: Listado de productos filtrados según lo que escriba el usuario en la barra de búsqueda.
3.  **Vista de productos**: Ficha detallada de cada producto con información nutricional, precios en distintas cadenas y productos similares.
4.  **Vista de carritos**: Gestión de varios carritos con nombres personalizados. Permite comparar cuánto costaría la misma lista de la compra en los distintos supermercados que recoge la aplicación.
5.  **Vista de solicitudes**: Formulario para que los usuarios reporten datos erróneos o soliciten añadir productos que no están en la base de datos.
6.  **Vista de notificaciones**: Panel donde el usuario recibe avisos sobre el estado de sus solicitudes (aceptado o rechazado) por parte de la administración.
7.  **Vista de administrador**: Panel exclusivo para la gestión de la plataforma, permitiendo validar o rechazar las solicitudes de los usuarios para mantener la base de datos actualizada.



# PrecioSmart - Estado de implementación de vistas

Este documento describe brevemente el grado de implementación de las vistas de la aplicación y qué falta por completar. Se pone especial atención a las vistas ejercitadas en las pruebas externas (`src/test/java/external/*`).

Resumen rápido

- `login` (vista: `templates/login.html`): Completa y funcional. Formulario de login existente y mostrado. Pruebas externas (`login.feature`) usan `/login` y la redirección tras autenticación funciona.

- `cart` (vista: `templates/cart.html` y JS: `static/js/script-cart.js`): Implementada en su mayor parte. Soporta:
  - Listado de carritos del usuario.
  - Crear nuevo carrito (`/user/cart` -> acción `newCart`).
  - Selección de carrito, edición de nombre (modo edición), eliminación de carrito.
  - Gestión de productos dentro del carrito (sumar, restar, borrar, vaciar) mediante endpoint `/user/cart/update` y fragmentos Thymeleaf (`cart :: #tablaProductos`) para actualización via fetch.
  - Pruebas externas en `src/test/java/external/cart.feature` comprueban: crear, renombrar, eliminar carritos; navegar dentro de carrito y operaciones sobre productos. Estado: mayoritariamente implementado, pendientes y verificación:
    * Asegurar que CSRF token está presente en el DOM cuando la prueba usa `submit()` o `fetch`.
    * Confirmar que las respuestas parciales devuelvan exactamente el fragmento esperado (id `tablaProductos`) para las actualizaciones AJAX.
    * Revisar tiempos de espera en pruebas (ya se usan delays en el feature).

- `ws` (mensajería via WebSocket — vista: `templates/user.html`, JS: `static/js/stomp.js` / `static/js/script-notifications.js`): Implementado parcialmente.
  - Envío y recepción de mensajes con WebSocket configurado en `WebSocketConfig` y plantillas que muestran mensajes.
  - Pruebas externas `src/test/java/external/ws.feature` requieren que el flujo de envío y aparición de mensajes via AJAX/WebSocket funcione entre dos usuarios (login_a y login_b). Estado: funcional en el controlador y configuración, posible flakiness por tiempos; verificar servidor en ejecución y la carga del cliente websocket en la página.

Otras vistas relevantes

- `product` (`templates/product.html`, `static/js/script-product.js`): Ficha de producto implementada con lista de precios por supermercado. Revisión importante: Thymeleaf no soporta el operador `?.`; se reemplaza por comprobaciones null (`product != null ? product.name : '...'`). Asegurar plantilla sin errores de parsing.

- `user` (`templates/user.html`): Perfil de usuario y envío/recepción de mensajes; implementada.

- `search`, `request`, `notifications`, `admin`, `register`, `authors`, `faq`, `index`: Vistas existentes con markup y muchas funcionalidades implementadas. Algunas funciones administrativas y de datos pueden depender de controladores/back-end adicionales.

Qué falta / acciones recomendadas para pasar las pruebas externas

1. Revisión y pruebas locales de CSRF y formularios
   - Confirmar que las plantillas renderizan el input CSRF (`<input type="hidden" name="_csrf" ...>`) cuando se usa `fetch` o `submit()` en tests.
   - Asegurar que `script-cart.js` obtiene el token usando `input[name="_csrf"]` (ya implementado). Confirmar que existe en la página antes de ejecutar fetch.

2. Fragmentos y selectores de actualización
   - Verificar que el endpoint `/user/cart/update` devuelve exactamente el fragmento `cart :: #tablaProductos` (ya implementado) y que el HTML resultante reemplace el contenedor correcto en el DOM.
   - Asegurar que el elemento con id `tablaProductos` existe en las respuestas y en la plantilla principal.

3. Robustez en WebSocket/tests de mensajería
   - Añadir delays si las pruebas fallan por sincronización. Revisar carga de `stomp.js` y config del `ws` en `fragments/nav.html` o en la plantilla `user.html`.

4. Manejo de errores en plantillas
   - Evitar operadores no soportados en Thymeleaf (por ejemplo `?.`). Usar ternarias o comprobaciones `th:if` para evitar errores 500.
   - Revisar cualquier plantilla que haya dado `TemplateInputException` y corregir expresiones invalidas.

5. Asegurar que los endpoints protegidos requieran login y que las pruebas externas invocan `login.feature@login_b` o `@login_a` adecuadamente.

Prioridades (centrarse en pruebas externas)

- Prioridad alta:
  - `cart` — asegurar que crear/renombrar/eliminar y operaciones sobre productos funcionan de forma estable con CSRF y fragmentos (esto cubre la mayor parte de `cart.feature`).
  - `login` — verificar redirecciones y mensajes de error en login (ya funcional, testear casos positivos/negativos).
  - `ws` — asegurar mensajería entre usuarios (sincronización y carga del cliente websocket).

- Prioridad media:
  - `product` — corregir cualquier Thymeleaf malformado y asegurar que la selección de carritos en la ficha de producto funcione (si se usa en tests).

- Prioridad baja:
  - Resto de vistas administrativas y visuales.

Si quieres, actualizo este README con checklist más detallado (tareas por fichero y líneas) y creo issues o TODOs en código para los puntos pendientes. ¿Lo dejo así o quieres que cree la lista de tareas detallada?
