# PlayRole Backend — Spring Boot API

API REST de PlayLore/PlayRole. Spring Boot 4.0.3, Java 21, Maven.

---

## Requisitos previos

- **Java JDK 21** ([Adoptium Temurin 21](https://adoptium.net/es/temurin/releases/?version=21))
- **MySQL 8+** (o Docker — ver más abajo)
- **Git**
- IDE recomendado: IntelliJ IDEA o VS Code con extensionses de Java

---

## Arranque rápido (con Docker)

El método más rápido para levantar MySQL sin instalarlo:

```bash
# Levantar MySQL en Docker
docker-compose up -d mysql

# Arrancar el backend (usa el perfil dev por defecto)
.\mvnw.cmd spring-boot:run
```

El backend se conectará a MySQL en `localhost:3306/playlore_db` con usuario `root`/`root`.

---

## Arranque manual (sin Docker)

1. Asegúrate de tener MySQL 8+ corriendo en `localhost:3306`.
2. Crea la base de datos:
   ```sql
   CREATE DATABASE playlore_db;
   ```
3. Las tablas se crean automáticamente con `ddl-auto=update` en el perfil `dev`.

---

## Variables de entorno

Copia `.env.example` como `.env` y rellena los valores. En perfil `dev` todos tienen valores por defecto razonables.

| Variable | Descripción | Default (dev) |
|---|---|---|
| `DB_URL` | JDBC URL de MySQL | `jdbc:mysql://localhost:3306/playlore_db` |
| `DB_USER` | Usuario MySQL | `root` |
| `DB_PASS` | Contraseña MySQL | `root` |
| `JWT_SECRET` | Clave de firmado JWT (obligatorio en prod) | valor de dev |
| `RECAPTCHA_SECRET` | Clave secreta de reCAPTCHA | clave de prueba Google |
| `RECAPTCHA_SKIP_VERIFICATION` | Saltar verificación reCAPTCHA | `true` |
| `GOOGLE_CLIENT_ID` | Client ID de Google OAuth | client-id de dev |
| `CORS_ALLOWED_ORIGINS` | Orígenes CORS permitidos | `http://localhost:5173` |

---

## Comandos de verificación

```bash
# Tests (H2 en memoria, no necesita MySQL)
.\mvnw.cmd test

# Compilar sin tests
.\mvnw.cmd -q compile -DskipTests
```

---

## Estructura del proyecto

```
src/main/java/com/playrole/
  controller/    — Endpoints REST (Usuarios, Personajes, Chat, Moderación...)
  chat/          — WebSocket, sesiones de personaje, presencia
  service/       — Lógica de negocio
  security/      — JWT, filtros, configuración de seguridad
  config/        — CORS, WebSocket, properties
  repository/    — Spring Data JPA
  model/         — Entidades JPA
  dto/           — Data Transfer Objects
  utils/         — HtmlUtils, ImageFileValidator, JwtUtils
```

---

## Perfiles

- **dev** (`application-dev.properties`): ddl-auto=update, credenciales de desarrollo, reCAPTCHA de prueba.
- **test** (`application-test.properties`): H2 en memoria para tests automatizados.
- **prod** (default `application.properties`): ddl-auto=validate, secretos por variables de entorno.

---

    PlayRole (c) 2026 by Daniel is licensed under CC BY-SA 4.0.
    To view a copy of this license, visit https://creativecommons.org/licenses/by-sa/4.0/
