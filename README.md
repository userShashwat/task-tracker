# Task Tracker

A Spring Boot REST API for managing personal tasks with JWT-based authentication and email verification.

## Tech Stack

- Java + Spring Boot
- Spring Security + JWT
- JPA / Hibernate
- BCrypt Password Encoding
- Email Verification

## Features

- User registration with email verification
- JWT login/logout with token management
- Create, read, update, delete tasks
- Tasks linked to authenticated users by email
- Stateless session (no cookies)

## Getting Started

1. **Clone the repo**
````bash
   git clone https://github.com/userShashwat/task-tracker.git
   cd task-tracker
````

2. **Configure your database and email** in `src/main/resources/application.properties`

````properties
   spring.datasource.url=jdbc:mysql://localhost:3306/tasktracker
   spring.datasource.username=your_db_user
   spring.datasource.password=your_db_password

   spring.mail.host=smtp.gmail.com
   spring.mail.username=your_email@gmail.com
   spring.mail.password=your_email_password
````

3. **Run the app**
````bash
   ./mvnw spring-boot:run
````

## API Endpoints

| Method | Endpoint | Auth Required |
|--------|----------|---------------|
| POST | `/auth/register` | No |
| POST | `/auth/login` | No |
| POST | `/auth/logout` | Yes |
| GET | `/api/tasks` | Yes |
| POST | `/api/tasks` | Yes |
| PUT | `/api/tasks/{id}` | Yes |
| DELETE | `/api/tasks/{id}` | Yes |

## Project Structure

````
src/main/java/com/example/project1/
├── Configuration/   # Security, JWT filter
├── Controller/      # Auth & Task controllers
├── Service/         # Business logic, JWT, Email
├── Repository/      # JPA repositories
├── model/           # Users, Tasks entities
├── Token/           # JWT token management
└── email/           # Email sender & validator
````