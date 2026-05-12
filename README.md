# Oasis Backend

Spring Boot API for Oasis, a manga reading app with account authentication, profile management, saved library titles, reading history, and MangaDex source integration.

## Tech Stack

- Java 17
- Spring Boot 3.5.11
- Maven wrapper
- Spring Web
- Spring Actuator
- Apache HttpClient 5
- Supabase for account/profile/library storage
- Docker and Render deployment

## Features

- Health check endpoint
- Login and registration
- Home feed data
- Local library series, chapters, and reader pages
- MangaDex search, details, chapters, reader pages, navigation, and image proxying
- Saved titles and reading history
- Profile retrieval, edits, password changes, photo uploads, and account deletion
- Configurable CORS for frontend clients

## Project Structure

```text
src/main/java/com/oasis/backend
+-- accountlibrary   # Saved titles and reading history
+-- auth             # Login and registration
+-- browse           # Browse and genre endpoints
+-- config           # CORS configuration
+-- health           # Health checks
+-- home             # Dashboard and feed data
+-- library          # Series, chapters, and search
+-- profile          # Profile, password, photo, delete account
+-- reader           # Reader pages, navigation, and progress
+-- source/mangadex  # MangaDex API integration
```

## Getting Started

Prerequisites:

- Java 17
- Maven is optional because the project includes `mvnw` and `mvnw.cmd`
- Supabase project credentials

Create a local environment file from the example:

```powershell
Copy-Item .env.example .env
```

Set these values for your Supabase project:

```text
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:4173
```

Run the API locally:

```powershell
.\mvnw.cmd spring-boot:run
```

The service starts on `http://localhost:8080` by default.

## Configuration

Main configuration lives in:

```text
src/main/resources/application.properties
```

Important environment variables:

- `PORT` - server port, defaults to `8080`
- `SUPABASE_URL` - Supabase project URL
- `SUPABASE_ANON_KEY` - Supabase anon key
- `SUPABASE_SERVICE_ROLE_KEY` - optional Supabase service role key for privileged operations
- `APP_CORS_ALLOWED_ORIGINS` - comma-separated frontend origins allowed by CORS
- `APP_PUBLIC_BASE_URL` - public backend URL used for generated asset URLs

## Useful Commands

```powershell
.\mvnw.cmd spring-boot:run  # Start local API
.\mvnw.cmd test             # Run tests
.\mvnw.cmd clean package    # Build the jar
```

## API Groups

All main API routes are under `/api`.

- `GET /api/health`
- `/api/auth` - login and registration
- `/api/home` - home dashboard data
- `/api/browse` - browse and genre data
- `/api/library` - series, chapters, and local library reads
- `/api/reader` - reader pages and chapter navigation
- `/api/reader/progress` - reader progress
- `/api/profile` - profile, password, photo, and account actions
- `/api/account-library` - saved titles and reading history
- `/api/sources/mangadex` - MangaDex search, details, chapters, pages, navigation, and image proxy

## Deployment

This project includes Docker and Render configuration:

- `Dockerfile`
- `render.yaml`
- `RENDER_DEPLOYMENT.md`

Render uses:

- Runtime: Docker
- Health check path: `/api/health`
- Required secrets: `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_SERVICE_ROLE_KEY`
- CORS setting: `APP_CORS_ALLOWED_ORIGINS`

See `RENDER_DEPLOYMENT.md` for the manual deployment checklist.

## Git Notes

The repository ignores generated build output, local Maven caches, environment files, and editor-specific files. Do not commit real Supabase secrets, `.env`, generated `target` files, or local machine configuration.
