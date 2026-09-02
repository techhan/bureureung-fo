# 1단계: 빌드
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

# 2단계: 실행
FROM eclipse-temurin:21-jre-alpine
WORKDIR /spring-boot
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/spring-boot/app.jar"]