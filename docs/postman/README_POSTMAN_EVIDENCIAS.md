# Evidencias en Postman

## Archivos a importar

1. `MINIMARKET_PLUS_Seguridad.postman_collection.json`
2. `MINIMARKET_PLUS_Local.postman_environment.json`

Despues de importar ambos archivos, selecciona el ambiente **MINIMARKET PLUS - Local** en Postman.

## Preparacion

Levanta el backend:

```bash
./mvnw spring-boot:run
```

La API debe quedar disponible en:

```text
http://localhost:8080
```

## Orden recomendado para ejecutar y capturar

Ejecuta los requests en este orden:

1. `01 Publico - Hola`
   - Evidencia: endpoint publico responde `200 OK`.

2. `02 Registro - Nuevo cliente`
   - Evidencia: registro responde `201 Created`.
   - Si ya ejecutaste antes el mismo registro, puede responder `409 Conflict`; para una captura limpia cambia el `username`.

3. `03 Login - Cliente`
   - Evidencia: login responde `200 OK`.
   - El test guarda automaticamente `clienteToken`.
   - Captura donde se vea el campo `token`.

4. `04 Login - Empleado`
   - Evidencia: login responde `200 OK`.
   - El test guarda automaticamente `empleadoToken`.

5. `05 Login - Gerente`
   - Evidencia: login responde `200 OK`.
   - El test guarda automaticamente `gerenteToken`.

6. `06 Protegido sin token - Productos`
   - Evidencia: endpoint protegido sin JWT responde `401 Unauthorized`.

7. `07 Cliente autorizado - Productos`
   - Evidencia: `CLIENTE` accede correctamente a productos con `200 OK`.

8. `08 Cliente denegado - Usuarios`
   - Evidencia: `CLIENTE` no puede administrar usuarios y recibe `403 Forbidden`.

9. `09 Gerente autorizado - Usuarios sin password`
   - Evidencia: `GERENTE` accede a usuarios con `200 OK`.
   - Verifica que la respuesta no incluya `password`.

10. `10 Cliente denegado - Inventario`
    - Evidencia: `CLIENTE` recibe `403 Forbidden` al intentar acceder a inventario.

11. `11 Empleado autorizado - Inventario`
    - Evidencia: `EMPLEADO` accede a inventario con `200 OK`.

12. `12 Empleado autorizado - Detalle ventas`
    - Evidencia: `EMPLEADO` accede a detalle de ventas con `200 OK`.

## Captura de contrasenas cifradas

Esta evidencia no se obtiene desde Postman. Debes abrir la consola H2 en el navegador:

```text
http://localhost:8080/h2-console
```

Datos:

```text
JDBC URL: jdbc:h2:mem:testdb
User Name: sa
Password: dejar vacio
```

Consulta:

```sql
SELECT username, password FROM usuario;
```

Captura donde se vea que las contrasenas aparecen como hashes BCrypt, por ejemplo con prefijo `$2a$`, `$2b$` o similar.
