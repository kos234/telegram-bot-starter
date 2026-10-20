# ЭТАП 1: Сборка (Build)
# Используем образ с полным JDK для компиляции
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Копируем файлы Maven Wrapper и pom-файлы модулей
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY telegram-bot-spring-boot-starter/pom.xml telegram-bot-spring-boot-starter/
COPY telegram-bot-payment-tinkoff-spring-boot-starter/pom.xml telegram-bot-payment-tinkoff-spring-boot-starter/
COPY telegram-bot-starter-runner/pom.xml telegram-bot-starter-runner/

# Делаем скрипт исполняемым
RUN chmod +x ./mvnw

# ОПТИМИЗАЦИЯ: Скачиваем зависимости ДО копирования исходного кода
RUN ./mvnw dependency:go-offline -pl telegram-bot-starter-runner -am || true

# Копируем исходный код модулей
COPY telegram-bot-spring-boot-starter telegram-bot-spring-boot-starter
COPY telegram-bot-payment-tinkoff-spring-boot-starter telegram-bot-payment-tinkoff-spring-boot-starter
COPY telegram-bot-starter-runner telegram-bot-starter-runner

# Собираем модуль runner и зависимые стартеры
RUN ./mvnw clean package -pl telegram-bot-starter-runner -am -DskipTests

# ЭТАП 2: Запуск (Run)
# Используем JRE для легковесного продакшн-образа
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем собранный JAR файл из этапа builder
COPY --from=builder /app/telegram-bot-starter-runner/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
