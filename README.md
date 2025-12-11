# Fitness App

A fitness tracking application built with Spring Boot that helps users track workouts, meals, daily logs, and progress.

## Tech Stack

- Spring Boot 3.5.8, Java 17, Maven
- Thymeleaf (Spring MVC), Spring Security
- MySQL 8, Spring Data JPA
- Spring Cache, Spring Scheduling

## Quick Start

1. Create database: `CREATE DATABASE fitness_app;`
2. Update `application.properties` with database credentials
3. Run: `mvn spring-boot:run`
4. Access: `http://localhost:8080`

## Features

- User authentication and role-based access (USER, ADMIN)
- Workout tracking with automatic calorie estimation
- Meal logging with nutrition calculation
- Daily activity logs and progress tracking
- Admin user management

## Functionalities

1. Create/Update/Delete Workout
2. Create/Update Daily Log
3. Add/Update/Delete Meal
4. Add Progress Entry
5. Generate Weekly Reports
6. Admin: Block/Unblock Users, Manage Roles

## Testing

Run: `mvn test`

Includes unit, integration, and API tests.
