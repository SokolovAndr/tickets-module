# Tickets-module
Многомодульный Maven-проект для работы с тикетами и перевозчиками.

📋 Описание
Проект представляет собой модульное приложение на Java, построенное на Maven и Spring Boot. Состоит из трёх модулей:

## 📦 Модули проекта

| Модуль | Назначение |
|--------|-----------|
| `tickets-module-api` | REST API, контроллеры, DTO |
| `tickets-module-db` | Слой доступа к данным, миграции, сущности |
| `tickets-module-impl` | Реализация бизнес-логики |

## 🛠 Технологии

| Технология | Назначение |
|------------|-----------|
| ☕ Java (Spring Boot) | Основной язык и фреймворк |
| 📦 Maven | Многомодульная сборка проекта |
| 🐘 PostgreSQL | База данных (через `compose.yaml`) |
| 🐳 Docker Compose | Локальный запуск БД |

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

### 🔨 Сборка проекта

```bash
./mvnw clean install
```

### ▶️ Запуск приложения

```bash
./mvnw spring-boot:run -pl tickets-module-api
```

## ⚙️ Конфигурация

Проект поддерживает профили Spring:

| Профиль | Назначение |
|---------|-----------|
| `application-local.yml` | Локальная конфигурация |
| `application-railway.yml` | Конфигурация для деплоя на [Railway](https://railway.app/) |


## 👤 Автор

**SokolovAndr** — [GitHub](https://github.com/SokolovAndr)

---

> ⚠️ Проект находится в активной разработке. API и структура могут меняться.
