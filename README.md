# Box Sender - Package Tracking System

A comprehensive full-stack web application for managing package deliveries in mailroom environments with role-based access control. The system supports three user roles (Admin, Mailroom Staff, and Employee) with different permission levels, automated pickup codes, email notifications, and comprehensive package tracking.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [User Roles & Permissions](#user-roles--permissions)
- [Project Structure](#project-structure)
- [How to Use](#how-to-use)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Security](#security)
- [Email Configuration](#email-configuration)
- [Troubleshooting](#troubleshooting)

---

## Overview

**Box Sender** is a complete package tracking system designed for mailrooms, front desks, or any environment where packages need to be logged, tracked, and recipients notified. The system provides comprehensive package lifecycle management from arrival to pickup, with role-based access control to ensure proper authorization.

### Team Members
- Casey Cunningham
- Tenzin Kunga
- Nick Herberg
- Brian Willems

### Key Highlights
- 🔐 **Role-Based Access Control (RBAC)** - Three permission levels: Admin, Mailroom Staff, Employee
- 🎫 **Automated Pickup Codes** - 6-character secure codes for package verification
- ✉️ **Email Notifications** - Automatic notifications with pickup codes
- 🔍 **Advanced Search** - Multi-field search with sorting and filtering
- 📊 **CSV/PDF Export** - Export search results and dashboard data
- 👥 **Admin Panel** - Full employee management (create, edit, delete accounts)

---

## Features

### 🔐 Role-Based Access Control (RBAC)

The system implements a three-tier permission model:

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full system access + employee management (create/edit/delete accounts, change roles) |
| **MAILROOM_STAFF** | Operational access (log packages, pickup packages, search) |
| **EMPLOYEE** | Limited access (pickup packages, search only - cannot log packages) |

**Security Features:**
- Default role: EMPLOYEE (least privilege principle)
- Admins can manage roles via Admin Panel
- Method-level security with `@PreAuthorize` annotations
- Dynamic UI rendering based on user role
- Prevents privilege escalation

### ✅ Core Functionality (All 7 Use Cases Implemented)

1. **UC-01: Employee Authentication**
   - Secure registration and login with BCrypt password encryption
   - Session-based authentication
   - Role assignment on registration (default: EMPLOYEE)
   - Automatic session management

2. **UC-02: Package Logging** *(ADMIN & MAILROOM_STAFF only)*
   - Quick entry of package details with tracking numbers
   - **Automatic 6-character pickup code generation** 🔐
   - Automatic recipient creation or lookup
   - Real-time email notifications with pickup code
   - Success confirmation showing code and email status
   - Duplicate tracking number prevention

3. **UC-03: Package Pickup** *(All roles)*
   - **Secure pickup code verification** or tracking number lookup 🔐
   - Mark packages as picked up with valid code or tracking number
   - Signature/verification capture
   - Automatic timestamp recording
   - Staff notes for audit trail
   - Status change from "received" to "picked"

4. **UC-04: Recipient Management** *(ADMIN & MAILROOM_STAFF)*
   - Full CRUD operations for recipients
   - View all recipients
   - Update recipient information
   - Department assignment

5. **UC-05: Package Search** *(All roles)*
   - **Multi-field comprehensive search**
   - Search by: tracking number, carrier, description, recipient name/email
   - **Dynamic sorting** by any column (ascending/descending)
   - Filter by status (received/picked)
   - **CSV and PDF export** of search results
   - Real-time result count
   - Relative timestamps ("2 hours ago")

6. **UC-06: Generate Reports** *(ADMIN & MAILROOM_STAFF)*
   - Daily package logs
   - Overdue package reports (>7 days)
   - Recipient package history
   - System summary statistics

7. **UC-07: Logout** *(All roles)*
   - Secure session termination

### 👥 Admin Panel Features *(ADMIN only)*

- **Create Employee Accounts** - Add new employees with specific roles
- **Edit Employee Details** - Update name, email, password, and role
- **Delete Employees** - Remove employee accounts (cannot delete own account)
- **View All Employees** - Comprehensive table with role badges
- **Role Management** - Promote/demote users between roles

### 📊 Dashboard & Analytics

- Real-time package statistics
- Recent activity feed with **CSV/PDF export**
- Overdue package alerts
- Pickup rate tracking
- Total packages and recipient counts
- Role-based UI (shows/hides Log Package and Admin Panel based on role)

### 🔒 Security Features

- BCrypt password hashing (10 rounds)
- Spring Security integration with method-level authorization
- Session-based authentication with HTTPOnly cookies
- Role-based access control (RBAC)
- HTML escaping to prevent XSS attacks
- CSRF protection
- Protected API endpoints with `@PreAuthorize`
- Audit logging for role changes and account management

---

## Technology Stack

### Backend
- **Java 21** - Programming language
- **Spring Boot 3.5.6** - Application framework
- **Spring Security** - Authentication and authorization with `@EnableMethodSecurity`
- **Spring Data JPA** - Database access with repository pattern
- **Hibernate 6.6** - ORM (Object-Relational Mapping)
- **MySQL** - Production database with optimized indexes
- **Maven** - Dependency management and build tool

### Frontend
- **HTML5** - Page structure
- **CSS3** - Custom styling
- **Bootstrap 5.3.3** - Responsive UI framework
- **JavaScript (ES6+)** - Client-side logic with async/await
- **Fetch API** - RESTful API consumption

### Email & Security
- **JavaMailSender** - Spring email abstraction
- **Brevo (SMTP)** - Email service provider (300 emails/day free tier)
- **SecureRandom** - Cryptographically secure pickup code generation

---

## Architecture

The application follows a **layered architecture** pattern with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│       Frontend (Browser)                    │
│   HTML/CSS/JavaScript + Bootstrap           │
│   - index.html (Login/Register)             │
│   - dashboard.html (Statistics)             │
│   - log.html (Package Logging)              │
│   - pickup.html (Mark Pickup)               │
│   - search.html (Search Packages)           │
│   - admin.html (Admin Panel) 🔐 ADMIN ONLY  │
└──────────────┬──────────────────────────────┘
               │ HTTP/JSON (REST API)
               ↓
┌─────────────────────────────────────────────┐
│       Controllers (REST API Layer)          │
│   - AuthController (authentication)         │
│   - PackageController (@PreAuthorize)       │
│   - RecipientController                     │
│   - ReportController                        │
│   - DashboardController                     │
│   - AdminController 🔐 ADMIN ONLY           │
└──────────────┬──────────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────────┐
│     Services (Business Logic Layer)         │
│   - EmailService                            │
│   - ReportService                           │
└──────────────┬──────────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────────┐
│   Repositories (Data Access Layer)          │
│   - PackageRepository                       │
│   - RecipientRepository                     │
│   - EmployeeRepository                      │
│   - ReportRepository                        │
└──────────────┬──────────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────────┐
│        Database (MySQL)                     │
│   - employees (authentication + roles)      │
│   - packages (package tracking)             │
│   - recipients (recipient info)             │
│   - reports (generated reports)             │
│   - activity_log (audit trail)              │
│   - notifications (email history)           │
└─────────────────────────────────────────────┘
```

### Security Architecture

```
┌─────────────────────────────────────────────┐
│     Spring Security Filter Chain            │
│   - Session Management                      │
│   - Authentication Filter                   │
│   - Authorization Filter                    │
└──────────────┬──────────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────────┐
│     SecurityConfig                          │
│   - @EnableMethodSecurity                   │
│   - UserDetailsService (role loading)       │
│   - BCryptPasswordEncoder                   │
│   - DaoAuthenticationProvider               │
└──────────────┬──────────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────────┐
│     Controllers (Method-Level Security)     │
│   - @PreAuthorize("hasRole('ADMIN')")       │
│   - @PreAuthorize("hasAnyRole(...)")        │
└──────────────┬──────────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────────┐
│     Database (employees table)              │
│   - role VARCHAR(50) DEFAULT 'EMPLOYEE'     │
└─────────────────────────────────────────────┘
```

---

## Getting Started

### Prerequisites

- **Java 21 or higher** ([Download](https://adoptium.net/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))
- **MySQL 8.0+** ([Download](https://dev.mysql.com/downloads/mysql/))
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code recommended)
- **Brevo Account** (for email notifications - free tier available)

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Box_Sender
   ```

2. **Set up MySQL database**

   Create database and import schema:
   ```bash
   mysql -u root -p
   CREATE DATABASE boxsender;
   USE boxsender;
   SOURCE boxsender.sql;
   ```

3. **Configure application**

   Edit `app/src/main/resources/application.properties`:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:mysql://localhost:3307/boxsender
   spring.datasource.username=root
   spring.datasource.password=your_password

   # JPA/Hibernate settings
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=false
   ```

4. **Set up Brevo email (optional but recommended)**

   See [Email Configuration](#email-configuration) section below.

5. **Build and run**
   ```bash
   cd app
   mvn clean install
   mvn spring-boot:run
   ```

6. **Access the application**

   Open browser: `http://localhost:8080`

   **First time setup:**
   - Click "Register" to create an employee account
   - Use your email and create a password
   - You'll be assigned the EMPLOYEE role by default
   - You'll be automatically logged in

7. **Create admin account** *(Optional)*

   To create an admin account, either:
   - **Option A:** Have an existing admin use the Admin Panel to promote you
   - **Option B:** Manually update the database:
     ```sql
     UPDATE employees SET role = 'ADMIN' WHERE email = 'your-email@example.com';
     ```

---

## User Roles & Permissions

### Role Hierarchy

```
ADMIN
  └─> Full Access
      ├─ Manage employee accounts (create, edit, delete, change roles)
      ├─ Access Admin Panel
      ├─ Log packages
      ├─ Pickup packages
      └─ Search packages

MAILROOM_STAFF
  └─> Operational Access
      ├─ Log packages
      ├─ Pickup packages
      └─ Search packages

EMPLOYEE
  └─> Limited Access
      ├─ Pickup packages
      └─ Search packages
```

### Dashboard Views by Role

**ADMIN sees:**
- Dashboard
- Log Package card
- Package Pickup card
- Search Packages card
- **Admin Panel card** 🔐

**MAILROOM_STAFF sees:**
- Dashboard
- Log Package card
- Package Pickup card
- Search Packages card

**EMPLOYEE sees:**
- Dashboard
- Package Pickup card
- Search Packages card

### Protected Endpoints

| Endpoint | ADMIN | MAILROOM_STAFF | EMPLOYEE |
|----------|-------|----------------|----------|
| `POST /api/packages` | ✅ | ✅ | ❌ |
| `PUT /api/packages/{id}/pickup` | ✅ | ✅ | ✅ |
| `GET /api/packages/search` | ✅ | ✅ | ✅ |
| `GET /api/admin/**` | ✅ | ❌ | ❌ |
| `POST /api/admin/employees` | ✅ | ❌ | ❌ |
| `PUT /api/admin/employees/{id}` | ✅ | ❌ | ❌ |
| `DELETE /api/admin/employees/{id}` | ✅ | ❌ | ❌ |

---

## Project Structure

```
Box_Sender/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/boxsender/
│   │   │   │   ├── AppApplication.java              # Main entry point
│   │   │   │   ├── admin/                           # 🆕 Admin functionality
│   │   │   │   │   └── AdminController.java         # Employee management API
│   │   │   │   ├── auth/
│   │   │   │   │   └── AuthController.java          # Login/register + role info
│   │   │   │   ├── config/
│   │   │   │   │   └── SecurityConfig.java          # RBAC + @EnableMethodSecurity
│   │   │   │   ├── dashboard/
│   │   │   │   │   └── DashboardController.java     # Statistics API
│   │   │   │   ├── email/
│   │   │   │   │   └── EmailService.java            # Email notifications
│   │   │   │   ├── packages/
│   │   │   │   │   ├── Package.java                 # Package entity
│   │   │   │   │   ├── PackageController.java       # Package CRUD + @PreAuthorize
│   │   │   │   │   └── PackageRepository.java       # Database queries
│   │   │   │   ├── recipients/
│   │   │   │   │   ├── Recipient.java               # Recipient entity
│   │   │   │   │   ├── RecipientController.java     # Recipient CRUD
│   │   │   │   │   └── RecipientRepository.java     # Database queries
│   │   │   │   ├── reports/
│   │   │   │   │   ├── Report.java                  # Report entity
│   │   │   │   │   ├── ReportController.java        # Report generation API
│   │   │   │   │   ├── ReportRepository.java        # Database queries
│   │   │   │   │   └── ReportService.java           # Report business logic
│   │   │   │   └── users/
│   │   │   │       ├── Employee.java                # Employee entity + role field
│   │   │   │       └── EmployeeRepository.java      # Database queries
│   │   │   └── resources/
│   │   │       ├── application.properties           # App configuration
│   │   │       ├── db/migration/                    # Database migrations
│   │   │       │   └── V3__add_employee_roles.sql   # Role column migration
│   │   │       └── static/                          # Frontend files
│   │   │           ├── index.html                   # Login/registration
│   │   │           ├── dashboard.html               # Dashboard (role-based UI)
│   │   │           ├── log.html                     # Package logging
│   │   │           ├── pickup.html                  # Package pickup
│   │   │           ├── search.html                  # Package search + export
│   │   │           ├── admin.html                   # 🆕 Admin panel
│   │   │           └── assets/
│   │   │               ├── js/
│   │   │               │   ├── api.js               # API utilities
│   │   │               │   ├── login.js             # Auth logic
│   │   │               │   ├── dashboard.js         # Dashboard + role-based UI
│   │   │               │   ├── log.js               # Package logging
│   │   │               │   ├── pickup.js            # Package pickup (code or tracking)
│   │   │               │   ├── search.js            # Search + CSV/PDF export
│   │   │               │   └── admin.js             # 🆕 Admin panel logic
│   │   │               └── Css/
│   │   │                   └── style.css            # Custom styles
│   │   └── test/
│   │       └── java/                                # Unit tests
│   └── pom.xml                                      # Maven dependencies
├── boxsender_complete.sql                           # Complete DB schema with roles
├── fix_employee_roles.sql                           # Manual role fix script
└── README.md                                        # This file
```

---

## How to Use

### For Admins

1. **Log in** with admin credentials
2. **Dashboard** shows all statistics and recent activity
3. **Admin Panel** (Admin Panel card on dashboard):
   - View all employees with their roles
   - **Create new employees**: Click "Create Account", fill form with name, email, password, role
   - **Edit employees**: Click "Edit" button, update details (password optional)
   - **Delete employees**: Click "Delete" button (cannot delete own account)
   - **Change roles**: Edit employee and change role dropdown
4. **Log Package** (if needed):
   - Enter tracking number, carrier, description
   - Enter recipient name and email
   - System generates 6-character pickup code
   - Email sent automatically with code
5. **Pickup Package** (if needed):
   - Enter tracking number OR pickup code
   - Verify recipient details
   - Mark as picked up
6. **Search Packages**:
   - Use multi-field search
   - Export results to CSV or PDF
7. **Export Dashboard**: Click CSV or PDF export buttons

### For Mailroom Staff

1. **Log in** with mailroom staff credentials
2. **Dashboard** shows statistics and recent activity
3. **Log Package**:
   - Click "Log Package" card on dashboard
   - Enter tracking number, carrier, description
   - Enter recipient name and email
   - System generates 6-character pickup code
   - Email sent automatically with code
   - Confirmation shows pickup code and email status
4. **Pickup Package**:
   - Click "Package Pickup" card
   - Enter tracking number OR ask recipient for pickup code
   - Verify recipient identity
   - Enter signature/notes
   - Click "Confirm Pickup"
5. **Search Packages**:
   - Click "Search Packages" card
   - Search by tracking number, carrier, recipient name/email
   - Filter by status (received/picked up)
   - Sort by any column
   - Export results to CSV or PDF

### For Employees

1. **Log in** with employee credentials
2. **Dashboard** shows limited statistics
   - No "Log Package" card (hidden)
   - No "Admin Panel" card (hidden)
3. **Pickup Package**:
   - Click "Package Pickup" card
   - Ask recipient for tracking number or pickup code
   - Enter tracking number OR pickup code
   - Verify recipient identity
   - Enter signature/notes
   - Click "Confirm Pickup"
4. **Search Packages**:
   - Click "Search Packages" card
   - Search for packages
   - View package status
   - Export results to CSV or PDF

### Package Pickup Flow (All Roles)

**Option 1: Using Pickup Code** (Recommended)
1. Recipient receives email with 6-character code (e.g., "A7K2M9")
2. Recipient shows code to mailroom staff
3. Staff enters code in pickup form
4. System finds package and verifies code
5. Staff verifies recipient identity
6. Package marked as picked up

**Option 2: Using Tracking Number**
1. Staff asks for tracking number
2. Staff enters tracking number in pickup form
3. System finds package
4. Staff verifies recipient identity
5. Package marked as picked up

---

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth | Request Body |
|--------|----------|-------------|------|--------------|
| POST | `/api/auth/register` | Register employee (default role: EMPLOYEE) | No | `{firstName, lastName, email, password}` |
| POST | `/api/auth/login` | Login employee | No | `{email, password}` |
| GET | `/api/auth/me` | Get current user info (includes role) | Yes | - |
| POST | `/api/auth/logout` | Logout | No | - |

### Admin (ADMIN only)

| Method | Endpoint | Description | Auth | Request Body |
|--------|----------|-------------|------|--------------|
| GET | `/api/admin/employees` | Get all employees | ADMIN | - |
| POST | `/api/admin/employees` | Create employee account | ADMIN | `{firstName, lastName, email, password, role}` |
| PUT | `/api/admin/employees/{id}` | Update employee (name, email, password, role) | ADMIN | `{firstName?, lastName?, email?, password?, role?}` |
| DELETE | `/api/admin/employees/{id}` | Delete employee | ADMIN | - |
| PUT | `/api/admin/employees/{id}/role` | Update employee role only | ADMIN | `{role}` |

### Packages

| Method | Endpoint | Description | Auth | Request Body |
|--------|----------|-------------|------|--------------|
| POST | `/api/packages` | Log new package (auto-generates pickup code) | ADMIN, MAILROOM_STAFF | `{trackingNumber, carrier, description, recipientFirst, recipientLast, recipientEmail}` |
| GET | `/api/packages` | Get all packages | All | - |
| GET | `/api/packages/search` | Advanced search with sorting | All | Query params: `trackingNumber`, `carrier`, `description`, `recipientFirstName`, `recipientLastName`, `recipientEmail`, `status`, `sortBy`, `sortOrder` |
| PUT | `/api/packages/{id}/pickup` | Mark as picked up (requires code OR tracking number) | All | `{pickupCode?, trackingNumber?, signature, notes}` |

### Recipients

| Method | Endpoint | Description | Auth | Request Body |
|--------|----------|-------------|------|--------------|
| GET | `/api/recipients` | Get all recipients | All | - |
| GET | `/api/recipients/{id}` | Get recipient | All | - |
| POST | `/api/recipients` | Create recipient | ADMIN, MAILROOM_STAFF | `{firstName, lastName, email, department}` |
| PUT | `/api/recipients/{id}` | Update recipient | ADMIN, MAILROOM_STAFF | `{firstName, lastName, email, department}` |
| DELETE | `/api/recipients/{id}` | Delete recipient | ADMIN, MAILROOM_STAFF | - |

### Dashboard

| Method | Endpoint | Description | Auth | Returns |
|--------|----------|-------------|------|---------|
| GET | `/api/dashboard/stats` | Get statistics | All | `{totalPackages, pendingPickups, pickedUpToday, overduePackages, totalRecipients, pickupRate}` |
| GET | `/api/dashboard/recent` | Get recent packages | All | Array of 20 most recent packages |
| GET | `/api/dashboard/overdue` | Get overdue packages | All | Array of packages >7 days old |

---

## Database Schema

### employees table (with roles)

```sql
CREATE TABLE employees (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  email VARCHAR(200) UNIQUE NOT NULL,
  password_hash VARCHAR(225) NOT NULL,
  role VARCHAR(50) NOT NULL DEFAULT 'EMPLOYEE',  -- 🆕 ADMIN, MAILROOM_STAFF, EMPLOYEE
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email),
  INDEX idx_role (role)
);
```

### packages table (with pickup codes)

```sql
CREATE TABLE packages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tracking_number VARCHAR(255) UNIQUE NOT NULL,
  carrier VARCHAR(45) NOT NULL,
  description TEXT,
  status VARCHAR(20) DEFAULT 'received' NOT NULL,
  pickup_code VARCHAR(10),  -- 🔐 6-character code (e.g., "A7K2M9")
  recipient_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (recipient_id) REFERENCES recipients(id),
  FOREIGN KEY (employee_id) REFERENCES employees(id),
  INDEX idx_status (status),
  INDEX idx_pickup_code (pickup_code),
  INDEX idx_created_at (created_at),
  INDEX idx_tracking_number (tracking_number)
);
```

### recipients table

```sql
CREATE TABLE recipients (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(45) NOT NULL,
  last_name VARCHAR(45) NOT NULL,
  email VARCHAR(225) UNIQUE NOT NULL,
  department VARCHAR(120),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email)
);
```

---

## Security

### Role-Based Access Control (RBAC)

**Implementation:** [SecurityConfig.java](app/src/main/java/com/boxsender/config/SecurityConfig.java)

1. **@EnableMethodSecurity**
   - Enables `@PreAuthorize`, `@Secured`, `@RolesAllowed` annotations
   - Method-level security on controller endpoints

2. **Dynamic Role Loading**
   ```java
   // SecurityConfig.java
   return User.withUsername(employee.getEmail())
       .password(employee.getPasswordHash())
       .roles(employee.getRole())  // Load from database
       .build();
   ```

3. **Controller Protection**
   ```java
   @PostMapping
   @PreAuthorize("hasAnyRole('ADMIN', 'MAILROOM_STAFF')")
   public ResponseEntity<?> logPackage(...) {
       // Only ADMIN and MAILROOM_STAFF can access
   }
   ```

4. **Admin-Only Endpoints**
   ```java
   @RestController
   @RequestMapping("/api/admin")
   @PreAuthorize("hasRole('ADMIN')")  // Entire controller protected
   public class AdminController {
       // All endpoints require ADMIN role
   }
   ```

### Password Security

1. **BCrypt Hashing**
   - 10 rounds (automatic salt generation)
   - Passwords NEVER stored in plain text
   - Hash example: `$2a$10$jhk83FuD5oMJW9DpEDZJ1...`

2. **Session Management**
   - Server-side sessions (not JWT)
   - HTTPOnly cookies (JavaScript cannot access)
   - Automatic timeout after inactivity

### Pickup Code Security

**Implementation:** [PackageController.java](app/src/main/java/com/boxsender/packages/PackageController.java)

1. **Code Generation**
   - Cryptographically secure random generation using `SecureRandom`
   - 6-character alphanumeric codes (e.g., "A7K2M9")
   - Excludes confusing characters (0, O, I, 1)
   - Character set: `ABCDEFGHJKLMNPQRSTUVWXYZ23456789`

2. **Verification Process**
   - Code required during package pickup (or tracking number)
   - Case-insensitive validation
   - Prevents unauthorized package collection

### Audit Logging

All administrative actions are logged:
- Role changes (who changed what, when)
- Account creation (who created which account, with what role)
- Account deletion (who deleted which account)
- Employee updates (what changed, who made the change)

Logs are printed to console and can be redirected to files in production.

---

## Email Configuration

### Setting up Brevo (Recommended)

1. **Create Brevo Account**
   - Visit https://www.brevo.com
   - Sign up for free (300 emails/day)
   - Verify your email address

2. **Get SMTP Credentials**
   - Log into Brevo dashboard
   - Go to **Settings** → **SMTP & API**
   - Click **SMTP** tab
   - Generate a new SMTP key

3. **Configure Application**

   **Option A: Environment Variables (Recommended)**

   Windows (PowerShell):
   ```powershell
   $env:BREVO_USERNAME="your-email@example.com"
   $env:BREVO_PASSWORD="your-smtp-key"
   $env:BREVO_FROM_EMAIL="your-verified-email@example.com"
   cd app
   mvn spring-boot:run
   ```

   Linux/Mac:
   ```bash
   export BREVO_USERNAME=your-email@example.com
   export BREVO_PASSWORD=your-smtp-key
   export BREVO_FROM_EMAIL=your-verified-email@example.com
   cd app
   mvn spring-boot:run
   ```

   **Option B: Direct Configuration**

   Edit `application.properties`:
   ```properties
   spring.mail.username=your-email@example.com
   spring.mail.password=your-smtp-key
   brevo.from.email=your-verified-email@example.com
   ```

### Email Notification Features

When a package is logged, recipients receive a professional HTML email with:
- Personalized greeting
- **Large pickup code display** in green box
- Tracking number
- Carrier information
- Pickup instructions
- Warning to keep code secure
- Responsive design (mobile-friendly)

---

## Troubleshooting

### Problem: Admin cannot log in after role migration

**Solution:**
Run the fix script to ensure all employees have a role:
```bash
mysql -u root -P 3307 -h localhost boxsender < fix_employee_roles.sql
```

Or manually:
```sql
UPDATE employees SET role = 'EMPLOYEE' WHERE role IS NULL OR role = '';
UPDATE employees SET role = 'ADMIN' WHERE email = 'your-admin@example.com';
```

### Problem: "Access Denied" when trying to log package

**Cause:** User has EMPLOYEE role (not authorized to log packages)

**Solution:**
- Have an admin promote you to MAILROOM_STAFF or ADMIN via Admin Panel
- Or manually update database:
  ```sql
  UPDATE employees SET role = 'MAILROOM_STAFF' WHERE email = 'your-email@example.com';
  ```

### Problem: Employee sign-ins not working

**Symptoms:** Login redirects back to index.html

**Solution:**
1. Ensure `role` column exists in employees table:
   ```sql
   DESCRIBE employees;
   ```
2. If missing, run:
   ```sql
   ALTER TABLE employees ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'EMPLOYEE';
   ```
3. Set role for existing employees:
   ```sql
   UPDATE employees SET role = 'EMPLOYEE' WHERE role IS NULL;
   ```
4. Restart application

### Problem: Application won't start

**Solutions:**
- Check if MySQL is running: `mysql -u root -p`
- Verify database exists: `SHOW DATABASES;`
- Check `application.properties` for correct credentials
- Look for port conflicts (8080)

### Problem: Email not sending

**Solutions:**
- Verify Brevo credentials in `application.properties`
- Check sender email is verified in Brevo dashboard
- Check spam/junk folder
- Look for errors in console: `Mail server connection failed`
- Verify environment variables are set correctly

---

## Quick Reference

### Start Application
```bash
cd app
mvn spring-boot:run
```

### Access URLs
- **Application:** http://localhost:8080
- **Login:** http://localhost:8080/index.html
- **Dashboard:** http://localhost:8080/dashboard.html
- **Log Package:** http://localhost:8080/log.html (ADMIN/MAILROOM_STAFF only)
- **Pickup:** http://localhost:8080/pickup.html
- **Search:** http://localhost:8080/search.html
- **Admin Panel:** http://localhost:8080/admin.html (ADMIN only)

### Default Configuration
- **Application Port:** 8080
- **MySQL Port:** 3307 (as configured)
- **Default Role:** EMPLOYEE
- **Admin Emails:** Configured in migration script or manually

### Admin Tasks

**Create Admin User:**
```sql
UPDATE employees SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

**View All Roles:**
```sql
SELECT first_name, last_name, email, role FROM employees ORDER BY role DESC;
```

**Promote User:**
```sql
UPDATE employees SET role = 'MAILROOM_STAFF' WHERE email = 'user@example.com';
```

---

## Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

**Code Style:**
- Follow existing code formatting
- Add JavaDoc comments for public methods
- Write unit tests for new features
- Update README.md if adding new features

---

## License

This project is created for educational purposes as part of **ICS 370 - Software Design and Models** at Metropolitan State University.

---

## Acknowledgments

- **Spring Boot Team** - Excellent framework and documentation
- **Bootstrap Team** - Responsive UI components
- **Brevo** - Free email service for students
- **Professor & TA** - Guidance and support

---

**Built with ❤️ using Spring Boot, Bootstrap, and Modern Web Technologies**

**Team:** Casey Cunningham, Tenzin Kunga, Nick Herberg, Brian Willems
**Course:** ICS 370 - Software Design and Models
**Date:** November 2025
