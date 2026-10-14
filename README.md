# KMIE Student Enquiry & Demo Booking Automation System

## 1. Project Overview

The KMIE Student Enquiry & Demo Booking Automation System is a web-based application developed using Java, Spring Boot, Thymeleaf, and MySQL.

The system allows students to register enquiries for NEET or JEE coaching and book demo sessions. It prevents duplicate registrations and duplicate bookings.

An administrator or counsellor can securely access the dashboard to view student enquiries, check demo bookings, and filter bookings by date and status.

## 2. Technologies Used

- **Backend:** Java, Spring Boot
- **Frontend:** HTML, CSS, Thymeleaf
- **Database:** MySQL
- **ORM:** Spring Data JPA, Hibernate
- **Security:** Spring Security
- **API Testing:** Postman
- **Development IDE:** Visual Studio Code
- **Build Tool:** Maven Wrapper

## 3. Main Features

### A. Student Enquiry Registration

- Register students with name, email, phone number, class, course, and message.
- Support Class 11 and Class 12.
- Support NEET and JEE coaching enquiries.
- Validate required student information.
- Automatically save the enquiry date.
- Store student records in MySQL.
- Display a confirmation page after registration.

### B. Duplicate Student Prevention

- Check whether a mobile number is already registered.
- Reject new enquiries using an existing registered mobile number.
- Display an appropriate validation message.

### C. Demo Booking System

- Allow demo bookings only for registered students.
- Identify students using their registered mobile numbers.
- Allow students to choose a demo date.
- Available demo time slots: 1:00 PM, 3:00 PM, 5:00 PM, and 7:00 PM.
- Reject past dates and expired time slots.
- Prevent duplicate bookings for the same student, date, and time.
- Allow bookings for different available time slots.
- Save new bookings with the initial status `PENDING`.
- Display booking confirmation information.

### D. REST API Integration

REST endpoints are available for creating and retrieving student enquiries and demo bookings.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/enquiries` | Retrieve student enquiries |
| POST | `/api/enquiries` | Create a student enquiry |
| GET | `/api/bookings` | Retrieve demo bookings |
| POST | `/api/bookings` | Create a demo booking |

### E. Admin / Counsellor Dashboard

- Secure administrator login.
- View registered student enquiries.
- Search enquiries by name, email, or course.
- View enquiry statistics.
- View all demo booking records.
- View student names, mobile numbers, courses, dates, time slots, and booking statuses.
- Filter bookings by demo date or status.
- Clear filters to display all booking records.
- Secure logout.

## 4. Project Structure

```text
kmie-enquiry-system/
|-- src/
|   |-- main/
|   |   |-- java/kmie_enquiry_system/
|   |   |   |-- StudentEnquiry.java
|   |   |   |-- StudentEnquiryRepository.java
|   |   |   |-- EnquiryController.java
|   |   |   |-- StudentEnquiryRestController.java
|   |   |   |-- DemoBooking.java
|   |   |   |-- DemoBookingRepository.java
|   |   |   |-- DemoBookingController.java
|   |   |   |-- DemoBookingRestController.java
|   |   |   |-- AdminController.java
|   |   |   |-- SecurityConfig.java
|   |   |-- resources/
|   |       |-- application.properties
|   |       |-- templates/
|   |           |-- index.html
|   |           |-- enquiry.html
|   |           |-- enquiry-success.html
|   |           |-- demo-booking.html
|   |           |-- demo-booking-success.html
|   |           |-- admin-enquiries.html
|-- pom.xml
|-- mvnw
|-- mvnw.cmd
|-- README.md
```

## 5. Database Configuration

**Database name:** `kmie_db`

The application uses MySQL for storing student enquiries and demo bookings.

Create the database using MySQL Workbench:

```sql
CREATE DATABASE IF NOT EXISTS kmie_db;
```

The database connection is configured in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/kmie_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
```

