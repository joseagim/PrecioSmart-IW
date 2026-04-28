# Flujo de WebSocket para Notificaciones sin Leer

## Descripción General

El sistema utiliza **WebSockets con STOMP** (Simple Text Oriented Messaging Protocol) para actualizar en tiempo real el contador de notificaciones sin leer en la barra de navegación. Cada vez que llega una notificación, se incrementa el badge rojo (`#nav-unread`).

---

## 1. Componente UI

### Ubicación
- **Archivo**: `src/main/resources/templates/fragments/nav.html`
- **Elemento**: 
```html
<span id="nav-unread" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-secondary">+99</span>
```

**Propósito**: Mostrar el número de notificaciones sin leer en la barra de navegación.

---

## 2. Inicialización del WebSocket (Cliente)

### Flujo de Inicialización

#### 2.1 Evento: `DOMContentLoaded`
**Archivo**: `src/main/resources/static/js/iw.js` (líneas 208-220)

Cuando la página carga completamente, se ejecuta:

```javascript
document.addEventListener("DOMContentLoaded", () => {
    if (config.socketUrl) {
        // Determinar qué tópicos suscribirse
        let subs = config.admin ? 
            ["/topic/admin", "/user/queue/updates"] : 
            ["/user/queue/updates"];
        
        // Agregar tópicos adicionales si existen
        if (config.topics && config.topics.length > 0) {
            subs = subs.concat(
                config.topics.split(",").map(t => `/topic/${t}`)
            );
        }
        
        // Inicializar WebSocket
        ws.initialize(config.socketUrl, subs);
        
        // Obtener contador inicial desde el servidor
        let p = document.querySelector("#nav-unread");
        if (p) {
            go(`${config.rootUrl}/user/unread`, "GET")
                .then(d => p.textContent = d.unread);
        }
    }
});
```

**Qué sucede**:
1. Se verifica si `config.socketUrl` existe (URL del servidor WebSocket)
2. Se prepara una lista de tópicos a los que suscribirse:
   - **Siempre**: `/user/queue/updates` - recibe mensajes privados
   - **Si es admin**: `/topic/admin` - recibe notificaciones administrativas
   - **Dinámicos**: Otros tópicos configurados en `config.topics`
3. Se inicializa la conexión WebSocket
4. Se hace una llamada GET a `/user/unread` para obtener el contador actual de mensajes sin leer

#### 2.2 GET `/user/unread` - Obtener Contador Inicial
**Archivo**: `src/main/java/es/ucm/fdi/iw/controller/UserController.java` (líneas 269-277)

```java
@GetMapping(path = "unread", produces = "application/json")
@ResponseBody
public String checkUnread(HttpSession session) {
    long userId = ((User) session.getAttribute("u")).getId();
    long unread = entityManager.createNamedQuery("Message.countUnread", Long.class)
        .setParameter("userId", userId)
        .getSingleResult();
    session.setAttribute("unread", unread);
    return "{\"unread\": " + unread + "}";
}
```

**Consulta SQL** (`Message.countUnread` en `Message.java`):
```sql
SELECT COUNT(m) FROM Message m 
WHERE m.recipient.id = :userId AND m.dateRead = null
```

**Retorna**: `{"unread": <número>}` - cuenta de mensajes sin leer

---

## 3. Configuración del WebSocket (Servidor)

