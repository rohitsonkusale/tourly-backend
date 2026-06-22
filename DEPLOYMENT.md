# Roamaya Platform — Deployment Guide

## Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Nginx     │────▶│  Next.js    │     │   MySQL     │
│  (TLS/443)  │     │  Frontend   │     │   8.0       │
│             │────▶│  :3000      │     │  :3306      │
└─────────────┘     └─────────────┘     └─────────────┘
       │                                       ▲
       │            ┌─────────────┐            │
       └───────────▶│ Spring Boot │────────────┘
                    │  Backend    │
                    │  :8080      │
                    └─────────────┘
```

- **Nginx** handles TLS termination, HTTP→HTTPS redirect, security headers, and rate limiting
- **Frontend** (Next.js 16 standalone) serves the React app
- **Backend** (Spring Boot 3.5 / Java 21) handles API, auth, payments
- **MySQL 8.0** stores all application data
- **Certbot** auto-renews Let's Encrypt certificates

## Prerequisites

- Docker Engine 24+ and Docker Compose v2
- A server with ports 80 and 443 open
- DNS A records pointing to the server:
  - `roamaya.in` → server IP
  - `www.roamaya.in` → server IP
  - `api.roamaya.in` → server IP

## Quick Start (Production)

### 1. Configure environment

```bash
cp .env.production .env
# Edit .env and fill in ALL required values (marked with REQUIRED comments)
```

### 2. Generate a secure JWT secret

```bash
openssl rand -base64 64
# Paste the output into JWT_SECRET in .env
```

### 3. First-time deploy

```bash
chmod +x deploy.sh init-ssl.sh
./deploy.sh init
```

### 4. Set up SSL certificates

After DNS propagation (verify with `dig roamaya.in`):

```bash
./init-ssl.sh roamaya.in www.roamaya.in api.roamaya.in
```

### 5. Verify

```bash
curl -I https://roamaya.in
curl -I https://api.roamaya.in/v3/api-docs  # Should return 404 (blocked in prod)
```

## Local Development (Docker)

```bash
cp .env.development .env
docker compose -f docker-compose.yml -f docker-compose.dev.yml up
```

This exposes:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- MySQL: localhost:3306
- Java Debug: localhost:5005

## Commands

| Command | Description |
|---------|-------------|
| `./deploy.sh init` | First-time build + start |
| `./deploy.sh update` | Rebuild and restart (rolling) |
| `./deploy.sh status` | Show container health |
| `./deploy.sh logs backend` | Stream backend logs |
| `./deploy.sh down` | Stop everything |

## SSL Certificate Renewal

Certbot auto-renews every 12 hours. To manually renew:

```bash
docker compose run --rm certbot renew
docker compose exec nginx nginx -s reload
```

## Security Hardening Checklist

- [x] All secrets via environment variables (no defaults in code)
- [x] Flyway clean disabled in production
- [x] Swagger/API docs disabled in production
- [x] Stack traces hidden from responses
- [x] HSTS header (2-year max-age, includeSubDomains, preload)
- [x] X-Frame-Options, X-Content-Type-Options, X-XSS-Protection
- [x] TLS 1.2+ only, strong cipher suite
- [x] Rate limiting on auth endpoints (Nginx + application layer)
- [x] Non-root container user
- [x] Health checks on all services
- [x] Graceful shutdown configured
- [x] Production secrets validator (fails fast if secrets missing)

## Scaling

For horizontal scaling, add replicas:

```yaml
# In docker-compose.yml
backend:
  deploy:
    replicas: 2
```

Update nginx upstream to load-balance:

```nginx
upstream backend {
    server backend:8080;
    # Docker Compose DNS handles round-robin automatically
}
```

## Troubleshooting

**Backend won't start:** Check `docker compose logs backend` — likely missing env vars. The `ProductionSecretsValidator` will print exactly which secrets are missing.

**SSL cert issues:** Ensure DNS is propagated (`dig roamaya.in`). Try staging mode first (set `STAGING=1` in `init-ssl.sh`).

**Frontend 502:** The frontend container may still be building. Check `docker compose logs frontend` and wait for the health check.