The `DB_USERNAME` and `DB_PASSWORD` environment variables must be set before application startup. Never put actual passwords in this file when distributing the project.

Hibernate manages the database mappings for the application entities.

### Main Database Tables

**`student_enquiries`** stores the student ID, full name, email, mobile number, class, course, message, and enquiry date.

**`demo_bookings`** stores the booking ID, student enquiry ID, demo date, demo time, and booking status.

The demo booking table has a relationship with the student enquiry table. A database uniqueness constraint prevents duplicate bookings for the same student, date, and time slot.

## 6. How to Run the Project

### Prerequisites

- A Java JDK compatible with the project's Maven configuration (Java 21 or later as supported by the installed Spring Boot version).
- MySQL Server.
- Visual Studio Code or another Java IDE.
- Internet connection for initial Maven dependency downloads.
- Postman for REST API testing (optional for running the website).

### Step 1: Open the Project

Open the inner `kmie-enquiry-system` folder containing `pom.xml` in VS Code.

### Step 2: Start MySQL

Ensure that MySQL Server is running and the `kmie_db` database exists. The configured MySQL user must have access to this database.

### Step 3: Configure Environment Variables

Open a **Windows PowerShell terminal** in the project folder. Set the values below **in that same terminal** before running Spring Boot:

```powershell
$env:DB_USERNAME="YOUR_MYSQL_USERNAME"
$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"
$env:ADMIN_PASSWORD="YOUR_ADMIN_PASSWORD"
```

Replace each placeholder with the appropriate value **on the computer where the application will run**:

- `DB_USERNAME`: the name of an existing MySQL account with access to `kmie_db`.
- `DB_PASSWORD`: that MySQL account's password.
- `ADMIN_PASSWORD`: the password you want to use for the application's `admin` login.

These PowerShell environment variables apply to the current terminal session. If you open another terminal, set them again there before starting the application. Do not add real credentials to this README or the submitted ZIP.

**Counsellor username:** `counsellor`

**Admin password:** the value supplied through `ADMIN_PASSWORD`.

The submitted `SecurityConfig.java` expects the `ADMIN_PASSWORD` environment variable. If it is missing, application startup may fail.

### Step 4: Compile the Application

From the same PowerShell terminal, run:

```powershell
.\mvnw.cmd clean compile
```

Expected output includes `BUILD SUCCESS`.

### Step 5: Start Spring Boot

From the **same PowerShell terminal**, run:

```powershell
.\mvnw.cmd spring-boot:run
```

Wait until Spring Boot reports that the application has started on port 8080. Do not start a second instance while another application is using that port.

### Step 6: Open the Application

- Home page: `http://localhost:8080/`
- Student enquiry: `http://localhost:8080/enquiry`
- Demo booking: `http://localhost:8080/demo-booking`
- Admin dashboard: `http://localhost:8080/admin/enquiries`

## 7. REST API Testing Using Postman

The base URL is `http://localhost:8080`. Read-only GET requests are public. POST requests require an authenticated counsellor session and a valid CSRF token.

### GET Student Enquiries

**Method:** `GET`  
**Endpoint:** `/api/enquiries`  
**Expected:** `200 OK`, with registered student records in JSON.

### POST Student Enquiry

**Method:** `POST`  
**Endpoint:** `/api/enquiries`  
**Expected:** `201 Created` for valid registration; `409 Conflict` when the phone number is already registered.

Send valid registration details as JSON, using the fields defined by `StudentEnquiryRestController.java`.

### GET Demo Bookings

**Method:** `GET`  
**Endpoint:** `/api/bookings`  
**Expected:** `200 OK`, containing booking records in JSON.

### POST Demo Booking

**Method:** `POST`  
**Endpoint:** `/api/bookings`

Example request body:

```json
{
  "phone": "YOUR_REGISTERED_10_DIGIT_NUMBER",
  "demoDate": "YYYY-MM-DD",
  "demoTime": "13:00:00"
}
```

