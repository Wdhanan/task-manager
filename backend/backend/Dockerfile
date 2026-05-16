# backend/Dockerfile

# ═══════════════════════════════════════════
# STUFE 1: BUILD (Java kompilieren)
# ═══════════════════════════════════════════
FROM maven:3.9-eclipse-temurin-21 AS build
# ↑ Maven (Build-Tool) + Java 21

WORKDIR /app

# Erst nur pom.xml kopieren (Cache-Optimierung!)
# Wenn sich pom.xml nicht ändert, werden Dependencies nicht neu heruntergeladen
COPY pom.xml .
RUN mvn dependency:go-offline -B
# ↑ Alle Dependencies herunterladen (offline-fähig)
#   -B = "Batch Mode" (keine interaktive Ausgabe)

# Quellcode kopieren
COPY src ./src

# Testen und bauen
RUN mvn clean package -DskipTests
# ↑ -DskipTests = Tests überspringen im Docker Build
#   (Tests laufen in CI/CD Pipeline)
# Ergebnis: target/backend-0.0.1-SNAPSHOT.jar

# ═══════════════════════════════════════════
# STUFE 2: RUN (nur JRE, kein Maven)
# ═══════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine
# ↑ Nur Java Runtime (kein Maven, kein Compiler)
#   Viel kleiner als das Build-Image!

WORKDIR /app

# Nur das fertige JAR kopieren
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Java-Optimierungen für Container
ENTRYPOINT ["java", "-jar", "app.jar"]