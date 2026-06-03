# PrecioSmart - Plataforma Inteligente de Comparación de Precios

**PrecioSmart** es una aplicación web diseñada para ayudar a optimizar su gasto en supermercados mediante la comparación de precios y características de productos de supermercado.

Utiliza el código de barras (EAN) como identificador único para garantizar búsquedas precisas y comparaciones justas.

[Enlace al sitio web](https://vm039.containers.fdi.ucm.es/)

(Necesario conectarse a la VPN de la UCM)
## 🚀 Funcionalidades principales

* **Identificación de productos**: Uso del código de barras (EAN) para localizar productos de forma rápida y sin errores.

> Todos los **productos** que tienen **foto** pueden ser **escaneados** mediante su **EAN**. Estos productos son Leche Semidesnatada, Leche Entera, Arroz Integral, Pasta Plumas, Tomate Frito, Aceite de Oliva, etc. Incluyendo algunas marcas distintas de estos productos. El producto que más variedad tiene de códigos EAN reales implementados por el momento es Leche Semidesnatada y Leche Entera, contando con un total de 5 códigos EAN diferentes para probar. El resto de códigos EAN son autogenerados por IA a la hora de meter productos de relleno en la app.

* **Consulta accesible**: Búsqueda mediante escaneo con la cámara del dispositivo móvil, por introducción manual del código numérico o con el nombre del producto.
* **Análisis comparativo**: Visualización de precios en distintos supermercados y comparaciones según precio.
* **Carrito inteligente**: Gestión de cestas de la compra que permiten mezclar productos de distintas cadenas para que se pueda ver en qué supermercado sale más barato comprar todo el carrito.
* **Actualización de datos**: Los usuarios pueden solicitar agregar nuevos productos o modificar los ya existentes con la intención de actualizar el precio si este ha cambiado, pueden ver su historial de solicitudes realizadas y si lo desean 
ocultarlas.
* **Notificaciones**: Los usuarios reciben notificaciones cuando un administrador les acepta o rechaza una solicitud.
* **Panel de administración**: Los administradores pueden crear, editar y eliminar supermercados, editar productos y sus precios, y banear o desbanear usuarios desde la interfaz de administración.

## Credenciales de prueba (base de datos)

Los usuarios iniciales se cargan desde `src/main/resources/import.sql`.

- Usuario administrador: `a`  — contraseña: `aa`  (roles: ADMIN,USER)
- Usuario normal: `b`  — contraseña: `aa`  (roles: USER)

## Recursos externos / librerías notables 

- ZXing (com.google.zxing) — lectura de códigos de barras (EAN)

Librerias usadas:

- com.google.zxing.client.j2se.BufferedImageLuminanceSource; Pasa la imagen a escala de grises
- com.google.zxing.common.HybridBinarizer; Convierte la imagen en binario (blanco/negro)
- org.springframework.stereotype.Service; Marca la clase como un componente de servicio

## Uso de IA

- Para realizar partes del frontend, como degradados, cards con estilo, etc
- Para rellenar la vista FAQ con dudas habituales de los usuarios de la aplicación.
- Para poblar la base de datos de la aplicación.
- Para realizar la conexión con la API ZXing de google para la lectura automática de los códigos EAN en las imágenes envía el usuario para la búsqueda rápida de productos.
- Para resolver dudas sobre los frameworks y librerias utilizadas.
- Código para mover una foto de una carpeta a otra, la función copyImageToProduct en adminController.
- Funcion en search.html de navegación entre páginas

## 💻 Vistas de la aplicación

La aplicación dispone de las siguientes interfaces de usuario:

1.  **Vista principal**: Página de inicio con información sobre la aplicación y acceso directo a las herramientas de búsqueda y escaneo.
2.  **Vista de búsqueda**: Listado de productos filtrados según lo que escriba el usuario en la barra de búsqueda, con paginación.
3.  **Vista de productos**: Ficha detallada de cada producto con marca, cantidad, precios en distintas cadenas y productos similares.
4.  **Vista de carritos**: Gestión de varios carritos con nombres personalizados. Permite comparar cuánto costaría la misma lista de la compra en los distintos supermercados que recoge la aplicación.
5.  **Vista de solicitudes**: Formulario para que los usuarios o soliciten añadir productos que no están en la base de datos o modificar el precio.
6.  **Vista de notificaciones**: Panel donde el usuario recibe avisos sobre el estado de sus solicitudes (aceptado o rechazado) por parte de la administración.
7.  **Vista de administrador**: Panel exclusivo para la gestión de la plataforma, permitiendo validar o rechazar solicitudes, administrar supermercados, editar productos, ajustar precios y banear o desbanear usuarios para mantener la base de datos actualizada.
8.  **Vista de registro**: Formulario para crear una nueva cuenta de usuario.
9.  **Vista de login**: Pantalla de inicio de sesión para acceder a la cuenta.
10. **Vista de autores**: Página con información del equipo de desarrollo.
11. **Vista de FAQ**: Sección de preguntas frecuentes y respuestas rápidas.
12. **Vista de usuario**: Área personal con datos del perfil y con opción de cambiar la foto de perfil.