Use a phone number that already exists in `student_enquiries`, and replace the date placeholder with an available future date. Expected: `201 Created`.

Example response **from a previous development test**:

```json
{
  "id": 5,
  "studentId": 6,
  "studentName": "Test Student API",
  "course": "JEE",
  "demoDate": "2026-10-17",
  "demoTime": "15:00:00",
  "status": "PENDING"
}
```

### Important: Spring Security and CSRF

Spring Security protects `/admin/**` and API write requests. For POST requests, sign in as `counsellor` using the `ADMIN_PASSWORD` value and use that authenticated session in Postman. POST requests also require a valid CSRF token, for example in the `X-CSRF-TOKEN` header. Obtain the current token from a page in the same authenticated session. After a server restart or a new login, the token or session may need to be refreshed.

## 8. Validation and Test Results

The following scenarios were verified during development:

| Test Scenario | Expected Result | Outcome |
|---|---|---|
| Student enquiry registration | Registration successful | Passed |
| Duplicate student registration | Duplicate rejected | Passed |
| GET student enquiries | 200 OK | Passed |
| POST student enquiry | 201 Created | Passed |
| GET demo bookings | 200 OK | Passed |
| POST valid demo booking | 201 Created | Passed |
| Duplicate demo booking | 409 Conflict | Passed |
| Different time slot booking | 201 Created | Passed |
| Unregistered student booking | 400 Bad Request | Passed |
| Admin dashboard booking display | Existing bookings displayed | Passed |
| Demo date filter | Matching bookings displayed | Passed |
| Clear booking filters | All bookings displayed | Passed |

### Duplicate Booking Validation

When a student submits a booking for a slot they already booked, the API returns:

```json
{
  "error": "You have already booked this date and time slot."
}
```

### Unregistered Student Validation

When an unregistered mobile number is submitted, the API returns:

```json
{
  "error": "Student is not registered."
}
```

### Different Slot Validation

A student can successfully book another available time slot on the same date. Duplicate prevention applies to the exact combination of student, date, and time.

## 9. Admin Dashboard Verification

During development, the dashboard displayed **6 student enquiries** and **5 demo bookings**.

Date filtering was verified for **17 October 2026**, displaying two matching booking records.

The dashboard supports these status filter options:

- `PENDING`
- `CONFIRMED`
- `COMPLETED`
- `CANCELLED`

New bookings are initially stored as `PENDING`. The current dashboard displays and filters statuses; it does not provide an action to change a booking's status.

## 10. Error Handling

The application includes validation or rejection logic for:

- Invalid student details.
- Duplicate mobile number registration.
- Unregistered students attempting demo bookings.
- Duplicate booking attempts.
- Invalid booking time slots.
- Past demo dates.
- Expired same-day slots.
- Unauthorized requests.

The REST API returns meaningful error messages and appropriate HTTP response codes for supported validation cases. Not every listed case was independently retested during the final submission review.

## 11. Security

The application uses Spring Security, including:

- Admin authentication.
- Role-based access control for admin and API endpoints.
- Session-based login.
- CSRF protection.
- Logout functionality.
- BCrypt password hashing for the admin user.

The submission copy reads the administrator password from `ADMIN_PASSWORD`, and the MySQL connection credentials from `DB_USERNAME` and `DB_PASSWORD`. Do not commit real secrets to source control or include them in your submission ZIP.

If previously used credentials have been shared or exposed, change those credentials before using the application in a real environment.

## 12. Conclusion

The KMIE Student Enquiry & Demo Booking Automation System provides a practical solution for managing student enquiries and demo session registrations.

It integrates Spring Boot, Thymeleaf, MySQL, Spring Data JPA, Spring Security, and REST APIs.

The implemented system supports student registration, duplicate prevention, demo booking, booking validation, REST API operations, and a secured administration dashboard for viewing and filtering records.
