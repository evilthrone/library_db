# library_db

Проект на Spring Boot для работы с базами данных. Приложение реализует простую библиотечную систему и показывает работу с двумя СУБД: PostgreSQL и MySQL.

## Что реализовано

- регистрация читателей;
- добавление книг, авторов и издательств;
- поиск книг по одному параметру;
- поиск по двум связанным параметрам;
- агрегирующие отчеты по жанрам и должникам;
- выбор базы данных в интерфейсе:
  - PostgreSQL;
  - MySQL;
  - PostgreSQL + MySQL для операций добавления;
- отдельные JDBC-подключения к PostgreSQL и MySQL;
- серверная валидация форм и обработка типовых ошибок ввода.

## Стек

- Java 21
- Spring Boot 4
- Spring MVC
- Thymeleaf
- JDBC / JdbcTemplate
- PostgreSQL
- MySQL
- Gradle

## Структура проекта

- `src/main/java/com/example/library_db/controller` - MVC-контроллеры и обработка ошибок.
- `src/main/java/com/example/library_db/service` - бизнес-логика и работа с PostgreSQL/MySQL.
- `src/main/java/com/example/library_db/config` - конфигурация двух источников данных.
- `src/main/java/com/example/library_db/dto` - DTO и формы.
- `src/main/resources/templates` - Thymeleaf-страницы.
- `src/main/resources/static` - CSS и JavaScript.

## Настройка баз данных

Проект ожидает, что локальные базы данных уже созданы:

- PostgreSQL: `library_db`
- MySQL: `library_db_mysql`

Подключения настраиваются в `src/main/resources/application.properties`:

```properties
app.datasource.postgres.jdbc-url=jdbc:postgresql://localhost:5432/library_db
app.datasource.postgres.username=postgres
app.datasource.postgres.password=your_password

app.datasource.mysql.jdbc-url=jdbc:mysql://localhost:3306/library_db_mysql?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
app.datasource.mysql.username=root
app.datasource.mysql.password=your_password
```

## Запуск

```powershell
.\gradlew.bat bootRun
```

После запуска приложение будет доступно по адресу:

```text
http://localhost:8080
```

## Тесты

```powershell
.\gradlew.bat test
```

## Примечание

Это учебный проект, поэтому схема базы данных и тестовые данные должны быть подготовлены отдельно в PostgreSQL и MySQL.
