# ============================================================
# WOLVERINE STACK — Backend Dockerfile
# Multi-stage build: build stage + runtime stage
# ============================================================

# ---- Stage 1: Build ----
FROM gradle:8.13-jdk21 AS build

WORKDIR /app

# Copy only build config first (cache deps)
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle gradle
COPY gradlew gradlew.bat ./

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the application
RUN ./gradlew bootJar -x test --no-daemon

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/app.jar app.jar

# Expose the port
EXPOSE 8027

# Healthcheck (matches application.properties default port 8027)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8027}/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
