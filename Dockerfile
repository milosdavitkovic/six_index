# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
# Cache dependency layer separately for faster rebuilds
COPY pom.xml .
RUN mvn -q dependency:resolve dependency:resolve-plugins -DskipTests
# Copy source and build
COPY src ./src
RUN mvn -q -DskipTests -Dmaven.test.skip=true clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# curl is used by the container health check
RUN apk add --no-cache curl
# Create non-root user with minimal privileges
RUN addgroup -S -g 1001 spring && adduser -S -D -H -u 1001 -G spring spring \
    && chown -R spring:spring /app
USER spring
# Copy built JAR from builder
COPY --from=build --chown=spring:spring /workspace/target/*.jar app.jar
EXPOSE 8080
# Health check using curl
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -s --fail http://127.0.0.1:8080/actuator/health/readiness || exit 1
# Optimized JVM startup flags
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75", "-XX:+ParallelRefProcEnabled", "-XX:+UseStringDeduplication", "-jar", "app.jar"]
