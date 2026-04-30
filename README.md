# library_db

A Spring Boot project for working with databases. The application implements a simple library system and shows work with two databases: PostgreSQL and MySQL.

## What is implemented

- register readers;
- adding books, authors, and publishing houses;
- search for books by one parameter;
- search by two related parameters;
- aggregating reports on genres and debtors;
- database selection in the interface:
- PostgreSQL;
- MySQL;
- PostgreSQL + MySQL for addition operations;
- separate JDBC connections to PostgreSQL and MySQL;
- server-side validation of forms and processing of typical input errors.

## Stack

- Java 21
- Spring Boot 4
- Spring MVC
- Thymeleaf
- JDBC / JdbcTemplate
- PostgreSQL
- MySQL
- Gradle

## Project structure

- `src/main/java/com/example/library_db/controller` - MVC controllers and error handling.
- `src/main/java/com/example/library_db/service` - business logic and working with PostgreSQL/MySQL.
- `src/main/java/com/example/library_db/config' - configuration of two data sources.
- `src/main/java/com/example/library_db/dto` - DTO and forms.
- `src/main/resources/templates` - Thymeleaf pages.
- `src/main/resources/static' - CSS and JavaScript.

## Setting up databases

The project expects that local databases have already been created.:

- PostgreSQL: `library_db`
- MySQL: `library_db_mysql`

Connections are configured in `src/main/resources/application.properties`:

```properties
app.datasource.postgres.jdbc-url=jdbc:postgresql://localhost:5432/library_db
app.datasource.postgres.username=postgres
app.datasource.postgres.password=your_password

app.datasource.mysql.jdbc-url=jdbc:mysql://localhost:3306/library_db_mysql?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
app.datasource.mysql.username=root
app.datasource.mysql.password=your_password
```

## Launch

```powershell
.\gradlew.bat bootRun
```

After launch, the application will be available at:

```text
http://localhost:8080
```

## Tests

```powershell
.\gradlew.bat test
```

## Note

This is a learning project, so the database schema and test data must be prepared separately in PostgreSQL and MySQL.
