# Actividad Formativa 2 - Autenticacion y Autorizacion con Spring Security y JWT

## 1. Analisis del problema

MiniMarket Plus requiere proteger su API REST para evitar que recursos internos queden disponibles sin control de identidad. El backend ya contaba con entidades de negocio, controladores REST, repositorios JPA, entidades `Usuario` y `Rol`, BCrypt en el servicio de usuarios y una configuracion inicial de Spring Security. Sin embargo, la configuracion estaba orientada a formulario web y HTTP Basic, no a JWT stateless, y la clase `JwtUtil` no implementaba generacion ni validacion de tokens.

Riesgos detectados:

- Acceso no autorizado a endpoints de usuarios, ventas, inventario, productos y carrito.
- Exposicion de informacion sensible si se devuelven entidades de usuario completas.
- Almacenamiento inseguro de contrasenas si no se aplica hashing antes de persistir.
- Manipulacion de tokens si no existe firma criptografica y validacion en cada request.
- Escalamiento de privilegios si los roles no se validan en URL y metodo.

## 2. Estrategia seleccionada

Se selecciono Spring Security porque entrega una capa centralizada para autenticar, autorizar y filtrar solicitudes HTTP antes de llegar a los controladores. Se selecciono JWT porque permite una autenticacion stateless: el servidor no mantiene sesion en memoria y cada request incluye un token firmado en el encabezado `Authorization`.

Se utiliza BCrypt para almacenar contrasenas con hashing adaptativo, evitando guardar contrasenas en texto plano. La arquitectura stateless se configura con `SessionCreationPolicy.STATELESS`, deshabilitando formulario, HTTP Basic y logout de sesion, ya que el acceso se resuelve mediante tokens Bearer.

Esta estrategia es adecuada para APIs REST y microservicios porque reduce dependencia de sesiones en servidor, facilita escalabilidad horizontal y permite que otros servicios validen identidad y roles mediante claims firmados.

## 3. Implementacion realizada

Para esta nueva entrega se completo la seguridad del backend existente sin reemplazar la arquitectura base del proyecto. Se reutilizaron las entidades `Usuario` y `Rol`, el repositorio de usuarios, los servicios existentes y los controladores del dominio MINIMARKET PLUS. Sobre esa base se agrego una capa de autenticacion JWT y autorizacion por roles, alineada con los requerimientos de la pauta.

El cambio principal fue reemplazar el enfoque de autenticacion web tradicional por una API REST stateless. Por ello se deshabilitaron `formLogin`, `httpBasic`, `logout` y CSRF, y se incorporo un filtro propio que procesa el encabezado `Authorization: Bearer <token>` antes de que Spring Security evalue los permisos del endpoint.

Clases creadas:

- `com.minimarket.dto.auth.LoginRequest`
- `com.minimarket.dto.auth.RegisterRequest`
- `com.minimarket.dto.auth.AuthResponse`
- `com.minimarket.dto.usuario.UsuarioResponse`
- `com.minimarket.controller.AuthController`
- `com.minimarket.security.filter.JwtAuthenticationFilter`
- `com.minimarket.exception.ErrorResponse`
- `com.minimarket.exception.GlobalExceptionHandler`
- `com.minimarket.security.config.H2ConsoleConfig`

Clases modificadas:

- `pom.xml`: dependencias de validacion, JJWT y H2 disponible para registrar la consola local.
- `SecurityConfig`: configuracion stateless, filtro JWT, autorizacion por roles y errores 401/403.
- `JwtUtil`: generacion, firma, expiracion y validacion de JWT.
- `DataInitializer`: roles `ROLE_CLIENTE`, `ROLE_EMPLEADO`, `ROLE_GERENTE` y usuarios de prueba.
- Controladores de negocio: reglas `@PreAuthorize`.
- `UsuarioController`: respuesta con DTO para no exponer password.
- `application.properties`: secreto y expiracion JWT.

Endpoints incorporados:

- `POST /api/auth/register`: registra usuarios nuevos con rol `ROLE_CLIENTE` y contrasena cifrada.
- `POST /api/auth/login`: valida credenciales y retorna un JWT firmado.

Usuarios iniciales para pruebas:

- `cliente / cliente123` con rol `ROLE_CLIENTE`.
- `empleado / empleado123` con rol `ROLE_EMPLEADO`.
- `gerente / gerente123` con rol `ROLE_GERENTE`.

## 4. Flujo de autenticacion

1. El cliente envia credenciales a `POST /api/auth/login`.
2. `AuthenticationManager` valida usuario y contrasena usando `CustomUserDetailsService` y BCrypt.
3. Si las credenciales son correctas, `JwtUtil` genera un JWT firmado con HS256.
4. El cliente envia el token en cada request: `Authorization: Bearer <token>`.
5. `JwtAuthenticationFilter` extrae, valida y carga la autenticacion en `SecurityContextHolder`.

## 5. Flujo de autorizacion

Los roles definidos son:

- `ROLE_CLIENTE`
- `ROLE_EMPLEADO`
- `ROLE_GERENTE`

Reglas principales:

