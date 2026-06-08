# Guia de evidencias Postman - Semana 3

Usa esta guia para capturar pantallazos y completar el informe de seguridad de MiniMarket Plus.

## Preparacion

1. Ejecutar la aplicacion local en `http://localhost:8080`.
2. Importar en Postman:
   - `docs/postman/MINIMARKET_PLUS_Seguridad.postman_collection.json`
   - `docs/postman/MINIMARKET_PLUS_Local.postman_environment.json`
3. Seleccionar el environment `MINIMARKET PLUS - Local`.
4. Ejecutar las solicitudes en orden, porque los login guardan `clienteToken`, `empleadoToken` y `gerenteToken`.

## Capturas recomendadas

| Evidencia | Solicitud Postman | Resultado esperado | Uso en informe |
| --- | --- | --- | --- |
| Endpoint publico | `01 Publico - Hola` | `200 OK` | Muestra ruta permitida sin autenticacion. |
| Registro cliente | `02 Registro - Nuevo cliente` | `201 Created` o `409 Conflict` si ya existe | Muestra registro con rol `ROLE_CLIENTE`. |
| Login cliente | `03 Login - Cliente` | `200 OK` con `token` | Generacion de JWT para rol cliente. |
| Login empleado | `04 Login - Empleado` | `200 OK` con `token` | Generacion de JWT para rol empleado. |
| Login gerente | `05 Login - Gerente` | `200 OK` con `token` | Generacion de JWT para rol gerente. |
| Credenciales invalidas | `06 Login fallido - Credenciales invalidas` | `401 Unauthorized` | Proteccion ante autenticacion incorrecta. |
| Ruta sin token | `07 Sin token - Productos protegido` | `401 Unauthorized` y `WWW-Authenticate` | Recurso protegido exige Bearer token. |
| Cliente permitido | `08 Cliente permitido - Consultar productos` | `200 OK` | Acceso permitido por rol. |
| Cliente denegado | `09 Cliente denegado - Usuarios` | `403 Forbidden` | Control de autorizacion por rol. |
| Gerente permitido | `10 Gerente permitido - Usuarios sin password` | `200 OK`, sin `password` | Acceso administrativo sin exponer hash BCrypt. |
| Inventario denegado | `11 Cliente denegado - Inventario` | `403 Forbidden` | Cliente no puede administrar inventario. |
| Inventario permitido | `12 Empleado permitido - Inventario` | `200 OK` | Empleado accede a modulo operativo. |
| Token invalido | `13 Token invalido - Productos` | `401 Unauthorized`, `invalid_token` | Validacion de firma/formato JWT. |
| SQL Injection | `14 SQL Injection - Login no debe autenticar` | `401 Unauthorized` | Payload SQLi no logra iniciar sesion. |
| XSS | `15 XSS - Registro rechaza script en username` | `400 Bad Request` | Validacion de entrada rechaza script. |
| CSRF | `16 CSRF - POST sin JWT queda bloqueado` | `401 Unauthorized` | API stateless no acepta cambios sin JWT. |

## Texto breve para el informe

Las pruebas se realizaron en Postman contra el backend local. La coleccion valida que el backend use autenticacion JWT stateless, que las rutas protegidas devuelvan `401` cuando no existe token o el token es invalido, y que la autorizacion por roles devuelva `403` cuando el usuario autenticado no tiene permisos. Tambien se probaron amenazas comunes: un payload de SQL Injection en login no autentica, un payload XSS en registro es rechazado por validacion de entrada, y una solicitud POST sin Bearer token queda bloqueada, lo que reduce el riesgo CSRF al no depender de sesiones por cookie.
