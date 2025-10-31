# Box Sender - Package Tracking System

A comprehensive full-stack web application for managing package deliveries in mailroom environments. Employees can log incoming packages, track pickups, search package history, and generate reports. Recipients automatically receive email notifications when their packages arrive.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Database Setup](#database-setup)
- [Email Configuration](#email-configuration)
- [Project Structure](#project-structure)
- [Use Cases](#use-cases)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Security](#security)
- [Development Guide](#development-guide)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)

## Overview

Box Sender is a complete package tracking system designed for mailrooms, front desks, or any environment where packages need to be logged, tracked, and recipients notified. The system provides comprehensive package lifecycle management from arrival to pickup.

### Team Members
- Casey Cunningham
- Tenzin Kunga
- Nick Herberg
- Brian Willems

## Features

### ✅ Core Functionality (All 7 Use Cases Implemented)

1. **UC-01: Employee Authentication**
   - Secure registration and login with BCrypt password encryption
   - Session-based authentication
   - Automatic session management

2. **UC-02: Package Logging**
   - Quick entry of package details with tracking numbers
   - Automatic recipient creation or lookup
   - Real-time email notifications to recipients
   - Duplicate tracking number prevention

3. **UC-03: Package Pickup** ✨ *New*
   - Mark packages as picked up
   - Signature/verification capture
   - Automatic timestamp recording
   - Status change from "received" to "picked"

4. **UC-04: Recipient Management** ✨ *New*
   - Full CRUD operations for recipients
   - View all recipients
   - Update recipient information
   - Department assignment

5. **UC-05: Package Search** ✨ *New*
   - Search by tracking number (partial match supported)
   - Search by recipient email (partial match supported)
   - Filter by status (received/picked)
   - Real-time search with debouncing

6. **UC-06: Generate Reports** ✨ *New*
   - Daily package logs
   - Overdue package reports (>7 days)
   - Recipient package history
   - System summary statistics

7. **UC-07: Logout**
   - Secure session termination

### 📊 Dashboard & Analytics
- Real-time package statistics
- Recent activity feed (auto-refreshes every 30 seconds)
- Overdue package alerts
- Pickup rate tracking
- Total packages and recipient counts

### 🔒 Security Features
- BCrypt password hashing (10 rounds)
- Spring Security integration
- Session-based authentication with HTTPOnly cookies
- HTML escaping to prevent XSS attacks
- CSRF protection
- Protected API endpoints

## Technology Stack

### Backend
- **Java 17+** - Programming language
- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database access with repository pattern
- **Hibernate** - ORM (Object-Relational Mapping)
- **MySQL** - Production database with optimized indexes
- **Maven** - Dependency management and build tool

### Frontend
- **HTML5** - Page structure
- **CSS3** - Custom styling
- **Bootstrap 5** - Responsive UI framework
- **JavaScript (ES6+)** - Client-side logic with async/await
- **Fetch API** - RESTful API consumption

### Email
- **JavaMailSender** - Spring email abstraction
- **Brevo (SMTP)** - Email service provider (300 emails/day free tier)

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
└──────────────┬──────────────────────────────┘
               │ HTTP/JSON (REST API)
               ↓
┌─────────────────────────────────────────────┐
│       Controllers (REST API Layer)          │
│   - AuthController                          │
│   - PackageController                       │
│   - RecipientController                     │
│   - ReportController                        │
│   - DashboardController                     │
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
│   - employees (authentication)              │
│   - packages (package tracking)             │
│   - recipients (recipient info)             │
│   - reports (generated reports)             │
│   - activity_log (audit trail)              │
│   - notifications (email history)           │
└─────────────────────────────────────────────┘
```

## Getting Started

### Prerequisites

- **Java 17 or higher** ([Download](https://adoptium.net/))
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
   ```sql
   -- Create database
   CREATE DATABASE boxsender;

   -- Import schema (from root directory)
   mysql -u root -p boxsender < boxsender_complete.sql
   ```

3. **Configure application**

   Edit `app/src/main/resources/application.properties`:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:mysql://localhost:3306/boxsender
   spring.datasource.username=root
   spring.datasource.password=your_password
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
   - You'll be automatically logged in

## Database Setup

### Option 1: Complete Schema Import (Recommended)

The `boxsender_complete.sql` file includes all tables with optimizations:

```bash
mysql -u root -p
CREATE DATABASE boxsender;
USE boxsender;
source boxsender_complete.sql;
```

**This includes:**
- All 6 required tables (employees, packages, recipients, reports, activity_log, notifications)
- ENUM validation for package status
- UNIQUE indexes on tracking numbers and emails
- Performance indexes for fast searches
- Composite indexes for dashboard queries
- Foreign key constraints with proper cascading

### Option 2: Manual Table Creation

If you prefer to create tables manually or already have an existing database, use the migration script:

```bash
# Apply improvements to existing database
mysql -u root -p boxsender < database_improvements.sql
```

### Database Schema Highlights

**Key Features:**
- ✅ **Data Integrity:** UNIQUE constraints on tracking numbers and emails
- ✅ **Performance:** 10+ indexes for fast queries (search, dashboard, reports)
- ✅ **Validation:** ENUM for package status (prevents invalid data)
- ✅ **Relationships:** Proper foreign keys with cascading deletes
- ✅ **Timestamps:** Automatic `created_at` and `updated_at` tracking

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

3. **Verify Sender Email**
   - Go to **Senders & IP** in Brevo
   - Add your "from" email address
   - Verify it via the confirmation email

4. **Configure Application**

   **Option A: Environment Variables (Recommended for security)**

   Windows (Command Prompt):
   ```cmd
   set BREVO_USERNAME=your-email@example.com
   set BREVO_PASSWORD=your-smtp-key
   set BREVO_FROM_EMAIL=your-verified-email@example.com
   mvn spring-boot:run
   ```

   Windows (PowerShell):
   ```powershell
   $env:BREVO_USERNAME="your-email@example.com"
   $env:BREVO_PASSWORD="your-smtp-key"
   $env:BREVO_FROM_EMAIL="your-verified-email@example.com"
   mvn spring-boot:run
   ```

   Linux/Mac:
   ```bash
   export BREVO_USERNAME=your-email@example.com
   export BREVO_PASSWORD=your-smtp-key
   export BREVO_FROM_EMAIL=your-verified-email@example.com
   mvn spring-boot:run
   ```

   **Option B: Direct Configuration (Not recommended for production)**

   Edit `application.properties`:
   ```properties
   spring.mail.username=your-email@example.com
   spring.mail.password=your-smtp-key
   brevo.from.email=your-verified-email@example.com
   ```

   ⚠️ **Important:** Add `application.properties` to `.gitignore` if using this method!

### Email Notification Features

When a package is logged, recipients receive a professional HTML email with:
- Personalized greeting
- Large tracking number display
- Carrier information
- Pickup instructions
- Responsive design (mobile-friendly)
- XSS protection via HTML escaping

## Project Structure

```
Box_Sender/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/boxsender/
│   │   │   │   ├── AppApplication.java              # Main entry point
│   │   │   │   ├── auth/
│   │   │   │   │   └── AuthController.java          # Login/register endpoints
│   │   │   │   ├── config/
│   │   │   │   │   └── SecurityConfig.java          # Security & BCrypt config
│   │   │   │   ├── dashboard/
│   │   │   │   │   └── DashboardController.java     # Statistics API
│   │   │   │   ├── email/
│   │   │   │   │   └── EmailService.java            # Email notifications
│   │   │   │   ├── packages/
│   │   │   │   │   ├── Package.java                 # Package entity
│   │   │   │   │   ├── PackageController.java       # Package CRUD + search
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
│   │   │   │       ├── Employee.java                # Employee entity
│   │   │   │       └── EmployeeRepository.java      # Database queries
│   │   │   └── resources/
│   │   │       ├── application.properties           # App configuration
│   │   │       └── static/                          # Frontend files
│   │   │           ├── index.html                   # Login/registration
│   │   │           ├── dashboard.html               # Dashboard & stats
│   │   │           ├── log.html                     # Package logging form
│   │   │           ├── pickup.html                  # Package pickup form
│   │   │           ├── search.html                  # Package search
│   │   │           └── assets/
│   │   │               ├── js/
│   │   │               │   ├── api.js               # API utilities
│   │   │               │   ├── login.js             # Auth logic
│   │   │               │   ├── dashboard.js         # Dashboard + stats
│   │   │               │   ├── log.js               # Package logging
│   │   │               │   ├── pickup.js            # Package pickup
│   │   │               │   └── search.js            # Search functionality
│   │   │               └── Css/
│   │   │                   └── style.css            # Custom styles
│   │   └── test/
│   │       └── java/                                # Unit tests
│   └── pom.xml                                      # Maven dependencies
├── boxsender_complete.sql                           # Complete DB schema
├── database_improvements.sql                        # DB migration script
└── README.md                                        # This file
```

## Use Cases

### UC-01: Employee Login/Registration

**File:** [AuthController.java](app/src/main/java/com/boxsender/auth/AuthController.java)

**Endpoints:**
```java
POST /api/auth/register  // Create new employee account
POST /api/auth/login     // Authenticate employee
GET  /api/auth/me        // Get current user info
POST /api/auth/logout    // End session
```

**Flow:**
1. Employee visits `/index.html`
2. Submits registration form
3. Backend hashes password with BCrypt
4. Saves employee to database
5. Automatically logs in and creates session
6. Redirects to dashboard

### UC-02: Log Package

**File:** [PackageController.java](app/src/main/java/com/boxsender/packages/PackageController.java)

**Endpoint:**
```java
POST /api/packages
```

**Flow:**
1. Employee navigates to `/log.html`
2. Enters package details (tracking number, carrier, recipient)
3. Backend validates tracking number is unique
4. Finds or creates recipient record
5. Saves package with status "received"
6. Sends email notification
7. Returns success message

### UC-03: Pick Up Package ✨

**File:** [PackageController.java](app/src/main/java/com/boxsender/packages/PackageController.java)

**Endpoint:**
```java
PUT /api/packages/{id}/pickup
```

**Flow:**
1. Employee navigates to `/pickup.html`
2. Enters tracking number
3. System searches for package
4. Enters signature/verification
5. Backend validates package exists and status is "received"
6. Updates status to "picked"
7. Records timestamp

**Frontend:** [pickup.js](app/src/main/resources/static/assets/js/pickup.js)

### UC-04: Manage Recipients ✨

**File:** [RecipientController.java](app/src/main/java/com/boxsender/recipients/RecipientController.java)

**Endpoints:**
```java
GET    /api/recipients       // Get all recipients
GET    /api/recipients/{id}  // Get specific recipient
POST   /api/recipients       // Create recipient
PUT    /api/recipients/{id}  // Update recipient
DELETE /api/recipients/{id}  // Delete recipient
```

**Features:**
- View all recipients in system
- Add new recipients manually
- Update recipient information (name, email, department)
- Delete recipients (with validation)

### UC-05: Search Packages ✨

**File:** [PackageController.java](app/src/main/java/com/boxsender/packages/PackageController.java)

**Endpoints:**
```java
GET /api/packages/search?trackingNumber=...
GET /api/packages/search?recipientEmail=...
GET /api/packages/search?status=received
GET /api/packages              // Get all packages
```

**Features:**
- Search by tracking number (partial match)
- Search by recipient email (partial match)
- Filter by status (received/picked)
- Real-time search with 300ms debouncing
- Display results in sortable table

**Frontend:** [search.js](app/src/main/resources/static/assets/js/search.js)

### UC-06: Generate Reports ✨

**Files:**
- [ReportController.java](app/src/main/java/com/boxsender/reports/ReportController.java)
- [ReportService.java](app/src/main/java/com/boxsender/reports/ReportService.java)

**Endpoints:**
```java
POST /api/reports/daily?date=2025-10-31      // Daily log
POST /api/reports/overdue?days=7             // Overdue packages
POST /api/reports/recipient?email=...        // Recipient history
POST /api/reports/summary                    // System summary
GET  /api/reports                            // List all reports
GET  /api/reports/{id}                       // View specific report
```

**Report Types:**

1. **Daily Log:** All packages logged on a specific date
2. **Overdue Packages:** Packages in "received" status older than X days
3. **Recipient History:** All packages for a specific recipient
4. **Summary Statistics:** Overall system stats (total packages, pickup rate, etc.)

**Format:** Reports are stored as CSV-style text in the database

### UC-07: Logout

**Endpoint:**
```java
POST /api/auth/logout
```

## API Endpoints

### Complete API Reference

#### Authentication

| Method | Endpoint | Description | Auth | Request Body |
|--------|----------|-------------|------|--------------|
| POST | `/api/auth/register` | Register employee | No | `{firstName, lastName, email, password}` |
| POST | `/api/auth/login` | Login employee | No | `{email, password}` |
| GET | `/api/auth/me` | Get current user | Yes | - |
| POST | `/api/auth/logout` | Logout | No | - |

#### Packages

| Method | Endpoint | Description | Auth | Request Body |
|--------|----------|-------------|------|--------------|
| POST | `/api/packages` | Log new package | Yes | `{trackingNumber, carrier, description, recipientFirst, recipientLast, recipientEmail}` |
| GET | `/api/packages` | Get all packages | Yes | - |
| GET | `/api/packages/search` | Search packages | Yes | Query params: `trackingNumber`, `recipientEmail`, `status` |
| PUT | `/api/packages/{id}/pickup` | Mark as picked up | Yes | `{signature, notes}` |

#### Recipients

| Method | Endpoint | Description | Auth | Request Body |
|--------|----------|-------------|------|--------------|
| GET | `/api/recipients` | Get all recipients | Yes | - |
| GET | `/api/recipients/{id}` | Get recipient | Yes | - |
| POST | `/api/recipients` | Create recipient | Yes | `{firstName, lastName, email, department}` |
| PUT | `/api/recipients/{id}` | Update recipient | Yes | `{firstName, lastName, email, department}` |
| DELETE | `/api/recipients/{id}` | Delete recipient | Yes | - |

#### Reports

| Method | Endpoint | Description | Auth | Query Params |
|--------|----------|-------------|------|--------------|
| POST | `/api/reports/daily` | Generate daily log | Yes | `date` (YYYY-MM-DD) |
| POST | `/api/reports/overdue` | Generate overdue report | Yes | `days` (default: 7) |
| POST | `/api/reports/recipient` | Generate recipient history | Yes | `email` (required) |
| POST | `/api/reports/summary` | Generate summary | Yes | - |
| GET | `/api/reports` | List all reports | Yes | - |
| GET | `/api/reports/{id}` | View report | Yes | - |

#### Dashboard

| Method | Endpoint | Description | Auth | Returns |
|--------|----------|-------------|------|---------|
| GET | `/api/dashboard/stats` | Get statistics | Yes | `{totalPackages, pendingPickups, pickedUpToday, overduePackages, totalRecipients, pickupRate}` |
| GET | `/api/dashboard/recent` | Get recent packages | Yes | Array of 20 most recent packages |
| GET | `/api/dashboard/overdue` | Get overdue packages | Yes | Array of packages >7 days old |

### Example Requests

**Log a Package:**
```bash
curl -X POST http://localhost:8080/api/packages \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "trackingNumber": "1Z999AA10123456784",
    "carrier": "UPS",
    "description": "Small box",
    "recipientFirst": "Jane",
    "recipientLast": "Smith",
    "recipientEmail": "jane.smith@example.com"
  }'
```

**Search Packages:**
```bash
curl -X GET "http://localhost:8080/api/packages/search?status=received" \
  -b cookies.txt
```

**Mark Package as Picked Up:**
```bash
curl -X PUT http://localhost:8080/api/packages/1/pickup \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "signature": "John Doe",
    "notes": "Photo ID verified"
  }'
```

**Generate Daily Report:**
```bash
curl -X POST "http://localhost:8080/api/reports/daily?date=2025-10-31" \
  -b cookies.txt
```

## Database Schema

### Complete Database Tables

**employees**
```sql
CREATE TABLE employees (
  id INT PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  email VARCHAR(200) UNIQUE NOT NULL,
  password_hash VARCHAR(225) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP
);
```

**recipients**
```sql
CREATE TABLE recipients (
  id INT PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(45) NOT NULL,
  last_name VARCHAR(45) NOT NULL,
  email VARCHAR(225) UNIQUE NOT NULL,
  department VARCHAR(120),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email)
);
```

**packages**
```sql
CREATE TABLE packages (
  id INT PRIMARY KEY AUTO_INCREMENT,
  tracking_number VARCHAR(50) UNIQUE NOT NULL,
  carrier VARCHAR(45) NOT NULL,
  description TEXT,
  status ENUM('received', 'picked') DEFAULT 'received' NOT NULL,
  recipient_id INT NOT NULL,
  employee_id INT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (recipient_id) REFERENCES recipients(id),
  FOREIGN KEY (employee_id) REFERENCES employees(id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at),
  INDEX idx_updated_at (updated_at),
  INDEX idx_status_created (status, created_at)
);
```

**reports**
```sql
CREATE TABLE reports (
  id INT PRIMARY KEY AUTO_INCREMENT,
  report_type VARCHAR(50) NOT NULL,
  title VARCHAR(200) NOT NULL,
  generated_date DATETIME NOT NULL,
  generated_by INT NOT NULL,
  report_data TEXT,
  date_range VARCHAR(100),
  record_count INT,
  FOREIGN KEY (generated_by) REFERENCES employees(id),
  INDEX idx_report_type (report_type),
  INDEX idx_generated_date (generated_date)
);
```

**activity_log** (optional - for audit trail)
```sql
CREATE TABLE activity_log (
  id INT PRIMARY KEY AUTO_INCREMENT,
  package_id INT NOT NULL,
  employee_id INT,
  action ENUM('RECEIVED', 'PICKED_UP') NOT NULL,
  detail TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE
);
```

**notifications** (optional - email history)
```sql
CREATE TABLE notifications (
  package_id INT NOT NULL,
  recipient_id INT NOT NULL,
  message TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (package_id, recipient_id),
  FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE,
  FOREIGN KEY (recipient_id) REFERENCES recipients(id) ON DELETE CASCADE
);
```

### Entity Relationships

```
Employee (1) ──── logs ────────> (*) Package
Recipient (1) ──── receives ───> (*) Package
Employee (1) ──── generates ──-> (*) Report
Package (1) ──── tracked by ──-> (*) Activity_Log
```

### Performance Optimizations

The database includes **10+ indexes** for optimal query performance:

1. **UNIQUE indexes** - Prevent duplicates (tracking_number, email)
2. **Search indexes** - Fast lookups (recipient email, package status)
3. **Date indexes** - Quick date-based queries (created_at, updated_at)
4. **Composite indexes** - Optimize complex queries (status + created_at)

**Result:** Search queries execute in milliseconds even with thousands of records.

## Security

### Authentication & Authorization

**File:** [SecurityConfig.java](app/src/main/java/com/boxsender/config/SecurityConfig.java)

1. **Password Security**
   - BCrypt hashing with 10 rounds (automatic salt generation)
   - Passwords NEVER stored in plain text
   - Hash example: `$2a$10$jhk83FuD5oMJW9DpEDZJ1...`

2. **Session Management**
   - Server-side sessions (not JWT)
   - HTTPOnly cookies (JavaScript cannot access)
   - Session timeout after inactivity
   - Secure cookie flag in production

3. **Authentication Provider**
   - Spring Security DaoAuthenticationProvider
   - Custom UserDetailsService for employee lookup
   - Automatic session creation on successful login

4. **Authorization**
   - Role-based access control (USER role for all employees)
   - Protected endpoints require authentication
   - Public endpoints: login, register, static assets

### URL Security Configuration

**Public URLs** (No authentication required):
- `/` - Home page
- `/index.html` - Login/registration
- `/assets/**` - CSS, JavaScript, images
- `/api/auth/register` - Registration endpoint
- `/api/auth/login` - Login endpoint

**Protected URLs** (Authentication required):
- `/dashboard.html` - Dashboard
- `/log.html` - Package logging
- `/pickup.html` - Package pickup
- `/search.html` - Package search
- `/api/packages/**` - All package operations
- `/api/recipients/**` - Recipient management
- `/api/reports/**` - Report generation
- `/api/dashboard/**` - Dashboard statistics

### XSS Protection

All user input is escaped before rendering:
- HTML escaping in email templates
- JavaScript `escapeHtml()` function in frontend
- Prevents cross-site scripting attacks

### SQL Injection Protection

- JPA parameterized queries (no raw SQL)
- Spring Data repository methods
- Automatic SQL escaping by Hibernate

## Development Guide

### Local Development Setup

1. **IDE Setup**
   - Import as Maven project
   - Set Java 17 as project SDK
   - Enable annotation processing
   - Install Spring Boot extension (optional)

2. **Database Development Mode**

   For quick development, use H2 in-memory database:
   ```properties
   # Comment out MySQL and add:
   spring.datasource.url=jdbc:h2:mem:testdb
   spring.datasource.driver-class-name=org.h2.Driver
   spring.jpa.hibernate.ddl-auto=create-drop
   ```

   Access H2 console: `http://localhost:8080/h2-console`

3. **Hot Reload**

   Add Spring Boot DevTools for automatic restart:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-devtools</artifactId>
       <optional>true</optional>
   </dependency>
   ```

### Common Development Tasks

**Add a new API endpoint:**
```java
// 1. Add to appropriate controller
@GetMapping("/my-endpoint")
public ResponseEntity<?> myEndpoint(Authentication auth) {
    // Implementation
    return ResponseEntity.ok(data);
}

// 2. Update SecurityConfig if endpoint should be public
.requestMatchers("/api/my-endpoint").permitAll()
```

**Add a new database field:**
```java
// 1. Add field to entity
@Column(name = "new_field")
private String newField;

// 2. Add getter/setter
public String getNewField() { return newField; }
public void setNewField(String newField) { this.newField = newField; }

// 3. JPA will auto-update schema (or write migration script)
```

**Add a new repository query method:**
```java
// Spring Data JPA generates query from method name
List<Package> findByStatusAndCreatedAtAfter(String status, LocalDateTime date);

// Or use @Query for custom JPQL
@Query("SELECT p FROM Package p WHERE p.status = :status")
List<Package> findPackagesByStatus(@Param("status") String status);
```

### Debugging Tips

**Enable SQL logging:**
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

**Check authentication:**
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
System.out.println("User: " + auth.getName());
System.out.println("Roles: " + auth.getAuthorities());
```

**Test email without sending:**
```java
// In EmailService.java, comment out:
// mailSender.send(message);
System.out.println("Email would be sent to: " + recipientEmail);
```

## Testing

### Manual Testing Checklist

#### Authentication Flow
- [ ] Register new employee account
- [ ] Login with correct credentials
- [ ] Login fails with wrong password
- [ ] Password is hashed in database (starts with `$2a$`)
- [ ] Logout successfully clears session
- [ ] Protected pages redirect to login when not authenticated

#### Package Logging
- [ ] Log package with all required fields
- [ ] Email notification is sent to recipient
- [ ] Duplicate tracking number is rejected
- [ ] New recipient is created automatically
- [ ] Existing recipient is reused correctly
- [ ] Package appears on dashboard immediately

#### Package Pickup
- [ ] Search for package by tracking number
- [ ] Mark package as picked up
- [ ] Status changes from "received" to "picked"
- [ ] Cannot pick up already-picked package
- [ ] Timestamp is recorded correctly

#### Package Search
- [ ] Search by full tracking number
- [ ] Search by partial tracking number
- [ ] Search by recipient email
- [ ] Filter by status (received/picked)
- [ ] Results display correctly in table
- [ ] No results shows appropriate message

#### Reports
- [ ] Generate daily report for today
- [ ] Generate daily report for specific date
- [ ] Generate overdue report (>7 days)
- [ ] Generate recipient history report
- [ ] Generate summary statistics report
- [ ] View generated report details

#### Dashboard
- [ ] Recent packages load automatically
- [ ] Statistics display correctly
- [ ] Auto-refresh works (wait 30-60 seconds)
- [ ] User's first name displays in greeting

### Unit Testing

Run existing tests:
```bash
mvn test
```

Example test structure:
```java
@SpringBootTest
class PackageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLogPackage() throws Exception {
        // Test implementation
    }
}
```

### API Testing with Postman

1. Import API collection (create from endpoints above)
2. Set up environment variables (baseUrl, authCookie)
3. Test all endpoints systematically
4. Verify response codes and data

## Troubleshooting

### Common Issues & Solutions

#### Problem: Application won't start

**Symptoms:** Error on startup, port already in use

**Solutions:**
- Check if MySQL is running: `mysql -u root -p`
- Check if port 8080 is free: `netstat -ano | findstr :8080` (Windows)
- Verify database exists: `SHOW DATABASES;`
- Check application.properties for correct credentials

#### Problem: Email not sending

**Symptoms:** Package logs but no email received

**Solutions:**
- ✅ Verify Brevo credentials in application.properties
- ✅ Check sender email is verified in Brevo dashboard
- ✅ Check spam/junk folder
- ✅ Look for errors in console: `Mail server connection failed`
- ✅ Test SMTP manually: `telnet smtp-relay.brevo.com 587`
- ✅ Verify environment variables are set correctly

#### Problem: Login fails with correct password

**Symptoms:** "Invalid credentials" error

**Solutions:**
- ✅ Check password is being hashed: `SELECT password_hash FROM employees;`
- ✅ Verify hash starts with `$2a$`
- ✅ Check SecurityConfig has `passwordEncoder` bean
- ✅ Clear browser cookies and try again
- ✅ Register new account to test fresh

#### Problem: Session not persisting

**Symptoms:** Logged out after page refresh

**Solutions:**
- ✅ Verify `credentials: 'include'` in all fetch calls
- ✅ Check browser cookies are enabled
- ✅ Look for CORS errors in browser console
- ✅ Verify SecurityConfig session management

#### Problem: Database connection fails

**Symptoms:** `Communications link failure` error

**Solutions:**
- ✅ Check MySQL service is running
- ✅ Verify connection URL: `jdbc:mysql://localhost:3306/boxsender`
- ✅ Test credentials: `mysql -u root -p`
- ✅ Create database: `CREATE DATABASE boxsender;`
- ✅ Try H2 for testing: Switch to in-memory database

#### Problem: Package pickup fails

**Symptoms:** Error when marking package as picked

**Solutions:**
- ✅ Verify package exists with tracking number
- ✅ Check package status is "received" not already "picked"
- ✅ Look for errors in console logs
- ✅ Verify `updated_at` field is being set

#### Problem: Search returns no results

**Symptoms:** Empty results when searching existing packages

**Solutions:**
- ✅ Check database has packages: `SELECT * FROM packages;`
- ✅ Verify search query parameters
- ✅ Try searching without filters first
- ✅ Check browser console for JavaScript errors

### Performance Issues

If application is slow:
- ✅ Check database indexes are created (see `database_improvements.sql`)
- ✅ Verify MySQL query cache is enabled
- ✅ Monitor slow queries: `SET profiling = 1; SHOW PROFILES;`
- ✅ Increase JVM heap size: `java -Xmx1024m -jar app.jar`

## Production Deployment

### Pre-deployment Checklist

- [ ] Change database to production instance
- [ ] Use environment variables for all secrets
- [ ] Enable HTTPS/SSL
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Configure production email limits
- [ ] Set up database backups
- [ ] Enable proper logging (file-based)
- [ ] Configure firewall rules
- [ ] Set session timeout appropriately
- [ ] Add CSRF protection if needed

### Build for Production

```bash
# Build JAR file
mvn clean package -DskipTests

# Run with production profile
java -jar target/app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

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

## License

This project is created for educational purposes as part of **ICS 370 - Software Design and Models** at Metropolitan State University.

## Acknowledgments

- **Spring Boot Team** - Excellent framework and documentation
- **Bootstrap Team** - Responsive UI components
- **Brevo** - Free email service for students
- **Professor & TA** - Guidance and support

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
- **Log Package:** http://localhost:8080/log.html
- **Pickup:** http://localhost:8080/pickup.html
- **Search:** http://localhost:8080/search.html

### Default Ports
- **Application:** 8080
- **MySQL:** 3306 or 3307 (as configured)

### Contact

For questions or issues:
- Open an issue in the repository
- Contact team members via university email
- See professor during office hours

---

**Built with ❤️ using Spring Boot, Bootstrap, and Modern Web Technologies**

**Team:** Casey Cunningham, Tenzin Kunga, Nick Herberg, Brian Willems
**Course:** ICS 370 - Software Design and Models
**Date:** October 2025