- `CLIENTE`: puede consultar productos/categorias y operar carrito.
- `EMPLEADO`: puede gestionar productos/categorias, ventas, detalle de ventas, inventario y carrito.
- `GERENTE`: puede administrar usuarios y ejecutar operaciones criticas, incluyendo eliminaciones.

La autorizacion se aplica en dos niveles:

- Reglas por URL en `SecurityConfig`.
- Reglas por metodo con `@PreAuthorize` en controladores.

## 6. Evidencias de funcionamiento

Las evidencias fueron capturadas manualmente en Postman y en la consola H2. La coleccion utilizada se encuentra en `docs/postman/MINIMARKET_PLUS_Seguridad.postman_collection.json` y el ambiente en `docs/postman/MINIMARKET_PLUS_Local.postman_environment.json`.

Las capturas originales se guardaron en `docs/evidencias/postman_manual/`.

Evidencias capturadas:

- `01_publico_hola.png`: endpoint publico `GET /public/hola`, respuesta `200 OK`.
- `02_registro_nuevo_cliente.png`: registro de usuario por `POST /api/auth/register`, respuesta `201 Created` con rol `ROLE_CLIENTE`.
- `03_login_cliente.png`: login de cliente por `POST /api/auth/login`, respuesta `200 OK` con token JWT.
- `04_login_empleado.png`: login de empleado, respuesta `200 OK` con token JWT y rol `ROLE_EMPLEADO`.
- `05_login_gerente.png`: login de gerente, respuesta `200 OK` con token JWT y rol `ROLE_GERENTE`.
- `06_protegido_sin_token_productos.png`: intento de acceder a `GET /api/productos` sin token, respuesta `401 Unauthorized`.
- `07_cliente_autorizado_productos.png`: cliente accede a productos con JWT, respuesta `200 OK`.
- `08_cliente_denegado_usuarios.png`: cliente intenta acceder a usuarios, respuesta `403 Forbidden`.
- `09_gerente_autorizado_usuarios.png`: gerente accede a usuarios, respuesta `200 OK`; no se expone el campo `password`.
- `10_cliente_denegado_inventario.png`: cliente intenta acceder a inventario, respuesta `403 Forbidden`.
- `11_empleado_autorizado_inventario.png`: empleado accede a inventario, respuesta `200 OK`.
- `12_empleado_autorizado_detalle_ventas.png`: empleado accede a detalle de ventas, respuesta `200 OK`.
- `13_h2_passwords_bcrypt.png`: consulta H2 `SELECT username, password FROM usuario;`, donde se observa que las contrasenas estan cifradas con BCrypt.

Estas evidencias demuestran que el backend diferencia correctamente autenticacion y autorizacion: primero valida la identidad mediante login y token JWT, y luego aplica permisos segun el rol del usuario autenticado.

## 7. Proteccion frente a amenazas

- Acceso no autorizado: los endpoints requieren token JWT salvo rutas publicas de autenticacion y prueba.
- Robo de credenciales: las contrasenas se almacenan con BCrypt, no reversibles.
- Manipulacion de tokens: los JWT se firman con HS256 y una clave secreta configurada por propiedad.
- Escalamiento de privilegios: los endpoints validan roles en configuracion HTTP y con `@PreAuthorize`.
- Exposicion sensible: `UsuarioResponse` evita retornar contrasenas en respuestas de usuarios.

## 8. Respuestas tecnicas

JWT en arquitectura stateless funciona porque el servidor emite un token firmado tras autenticar al usuario. En solicitudes posteriores, el cliente envia ese token y el servidor valida firma, expiracion y usuario sin crear una sesion persistente.

Frente a sesiones tradicionales, JWT facilita escalabilidad horizontal porque no requiere almacenar estado de sesion en memoria del servidor. Esto es util en APIs REST y microservicios donde multiples instancias pueden procesar solicitudes.

La manipulacion del JWT se evita mediante firma criptografica. Si un atacante modifica el subject, roles o expiracion, la validacion de firma falla y el filtro no autentica la solicitud.

La autorizacion basada en roles se implemento con `ROLE_CLIENTE`, `ROLE_EMPLEADO` y `ROLE_GERENTE`. Spring Security evalua estos roles en `SecurityConfig` y en anotaciones `@PreAuthorize`, asegurando que cada endpoint sea usado solo por perfiles permitidos.

Buenas practicas aplicadas:

- API stateless.
- CSRF deshabilitado para API REST sin cookies de sesion.
- Form login y HTTP Basic deshabilitados.
- BCrypt para contrasenas.
- DTOs de autenticacion y respuesta.
- Validaciones con `@Valid`.
- Manejo global de excepciones.
- Logging de login exitoso, login fallido, registros y accesos restringidos.

## 9. Oportunidades de mejora

- Rate limiting para reducir abuso sobre `/api/auth/login`.
- Bloqueo temporal por intentos fallidos.
- Politicas de contrasena con complejidad, historial y expiracion.
- Refresh tokens con rotacion y revocacion.
- Rotacion periodica de claves JWT.
- Auditoria persistente de eventos criticos en base de datos.
- Separacion de permisos granulares ademas de roles.

## 10. Pruebas ejecutadas

Se ejecuto:

```bash
./mvnw test -q
```

Resultado: pruebas exitosas, incluyendo carga del contexto Spring Boot.
