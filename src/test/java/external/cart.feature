Feature: Pruebas UI de la gestión de carritos

  Background:
    # Asegurar sesión iniciada antes de probar las rutas protegidas del carrito
    Given call read('login.feature@login_b')

  Scenario: Ciclo de vida de un carrito (Crear, Renombrar, Eliminar)
    
    # 1. Navegar a la vista principal de carritos
    Given driver baseUrl + '/user/cart'
    
    # 2. Crear un nuevo carrito
    When submit().click("button[value=newCart]")
    Then delay(500)
    # Verificamos que el carrito por defecto se ha renderizado
    And match html('body') contains 'Nuevo Carrito'

    # 3. Habilitar la edición del nombre del carrito
    # El botón de editar intercepta el nombre 'editCart' y recarga la página con el input habilitado
    When submit().click("button[name=editCart]")
    Then delay(500)

    # 4. Modificar el nombre y guardar
    And clear("input[name=renameCart]")
    And input("input[name=renameCart]", 'Carrito Automatizado Karate')
    When submit().click("button[value=renameCart]")
    Then delay(1000)
    # Verificamos la persistencia del nuevo nombre en la vista
    And match html('body') contains 'Carrito Automatizado Karate'

    # 5. Navegar a la vista búsqueda
    Given driver baseUrl + '/search'
    #click en el primer producto
    And waitFor('.btn-comparar')
    When click('.btn-comparar')

    #6. añadir al nuevo carrito
    And waitFor('#cartId')
    When select('#cartId', '{}Carrito Automatizado Karate')
    Then delay(1000)
    And click('button[id=buy-button]')

    # 7. Navegar a la vista carrito
    Given driver baseUrl + '/user/cart'
    # 7.1 Entrar en el carrito
    When submit().click("button[name=selectCart]")
    Then delay(500)
    # Verificamos que la tabla de productos se ha renderizado
    And match html('body') contains 'Leche Semidesnatada'

    # 8.1 Modificar cantidad: Sumar una unidad al primer producto
    When click("button[name=sumaProd]")
    # Aplicamos un retraso ligeramente mayor para asegurar que la petición AJAX/fetch complete el reemplazo del fragmento DOM
    Then delay(1000)

    # 8.2 Modificar cantidad: Restar una unidad al primer producto
    When click("button[name=restaProd]")
    Then delay(1000)

    # 8.3 Eliminar un producto específico del carrito
    When click("button[name=deleteProd]")
    Then delay(1000)

    # 9. Eliminar el carrito creado
    When submit().click("button[value=deleteCart]")
    Then delay(1000)
  