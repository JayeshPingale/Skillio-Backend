# Skillio CRM Backend

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT%20%2B%20RBAC-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-black?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

**A secure Spring Boot backend for Skillio CRM with JWT authentication, permission-based RBAC, lead management, student lifecycle management, payments, invoices, commissions, notifications, and audit logging.**

</div>

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Core Modules](#core-modules)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Security](#security)
- [RBAC Model](#rbac-model)
- [Project Structure](#project-structure)
- [Setup Instructions](#setup-instructions)
- [Configuration](#configuration)
- [Run the Application](#run-the-application)
- [Authentication](#authentication)
- [Important API Endpoints](#important-api-endpoints)
- [Default Admin User](#default-admin-user)
- [Testing](#testing)
- [Future Improvements](#future-improvements)
- [Author](#author)

---

## Overview

**Skillio CRM Backend** is a backend system designed for managing CRM, sales, academic, and finance-related workflows in one platform.

The project supports:
- secure authentication and authorization
- users, roles, permissions
- lead management and lead assignment
- follow-up tracking
- course and batch management
- students and enrollments
- student fees and payments
- invoices
- commissions and commission payments
- notifications
- audit logs

This backend follows a layered Spring Boot architecture and uses **JWT + Spring Security + permission-based RBAC** for secure API access.

---

## Features

### Authentication & Authorization
- JWT-based authentication
- permission-based RBAC
- Spring Security integration
- controller-level protection using `@PreAuthorize`
- permissions included in JWT claims
- permissions included in login response
- stateless authorization using token data

### User & Access Control
- user management
- role management
- permission management
- role-permission mapping
- default admin initialization

### CRM Operations
- lead creation, update, delete
- lead assignment and unassignment
- lead status tracking
- lead source management
- follow-up management

### Academic Operations
- course management
- batch management
- student management
- enrollment workflows
- target management

### Finance Operations
- student fees
- payments
- payment installments
- invoices
- commissions
- commission payments

### Support Modules
- notifications
- audit logging
- history tracking
- self-scoped `/my-*` APIs for user-specific access

---

## Core Modules

The backend currently includes these main modules:

- `Auth`
- `User`
- `Role`
- `Permission`
- `Lead`
- `Lead Source`
- `Lead Status History`
- `Follow Up`
- `Course`
- `Batch`
- `Student`
- `Enrollment`
- `Student Fees`
- `Payment`
- `Payment Installment`
- `Invoice`
- `Commission`
- `Commission Payment`
- `Target`
- `Notification`
- `Audit Log`

---

## Tech Stack

### Backend
- **Java 17**
- **Spring Boot**
- **Spring Web**
- **Spring Data JPA**
- **Spring Security**
- **JWT**
- **Hibernate**
- **Lombok**
- **Maven**

### Database
- **MySQL 8+**

### Documentation / Tools
- **Swagger / OpenAPI**
- **Postman**
- **Maven Wrapper**

---

## Architecture

This project follows a standard layered architecture:
```text
Client / Frontend
      ↓
REST Controllers
      ↓
Service Layer
      ↓
Repository Layer
      ↓
MySQL Database
```

### Layer Responsibilities

**Controller Layer**
- exposes REST APIs
- handles request/response flow
- applies authorization with `@PreAuthorize`

**Service Layer**
- contains business logic
- manages workflows
- coordinates domain operations

**Repository Layer**
- handles persistence via Spring Data JPA

**Security Layer**
- authenticates login requests
- generates JWT tokens
- validates tokens on each request
- sets Spring Security authorities from JWT permissions

---

## Security

This backend uses **JWT authentication + Spring Security + permission-based authorization**.

### Request Security Flow

1. User logs in with email and password
2. Backend authenticates the user
3. Backend generates JWT token
4. JWT includes:
   - `userId`
   - `role`
   - `fullName`
   - `permissions`
5. For protected APIs:
   - token is extracted from `Authorization` header
   - token is validated
   - permissions are read from JWT
   - authorities are added to Spring Security context
   - access is enforced using `@PreAuthorize`

### Why This Approach
- stateless authentication
- no DB call required in JWT filter
- permission-level access control
- frontend and backend remain aligned using the same permission strings

---

## RBAC Model

This project uses **permission-based RBAC**.

### Entity Relationship
- A `User` has one `Role`
- A `Role` has many `RolePermission`
- A `RolePermission` maps a `Role` to a `Permission`
- A `Permission` has:
  - `permissionName`
  - `module`
  - `action`

### Example Permissions
```
USER_CREATE     USER_READ     USER_UPDATE     USER_DELETE     USER_LIST
LEAD_CREATE     LEAD_READ     LEAD_UPDATE     LEAD_DELETE     LEAD_LIST
COURSE_CREATE   COURSE_READ   COURSE_UPDATE   COURSE_DELETE   COURSE_LIST
```

### Authorization Example
```java
@PreAuthorize("hasAuthority('LEAD_READ')")
```

> **Important:** Authorization is permission-driven. Roles group permissions, but API protection happens through exact permission names.

---

## Project Structure
```
src/main/java/com/skillio
│
├── config/                # configuration classes
├── controller/            # REST controllers
├── dto/                   # request/response DTOs
├── entities/              # JPA entities
├── exepection/            # exception handling
├── repositories/          # repositories
├── security/              # security and JWT logic
├── services/              # service interfaces
└── services/impl/         # service implementations

src/main/resources
└── application.properties

src/test/java
└── test classes
```

### Key Backend Files
- `SecurityConfig`
- `JwtTokenProvider`
- `JwtAuthenticationFilter`
- `CustomUserDetails`
- `CustomUserDetailsService`
- `PermissionNameNormalizer`
- `DataInitializer`

---

## Setup Instructions

### Prerequisites

Make sure you have installed:
- Java 17
- MySQL 8+
- Maven 3.9+ or use Maven Wrapper
- Git

### Clone Repository
```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
cd YOUR_REPO_NAME
```

### Create Database
```sql
CREATE DATABASE skillio_crm;
```

### Update Application Properties

Edit `src/main/resources/application.properties` and configure:
- DB URL
- DB username
- DB password
- JWT secret
- JWT expiration

---

## Configuration

Example `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/Skillio_db?createDatabaseIfNotExist=true&useSSL=false&serverTimeZone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password


spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

app.jwt-secret=YOUR_BASE64_SECRET_KEY
app.jwt-expiration-milliseconds=86400000

server.port=8080
```

> **Notes:**
> - Use a valid Base64-encoded JWT secret
> - `ddl-auto=update` is suitable for development
> - Use migrations for production environments

---

## Run the Application

### Build
```bash
./mvnw clean install
```

### Start
```bash
./mvnw spring-boot:run
```

### On Windows PowerShell
```powershell
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run
```

Application will start on:
```
http://localhost:8080
```

---

## Authentication

### Login API
```
POST /api/auth/login
Content-Type: application/json
```

### Sample Request
```json
{
  "email": "admin@skillio.com",
  "password": "admin@123"
}
```

### Sample Response
```json
{
  "token": "jwt-token",
  "type": "Bearer",
  "userId": 1,
  "roleName": "ROLE_ADMIN",
  "fullName": "Admin User",
  "permissions": [
    "USER_CREATE",
    "USER_READ",
    "USER_UPDATE",
    "USER_DELETE",
    "USER_LIST"
  ]
}
```

### Authenticated Request Header
```
Authorization: Bearer <jwt-token>
```

---

## Important API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login and get JWT token |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users` | Create user |
| GET | `/api/users` | List all users |
| GET | `/api/users/{userId}` | Get user by ID |
| PUT | `/api/users/{userId}` | Update user |
| DELETE | `/api/users/{userId}` | Delete user |

### Roles & Permissions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/roles` | Create role |
| GET | `/api/roles` | List all roles |
| POST | `/api/roles/assign-permissions` | Assign permissions to role |
| GET | `/api/permissions` | List all permissions |
| POST | `/api/permissions` | Create permission |

### Leads
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/leads` | Create lead |
| GET | `/api/leads` | List all leads |
| GET | `/api/leads/my-leads` | Get current user's leads |
| PUT | `/api/leads/assign` | Assign lead |
| PUT | `/api/leads/change-status` | Change lead status |

### Follow-Ups
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/follow-ups` | Create follow-up |
| GET | `/api/follow-ups` | List all follow-ups |
| GET | `/api/follow-ups/my-follow-ups` | Get current user's follow-ups |

### Courses & Batches
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/courses` | List all courses |
| POST | `/api/courses` | Create course |
| GET | `/api/batches` | List all batches |
| POST | `/api/batches` | Create batch |

### Students & Enrollments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/students` | List all students |
| GET | `/api/students/my-students` | Get current user's students |
| GET | `/api/enrollments` | List all enrollments |
| GET | `/api/enrollments/my-enrollments` | Get current user's enrollments |

### Fees & Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/student-fees` | List all student fees |
| GET | `/api/student-fees/my-students` | Get current user's student fees |
| GET | `/api/payments` | List all payments |
| GET | `/api/payments/my-payments` | Get current user's payments |

### Invoices & Commissions
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/invoices` | List all invoices |
| GET | `/api/invoices/my-students-invoices` | Get current user's invoices |
| GET | `/api/commissions` | List all commissions |
| GET | `/api/commissions/my-commissions` | Get current user's commissions |
| POST | `/api/commissions/request` | Request a commission |

### Other Modules
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications` | List notifications |
| GET | `/api/audit-logs` | List audit logs |
| GET | `/api/targets` | List targets |

---

## Default Admin User

On startup, the backend initializes a default admin user if it does not exist.

### Default Credentials

| Field | Value |
|-------|-------|
| Email | `admin@skillio.com` |
| Password | `admin@123` |

### Behavior
- creates `ROLE_ADMIN` if missing
- auto-assigns all existing permissions to admin role if needed
- creates admin user if missing

> This is useful for first-time local setup.

---

## 🧪 Testing
```bash
./mvnw test
```

On Windows PowerShell:
```powershell
.\mvnw.cmd test
```

---

## 🚀 Future Improvements

- [ ] Refresh token support
- [ ] Docker & Docker Compose setup
- [ ] Flyway / Liquibase database migrations
- [ ] Better seed scripts for permissions
- [ ] Stronger integration test coverage
- [ ] Rate limiting on public APIs
- [ ] Centralized audit reporting dashboard
- [ ] Production logging strategy (ELK / Logback)
- [ ] Caching for expensive dashboard queries

---

## 🔗 Frontend Integration Notes

If you are connecting a frontend (e.g. Angular) to this backend:

1. Authenticate via `POST /api/auth/login`
2. Store the JWT token securely (e.g. `localStorage` or `HttpOnly cookie`)
3. Send `Authorization: Bearer <token>` header for all protected APIs
4. Use the `permissions` array from login response for frontend route/button-level authorization
5. Re-login after role-permission changes so the JWT reflects updated access

---

## 👨‍💻 Author

**Jayesh Pingale**

[![GitHub](https://img.shields.io/badge/GitHub-JayeshPingale-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/JayeshPingale)
[![Email](https://img.shields.io/badge/Email-jayeshpingale27@gmail.com-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:jayeshpingale27@gmail.com)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/your-link-here)

---

<div align="center">

**Skillio CRM Backend** &nbsp;•&nbsp; Built with ❤️ using Spring Boot &nbsp;•&nbsp; Java 17

⭐ If you found this project useful, consider giving it a star!

</div>