### Configuración STOMP
**Archivo**: `src/main/java/es/ucm/fdi/iw/WebSocketConfig.java`

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint donde se conectan los clientes
        registry.addEndpoint("/ws").setAllowedOrigins("*");
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitar broker para tópicos y colas
        config.enableSimpleBroker("/topic", "/queue");
    }
}
```

**Detalles**:
- **Endpoint**: `/ws` - URL del servidor WebSocket
- **Broker**: Usa un broker simple que soporta:
  - `/topic/*` - Broadcast (todos los suscritos reciben)
  - `/queue/*` - Punto a punto (solo el usuario específico recibe)

---

## 4. Conexión y Suscripción (Cliente)

### 4.1 Función `ws.initialize()`
**Archivo**: `src/main/resources/static/js/iw.js` (líneas 29-51)

```javascript
ws.initialize: (endpoint, subs = []) => {
    try {
        // Crear cliente STOMP sobre WebSocket
        ws.stompClient = Stomp.client(endpoint);
        ws.stompClient.reconnect_delay = 2000;
        ws.stompClient.reconnect_callback = () => ws.retries-- > 0;
        
        // Conectar al servidor
        ws.stompClient.connect(ws.headers, () => {
            ws.connected = true;
            console.log('Connected to ', endpoint, ' - subscribing:');
            
            // Suscribirse a cada tópico
            while (subs.length != 0) {
                let sub = subs.pop();
                console.log(` ... to ${sub} ...`);
                ws.subscribe(sub);
            }
        });
    } catch (e) {
        console.log("Error, connection to WS '" + endpoint + "' FAILED: ", e);
    }
}
```

**Características**:
- Usa STOMP (librería `stomp.js`)
- Reintentos automáticos cada 2 segundos (máx 3 intentos)
- Incluye CSRF token en headers

### 4.2 Función `ws.subscribe()`
**Archivo**: `src/main/resources/static/js/iw.js` (líneas 53-63)

```javascript
ws.subscribe: (sub) => {
    try {
        ws.stompClient.subscribe(sub, (m) => {
            // Parsear JSON y llamar a receive()
            ws.receive(JSON.parse(m.body));
        });
        console.log("Hopefully subscribed to " + sub);
    } catch (e) {
        console.log("Error, could not subscribe to " + sub, e);
    }
}
```

**Flujo**:
1. Se suscribe al tópico usando STOMP
2. Cuando llega un mensaje, se parsea como JSON
3. Se pasa al manejador `ws.receive()`

---

## 5. Recepción y Actualización del Contador

### 5.1 Función `ws.receive()` - Manejador por Defecto
**Archivo**: `src/main/resources/static/js/iw.js` (líneas 15-23)

```javascript
ws.receive: (text) => {
    console.log(text);
    
    // Buscar el span de notificaciones sin leer
    let p = document.querySelector("#nav-unread");
    if (p) {
        // Incrementar el contador en 1
        p.textContent = +p.textContent + 1;
    }
}
```

**¿QUÉ SUCEDE?**:
- **Entrada**: Objeto JSON recibido del servidor
- **Acción**: Incrementa `+1` al texto del span `#nav-unread`
- **Conversión**: `+p.textContent` convierte el texto a número

**Ejemplo**:
```
Valor inicial: "5"
Recibe un mensaje → "6"
Recibe otro mensaje → "7"
```

### 5.2 Manejador Personalizado - Script de Notificaciones
**Archivo**: `src/main/resources/static/js/script-notifications.js` (líneas 29-39)

En la página de notificaciones se sobrescribe `ws.receive()`:

```javascript
// Guardar manejador anterior
const manejadorAnterior = ws.receive;

// Sobrescribir con comportamiento personalizado
ws.receive = (m) => {
    // Ejecutar manejador anterior (incrementa contador)
    manejadorAnterior(m);
    
    // Manejador personalizado
    if (m.tipo === "request") {
        console.log("peticion recibida correctamente");
        // Aquí iría lógica adicional para mostrar la notificación
    }
}
```

**Propósito**: Permite que diferentes páginas reaccionen de forma distinta a los mensajes.

---

## 6. Envío de Mensajes (Servidor)

### 6.1 Cuando se ACEPTA una Solicitud
**Archivo**: `src/main/java/es/ucm/fdi/iw/controller/AdminController.java` (líneas 208-213)

```java
// Cambiar estado de la solicitud a APROBADA
request.setStatus(RequestStatus.APPROVED);
entityManager.merge(request);

// Construir mensaje JSON
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(
    Map.of("tipo", "request", "resultado", "aceptada")
);

// ENVIAR por WebSocket
messagingTemplate.convertAndSend(
    "/user/" + request.getUser().getUsername() + "/queue/updates", 
    json
);
```

**Flujo**:
1. Se aprueba la solicitud en la BD
2. Se construye un objeto con tipo `"request"` y resultado `"aceptada"`
3. Se envía a `/user/{username}/queue/updates` (solo ese usuario lo recibe)

### 6.2 Cuando se ENVÍA un Mensaje
**Archivo**: `src/main/java/es/ucm/fdi/iw/controller/UserController.java` (líneas 310-324)

```java
// Crear mensaje
Message m = new Message();
m.setRecipient(u);
m.setSender(sender);
m.setDateSent(LocalDateTime.now());
m.setText(text);
entityManager.persist(m);

// Convertir a JSON
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(m.toTransfer());

// ENVIAR por WebSocket
messagingTemplate.convertAndSend(
    "/user/" + u.getUsername() + "/queue/updates", 
    json
);
```

**Mensaje enviado** (estructura JSON):
```json
{
  "from": "usuario1",
  "to": "usuario2",
  "sent": "2024-01-15T10:30:00",
  "received": null,
  "topic": "null",
  "text": "Contenido del mensaje",
  "id": 12345
}
```

---

## 7. ¿CUÁNDO SUMA Y CUÁNDO SE QUEDA A 0?

### 7.1 ¿CUÁNDO SUMA?

El contador **SUMA (+1)** cada vez que:

1. **Se recibe un mensaje privado** → WebSocket dispara `ws.receive()`
   - Alguien te envía un mensaje directo
   - Admin acepta/rechaza tu solicitud

2. **Se actualiza la página** → Se carga el contador inicial desde `/user/unread`
   - El span se inicializa con el número de mensajes sin leer en BD

### 7.2 ¿CUÁNDO SE QUEDA A 0 O DISMINUYE?

El contador **NO SE ACTUALIZA AUTOMÁTICAMENTE** a 0. Permanece en 0 cuando:

1. **Página cargada sin mensajes sin leer**
   - `/user/unread` devuelve `{"unread": 0}`
   - El span muestra "0"

2. **El usuario recarga la página**
   - Se vuelve a llamar a `/user/unread`
   - Se sincroniza con el servidor

3. **Si el usuario marca mensajes como leídos**
   - Habría que actualizar la BD (no visible en el código actual)
   - Luego recargar para sincronizar

### ¿Por qué no disminuye automáticamente?

**LIMITACIÓN ACTUAL**: El sistema no tiene un mecanismo para decrementar el contador cuando el usuario lee un mensaje. El contador solo sube y se sincroniza al recargar.

---

## 8. Arquitectura Completa - Diagrama de Flujo

```
┌─────────────────────────────────────────────────────────┐
│                   CLIENTE (Navegador)                    │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  1. DOMContentLoaded → ws.initialize(endpoint, subs)    │
│                                                           │
│  2. GET /user/unread → span#nav-unread = count          │
│                                                           │
│  3. STOMP.subscribe(["/user/queue/updates", ...])       │
│                                                           │
│  4. Esperar mensajes → ws.receive(JSON)                 │
│                                                           │
│  5. span#nav-unread++ (incrementar en 1)                │
│                                                           │
└─────────────────────────────────────────────────────────┘
                           ▲
                           │ WebSocket
                           │ STOMP Protocol
                           ▼
┌─────────────────────────────────────────────────────────┐
│               SERVIDOR (Spring Boot)                     │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  WebSocketConfig: Configura broker STOMP                │
│    - Endpoint: /ws                                       │
│    - Broker: /topic/*, /queue/*                          │
│                                                           │
│  UserController.checkUnread(): GET /user/unread         │
│    - Query: COUNT(*) FROM Message WHERE dateRead=null   │
│    - Retorna: {"unread": count}                          │
│                                                           │
│  AdminController.approveRequest():                       │
│    - convertAndSend("/user/{username}/queue/updates",   │
│                     JSON)                                │
│                                                           │
│  UserController.sendMessage():                           │
│    - convertAndSend("/user/{username}/queue/updates",   │
│                     JSON)                                │
│                                                           │
└─────────────────────────────────────────────────────────┘
                           ▲
                           │ HTTP / Database
                           ▼
┌─────────────────────────────────────────────────────────┐
│              BASE DE DATOS (PostgreSQL)                  │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  Tabla: message                                          │
│    - id, sender_id, recipient_id, text                  │
│    - date_sent, date_read (NULL = sin leer)             │
│                                                           │
│  Query NamedQuery: Message.countUnread                  │
│    - Cuenta filas con dateRead = null                   │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

---

## 9. Secuencia de Eventos Típica

### Caso 1: Usuario A recibe un mensaje de Usuario B

```
1. Usuario B envía mensaje
   ↓
2. UserController.sendMessage() crea Message en BD
   ↓
3. messagingTemplate.convertAndSend(
       "/user/usuarioA/queue/updates", 
       jsonMessage
   )
   ↓
4. Servidor STOMP envía JSON a usuarioA (si está conectado)
   ↓
5. Cliente: ws.receive(jsonMessage)
   ↓
6. Cliente: span#nav-unread.textContent++
   ↓
7. UI: El badge se incrementa en 1
   (visual: 5 → 6)
```

### Caso 2: Admin acepta una solicitud del Usuario A

```
1. Admin hace clic en "Aceptar solicitud"
   ↓
2. AdminController.approveRequest() 
   ↓
3. messagingTemplate.convertAndSend(
       "/user/usuarioA/queue/updates",
       {"tipo": "request", "resultado": "aceptada"}
   )
   ↓
4. Cliente: ws.receive({"tipo": "request", ...})
   ↓
5. Cliente: span#nav-unread.textContent++
   ↓
6. UI: El badge se incrementa (ej: 2 → 3)
   (Además, script-notifications.js detecta tipo="request")
```

### Caso 3: Usuario recarga la página

```
1. Usuario presiona F5
   ↓
2. HTML carga, ejecuta DOMContentLoaded
   ↓
3. ws.initialize() conecta WebSocket
   ↓
4. GET /user/unread devuelve {"unread": 8}
   ↓
5. span#nav-unread.textContent = 8
   ↓
6. UI: El badge muestra "8" (sincronizado con BD)
```

---

## 10. Configuración Requerida

### 10.1 Config de Cliente (JS)
Debe haber un objeto `config` global con:
- `config.socketUrl`: URL del endpoint WebSocket (ej: "ws://localhost:8080/ws")
- `config.rootUrl`: URL raíz de la aplicación (ej: "http://localhost:8080")
- `config.csrf.value`: Token CSRF para seguridad
- `config.admin`: Booleano indicando si es administrador
- `config.topics`: Tópicos adicionales (opcional)

### 10.2 Dependencias del Servidor
- Spring Boot WebSocket
- Spring Messaging (STOMP)
- Jackson (para serializar JSON)

### 10.3 Dependencias del Cliente
- `stomp.js`: Protocolo STOMP para WebSocket

---

## 11. Posibles Mejoras Futuras

1. **Decrementar contador**: Cuando se marque un mensaje como leído, decrementar el contador
2. **Categorías de notificaciones**: Diferentes contadores para diferentes tipos (mensajes, solicitudes, etc.)
3. **Sonido/Notificador**: Reproducir sonido o mostrar notificación del navegador
4. **Persistencia**: Guardar que el usuario vio la notificación en BD
5. **Typing indicator**: Mostrar cuando alguien está escribiendo
6. **Reacciones en tiempo real**: Emojis y reacciones en vivo

---

## 12. Resumen

| Aspecto | Valor |
|--------|-------|
| **Protocolo** | WebSocket + STOMP |
| **Endpoint Servidor** | `/ws` |
| **Tópicos Cliente** | `/user/queue/updates`, `/topic/admin` (opcional) |
| **Elemento UI** | `#nav-unread` (span en nav.html) |
| **Incremento** | Se suma +1 con cada mensaje recibido |
| **Inicialización** | GET `/user/unread` al cargar página |
| **Sincronización** | Al recargar página |
| **Evento Activador** | `DOMContentLoaded` |
| **Remitentes** | AdminController, UserController |
| **Reintentos** | Máx 3, cada 2 segundos |
