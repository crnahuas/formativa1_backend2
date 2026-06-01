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

Clases creadas:

- `com.minimarket.dto.auth.LoginRequest`
- `com.minimarket.dto.auth.RegisterRequest`
- `com.minimarket.dto.auth.AuthResponse`
- `com.minimarket.dto.usuario.UsuarioResponse`
- `com.minimarket.controller.AuthController`
- `com.minimarket.security.filter.JwtAuthenticationFilter`
- `com.minimarket.exception.ErrorResponse`
- `com.minimarket.exception.GlobalExceptionHandler`

Clases modificadas:

- `pom.xml`: dependencias de validacion y JJWT.
- `SecurityConfig`: configuracion stateless, filtro JWT, autorizacion por roles y errores 401/403.
- `JwtUtil`: generacion, firma, expiracion y validacion de JWT.
- `DataInitializer`: roles `ROLE_CLIENTE`, `ROLE_EMPLEADO`, `ROLE_GERENTE` y usuarios de prueba.
- Controladores de negocio: reglas `@PreAuthorize`.
- `UsuarioController`: respuesta con DTO para no exponer password.
- `application.properties`: secreto y expiracion JWT.

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

Comando de login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"cliente","password":"cliente123"}'
```

Resultado observado:

```text
JWT generado con prefijo: eyJhbGciOiJIUzI1NiJ9
```

Registro de usuario:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"cliente_nuevo","password":"cliente12345"}'
```

Resultado observado:

```json
{"id":4,"username":"cliente_nuevo","roles":["ROLE_CLIENTE"]}
```

Acceso autorizado con rol `CLIENTE`:

```bash
curl -H "Authorization: Bearer <TOKEN_CLIENTE>" \
  http://localhost:8080/api/productos
```

Resultado observado:

```text
HTTP 200
```

Acceso denegado por rol:

```bash
curl -H "Authorization: Bearer <TOKEN_CLIENTE>" \
  http://localhost:8080/api/usuarios
```

Resultado observado:

```text
HTTP 403
```

Acceso autorizado con rol `GERENTE`:

```bash
curl -H "Authorization: Bearer <TOKEN_GERENTE>" \
  http://localhost:8080/api/usuarios
```

Resultado observado:

```json
[
  {"id":1,"username":"gerente","roles":["ROLE_GERENTE"]},
  {"id":2,"username":"empleado","roles":["ROLE_EMPLEADO"]},
  {"id":3,"username":"cliente","roles":["ROLE_CLIENTE"]}
]
```

Solicitud sin token:

```bash
curl http://localhost:8080/api/productos
```

Resultado observado:

```json
{"error":"No autenticado","message":"Debe enviar un token JWT valido"}
```

Verificacion de contrasenas cifradas en H2:

```sql
SELECT username, password FROM usuario;
```

Las contrasenas se almacenan con hash BCrypt, reconocible por prefijos como `$2a$`, `$2b$` o `$2y$`, no como texto plano.

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
