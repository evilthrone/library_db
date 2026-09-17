# library_db

## Веб-сайт: этапы 1–4

- `/` — информационная главная учебной библиотеки.
- `/app` — прежний выбор раздела читателя или сотрудника.
- `/reader`, `/staff` и существующие маршруты форм, поиска, API и отчётов сохранены.
- Общие фрагменты новых страниц: `templates/fragments/site.html`; оформление: `static/css/site.css`.
  Все существующие HTML-экраны, включая формы, результаты поиска, агрегацию и ошибки,
  используют общие шапку, логотип, верхнее меню и подвал. Стили элементов форм из `style.css`
  дополнены оформлением в `site.css`; поля, обработчики и логика работы с БД сохранены.
- Настройки библиотеки собраны в `src/main/resources/site.properties` (UTF-8).
  Перед сдачей заполни название, город, адрес, график, телефон, email, автора проекта и координаты.
  Текущие название, город и координаты демонстрационные; координаты не обозначают адрес библиотеки.
  Свойства можно переопределить параметрами запуска, например `--site.name="Моя библиотека"`.
- Выбор читателя/сотрудника открывает меню и не является авторизацией.

Информационные страницы и меню приложения не запрашивают данные БД. Поиск книг, отчёты и операции записи
по-прежнему требуют заранее подготовленных PostgreSQL/MySQL. Схема БД автоматически не создаётся.

Для запуска используй Java 21. Если `JAVA_HOME` указывает на другую версию, задай его
для текущей PowerShell-сессии (укажи свой путь к JDK):

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
.\gradlew.bat test bootJar
.\gradlew.bat bootRun
```

В следующих этапах: поиск по сайту, десять инструментов,
итоговая проверка HTML и подготовка публикации.
Ссылки на ещё не реализованные разделы в меню не добавлены.
XML и новости из БД относятся к последующим лабораторным.

Публикация, регистрация в поисковике и отправка адреса преподавателю пока не выполнялись.

Проверка этапов 1–2 (17.09.2026): `test bootJar` на Java 21 выполнена успешно, 3 теста прошли.
Тесты проверяют рендеринг главной и меню, переопределение названия и отсутствие вызовов
сервисов БД при открытии этих страниц. При запуске JAR страницы `/`, `/app`, `/reader`,
`/staff`, `/input` и `/css/site.css` вернули HTTP 200 без подключённой БД.
Операции с данными без БД не проверялись. Проверка в браузере на разных размерах экрана
и валидация W3C пока не выполнялись.

После объединения оформления: `test bootJar` выполнена успешно, 5 тестов прошли.
Дополнительно проверены все старые HTML-экраны, результаты поиска с тестовой книгой,
повторный показ форм при ошибках валидации и страница ошибки подключения к БД.
В этих тестах сервисы БД заменены mock-объектами; это проверка рендеринга,
а не подтверждение работоспособности операций с реальными БД.
Дополнительно просмотрены снимки Chrome: меню читателя на широком экране и форма
регистрации в узком окне. Полная проверка всех страниц на мобильных устройствах
и валидация W3C остаются отдельной задачей.

### Информационные страницы

| Адрес | Страница |
| --- | --- |
| `/` | Главная |
| `/about` | О библиотеке |
| `/rules` | Правила пользования |
| `/membership` | Как стать читателем |
| `/departments` | Отделы библиотеки |
| `/library-services` | Услуги библиотеки |
| `/achievements` | Достижения и проекты |
| `/resources` | Полезные ресурсы |
| `/faq` | Вопросы и ответы |
| `/contacts` | Контакты |

Девять новых страниц используют общий шаблон `templates/site/information.html`, но имеют
отдельные URL, title, h1 и содержимое. Статические тексты, ссылки и описания находятся
в `src/main/java/com/example/library_db/site/SitePageCatalog.java`. Этот каталог можно
использовать для следующего этапа поиска без отдельной копии текстов.
Главная остаётся в `templates/site/home.html`, контакты берутся из `site.properties`.
Никакие информационные тексты не загружаются из БД.

Содержимое описывает учебную модель: реальные награды, мероприятия, сроки выдачи и адрес
не заявлены. Перед сдачей владелец должен проверить и дополнить правила обслуживания,
перечень документов, структуру отделов, услуги и сведения о реальных проектах.

На `/resources` размещены ссылки на [НЭБ](https://rusneb.ru/),
[РГБ](https://www.rsl.ru/), [Президентскую библиотеку](https://www.prlib.ru/),
[Грамоту.ру](https://gramota.ru/) и [Project Gutenberg](https://www.gutenberg.org/).
Назначение ресурсов сверено с их официальными страницами; для НЭБ и Грамоты использованы
также результаты поиска по официальным сайтам, поскольку прямое чтение вернуло 403.
Доступность всех внешних сайтов из браузера пользователя не гарантируется.

Проверка этапов 3–4: `test bootJar` на Java 21 прошла, всего 7 тестов.
Проверены уникальные заголовки десяти информационных страниц, общий макет,
пять внешних ссылок и отсутствие вызовов сервисов БД при открытии новых страниц.
Запущенный JAR вернул HTTP 200 для всех десяти информационных URL.
Просмотрены снимки Chrome: `/about` при ширине 1280 px и `/contacts` при ширине 600 px.
Поиск по сайту, инструменты, полная валидация W3C и публикация ещё не выполнены.

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
