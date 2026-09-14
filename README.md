Tickets-module
Многомодульный Maven-проект для работы с тикетами и перевозчиками.

📋 Описание
Проект представляет собой модульное приложение на Java, построенное на Maven и Spring Boot. Состоит из трёх модулей:

Модуль	Назначение
tickets-module-api	REST API, контроллеры, DTO
tickets-module-db	Слой доступа к данным, миграции, сущности
tickets-module-impl	Реализация бизнес-логики

🛠 Технологии
Java (Spring Boot)
Maven (многомодульная сборка)
PostgreSQL (через compose.yaml)
Docker Compose для локального запуска БД

🚀 Быстрый старт
Требования
JDK 17+
Maven (или используйте встроенный ./mvnw)
Docker и Docker Compose (для запуска БД)

⚙️ Конфигурация
Проект поддерживает профили Spring:
application-local.yml - локальная конфигурация
application-railway.yml — конфигурация для деплоя на Railway

👤 Автор
SokolovAndr — GitHub
