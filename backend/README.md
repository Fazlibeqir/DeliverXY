# DeliverXY Backend

Spring Boot REST API backend for the DeliverXY on-demand delivery platform.

## 📋 Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)

---

## Overview

The DeliverXY backend is a Spring Boot 3.5.0 application that provides REST API endpoints for:
- User authentication and authorization
- Delivery management and tracking
- Wallet system and payment processing
- KYC verification
- Vehicle management
- Earnings and payouts
- Rating system
- Admin operations

---

## Technology Stack

- **Framework**: Spring Boot 3.5.0
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **Other**:
  - Lombok (code generation)
  - Jackson (JSON processing)
  - BCrypt (password hashing)
  - Stripe Java SDK (prepared for future integration)

---

## Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6+ (or use included Maven Wrapper)
- **PostgreSQL**: 15+ (or use Docker)
- **IDE**: IntelliJ IDEA / Eclipse / VS Code (optional)

---

## Setup & Installation

### 1. Enter Backend Directory

From the project root:

```bash
cd backend
```

### 2. Database Setup

#### Option A: Using Docker Compose (Recommended)

From the project root:

```bash
docker-compose up -d postgres
```

This will start PostgreSQL on port `5432`.

#### Option B: Local PostgreSQL

1. Install PostgreSQL 15+
2. Create database:
```sql
CREATE DATABASE deliverxy;
CREATE USER deliverxy WITH PASSWORD 'deliverxy';
GRANT ALL PRIVILEGES ON DATABASE deliverxy TO deliverxy;
```

### 3. Configure Environment Variables

Create `.env` file in project root (or set environment variables):

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/deliverxy
SPRING_DATASOURCE_USERNAME=deliverxy
SPRING_DATASOURCE_PASSWORD=deliverxy

# JWT
JWT_SECRET=your-secret-key-here-make-it-very-long-and-secure-in-production

# File Uploads
APP_UPLOADS_DIR=./uploads

# Stripe (for future integration)
STRIPE_SECRET_KEY=
STRIPE_PUBLISHABLE_KEY=
```

### 4. Build the Project

```bash
# Using Maven Wrapper (recommended)
./mvnw clean install

# Or using system Maven
mvn clean install
```

---

## Configuration

### Application Profiles

The application supports multiple profiles:

- **dev**: Development profile (default)
- **prod**: Production profile

Set active profile via environment variable:
```bash
export SPRING_PROFILES_ACTIVE=dev
```

### Configuration Files

- `application.properties`: Base configuration
- `application-dev.properties`: Development overrides
- `application-prod.properties`: Production overrides
- `application-env.properties`: Environment-specific (git-ignored)

### Key Configuration Properties

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/deliverxy
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=your-secret-key
jwt.access-expiration-ms=3600000  # 1 hour
jwt.refresh-expiration-ms=604800000  # 7 days

# File Upload
spring.servlet.multipart.max-file-size=10MB
app.uploads.dir=./uploads
```

---

## Running the Application

### Development Mode

```bash
# Using Maven Wrapper (Linux/macOS/Git Bash)
./mvnw spring-boot:run

# Windows (PowerShell/CMD)
.\mvnw.cmd spring-boot:run

# Or using system Maven
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080`

### Using IDE

1. Open project in IntelliJ IDEA / Eclipse
2. Run `BackendApplication.java` main method
3. Ensure PostgreSQL is running

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

---

## Project Structure

```
src/main/java/com/deliverXY/backend/
├── BackendApplication.java          # Main application class
└── NewCode/
    ├── auth/                        # Authentication & Authorization
    │   ├── controller/
    │   ├── service/
    │   └── validator/
    ├── deliveries/                  # Delivery Management
    │   ├── controller/
    │   ├── domain/
    │   ├── dto/
    │   ├── repository/
    │   └── service/
    ├── payments/                    # Payment Processing
    │   ├── controller/
    │   ├── domain/
    │   ├── service/Provider/
    │   └── repository/
    ├── wallet/                      # Wallet System
    │   ├── controller/
    │   ├── domain/
    │   └── service/
    ├── user/                        # User Management
    ├── vehicle/                     # Vehicle Management
    ├── kyc/                         # KYC Verification
    ├── earnings/                    # Earnings & Payouts
    ├── rating/                      # Rating System
    ├── admin/                       # Admin Operations
    ├── notifications/               # Notifications
    ├── drivers/                     # Driver Services
    ├── security/                    # Security Configuration
    ├── common/                      # Shared Utilities
    └── exceptions/                  # Exception Handling
```

---

## API Documentation

### Base URL

```
http://localhost:8080/api
```

