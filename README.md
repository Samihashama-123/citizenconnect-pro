# CitizenConnect Pro
### Smart Public Service Platform Using Java

CitizenConnect Pro is a Java-based desktop application designed to improve communication between citizens and public service administrators.

The platform provides a centralized system where citizens can access public services, submit applications, track their requests, submit complaints, and receive important public information.

Administrators can manage users, services, applications, complaints, emergency alerts, important information, and volunteer activities through a dedicated Admin Dashboard.

## 🚀 Features

### 👤 Citizen Panel
- User Registration and Login
- Citizen Profile Management
- Browse Available Public Services
- Apply for Public Services
- Track Application Status
- Submit Complaints
- View Important Information
- View Emergency Alerts
- Volunteer Registration
- Feedback Submission

### 🛡️ Admin Panel
- Admin Authentication
- Dashboard with Statistics
- Manage Users
- Manage Public Services
- Manage Applications
- Manage Complaints
- Manage Important Information
- Manage Emergency Alerts
- Manage Volunteers and Feedback
- Search and Filter Records
- Update and Manage Records
- Admin Sign Out

## 🛠️ Technology Stack

| Technology | Version / Purpose |
|---|---|
| Java | 21 |
| Java Swing | Desktop GUI |
| Java AWT | GUI Components |
| MongoDB Atlas | Cloud Database |
| MongoDB Java Sync Driver | 5.6.0 |
| Apache Maven | Build and Dependency Management |
| JUnit | 4.11 (Testing) |
| Git & GitHub | Version Control |

## 🏗️ Project Architecture

The application is organized into different layers:

- **UI Layer:** Handles the graphical user interface using Java Swing.
- **Repository Layer:** Handles database operations.
- **Configuration Layer:** Manages application configuration and user sessions.
- **Theme Layer:** Contains reusable UI components and styling.
- **Database Layer:** Uses MongoDB Atlas for data storage.

## 📂 Project Structure

```text
citizenconnect-pro/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/citizenconnect/
│   │   │       ├── config/
│   │   │       ├── repository/
│   │   │       │   └── admin/
│   │   │       ├── theme/
│   │   │       ├── ui/
│   │   │       │   └── admin/
│   │   │       └── App.java
│   │   │
│   │   └── resources/
│   │
│   └── test/
│
├── pom.xml
├── .gitignore
└── README.md
```

## ⚙️ Installation and Setup

### Prerequisites

- Java JDK 21
- Apache Maven
- Git
- MongoDB Atlas Account
- Visual Studio Code, IntelliJ IDEA, or Eclipse

### 1. Clone the Repository

```bash
git clone https://github.com/Samihashama-123/citizenconnect-pro.git
```

### 2. Open the Project

Open the `citizenconnect-pro` folder in your preferred Java IDE.

### 3. Configure MongoDB Atlas

1. Create a MongoDB Atlas cluster.
2. Create a database user.
3. Configure network access.
4. Configure the MongoDB connection URI in the project's database configuration.

**Security Note:** Never upload MongoDB credentials, passwords, or connection URIs to GitHub.

### 4. Build the Project

Open a terminal in the project root directory:

```bash
mvn clean compile
```

### 5. Run the Application

Run the application's main class (`App.java`) from your IDE after configuring the database connection.

## 🗄️ Database

CitizenConnect Pro uses MongoDB Atlas for cloud-based data storage.

The application manages information related to:

- User Accounts
- Public Services
- Service Applications
- Complaints
- Important Information
- Emergency Alerts
- Volunteer Registrations
- Feedback

## 🔐 Security

- Role-based access for Admin and Citizen users.
- Protected administrative functionalities.
- Session management.
- Password security through the authentication implementation.
- MongoDB credentials should remain outside public source control.

## 🎯 Project Objectives

- Digitize public service management.
- Improve communication between citizens and administrators.
- Make public services more accessible.
- Simplify application and complaint management.
- Provide important public information efficiently.

## 👥 Project Team

Developed as an academic software engineering project.

**GitHub Repository:**  
https://github.com/Samihashama-123/citizenconnect-pro

## 🔮 Future Improvements

- Email and SMS Notifications
- Advanced Reporting and Analytics
- Improved Application Tracking
- Additional Administrative Automation
- Enhanced Security and Validation

## 📄 License

This project is developed for academic and educational purposes.
