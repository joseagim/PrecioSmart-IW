Feature: flujo representativo de PrecioSmart

    Background:
        # Punto de entrada comun para escenarios de UI en local
        * def appUrl = baseUrl

    Scenario: registro fallido por campos vacios
        # Verifica que el usuario no pueda registrarse si no completa los datos minimos
        Given driver appUrl + '/register'
        When submit().click(".form-signin button")
        Then match html('body') contains 'error'

    Scenario: acceso correcto de admin al panel
        # Reutiliza el login etiquetado para validar el flujo principal de administracion
        Given call read('classpath:external/login.feature@login_a')
        Then waitForUrl(appUrl + '/admin')
        And match html('h1, h2, body') contains 'admin'

    Scenario: usuario inicia sesion y consulta producto
        # Simula el recorrido principal de un comprador: login y visita de producto
        Given call read('classpath:external/login.feature@login_b')
        And driver appUrl + '/search'
        And input('input[name=q], #search, input[type=search]', 'leche')
        When submit().click('form button, .search button')
        Then match html('body') contains 'product'

    Scenario: comprador anade producto al carrito
        # Ejercita una accion clave de negocio: anadir un articulo al carrito
        Given call read('classpath:external/login.feature@login_b')
        And driver appUrl + '/product/1'
        When click('button#add-to-cart, button[name=add], .add-cart')
        And driver appUrl + '/cart'
        Then match html('body') contains 'cart'

    Scenario: mensajes entre usuarios con flujo externo
        # Reutiliza un escenario externo completo 
        Given call read('classpath:external/ws.feature')
