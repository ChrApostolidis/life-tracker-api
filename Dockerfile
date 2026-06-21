# ---- Build stage: compile and package the jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copy pom first and cache dependencies in their own layer, so changing
# source code doesn't force a re-download of every dependency.
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage: small image with just a JRE ----
# glibc-based JRE (not alpine/musl) so the SQLite native library loads reliably.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user instead of root.
RUN useradd --system --uid 1001 appuser

# The SQLite file lives here. Mount a volume at /data so the database
# survives container restarts and redeploys.
ENV DB_PATH=/data/lifetracker.db
RUN mkdir -p /data && chown appuser:appuser /data
VOLUME /data

COPY --from=build /app/target/*.jar app.jar

USER appuser
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
