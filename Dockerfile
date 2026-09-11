# Этап 1: Сборка
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Сначала только pom.xml — для кэширования зависимостей
COPY pom.xml .
COPY tickets-module-db/pom.xml ./tickets-module-db/
COPY tickets-module-api/pom.xml ./tickets-module-api/
COPY tickets-module-impl/pom.xml ./tickets-module-impl/

# Копируем весь исходный код
COPY . .

# Собираем все модули
RUN mvn clean package -DskipTests

# Этап 2: Запуск
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Копируем только готовый fat JAR
COPY --from=build /app/tickets-module-impl/target/tickets-module-impl-0.0.1-SNAPSHOT.jar app.jar

# Railway сам подставит $PORT через application-railway.yml
CMD ["java", "-jar", "app.jar"]