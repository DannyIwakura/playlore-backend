# Backend PlayRole API (Spring Boot)

API REST de PlayLore/PlayRole. Spring Boot 4.0.3, Java 21, Maven (wrapper `.\mvnw.cmd`), MySQL.

## Comandos de verificación (SIEMPRE al terminar una tarea)

- Tests: `.\mvnw.cmd test` (usa H2 en memoria con el perfil `test`, no necesita MySQL)
- Compilar: `.\mvnw.cmd -q compile -DskipTests`
- La app real usa MySQL y el perfil `dev` (o `SPRING_PROFILES_ACTIVE=prod` en producción).
- CI: `.github/workflows/ci.yml` ejecuta compile + test en cada push/PR a `main` (runner Linux, `./mvnw`).

## Configuración y secretos

- `application.properties` NO contiene secretos. Valores sensibles vienen por variables de entorno:
  `DB_URL`, `DB_USER`, `DB_PASS`, `DB_DDL_AUTO`, `JWT_SECRET`, `RECAPTCHA_SECRET`, `GOOGLE_CLIENT_ID`, `CORS_ALLOWED_ORIGINS`.
- `jwt.secret` NO tiene fallback: `JwtUtils` y `SessionJwtUtils` lanzan `IllegalArgumentException` al arrancar si está en blanco (fail-fast).
- Orígenes permitidos de WebSocket/CORS vienen de la property `app.cors.allowed-origins`
  (default dev: `http://localhost:5173,http://127.0.0.1:5173`; en prod se inyecta `CORS_ALLOWED_ORIGINS`). No hardcodear `*`.
- Perfil dev (`application-dev.properties`): credenciales de desarrollo, `ddl-auto=update`, reCAPTCHA de prueba + `skip-verification=true`, client-id de Google de desarrollo.
- Perfil test (`src/test/resources/application-test.properties`): H2 en memoria, `server.servlet.context-path` vacío.

## Convenciones de seguridad

- Contrato de error JSON unificado: `{"error": "mensaje"}`. `GlobalExceptionHandler` centraliza todos los handlers.
- Lanzar la excepción propia `com.playrole.exception.AccessDeniedException` (EXTIENDE la de Spring) para denegar
  acceso con mensaje custom; el `accessDeniedHandler` de `SecurityConfig` lo serializa como 403 JSON conservando el mensaje.
  La denegación por `@PreAuthorize`/método security devuelve el mensaje genérico.
- Endpoints que tocan recursos ajenos: verificar propiedad con el helper `obtenerUserId(Authentication)`, que soporta
  tanto `CustomUserDetails.getUsuario().getUserId()` como `CharacterSessionPrincipal.getUsuario().getUserId()`.
  Los helpers deben aceptar también sesiones de personaje (`CharacterSessionPrincipal`), no solo `CustomUserDetails`.
- Cambio de presencia `PUT /personajes/{id}/status`: solo el dueño del personaje o ADMIN/MOD (patrón en `PresenceController`).
- `PersonajeCategoriaController`: devolver `ResourceNotFoundException` (404) para entidades inexistentes y denegar 403 con
  mensaje custom si el personaje no es del usuario y no es moderador.
- Listados que agrupan datos por entidad: evitar N+1. Ejemplo: counts/roles de canales se hacen con consultas batch
  (`MiembroCanalRepository.countByCanalIn`/`findRolesPorCanales`), no con un `count` por canal.
- `@EnableMethodSecurity` activo en `SecurityConfig` (usar `@PreAuthorize("hasAnyRole('ADMIN','MOD')")` etc.).
- Subida de imágenes: validar SIEMPRE firma (magic bytes) con `com.playrole.utils.ImageFileValidator.detectarTipoReal`
  además del `Content-Type`, y dimensiones con `ImageIO`. No confiar en la cabecera `Content-Type` (suplantable).
- XSS: sanitizar todo HTML de usuario con `com.playrole.utils.HtmlUtils.sanitize` (OWASP html-sanitizer) antes de persistir.
- JSON en `SecurityConfig` usa el `ObjectMapper` de Jackson 3 (`tools.jackson.databind.ObjectMapper`), que es el que
  auto-configura Spring Boot 4. NO inyectar `com.fasterxml.jackson.databind.ObjectMapper` (Jackson 2, solo presente por `jjwt-jackson`, sin bean).
- Rate-limit de login (incl. `/google-login`) por IP vía `LoginAttemptService`; `server.forward-headers-strategy=NATIVE` para IP real tras proxy.

## Estructura destacada

- `controller/`: REST (Usuarios, Personajes, Amistades, Denuncias, Moderación, Categorías...)
- `chat/`: sistema de canales/chat por WebSocket, sesiones de personaje (JWTs de sesión), presencia
- `service/`: lógica de negocio (validaciones, sanitización, rate-limit)
- `security/` + `config/`: JWT de usuario, method security, CORS, 401/403 JSON
- `utils/`: `HtmlUtils` (sanitización), `ImageFileValidator` (firma de imágenes), `JwtUtils`

## Reglas UI (frontend relacionado)

- PROHIBIDOS `alert()`/`confirm()`/`prompt()` nativos en el frontend; usar `ModalAviso.vue`/`ModalConfirmacion.vue`/`ModalInput.vue`.
- Los repositorios llevan un `ROADMAP.md` con la planificación de fases/lotes (ver `ROADMAP.md` de este repo).
- Ver el `ROADMAP.md` del frontend en `E:\playlore-frontend\playlore-frontend\`.
