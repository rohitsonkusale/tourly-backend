# 🏭 Production Deployment Guide - Tourly Backend

## ⚠️ Before You Deploy - Critical Checklist

### 1. Environment Variables (MUST BE SET)
| Variable               | Description                                                                 | How to Generate/Get                                                          |
|------------------------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE` | Set to `prod` (REQUIRED!)                                                 | `export SPRING_PROFILES_ACTIVE=prod`                                        |
| `DB_URL`               | JDBC URL to your production MySQL database (with SSL enabled!)            | Your cloud provider (AWS RDS, GCP Cloud SQL, etc.)                          |
| `DB_USERNAME`          | Production database username                                                | Create a restricted database user (only necessary permissions)              |
| `DB_PASSWORD`          | Production database password (STRONG!)                                     | Use password manager to generate 20+ character random password               |
| `JWT_SECRET`           | Strong JWT signing secret (AT LEAST 256 bits!)                             | Run `openssl rand -base64 64` in terminal                                   |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of your production frontend domain(s)                | `https://yourdomain.com,https://www.yourdomain.com`                         |
| `COOKIE_SECURE`        | Set to `true` (ALWAYS in production!)                                      |                                                                             |
| `COOKIE_DOMAIN`        | Your production domain (e.g. `yourdomain.com`)                            |                                                                             |
| `RAZORPAY_KEY`         | Production Razorpay key                                                    | Razorpay Dashboard (https://dashboard.razorpay.com/)                        |
| `RAZORPAY_SECRET`      | Production Razorpay secret                                                 | Razorpay Dashboard                                                          |
| `RAZORPAY_WEBHOOK_SECRET` | Production Razorpay webhook secret                                        | Generate in Razorpay Dashboard under Webhooks                               |
| `GOOGLE_CLIENT_ID`     | Production Google OAuth Client ID                                           | https://console.cloud.google.com/                                            |
| `GOOGLE_CLIENT_SECRET` | Production Google OAuth Client Secret                                      | Google Cloud Console                                                        |
| `MAIL_HOST`            | Production SMTP host (e.g. `smtp.gmail.com`, SendGrid, etc.)              | Your transactional email provider                                          |
| `MAIL_PORT`            | Production SMTP port                                                        | Your transactional email provider                                          |
| `MAIL_USERNAME`        | Production SMTP username                                                   | Your transactional email provider                                          |
| `MAIL_PASSWORD`        | Production SMTP password                                                   | Your transactional email provider                                          |
| `MAIL_ENABLED`         | Set to `true` to send transactional emails in production                   |                                                                             |
| `MAIL_FROM_NAME`       | Your app name (e.g. `Roamaya`)                                              |                                                                             |
| `MAIL_FROM`            | Your production from email (e.g. `noreply@yourdomain.com`)                 |                                                                             |

### 2. Database Setup (Production)
- [ ] **Enable SSL/TLS** for all database connections!
- [ ] Create a **restricted database user** with only necessary permissions
- [ ] Take a backup of your database before deploying
- [ ] Enable database **automatic backups** in your cloud provider
- [ ] Run Flyway migrations first (never rely on `ddl-auto=update`!)

### 3. Security Hardening (Production)
- [ ] **Enforce HTTPS everywhere** (HSTS already enabled in config!)
- [ ] Set up a **Web Application Firewall (WAF)** if possible
- [ ] Disable Swagger/OpenAPI UI in production OR restrict it
- [ ] Set up **rate limiting** at the load balancer/CDN level
- [ ] Use a **secrets manager** (AWS Secrets Manager, Hashicorp Vault, etc.) – DON'T commit secrets!
- [ ] Regularly rotate secrets (every 90 days or sooner)

### 4. Monitoring & Health Checks
- [ ] Monitor `/actuator/health` endpoint (already exposed!)
- [ ] Monitor `/actuator/health/liveness` and `/actuator/health/readiness` for Kubernetes/Docker
- [ ] Monitor JVM memory and CPU usage
- [ ] Set up **application logging** to a centralized service (ELK Stack, CloudWatch, Datadog, etc.)
- [ ] Set up **alerts** for errors and high CPU/memory usage

---

## 🚀 Deployment Steps (Spring Boot)

### Option A: JAR File Deployment
1. Build the production JAR:
   ```bash
   mvn clean package -DskipTests
   ```
2. Run the JAR with all environment variables set:
   ```bash
   java -jar target/tourly-0.0.1-SNAPSHOT.jar
   ```

### Option B: Docker Deployment (Recommended)
1. Use the existing `Dockerfile` in the repo
2. Build the production image:
   ```bash
   docker build -t tourly-prod .
   ```
3. Run the container, passing all environment variables:
   ```bash
   docker run -d \
     -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e DB_URL=your-jdbc-url \
     -e DB_USERNAME=your-db-user \
     -e DB_PASSWORD=your-db-password \
     -e JWT_SECRET=your-jwt-secret \
     tourly-prod
   ```

---

## 📊 Post-Deployment Checks
1. Visit `/actuator/health` - should return `{"status":"UP"}`
2. Verify Swagger UI is disabled (if you chose to disable it)
3. Test a full user flow: Signup → Login → Book Trip → Payment
4. Check that emails are being sent (if mail is enabled)
5. Check that webhooks work (Razorpay)

---

## 🔄 Rollback Plan
Always have a rollback plan:
- Keep the previous production image/tag
- Keep database backups
- Know how to roll back quickly (1-click if possible)
