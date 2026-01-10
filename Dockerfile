# Multi-stage Dockerfile for Family Archive
# Stage 1: Build the application
FROM gradle:8.12.0-jdk21 AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files first (for layer caching)
COPY gradle gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies (cached if build files unchanged)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src
COPY vite.config.ts .
COPY tsconfig.json .
COPY tsconfig.node.json .
COPY postcss.config.js .
COPY tailwind.config.js .
COPY package.json .
COPY package-lock.json .

# Build the application (includes frontend build via Gradle)
# Skip tests in Docker build for faster iteration
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8080

# Health check (checks if the application is responding)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/ || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
