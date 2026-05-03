# Oasis Backend Render Deployment

## Render settings

- Service type: Web Service
- Environment: Docker
- Root directory: `oasis-backend`
- Dockerfile path: `./Dockerfile`
- Health check path: `/api/health`

## Environment variables

Set these in Render before deploying:

```text
SUPABASE_URL=your Supabase project URL
SUPABASE_ANON_KEY=your Supabase anon public key
SUPABASE_SERVICE_ROLE_KEY=your Supabase service role key
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:4173
```

After the frontend is deployed, add its URL to `APP_CORS_ALLOWED_ORIGINS`, separated by a comma.

Important: keep the service role key only in Render/Supabase settings. Do not commit it into the repository.

## Quick checks

After deployment, open:

```text
https://your-render-service.onrender.com/api/health
```

Expected response:

```text
OK
```

Use this as the backend link for the assignment.

## Local run reminder

Because secrets are now read from environment variables, set the same values locally before starting the backend.

PowerShell example:

```powershell
$env:SUPABASE_URL="https://your-project.supabase.co"
$env:SUPABASE_ANON_KEY="your-anon-key"
$env:SUPABASE_SERVICE_ROLE_KEY="your-service-role-key"
$env:APP_CORS_ALLOWED_ORIGINS="http://localhost:5173,http://localhost:4173"
```
