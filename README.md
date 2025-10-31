# Box Sender - Package Tracking System

A full-stack web application for managing package deliveries in a mailroom environment. Employees can log incoming packages, and recipients automatically receive email notifications when their packages arrive.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [How It Works](#how-it-works)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Security](#security)
- [Email Notifications](#email-notifications)
- [Development Guide](#development-guide)

## Overview

Box Sender is a package tracking system designed for mailrooms, front desks, or any environment where packages need to be logged and recipients notified. The system provides:

- **Employee Authentication**: Secure login/registration for mailroom staff
- **Package Logging**: Quick entry of package details with tracking numbers
- **Recipient Management**: Automatic creation of recipient records
- **Email Notifications**: Automated emails to recipients when packages arrive
- **Dashboard**: Real-time view of recent package activity

## Features

### Core Functionality
- ✅ Employee registration and login with BCrypt password encryption
- ✅ Log packages with tracking number, carrier, and recipient info
- ✅ Automatic email notifications to recipients
- ✅ Real-time activity dashboard
- ✅ Duplicate tracking number prevention
- ✅ Session-based authentication

### Security Features
- 🔒 BCrypt password hashing
- 🔒 Spring Security integration
- 🔒 Session-based authentication
- 🔒 HTML escaping to prevent XSS attacks
- 🔒 CSRF protection configuration

## Technology Stack

### Backend
- **Java 17+** - Programming language
- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database access layer
- **Hibernate** - ORM (Object-Relational Mapping)
- **MySQL** - Production database
- **H2** - In-memory database for development
- **Maven** - Dependency management and build tool

### Frontend
- **HTML5** - Page structure
- **CSS3** - Styling
- **Bootstrap 5** - UI framework
- **JavaScript (Vanilla)** - Client-side logic
- **Fetch API** - HTTP requests

### Email
- **JavaMailSender** - Email sending
- **Brevo (SMTP)** - Email service provider

## Architecture

The application follows a **layered architecture** pattern:

```
┌─────────────────────────────────────────┐
│          Frontend (Browser)             │
│   HTML/CSS/JavaScript + Bootstrap       │
└──────────────┬──────────────────────────┘
               │ HTTP/JSON
               ↓
┌─────────────────────────────────────────┐
│        Controllers (REST API)           │
│   PackageController, AuthController     │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│      Services (Business Logic)          │
│         EmailService                     │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│    Repositories (Data Access)           │
│  PackageRepo, RecipientRepo, EmployeeRepo│
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│         Database (MySQL/H2)             │
│  employees, packages, recipients tables │
└─────────────────────────────────────────┘
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ (or use embedded H2 for development)
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Box_Sender
   ```

2. **Configure Database**

   Edit `app/src/main/resources/application.properties`:

   ```properties
   # For MySQL (production)
   spring.datasource.url=jdbc:mysql://localhost:3306/boxsender
   spring.datasource.username=your_username
   spring.datasource.password=your_password

   # For H2 (development - already configured)
   # Uncomment these lines and comment out MySQL lines above
   # spring.datasource.url=jdbc:h2:mem:testdb
   # spring.jpa.hibernate.ddl-auto=create
   ```

3. **Configure Email (Brevo)**

   Add your Brevo SMTP credentials to `application.properties`:

   ```properties
   spring.mail.host=smtp-relay.brevo.com
   spring.mail.port=587
   spring.mail.username=your-brevo-login
   spring.mail.password=your-brevo-smtp-key
   brevo.from.email=your-verified-email@example.com
   ```

4. **Build and Run**
   ```bash
   cd app
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access the Application**

   Open your browser and navigate to: `http://localhost:8080`

## Project Structure

```
Box_Sender/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/boxsender/
│   │   │   │   ├── AppApplication.java           # Main entry point
│   │   │   │   ├── auth/
│   │   │   │   │   └── AuthController.java       # Login/registration endpoints
│   │   │   │   ├── config/
│   │   │   │   │   └── SecurityConfig.java       # Security configuration
│   │   │   │   ├── email/
│   │   │   │   │   └── EmailService.java         # Email sending service
│   │   │   │   ├── packages/
│   │   │   │   │   ├── Package.java              # Package entity
│   │   │   │   │   ├── PackageController.java    # Package API endpoints
│   │   │   │   │   └── PackageRepository.java    # Package database access
│   │   │   │   ├── recipients/
│   │   │   │   │   ├── Recipient.java            # Recipient entity
│   │   │   │   │   └── RecipientRepository.java  # Recipient database access
│   │   │   │   └── users/
│   │   │   │       ├── Employee.java             # Employee entity
│   │   │   │       └── EmployeeRepository.java   # Employee database access
│   │   │   └── resources/
│   │   │       ├── application.properties        # Configuration file
│   │   │       └── static/                       # Frontend files
│   │   │           ├── index.html                # Login/registration page
│   │   │           ├── dashboard.html            # Activity dashboard
│   │   │           ├── log.html                  # Package logging form
│   │   │           └── assets/
│   │   │               ├── js/
│   │   │               │   ├── api.js            # API utilities
│   │   │               │   ├── login.js          # Login/register logic
│   │   │               │   ├── dashboard.js      # Dashboard logic
│   │   │               │   └── log.js            # Package logging logic
│   │   │               └── Css/
│   │   │                   └── style.css         # Application styles
│   │   └── test/
│   │       └── java/                             # Unit tests
│   └── pom.xml                                   # Maven dependencies
└── README.md                                     # This file
```

## How It Works

### 1. User Registration & Login

**File**: [AuthController.java](app/src/main/java/com/boxsender/auth/AuthController.java)

```java
POST /api/auth/register  // Create new employee account
POST /api/auth/login     // Authenticate employee
GET  /api/auth/me        // Get current user info
POST /api/auth/logout    // End session
```

**Flow**:
1. Employee visits `/index.html`
2. Submits registration form (first name, last name, email, password)
3. Backend hashes password with BCrypt
4. Saves employee to database
5. Automatically logs in user and creates session
6. Redirects to dashboard

**Security**: Passwords are NEVER stored in plain text - always hashed with BCrypt.

### 2. Package Logging

**File**: [PackageController.java](app/src/main/java/com/boxsender/packages/PackageController.java)

```java
POST /api/packages  // Log a new package
```

**Flow**:
1. Employee navigates to `/log.html`
2. Enters package details:
   - Tracking number
   - Carrier (UPS, FedEx, USPS, etc.)
   - Recipient name and email
   - Optional description
3. Backend validates tracking number is unique
4. Finds or creates recipient record
5. Saves package with status "received"
6. Sends email notification to recipient
7. Returns success message

**File**: [log.js](app/src/main/resources/static/assets/js/log.js)

### 3. Email Notifications

**File**: [EmailService.java](app/src/main/java/com/boxsender/email/EmailService.java)

When a package is logged, the system automatically:
1. Generates a professional HTML email
2. Includes tracking number and pickup instructions
3. Sends via Brevo SMTP service
4. Escapes HTML to prevent XSS attacks

**Email Content**:
- Personalized greeting with recipient's name
- Large, prominent tracking number display
- Step-by-step pickup instructions
- Professional styling with gradients

### 4. Activity Dashboard

**File**: [dashboard.html](app/src/main/resources/static/dashboard.html)

Shows real-time list of recent packages:
- When received
- Status (Received/Picked Up)
- Tracking number
- Recipient name
- Package details

**Auto-refresh**: Updates every 30 seconds

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new employee | No |
| POST | `/api/auth/login` | Login employee | No |
| GET | `/api/auth/me` | Get current user info | Yes |
| POST | `/api/auth/logout` | Logout employee | No |

### Package Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/packages` | Log new package | Yes |
| GET | `/api/packages` | Get all packages | Yes |
| GET | `/api/packages/{id}` | Get package by ID | Yes |

### Example Request/Response

**Register Employee**:
```bash
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123"
}

Response: 200 OK
```

**Log Package**:
```bash
POST /api/packages
Content-Type: application/json

{
  "trackingNumber": "1Z999AA10123456784",
  "carrier": "UPS",
  "description": "Small box",
  "recipientFirst": "Jane",
  "recipientLast": "Smith",
  "recipientEmail": "jane.smith@example.com"
}

Response: 200 OK
{
  "id": 1,
  "trackingNumber": "1Z999AA10123456784",
  "status": "received",
  "recipientName": "Jane Smith",
  "message": "Package logged successfully. Notification email sent to jane.smith@example.com"
}
```

## Database Schema

### Tables

**employees**
- `id` (BIGINT, Primary Key, Auto-increment)
- `first_name` (VARCHAR(100), NOT NULL)
- `last_name` (VARCHAR(100), NOT NULL)
- `email` (VARCHAR(200), UNIQUE, NOT NULL)
- `password_hash` (VARCHAR(225), NOT NULL)

**recipients**
- `id` (BIGINT, Primary Key, Auto-increment)
- `first_name` (VARCHAR(45), NOT NULL)
- `last_name` (VARCHAR(45), NOT NULL)
- `email` (VARCHAR(200), UNIQUE, NOT NULL)
- `department` (VARCHAR(120))
- `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP)
- `updated_at` (DATETIME, ON UPDATE CURRENT_TIMESTAMP)

**packages**
- `id` (BIGINT, Primary Key, Auto-increment)
- `tracking_number` (VARCHAR(255), UNIQUE, NOT NULL)
- `carrier` (VARCHAR(45), NOT NULL)
- `description` (TEXT)
- `status` (VARCHAR(20)) - "received" or "picked"
- `recipient_id` (BIGINT, Foreign Key → recipients.id)
- `employee_id` (BIGINT, Foreign Key → employees.id)
- `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP)
- `updated_at` (DATETIME, ON UPDATE CURRENT_TIMESTAMP)

### Entity Relationships

```
Employee (1) ──── logs ────> (*) Package
Recipient (1) ──── receives ──> (*) Package
```

**Files**:
- [Employee.java](app/src/main/java/com/boxsender/users/Employee.java)
- [Recipient.java](app/src/main/java/com/boxsender/recipients/Recipient.java)
- [Package.java](app/src/main/java/com/boxsender/packages/Package.java)

## Security

### Authentication Flow

**File**: [SecurityConfig.java](app/src/main/java/com/boxsender/config/SecurityConfig.java)

1. **Password Storage**: BCrypt hashing with automatic salt generation
2. **Session Management**: Server-side sessions (not JWT)
3. **Authentication**: Spring Security with DaoAuthenticationProvider
4. **Authorization**: Role-based access control (USER role)

### Security Features

- **BCrypt Password Hashing**: Passwords hashed with BCrypt (10 rounds by default)
- **Session Cookies**: HTTPOnly cookies for session management
- **CSRF Protection**: Disabled for JSON API (could be enabled if needed)
- **HTML Escaping**: All user input escaped before email rendering
- **Unique Constraints**: Email addresses must be unique

### Public vs Protected URLs

**Public** (No authentication required):
- `/` - Home page
- `/index.html` - Login page
- `/assets/**` - Static resources
- `/api/auth/register` - Registration endpoint
- `/api/auth/login` - Login endpoint

**Protected** (Authentication required):
- `/dashboard.html` - Dashboard
- `/log.html` - Package logging
- `/api/packages` - Package endpoints
- All other endpoints

## Email Notifications

**File**: [EmailService.java](app/src/main/java/com/boxsender/email/EmailService.java)

### Configuration

The application uses **Brevo** (formerly Sendinblue) for email delivery:

```properties
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=587
spring.mail.username=your-brevo-login
spring.mail.password=your-brevo-api-key
brevo.from.email=verified-sender@example.com
```

### Email Template

HTML email includes:
- Professional header with gradient
- Personalized greeting
- Large tracking number display
- Step-by-step pickup instructions
- Responsive design (max-width: 600px)

### Error Handling

Email failures do **not** prevent package logging:
- Package is saved first
- Email is sent as a secondary operation
- Errors are logged but don't throw exceptions
- This ensures packages aren't lost due to email issues

## Development Guide

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package
java -jar target/app-0.0.1-SNAPSHOT.jar
```

### Common Development Tasks

**Add a new API endpoint**:
1. Create method in appropriate controller
2. Add `@GetMapping`, `@PostMapping`, etc.
3. Define request/response DTOs if needed
4. Update [SecurityConfig.java](app/src/main/java/com/boxsender/config/SecurityConfig.java) if endpoint should be public

**Add a new database field**:
1. Add field to entity class (e.g., [Package.java](app/src/main/java/com/boxsender/packages/Package.java))
2. Add getter/setter methods
3. JPA will auto-update schema (or create migration script)

**Change password hashing**:
1. Modify [SecurityConfig.java](app/src/main/java/com/boxsender/config/SecurityConfig.java)
2. Update `passwordEncoder()` bean
3. Note: Existing passwords won't work after change

### Debugging Tips

**Check if user is authenticated**:
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
System.out.println("User: " + auth.getName());
```

**View SQL queries**:
```properties
# In application.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**Test email sending**:
```java
emailService.sendPackageNotification(
    "test@example.com",
    "Test User",
    "TEST123456"
);
```

## Troubleshooting

### Common Issues

**Problem**: Email not sending
- ✅ Check Brevo credentials in `application.properties`
- ✅ Verify sender email is verified in Brevo account
- ✅ Check console for error messages
- ✅ Test SMTP connection manually

**Problem**: Login fails with correct password
- ✅ Check password is being hashed with BCrypt
- ✅ Verify `passwordEncoder` bean is configured
- ✅ Check database for password hash (should start with `$2a$`)

**Problem**: Session not persisting
- ✅ Verify `credentials: 'include'` in JavaScript fetch calls
- ✅ Check browser cookies are enabled
- ✅ Ensure CORS is configured if frontend on different domain

**Problem**: Database connection fails
- ✅ Check MySQL is running
- ✅ Verify connection URL, username, password
- ✅ Check database exists: `CREATE DATABASE boxsender;`
- ✅ Try H2 in-memory database for testing

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is created for educational purposes as part of ICS 370 - Software Design and Models.

## Contact

For questions or issues, please contact the development team or open an issue in the repository.

---

**Built with ❤️ using Spring Boot, Bootstrap, and Modern Web Technologies**
