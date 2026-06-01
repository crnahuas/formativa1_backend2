# Evidencias de Autenticacion y Autorizacion

Fecha de generacion: 2026-06-01

Las evidencias fueron generadas ejecutando la API local en `http://localhost:8080` y realizando solicitudes reales contra los endpoints de seguridad.

## Archivos generados
- Captura: `docs/evidencias/capturas/01_registro_usuario.svg`
- Captura: `docs/evidencias/capturas/02_login_cliente_jwt.svg`
- Captura: `docs/evidencias/capturas/03_jwt_claims_decodificados.svg`
- Captura: `docs/evidencias/capturas/04_acceso_autorizado_productos_cliente.svg`
- Captura: `docs/evidencias/capturas/05_acceso_denegado_usuarios_cliente.svg`
- Captura: `docs/evidencias/capturas/06_acceso_autorizado_usuarios_gerente.svg`
- Captura: `docs/evidencias/capturas/07_sin_token_productos.svg`
- Captura: `docs/evidencias/capturas/08_bcrypt_en_codigo.svg`
- Captura: `docs/evidencias/capturas/09_postman_collection_jwt.svg`
- Respuesta JSON: `docs/evidencias/respuestas/01_registro_usuario.json`
- Respuesta JSON: `docs/evidencias/respuestas/02_login_cliente_jwt.json`
- Respuesta JSON: `docs/evidencias/respuestas/03_jwt_claims_decodificados.json`
- Respuesta JSON: `docs/evidencias/respuestas/04_acceso_autorizado_productos_cliente.json`
- Respuesta JSON: `docs/evidencias/respuestas/05_acceso_denegado_usuarios_cliente.json`
- Respuesta JSON: `docs/evidencias/respuestas/06_acceso_autorizado_usuarios_gerente.json`
- Respuesta JSON: `docs/evidencias/respuestas/07_sin_token_productos.json`

## Resultado resumido

- Registro de usuario: HTTP 201.
- Login de cliente: HTTP 200 y token JWT generado.
- Acceso de CLIENTE a productos: HTTP 200.
- Acceso de CLIENTE a usuarios: HTTP 403.
- Acceso de GERENTE a usuarios: HTTP 200 sin exponer password.
- Acceso sin token a productos: HTTP 401.
- BCrypt evidenciado en codigo fuente con `passwordEncoder.encode(...)`.
- Coleccion Postman actualizada para Bearer JWT.