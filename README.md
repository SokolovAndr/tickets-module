# Tickets-module
Многомодульный Maven-проект для работы с билетами и перевозчиками на Spring Boot.

[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=SokolovAndr_tickets-module&metric=alert_status)](https://sonarcloud.io/dashboard?id=SokolovAndr_tickets-module)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=SokolovAndr_tickets-module&metric=coverage)](https://sonarcloud.io/dashboard?id=SokolovAndr_tickets-module)

## 📋 Описание

Проект представляет собой модульное приложение на Java 17, построенное на Maven и Spring Boot 3.2.5.  
Основная бизнес-логика — управление билетами, маршрутами и перевозчиками.

## 📦 Модули проекта

| Модуль | Назначение |
|--------|-----------|
| `coverage-report` | Агрегация отчётов JaCoCo из всех модулей для SonarCloud |
| `tickets-module-app` | Точка входа, конфигурация, интеграционные тесты |
| `tickets-module-impl` | Реализация бизнес-логики, UNIT тесты |
| `tickets-module-api` | Контракты REST API: DTO и интерфейсы контроллеров (генерируются из OpenAPI) |
| `tickets-module-db` | Слой доступа к данным, миграции |

### Зависимости между модулями
```mermaid
graph TD
    APP[tickets-module-app] --> IMPL[tickets-module-impl]
    APP --> COV[coverage-report<br/><i>только на этапе сборки</i>]
    IMPL --> DB[tickets-module-db]
    IMPL --> API[tickets-module-api]
```

## 🛠 Технологии

| Технология | Назначение |
|------------|-----------|
| ☕ Java 17 + Spring Boot 3.2.5 | Основной язык и фреймворк |
| 📦 Maven (wrapper) | Многомодульная сборка |
| 🐘 PostgreSQL 13 | Основная база данных (через `compose.yaml`) |
| 🧪 JUnit 5 + MockMvc | Юнит- и интеграционные тесты |
| 📊 JaCoCo 0.8.11 | Покрытие кода |
| 🔍 SonarCloud | Анализ качества и Quality Gate |
| 🐳 Docker Compose | Локальный запуск БД |
| 📄 OpenAPI Generator | Генерация DTO и интерфейсов контроллеров |
| 🐳 Docker File | Сборка образа для деплоя на Railway |

## 🚀 Быстрый старт

### ✅ Требования

| Требование | Версия / Примечание |
|------------|---------------------|
| ☕ JDK | 17+ |
| 📦 Maven | или встроенный `./mvnw` |
| 🐳 Docker + Docker Compose | для запуска БД |

### 📥 Клонирование репозитория

```bash
git clone https://github.com/SokolovAndr/tickets-module.git
cd tickets-module
```

### 🐳 Запуск базы данных

```bash
docker compose up -d
```

### Сборка проекта

```bash
./mvnw clean install
```

### 🔨 Для полной сборки с интеграционными тестами и агрегированным отчётом JaCoCo:

```bash
./mvnw clean verify
```
Агрегированный отчёт будет доступен по пути:
coverage-report/target/site/jacoco/index.html 


### ▶️ Запуск приложения

```bash
./mvnw spring-boot:run -pl tickets-module-app
```

### или через собранный JAR:

```bash
java -jar tickets-module-app/target/tickets-module-app-0.0.1-SNAPSHOT.jar
```
Приложение по умолчанию будет доступно на http://localhost:8080.


## ⚙️ Конфигурация

Проект поддерживает профили Spring:

| Профиль | Назначение |
|---------|-----------|
| `application-local.yml` | Локальная конфигурация |
| `application-railway.yml` | Конфигурация для деплоя на [Railway](https://railway.app/) |
| `application-test.yml` | Конфигурация для тестов (H2 in-memory) |

## 🧪 Тестирование

| Тип тестов	| Плагин| Расположение |
|-------------|-------|------------- |
| Юнит-тесты	| Surefire	| tickets-module-impl/src/test/java |
| Интеграционные тесты |	Failsafe |	tickets-module-app/src/test/java, классы с суффиксом *IT |

### Запуск только юнит-тестов:

```bash
./mvnw test
```
### Запуск всех тестов (юнит + интеграционные):

```bash
./mvnw verify
```

## 📊 Покрытие кода

Проект использует агрегированный отчёт JaCoCo — отчёты со всех модулей собираются в coverage-report:
```bash
./mvnw verify
```
SonarCloud использует агрегированный отчёт через параметр:
sonar.coverage.jacoco.aggregateXmlReportPaths=coverage-report/target/site/jacoco/jacoco.xml

## 👤 Автор

**SokolovAndr** — [GitHub](https://github.com/SokolovAndr)

---

> ⚠️ Проект находится в активной разработке. API и структура могут меняться.
