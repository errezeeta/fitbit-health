# Diseño: Fitbit Health Android App

Fecha: 2026-08-03  
Estado: Aprobado para planificación  
Producto: app Android nativa pública + gateway privado de salud

## Objetivo

Crear una app Android nativa para consultar los datos de Google Fitbit Air, visualizar resúmenes y tendencias, y conversar con un chat de IA exclusivamente sobre salud/Fitbit.

La app no accederá directamente a Google Health API. El PC con Hermes será el gateway: mantiene credenciales y SQLite, ejecuta la sincronización y sirve una API privada accesible mediante Tailscale.

## Alcance aprobado

- Android nativo con Kotlin y Jetpack Compose.
- Material 3, soporte de tema claro/oscuro y diseño adaptado a móvil.
- Dashboard con sueño, RHR, HRV, SpO2, temperatura, respiración y pasos.
- Gráficos para 7, 30 y 90 días, además de rango personalizado.
- Chat limitado a salud/Fitbit, conectado al backend de Hermes.
- Sincronización automática cada 30 minutos.
- Botón «Sincronizar ahora».
- URL del gateway y token configurables desde Ajustes.
- Repositorio público sin secretos, tokens, IPs, datos personales ni base de datos.

Fuera de alcance inicial: publicación en Play Store, acceso directo desde la app a Google OAuth, edición de datos de salud, recomendaciones médicas, chat general de Hermes, notificaciones clínicas y multiusuario.

## Arquitectura

```text
Fitbit Air
  -> Google Health API
  -> google_health_sync.py (Hermes, cada 30 min)
  -> SQLite local
  -> FastAPI gateway local en el PC
  -> Tailscale tailnet
  -> App Android Kotlin + Compose
```

La app pública será un cliente genérico de cualquier gateway compatible. La implementación de Hermes será una instancia del backend, pero no se incluirán rutas, tokens o configuración personal en GitHub.

### Componentes

1. **Android app**
   - UI Compose y navegación.
   - Cliente HTTP con Retrofit/OkHttp.
   - Modelos DTO y repositorios.
   - Estado mediante ViewModel y StateFlow.
   - Gráficos con una librería Compose mantenida.
   - Almacenamiento seguro del token con Android Keystore/Encrypted DataStore.

2. **Gateway API**
   - FastAPI en el PC.
   - Lectura de SQLite con consultas parametrizadas.
   - Adaptadores para dashboard, métricas, tendencias y chat.
   - Endpoint de sincronización manual que lanza una tarea controlada.
   - Autenticación por bearer token.
   - Bind y firewall documentados para acceso por Tailscale.

3. **Hermes**
   - MCP ya existente para consultas internas.
   - El endpoint de chat invocará un flujo de salud restringido, con contexto Fitbit y sin exponer herramientas generales peligrosas.
   - El backend no devolverá secretos ni tokens al cliente.

## API propuesta

Base configurable, por ejemplo `http://<pc-tailscale-name>:8844`.

- `GET /health` — estado técnico y versión; requiere token salvo configuración explícita local.
- `GET /api/v1/dashboard?range=7d` — tarjetas y últimos valores.
- `GET /api/v1/sleep?start=YYYY-MM-DD&end=YYYY-MM-DD` — sesiones y fases.
- `GET /api/v1/heart-rate?date=YYYY-MM-DD&detail=daily|intraday`.
- `GET /api/v1/steps?start=...&end=...&detail=daily|intraday`.
- `GET /api/v1/trends?metrics=rhr,hrv,spo2,temp,breathing,steps&start=...&end=...`.
- `GET /api/v1/sync/status` — última sincronización, errores y frescura.
- `POST /api/v1/sync` — solicita «Sincronizar ahora» y devuelve un job/status.
- `GET /api/v1/sync/{job_id}` — seguimiento de sincronización.
- `POST /api/v1/chat` — pregunta de salud; respuesta, fuentes/métricas usadas y advertencia no médica.

Los rangos se validarán en servidor. Las consultas intraday tendrán límites para no enviar más de lo necesario al móvil.

## Pantallas

1. **Dashboard**
   - Última sincronización y estado de conexión.
   - Tarjetas: sueño reciente, RHR, HRV, SpO2, temperatura, respiración y pasos.
   - Acceso rápido a sincronización manual.

