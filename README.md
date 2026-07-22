# 🛡️ DocVault - Institutional Document Management & Verification Platform

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)
![Angular](https://img.shields.io/badge/Angular-17.0-red.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![AWS S3](https://img.shields.io/badge/AWS%20S3-Supported-yellow.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

**DocVault** is a secure, enterprise-grade institutional document management and verification platform designed for educational institutions and organizations. It simplifies document submissions, streamlines multi-tier approval workflows, ensures compliance, and provides real-time notifications, audit logs, and hybrid cloud storage.

---

## 📑 Table of Contents

- [Key Features](#-key-features)
- [Architecture & Tech Stack](#-architecture--tech-stack)
- [System Roles & Permissions](#-system-roles--permissions)
- [Project Directory Structure](#-project-directory-structure)
- [Prerequisites](#-prerequisites)
- [Configuration & Environment Variables](#-configuration--environment-variables)
- [Getting Started](#-getting-started)
  - [1. Database Setup](#1-database-setup)
  - [2. Frontend Setup (Angular)](#3-frontend-setup-angular)
- [License](#-license)

---

## ✨ Key Features

- 🔐 **Secure Authentication & RBAC**: JWT-based authentication with role-based access control (Student, Staff, Admin).
- 📧 **Email Verification & Password Recovery**: OTP-based email verification and secure password reset workflow.
- 📁 **Document Upload & Versioning**: Support for multi-format document uploads (PDF, Images) with automatic version tracking for resubmissions.
- 🔎 **Inline Document Preview & Download**: Instant browser preview and direct downloads for submitted documents.
- ☁️ **Hybrid File Storage Engine**: Primary storage on AWS S3 with seamless automatic fallback to local filesystem storage.
- 📊 **Staff Review & Approval Workflow**: Staff can approve, reject, or request document updates with mandatory remarks.
- 📢 **Broadcast Document Requests**: Staff & Admin can request custom documents globally from all enrolled students.
- 🔔 **Real-time Notifications**: In-app notifications alerting users about status updates, remarks, and requests.
- 📜 **Audit Logging**: Comprehensive system activity tracking (logins, uploads, reviews, user modifications) for compliance and auditing.
- 👥 **User & Institutional Management**: Search, filter, activate/deactivate users, and filter student records by College, Course, Class Level, and Caste Category.

---

## 🏗️ Architecture & Tech Stack

### High-Level Architecture

```
                                +-------------------+
                                |   Angular 17 UI   |
                                +---------+---------+
                                          | REST API / JWT
                                          v
                                +---------+---------+
                                | Spring Boot 3.2.4 |
                                +----+----+----+----+
                                     |    |    |
             +-----------------------+    |    +-----------------------+
             |                            v                            |
             v                   +--------+--------+                   v
    +--------+--------+          |  MySQL Database |          +--------+--------+
    | AWS S3 / Local  |          +-----------------+          |  SMTP Mail     |
    | Document Store  |                                       |  Service       |
    +-----------------+                                       +-----------------+
```

### Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Frontend** | Angular 17, RxJS, HTML5, Vanilla CSS / SCSS |
| **Backend** | Java 17, Spring Boot 3.2.4, Spring Security, Spring Data JPA |
| **Database** | MySQL 8.x |
| **Authentication** | JWT (JSON Web Tokens) with JJWT library |
| **Cloud Storage** | AWS SDK v2 for S3 (with Local Storage fallback) |
| **Email Service** | Spring Boot Starter Mail (JavaMailSender) |
| **Containerization**| Docker (Multi-stage build) |

---

## 👥 System Roles & Permissions

| Feature / Action | 🎓 Student | 👩‍🏫 Staff | 🛡️ Admin |
| :--- | :---: | :---: | :---: |
| Register & Verify Email |  | ❌ | ❌ |
| Upload & Manage Personal Documents |  | ❌ | ❌ |
| Resubmit Revised Document Versions |  | ❌ | ❌ |
| Review & Approve / Reject Documents | ❌ |  |  |
| Request Custom Document Submissions | ❌ |  |  |
| Send Target Notifications | ❌ |  |  |
| User Activation / Deactivation | ❌ | ❌ |  |
| View Full Audit Logs | ❌ | ❌ |  |
| System Dashboard & Global Search | ❌ |  |  |

---

## 📁 Project Directory Structure

```
DocVault/
├── backend/                         # Spring Boot 3.2.4 Backend Application
│   ├── Dockerfile                   # Multi-stage Docker build file
│   ├── pom.xml                      # Maven dependencies
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/app/institutional/
│   │   │   │   ├── config/          # CORS & Security configurations
│   │   │   │   ├── controller/      # REST API Controllers (Auth, Document, Admin, etc.)
│   │   │   │   ├── entity/          # JPA Entities (User, Document, AuditLog, Notification)
│   │   │   │   ├── payload/         # Request/Response DTOs
│   │   │   │   ├── repository/      # Spring Data JPA Repositories
│   │   │   │   ├── security/        # JWT Utils, UserDetailsService, AuthFilters
│   │   │   │   └── service/         # Business Logic & AWS S3 Storage Service
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-local.properties
├── frontend/                        # Angular 17 Frontend Application
│   ├── angular.json                 # Angular CLI configuration
│   ├── package.json                 # Node dependencies & npm scripts
│   ├── vercel.json                  # Vercel deployment configuration
│   └── src/
│       ├── app/
│       │   ├── components/          # Admin, Staff, Student, Auth Components
│       │   ├── services/            # API & Auth Services
│       │   └── app.routes.ts        # Route Definitions
├── document_uploads/                # Local storage fallback directory
└── README.md                        # Project documentation
```

---

## ⚙️ Prerequisites

Ensure you have the following installed on your environment:

- **JDK**: Java 17 or higher
- **Maven**: 3.8+
- **Node.js**: v18.x or v20.x
- **Angular CLI**: `npm install -g @angular/cli@17`
- **MySQL Server**: 8.0+
- **Docker**: (Optional, for containerized run)

---

---

## 🚀 Getting Started

### 1. Database Setup
Start your MySQL server and create the database (or let Spring Boot auto-create it):
```sql
CREATE DATABASE IF NOT EXISTS institutional_db;
```

### 2. Backend Setup (Spring Boot)

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build the application using Maven:
   ```bash
   mvn clean package -DskipTests
   ```
3. Run the backend application:
   ```bash
   mvn spring-boot:run
   ```
   *The server will start on `http://localhost:8080`.*

### 3. Frontend Setup (Angular)

1. Open a new terminal and navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm start
   ```
   *Access the web application at `http://localhost:4200`.*

---

## 📝 License

This project is licensed under the **MIT License**. Feel free to customize and expand it for institutional or commercial use.
