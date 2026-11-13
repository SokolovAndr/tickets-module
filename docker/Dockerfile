# Этап 1: Сборка
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Сначала только pom.xml — для кэширования зависимостей
COPY pom.xml .
COPY tickets-module-db/pom.xml ./tickets-module-db/
COPY tickets-module-api/pom.xml ./tickets-module-api/
COPY tickets-module-impl/pom.xml ./tickets-module-impl/
COPY tickets-module-app/pom.xml ./tickets-module-app/
COPY tickets-module-web/pom.xml ./tickets-module-web/
COPY tickets-module-domain/pom.xml ./tickets-module-domain/

# Скачиваем зависимости (этот слой кэшируется, если pom.xml не менялись)
RUN mvn dependency:go-offline -B

# Копируем весь исходный код
COPY . .

# Собираем все модули
RUN mvn clean package -Dmaven.test.skip=true

# Этап 2: Запуск
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Копируем только готовый fat JAR
COPY --from=build /app/tickets-module-app/target/tickets-module-app-0.0.1-SNAPSHOT.jar app.jar

# По умолчанию приложение запускается на порту 8080 (см. application.yml)
# Профиль и переменные окружения (например, JWT_SECRET) передаются при запуске контейнера
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]