2. **Detalle de sueño**
   - Sesión más reciente con horarios en zona local del dispositivo.
   - Barras por fases.
   - Tendencia de duración, deep, REM y despertares.

3. **Detalle de métricas**
   - Selector de métrica.
   - Línea temporal y estadísticas min/media/max.
   - Selector 7d/30d/90d/custom.

4. **Chat de salud**
   - Conversación persistente localmente, sin guardar datos sensibles en el repositorio.
   - Preguntas sugeridas.
   - Indicador de datos usados y fecha de actualización.
   - Mensaje claro: no es diagnóstico médico.

5. **Ajustes**
   - URL del gateway Tailscale.
   - Token.
   - Probar conexión.
   - Preferencias de tema, unidades y zona horaria.
   - Borrar credenciales y conversación local.

## Zona horaria y unidades

La API almacenará/consultará fechas canónicas y devolverá timestamps con zona o UTC explícito. La app mostrará las horas en la zona del teléfono, actualmente Europe/Madrid. No se hará un desplazamiento fijo de +2 horas: se usará `ZoneId`/reglas DST para distinguir invierno y verano.

## Seguridad y privacidad

- El repositorio público no contendrá secretos ni datos reales.
- Token del gateway en almacenamiento cifrado de Android.
- Tailscale será la red de transporte inicial; no se abrirán puertos del router.
- El gateway escuchará en la interfaz Tailscale o aplicará firewall equivalente.
- CORS no será la frontera de seguridad principal, ya que el cliente es Android.
- Logs sin tokens, preguntas completas ni datos de salud innecesarios.
- Validación estricta de inputs, límites de rango y rate limiting básico.
- HTTPS sobre Tailscale queda como mejora; la documentación dejará claro el modelo de amenaza de HTTP dentro de tailnet.

## Errores y estados

La app diferenciará: sin configuración, PC apagado, Tailscale desconectado, token inválido, datos antiguos, sync en curso, API de Google temporalmente no disponible y error de chat. Cada estado tendrá acción útil: editar configuración, reintentar, sincronizar o consultar último dato disponible.

## Testing

### Android

- Tests unitarios de parsing, rangos, zona horaria y ViewModels.
- Tests de repositorios con servidor HTTP falso.
- Compose UI tests para dashboard, selector de rango, sync y ajustes.
- Test de que tokens no aparecen en logs.

### Backend

- Tests FastAPI con SQLite temporal.
- Tests de autenticación y autorización.
- Tests de validación de rangos y límites intraday.
- Tests de conversión de timestamps Europe/Madrid con CET y CEST.
- Tests de sync job idempotente y errores.
- Tests de chat restringido al contexto de salud.

### Verificación manual

- PC accesible/no accesible por Tailscale.
- Sync manual y automático.
- Sueño cruzando medianoche mostrado correctamente.
- Datos vacíos/parciales y token revocado.

## Estructura de repositorios

Inicialmente se recomienda un monorepo público:

```text
fitbit-health/
  android/              # app Kotlin + Compose
  gateway/              # FastAPI genérico y adaptador SQLite
  docs/                  # arquitectura y configuración
  .github/workflows/    # build/test sin secretos
  README.md
```

La configuración privada de Hermes, `.env`, SQLite y tokens permanecerá fuera del repo. Si el gateway crece de forma independiente, podrá extraerse después a otro repositorio sin cambiar el contrato API.

## Criterios de éxito del MVP

- La app se instala en un Android sin WebView ni dashboard web.
- Puede configurar el host Tailscale y token y comprobar conexión.
- Dashboard carga datos reales del SQLite.
- Sleep, RHR, HRV y SpO2 tienen gráficos en 7/30/90/custom.
- «Sincronizar ahora» informa progreso y resultado.
- Chat responde usando datos Fitbit y rechaza/encuadra preguntas médicas peligrosas.
- Tests pasan localmente y CI puede compilar el proyecto sin credenciales privadas.

## Decisiones pendientes para la fase de planificación

- Nombre y URL definitivos del repositorio.
- Librería concreta de charts Compose.
- Puerto definitivo del gateway.
- Formato exacto del contrato de chat y trabajos de sync.
- Si el gateway vivirá en este repositorio o en uno separado.

Tras este documento aprobado, la siguiente fase es redactar el plan de implementación TDD por tareas pequeñas. No se implementa código en la fase de diseño.