### Authentication Endpoints

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - User logout

### Delivery Endpoints

- `GET /api/deliveries` - Get all deliveries (paginated)
- `GET /api/deliveries/{id}` - Get delivery by ID
- `POST /api/deliveries` - Create delivery
- `PUT /api/deliveries/{id}` - Update delivery
- `GET /api/deliveries/mine` - Get user's deliveries
- `POST /api/deliveries/{id}/accept` - Accept delivery (agent)
- `PUT /api/deliveries/{id}/status` - Update delivery status

### Wallet Endpoints

- `GET /api/wallet/balance` - Get wallet balance
- `GET /api/wallet/transactions` - Get transaction history
- `POST /api/wallet/top-up` - Top-up wallet
- `POST /api/wallet/withdraw` - Withdraw funds

### Payment Endpoints

- `POST /api/payments` - Process payment
- `GET /api/payments/{id}` - Get payment details

### User Endpoints

- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update profile
- `GET /api/agents` - Get available agents

### Vehicle Endpoints

- `GET /api/vehicles` - Get user's vehicles
- `POST /api/vehicles` - Register vehicle
- `PUT /api/vehicles/{id}` - Update vehicle
- `DELETE /api/vehicles/{id}` - Delete vehicle

### KYC Endpoints

- `POST /api/kyc/upload` - Upload KYC document
- `GET /api/kyc/status` - Get KYC status

### Earnings Endpoints

- `GET /api/earnings` - Get agent earnings
- `GET /api/earnings/payouts` - Get payout history
- `POST /api/earnings/payouts` - Request payout

### Admin Endpoints

- `GET /api/admin/dashboard` - Dashboard statistics
- `GET /api/admin/users` - Get all users
- `PUT /api/admin/kyc/{userId}/approve` - Approve KYC
- `PUT /api/admin/kyc/{userId}/reject` - Reject KYC
- `GET /api/admin/deliveries` - Get all deliveries
- `GET /api/admin/earnings` - Earnings overview
- `GET /api/admin/payouts` - Payout overview
- `GET /api/admin/pricing-config` - List pricing configs
- `GET /api/admin/pricing-config/{id}` - Get pricing config by ID
- `PUT /api/admin/pricing-config/{id}` - Update pricing config

### API Response Format

```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

---

## Development

### Code Style

- Follow Java naming conventions
- Use Lombok annotations for boilerplate code
- DTOs for data transfer
- Service layer for business logic
- Repository layer for data access

### Adding New Features

1. Create domain entity in appropriate package
2. Create repository interface
3. Create service interface and implementation
4. Create DTOs for request/response
5. Create controller with REST endpoints
6. Add security configuration if needed

### Database Migrations

The application uses Hibernate `ddl-auto=update` for development. For production, consider using Flyway or Liquibase.

---

## Testing

### Running Tests

```bash
./mvnw test
```

### Test Structure

- Unit tests: Service layer tests
- Integration tests: API endpoint tests
- Repository tests: Data access tests

---

## Deployment

### Docker

Build Docker image:

```bash
docker build -t deliverxy-backend .
```

Run container:

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/deliverxy \
  -e SPRING_DATASOURCE_USERNAME=deliverxy \
  -e SPRING_DATASOURCE_PASSWORD=deliverxy \
  deliverxy-backend
```

### Kubernetes (future)

The repo includes full Kubernetes manifests in [kubernetes/](../kubernetes/README.md). The backend is defined in `kubernetes/backend-deployment.yaml` (ConfigMap, secrets for DB and JWT). Do not commit real secrets; create them with `kubectl create secret` — see [kubernetes/README.md](../kubernetes/README.md).

### Environment Variables for Production

```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/deliverxy
SPRING_DATASOURCE_USERNAME=deliverxy
SPRING_DATASOURCE_PASSWORD=<secure-password>
JWT_SECRET=<secure-random-secret>
APP_UPLOADS_DIR=/app/uploads
```

---

## Troubleshooting

### Database Connection Issues

- Ensure PostgreSQL is running
- Check connection URL, username, and password
- Verify database exists

### Port Already in Use

Change port in `application.properties`:
```properties
server.port=8081
```

### File Upload Issues

- Ensure uploads directory exists and is writable
- Check file size limits in configuration
- Verify `app.uploads.dir` path is correct

### JWT Token Issues

- Ensure `jwt.secret` is set and consistent
- Check token expiration times
- Verify token is included in Authorization header: `Bearer <token>`

---

## Security Notes

- **Never commit** `.env` files or secrets
- Use strong JWT secrets in production
- Enable HTTPS in production
- Regularly update dependencies
- Follow OWASP security guidelines

---
