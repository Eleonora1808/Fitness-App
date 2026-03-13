# FitnessApp

A web application for tracking workouts, meals, daily logs, and weight progress. Users can register, log calories and exercises, view dashboards and reports, and (for admins) manage users and view aggregated reports.

## Tech Stack

- **Java 17** · **Spring Boot 3.5**
- **Spring Security** (form login)
- **Spring Data JPA** · **MySQL**
- **Thymeleaf** (templates) · **Bootstrap 5**
- **OpenFeign** (optional food microservice client)

## Prerequisites

- JDK 17+
- Maven 3.6+
- MySQL 8 (or compatible) running locally or remotely

## Setup

1. **Clone and build**

   ```bash
   git clone https://github.com/<username>/FitnessApp.git
   cd FitnessApp
   mvn clean install
   ```

2. **Database**

   Create a MySQL database and user so the app can connect:

   - Database name: `fitness_app`
   - Username: `root` (or change `spring.datasource.username` in `src/main/resources/application.properties`)
   - Password: `1234` (the committed config uses this; change it in `application.properties` if your MySQL user has a different password)

   Example in MySQL:

   ```sql
   CREATE DATABASE fitness_app;
   CREATE USER 'root'@'localhost' IDENTIFIED BY 'changeme';
   GRANT ALL ON fitness_app.* TO 'root'@'localhost';
   FLUSH PRIVILEGES;
   ```

   Schema is created automatically on first run (`spring.jpa.hibernate.ddl-auto=update`). Demo users and data are seeded when `fitnessapp.data.initialize=true` (default).

3. **Optional: Food microservice**

   Meal nutrition can be backed by an external service. Set `fitnessapp.microservice.food.url` (default `http://localhost:8081`) or disable/adapt the client if you don’t use it.

## Running

```bash
mvn spring-boot:run
```

Then open **http://localhost:8080** in a browser.

- **Register** a new account or use existing credentials.
- **Dashboard**: today’s calories, quick actions, recent workouts.
- **Workouts** · **Meals** · **Daily Log** · **Progress** · **Reports** · **Profile** are available from the navigation after login.
- **Admin**: admin users can access **Admin Panel** and **User Reports** (roles are seeded when data initialization is enabled).

## Configuration

| Property | Description |
|----------|-------------|
| `spring.datasource.*` | MySQL connection (url, username, password) |
| `fitnessapp.data.initialize` | Set to `true` to seed demo users and data on startup |
| `fitnessapp.microservice.food.url` | Base URL of the food/nutrition microservice (if used) |
| `server.servlet.session.timeout` | Session duration (default 30m) |

If you use a different MySQL password locally, edit `application.properties` and leave it uncommitted (or add `application.properties` to `.gitignore` again) so you don’t push real credentials.

## Project Structure

- `config/` — Security, data init, web and app properties
- `controller/` — Web controllers (auth, dashboard, workouts, meals, logs, progress, reports, admin)
- `entities/` — JPA entities (User, Workout, Meal, DailyLog, Progress, etc.)
- `repository/` — Spring Data JPA repositories
- `service/` — Business logic
- `client/` — Feign client for external food API
- `src/main/resources/templates/` — Thymeleaf HTML templates


