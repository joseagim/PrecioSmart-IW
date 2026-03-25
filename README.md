# PrecioSmart - Plataforma Inteligente de Comparación de Precios

**PrecioSmart** es una aplicación web diseñada para ayudar a optimizar su gasto en supermercados mediante la comparación de precios y características de productos de supermercado.

Utiliza el código de barras (EAN) como identificador único para garantizar búsquedas precisas y comparaciones justas.

[Enlace al sitio web](https://vm039.containers.fdi.ucm.es/)

(Necesario conectarse a la VPN de la UCM)
## 🚀 Funcionalidades principales

* **Identificación de productos**: Uso del código de barras (EAN) para localizar productos de forma rápida y sin errores.
* **Consulta accesible**: Búsqueda mediante escaneo con la cámara del dispositivo móvil o introducción manual del código numérico.
* **Análisis comparativo**: Visualización de precios en distintos supermercados y comparaciones según precio y otros filtros como categorías de los productos.
* **Carrito inteligente**: Gestión de cestas de la compra que permiten mezclar productos de distintas cadenas para que se pueda ver en qué supermercado sale más barato comprar todo el carrito.
* **Actualización de datos**: Los usuarios pueden solicitar agregar nuevos productos o modificar los ya existentes con la intención de actualizar el precio si este ha cambiado.


## 💻 Vistas de la aplicación

La aplicación dispone de las siguientes interfaces de usuario:

1.  **Vista principal**: Página de inicio con información sobre la aplicación y acceso directo a las herramientas de búsqueda y escaneo.
2.  **Vista de búsqueda**: Listado de productos filtrados según lo que escriba el usuario en la barra de búsqueda.
3.  **Vista de productos**: Ficha detallada de cada producto con marca, cantidad, precios en distintas cadenas y productos similares.
4.  **Vista de carritos**: Gestión de varios carritos con nombres personalizados. Permite comparar cuánto costaría la misma lista de la compra en los distintos supermercados que recoge la aplicación.
5.  **Vista de solicitudes**: Formulario para que los usuarios reporten datos erróneos o soliciten añadir productos que no están en la base de datos.
6.  **Vista de notificaciones**: Panel donde el usuario recibe avisos sobre el estado de sus solicitudes (aceptado o rechazado) por parte de la administración.
7.  **Vista de administrador**: Panel exclusivo para la gestión de la plataforma, permitiendo validar o rechazar las solicitudes de los usuarios para mantener la base de datos actualizada.
8.  **Vista de registro**: Formulario para crear una nueva cuenta de usuario.
9.  **Vista de login**: Pantalla de inicio de sesión para acceder a la cuenta.
10. **Vista de autores**: Página con información del equipo de desarrollo.
11. **Vista de FAQ**: Sección de preguntas frecuentes y respuestas rápidas.
12. **Vista de usuario**: Área personal con perfil y mensajería.


# PrecioSmart - Estado de implementación de vistas

Resumen sencillo

- Vista principal (`index`): implementada. Ya muestra la portada y acceso a funciones principales.
- Vista de busqueda (`search`): implementada en gran parte. Permite buscar productos, falta impeplementar la busqueda por foto de EAN.
- Vista de producto (`product`): implementada. Muestra informacion del producto y precios por cada supermercado.
- Vista de carritos (`cart`): implementada en gran parte. Permite crear carritos y gestionar productos; falta mostrar el precio total del carrito por cada supermercado.
- Vista de solicitudes (`request`): implementada a medias. La vista esta creada, pero falta gestionar la peticion POST y guardarla en la base de datos.
- Vista de notificaciones (`notifications`): sin implementar. La vista es un boceto de lo que seria.
- Vista de administrador (`admin`): sin implementar. La vista es un boceto de lo que seria.
- Vista de registro (`register`): implementada. Permite crear una cuenta.
- Vista de login (`login`): implementada y funcional. Permite iniciar sesion correctamente.
- Vista de autores (`authors`): implementada. Muestra informacion del equipo.
- Vista de FAQ (`faq`): implementada. Incluye preguntas frecuentes (Se irá ampliando).
- Vista de usuario (`user`): implementada. Incluye perfil y mensajeria.


