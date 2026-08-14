# Roadmap — PlayRole API (Backend)

> Documento de planificación por fases. Cada lote terminado se marca completado.
> La Fase 3 se divide en lotes; los que no afectan al backend (UI) se marcan como N/A aquí y viven en el roadmap del frontend.

## Fases

| Fase | Estado |
|---|---|
| F1 — Autenticación, usuarios, personajes, amistades | ✅ Completada |
| F2 — Chat por canales/WebSocket y sesiones de personaje | ✅ Completada |
| F3 — Mejora continua (lotes 0-6) | ✅ Completada |
| F4 — Deuda diferida (lotes 0-7) | ✅ Completada |
| F5 — Calidad de ingeniería + mejoras de producto | ✅ Completada |

## Fase 5 — checklist por lote

| Lote | Descripción | Estado |
|---|---|---|
| L0 | CI/CD: GitHub Actions en backend y frontend (compile + test con H2; typecheck + lint + test + build) | ✅ |
| L1 | Tests frontend: cobertura de composables de chat y utilidades (92 tests) | ✅ |
| L2 | Tests backend: cobertura de servicios con Mockito (83 tests nuevos, 111 total) | ✅ |
| L3 | Búsqueda de mensajes por canal: `GET /canales/{id}/mensajes/buscar?q=` con escape de LIKE y `GET /canales/{id}/mensajes/{mensajeId}/pagina` para saltar al resultado; buscador en `ChatWindow` | ✅ |
| L4 | Notificaciones fuera de foco (`notificarPush` solo con `document.hidden`; Web Push con Service Worker queda como deuda) + auto-eliminación de cuenta con contraseña BCrypt (`DELETE /usuarios/{id}` con `{password}`) y UI "Zona de peligro" | ✅ |
| L5 | Auditoría de moderación: registro persistente de acciones de mod (snapshots, sin FK) + `GET /moderacion/auditoria` para ADMIN/MOD + vista en `AdminPanel` | ✅ |
| L6 | Roadmap/AGENTS: cierre de fases y deuda documentada | ✅ |

## Decisiones de alcance (Fase 5)

- Solo desarrollo local; no hay producción desplegada. Flyway se mantiene fuera de alcance
  (se sigue usando `ddl-auto=update`) y Redis/presencia distribuida quedan documentados como deuda.
- L2 usa tests unitarios puros con Mockito (sin contexto Spring) siguiendo el patrón de
  `LoginAttemptServiceTest`; los `@SpringBootTest` existentes validan queries JPQL al arrancar.
- L3: los resultados de búsqueda se saltan al mensaje recargando la página exacta donde está.
- a11y: informe completo diferido (deuda documentada); solo lo crítico ya implementado.
- L4: la auto-eliminación exige la contraseña BCrypt del propio usuario (400 si falta, 403 si es incorrecta);
  ADMIN/MOD puede eliminar a otros sin contraseña. Las cuentas creadas por Google no pueden auto-eliminarse
  (guardan un password UUID aleatorio) y deben solicitarlo a un ADMIN.

## Fase 3 — checklist por lote

| Lote | Descripción | Estado |
|---|---|---|
| L0 | Higiene: código muerto, `System.out`/`printStackTrace`, debug BCrypt de arranque, rama muerta de auto-eliminación en `DELETE /usuarios/{id}` | ✅ |
| L1 | Seguridad: `jwt.secret` fail-fast (JwtUtils y SessionJwtUtils), orígenes WebSocket configurables por property, verificación de propiedad en `PUT /personajes/{id}/status`, endurecimiento de `PersonajeCategoriaController` (404 + propiedad/moderador), `ImagenPersonajeController` con `obtenerUserId(Authentication)` | ✅ |
| L2 | UX (solo frontend) | N/A backend |
| L3 | Rendimiento: eliminar N+1 en `listarCanales*` con batch de counts y roles (`MiembroCanalRepository.countByCanalIn`/`findRolesPorCanales`) | ✅ |
| L4 | Admin: botón "Banear objetivo" en denuncias (solo frontend; el backend ya expone `POST /moderacion/baneos`) | N/A backend |
| L5 | Documentación: AGENTS.md actualizado con convenios nuevos | ✅ |
| L6 | Roadmap (este archivo) | ✅ |

## Fase 4 — checklist por lote

| Lote | Descripción | Estado |
|---|---|---|
| L0 | Code-splitting del bundle (solo frontend) | N/A backend |
| L1 | Unificar estilos de `.modal-backdrop-custom`/`.modal-content-custom` en estilos compartidos (solo frontend) | N/A backend |
| L2 | Rendimiento: resolver el N+1 de `DenunciaService` con `enriquecerEnLote` + `PageImpl` | ✅ |
| L3 | Auto-eliminación de cuenta: cascada completa en `eliminarUsuario` con borrados batch y `DELETE /usuarios/{id}` autenticado (403 sin rol ADMIN/MOD); UI "Zona de peligro" en el frontend | ✅ |
| L4 | Refactor de `ChatWindow.vue`: extracción de composables (solo frontend) | N/A backend |
| L5 | Accesibilidad: informe + foco/teclado en modales compartidos (solo frontend) | N/A backend |
| L6 | `PresenceService` en memoria: advertencia `log.warn` al arrancar + nota en ROADMAP | ✅ |
| L7 | Roadmap (este archivo y el del frontend) | ✅ |

## Decisiones de alcance (Fase 4)

- Auto-eliminación de cuenta: se restaura como borrado con cascada para ADMIN/MOD; la eliminación con
  contraseña del propio usuario sigue siendo deuda (la UI elimina el objetivo desde moderación).
- Presencia online: se mantiene en memoria de instancia (una sola instancia); migrar a Redis si se
  escala horizontalmente. La advertencia en el arranque avisa del límite.
- `ChatWindow.vue`: se extraen composables, no componentes; la plantilla queda intacta.
- a11y: solo informe + gestión de foco/teclado en modales compartidos; el resto queda documentado en
  `docs/ACCESIBILIDAD.md` del frontend.

## Decisiones de alcance

- `DELETE /usuarios/{id}` queda solo para ADMIN; la auto-eliminación de cuenta es deuda diferida, no se construye en F3.
- No refactorizar `ChatWindow.vue` (frontend).
- El frontend conserva los endpoints REST del backend; solo se eliminan funciones muertas.
- `DenunciaService.toDTO`: el N+1 de canal/autor/objetivo solo ocurre si el snapshot está null (registros antiguos); no se aplicó batch por bajo impacto.

## Deuda técnica diferida

- [ ] `DenunciaService.toDTO`: resolver snapshots nulos en batch.
- [ ] `PresenceService` en memoria de instancia (advertencia `log.warn` al arrancar); migrar a Redis si se escala horizontalmente.
- [ ] Pruebas de carga en endpoints de chat (N+1 bajo carga real).
- [ ] Auditoría a11y completa (solo se hizo lo crítico).

## Verificación estándar

- Tests: `.\mvnw.cmd test` (H2 en memoria, perfil `test`)
- Compilar: `.\mvnw.cmd -q compile -DskipTests`
- App real: perfil `dev` (MySQL) o `SPRING_PROFILES_ACTIVE=prod` con variables de entorno.
