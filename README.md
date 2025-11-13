# Box Sender - Package Tracking System

A full-stack web application for managing package deliveries with role-based access control, automated pickup codes, and email notifications.

**Team:** Casey Cunningham, Tenzin Kunga, Nick Herberg, Brian Willems
**Course:** ICS 370 - Software Design and Models | Metropolitan State University

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [User Roles & Permissions](#user-roles--permissions)
- [Usage Guide](#usage-guide)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Email Configuration](#email-configuration)
- [Troubleshooting](#troubleshooting)

## Features

### Core Functionality
- **Role-Based Access Control** - Three user levels: Admin, Mailroom Staff, Employee
- **Automated Pickup Codes** - 6-character secure codes generated for each package
- **Email Notifications** - Automatic HTML emails with pickup codes and tracking details
- **Advanced Search** - Multi-field search with sorting, filtering, and CSV/PDF export
- **Package Management** - Complete lifecycle tracking from receipt to pickup
- **Admin Panel** - Employee account management (ADMIN role only)

### Security Features
- BCrypt password hashing (10 rounds)
- Spring Security with method-level authorization
- Session-based authentication
- CSRF protection
- Audit logging for administrative actions


## Technology Stack

**Backend**
- Java 21, Spring Boot 3.5.6, Spring Security
- Spring Data JPA, Hibernate 6.6
- MySQL database
- Maven build tool

**Frontend**
- HTML5, CSS3, Bootstrap 5.3.3
- JavaScript (ES6+), Fetch API

**Email & Security**
- JavaMailSender with Brevo SMTP
- BCrypt password hashing
- SecureRandom for pickup codes

## Getting Started

### Prerequisites

- Java 21+, Maven 3.6+, MySQL 8.0+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)
- Brevo account for email notifications (optional, free tier available)

### Installation

1. **Clone and navigate to project**
   ```bash
   git clone <repository-url>
   cd Box_Sender
   ```

2. **Set up MySQL database**
   ```sql
   mysql -u root -p
   CREATE DATABASE boxsender;
   USE boxsender;
   SOURCE boxsender_complete.sql;
   ```

3. **Configure database connection**

   Edit [app/src/main/resources/application.properties](app/src/main/resources/application.properties):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3307/boxsender
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

4. **Build and run**
   ```bash
   cd app
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access application**

   Open browser: http://localhost:8080

### First Time Setup

1. Click "Register" to create an account (default role: EMPLOYEE)
2. To create an admin account:
   ```sql
   UPDATE employees SET role = 'ADMIN' WHERE email = 'your-email@example.com';
   ```
3. Configure email notifications (see [Email Configuration](#email-configuration))

## User Roles & Permissions

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full access: manage employees, log packages, pickup packages, search |
| **MAILROOM_STAFF** | Operational access: log packages, pickup packages, search |
| **EMPLOYEE** | Limited access: pickup packages, search only |

### Key Endpoint Permissions

| Endpoint | ADMIN | MAILROOM_STAFF | EMPLOYEE |
|----------|-------|----------------|----------|
| Log Package (`POST /api/packages`) | ✅ | ✅ | ❌ |
| Package Pickup (`PUT /api/packages/{id}/pickup`) | ✅ | ✅ | ✅ |
| Search Packages (`GET /api/packages/search`) | ✅ | ✅ | ✅ |
| Admin Panel (`/api/admin/**`) | ✅ | ❌ | ❌ |

## Usage Guide

### Logging a Package (ADMIN & MAILROOM_STAFF only)

1. Navigate to "Log Package" from dashboard
2. Enter tracking number, carrier, and description
3. Enter recipient name and email
4. System automatically:
   - Generates 6-character pickup code
   - Sends email notification to recipient
   - Displays confirmation with code

### Package Pickup (All Roles)

**Option 1: Using Pickup Code (Recommended)**
1. Recipient receives email with 6-character code (e.g., "A7K2M9")
2. Staff enters code in pickup form
3. System verifies code and displays package details
4. Staff confirms recipient identity and marks as picked up

**Option 2: Using Tracking Number**
1. Staff enters tracking number
2. System finds package
3. Staff confirms recipient identity and marks as picked up

### Searching Packages (All Roles)

1. Navigate to "Search Packages"
2. Use search filters: tracking number, carrier, recipient name/email, status
3. Sort results by any column
4. Export to CSV or PDF

### Admin Panel (ADMIN only)

1. Navigate to "Admin Panel" from dashboard
2. Manage employee accounts:
   - **Create**: Click "Create Account", enter details and assign role
   - **Edit**: Update employee information or change role
   - **Delete**: Remove employee accounts (cannot delete own account)

## API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register employee (default: EMPLOYEE role) | None |
| POST | `/api/auth/login` | Login employee | None |
| GET | `/api/auth/me` | Get current user info with role | Authenticated |
| POST | `/api/auth/logout` | Logout | None |

### Package Endpoints

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/api/packages` | Log package (auto-generates pickup code) | ADMIN, MAILROOM_STAFF |
| GET | `/api/packages/search` | Search packages with filters and sorting | All |
| PUT | `/api/packages/{id}/pickup` | Mark package as picked up | All |

### Admin Endpoints

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/api/admin/employees` | Get all employees | ADMIN |
| POST | `/api/admin/employees` | Create employee account | ADMIN |
| PUT | `/api/admin/employees/{id}` | Update employee details | ADMIN |
| DELETE | `/api/admin/employees/{id}` | Delete employee | ADMIN |

### Dashboard Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/stats` | Get package statistics |
| GET | `/api/dashboard/recent` | Get 20 most recent packages |
| GET | `/api/dashboard/overdue` | Get packages >7 days old |

## Database Schema

### Key Tables

**employees**
- Stores user accounts with BCrypt-hashed passwords
- `role` field: ADMIN, MAILROOM_STAFF, or EMPLOYEE (default)
- Indexed on email and role for fast lookups

**packages**
- Tracks package information and status
- `pickup_code`: 6-character alphanumeric code (auto-generated)
- `status`: 'received' or 'picked'
- Foreign keys to recipients and employees

**recipients**
- Stores recipient contact information
- Unique email addresses
- Optional department field

See [boxsender_complete.sql](boxsender_complete.sql) for complete schema.

## Email Configuration

### Brevo SMTP Setup

1. **Create account** at https://www.brevo.com (free tier: 300 emails/day)
2. **Generate SMTP key**: Settings → SMTP & API → SMTP tab
3. **Configure application**:

   **Option A: Environment Variables (Recommended)**
   ```bash
   # Windows PowerShell
   $env:BREVO_USERNAME="your-email@example.com"
   $env:BREVO_PASSWORD="your-smtp-key"
   $env:BREVO_FROM_EMAIL="your-verified-email@example.com"

   # Linux/Mac
   export BREVO_USERNAME=your-email@example.com
   export BREVO_PASSWORD=your-smtp-key
   export BREVO_FROM_EMAIL=your-verified-email@example.com
   ```

   **Option B: Direct Configuration**

   Edit [application.properties](app/src/main/resources/application.properties):
   ```properties
   spring.mail.username=your-email@example.com
   spring.mail.password=your-smtp-key
   brevo.from.email=your-verified-email@example.com
   ```

### Email Features

Recipients receive HTML emails with:
- Personalized greeting
- Pickup code displayed prominently
- Tracking number and carrier info
- Pickup instructions
- Mobile-responsive design

## Troubleshooting

### Login Issues

**Problem:** Cannot log in after registration
**Solution:** Ensure role column exists and has default value:
```sql
ALTER TABLE employees ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'EMPLOYEE';
UPDATE employees SET role = 'EMPLOYEE' WHERE role IS NULL;
```

### Access Denied Errors

**Problem:** "Access Denied" when logging packages
**Solution:** User has EMPLOYEE role. Promote to MAILROOM_STAFF or ADMIN:
```sql
UPDATE employees SET role = 'MAILROOM_STAFF' WHERE email = 'user@example.com';
```

### Application Won't Start

**Solutions:**
- Verify MySQL is running: `mysql -u root -p`
- Check database exists: `SHOW DATABASES;`
- Verify credentials in [application.properties](app/src/main/resources/application.properties)
- Check for port conflicts on 8080

### Email Not Sending

**Solutions:**
- Verify Brevo SMTP credentials
- Check sender email is verified in Brevo dashboard
- Check recipient spam/junk folder
- Review console for connection errors

## Quick Reference

### Application URLs
- **Login:** http://localhost:8080
- **Dashboard:** http://localhost:8080/dashboard.html
- **Admin Panel:** http://localhost:8080/admin.html (ADMIN only)

### Common Commands

**Start application:**
```bash
cd app
mvn spring-boot:run
```

**Create admin user:**
```sql
UPDATE employees SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

**View all roles:**
```sql
SELECT first_name, last_name, email, role FROM employees ORDER BY role DESC;
```

## License

Educational project for ICS 370 - Software Design and Models at Metropolitan State University.

---

**Team:** Casey Cunningham, Tenzin Kunga, Nick Herberg, Brian Willems
