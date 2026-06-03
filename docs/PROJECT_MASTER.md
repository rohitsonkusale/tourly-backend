# Tourly Backend — Project Context

> Full product vision, architecture, gaps, and build roadmap live in the Roamaya repo:  
> **`D:\WEB APPLICATION\roamaya\docs\PROJECT_MASTER.md`**

Read that file first when resuming work.

## Backend quick facts

- **Stack:** Java 21, Spring Boot 3.5.11, MySQL `tourly_db`, Flyway, JWT, Razorpay
- **Port:** 8080, API prefix `/api`
- **Run:** `mvnw spring-boot:run`
- **Swagger:** http://localhost:8080/swagger-ui.html
- **Seeded admin:** `admin` / `SuperAdmin@2026`

## Package layout

| Package | Purpose |
|---------|---------|
| `auth` | Users, JWT, login/register/Google |
| `trip` | Trips, destinations |
| `booking` | Bookings + schedulers |
| `payment` | Razorpay, webhooks, refunds |
| `verification` | Host & planner KYC |
| `admin` | Dashboard, moderation |
| `userprofile` | Profile + uploads |
| `common` | ApiResponse, exceptions, shared entities |

## Next dev priority (aligned with frontend)

**Wave 1:** Traveller checkout is wired on Roamaya UI — backend endpoints already exist:

- `POST /api/bookings`
- `POST /api/payments/create-order`
- `POST /api/payments/verify`

No backend blockers for Wave 1 unless DTO/validation gaps appear during integration.

See **PROJECT_MASTER.md** in roamaya for booking state machine, trust gates, and full API list.
