# Fitbit Health

Android nativa + gateway privado para **Google Fitbit Air** con Hermes.

## Arquitectura

```
Fitbit Air → Google Health API → SQLite → FastAPI Gateway → Tailscale → Android App
```

## Estructura

| Directorio | Contenido |
|------------|-----------|
| `android/` | App Kotlin + Jetpack Compose |
| `gateway/` | API FastAPI privada |
| `docs/` | Diseño, plan y contrato API |
| `scripts/` | Utilidades de desarrollo |

## Requisitos

- PC con Hermes + Tailscale + Google Health API configurada
- Android 8+ con Tailscale instalado
- JDK 17 y Android Studio para compilar

## Instalación

1. Descarga el APK de [Releases](https://github.com/errezeeta/fitbit-health/releases)
2. Configura el gateway en tu PC (ver `gateway/README.md`)
3. Conecta PC y móvil a la misma tailnet de Tailscale
4. En Ajustes de la app, introduce URL y token del gateway

## Desarrollo

```bash
# Gateway
cd gateway && pip install -e ".[test]" && pytest tests -q

# Android (requiere Android Studio)
cd android && ./gradlew assembleDebug
```

## Privacidad

Este repositorio es público. No contiene credenciales, tokens, bases de datos ni datos personales.
