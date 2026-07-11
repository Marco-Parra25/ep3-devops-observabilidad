# ---------- Etapa 1: compilación ----------
# Compilamos el JAR dentro de una imagen con Maven + JDK 21 (build reproducible).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos primero el pom para aprovechar la caché de dependencias de Docker.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiamos el código y empaquetamos (sin correr tests aquí; ya corren en el pipeline).
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Etapa 2: ejecución ----------
# Imagen final liviana, solo con el JRE. Menor superficie de ataque (seguridad).
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Usuario no-root con UID fijo 1001 (coincide con runAsUser del despliegue en K8s).
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
USER 1001

COPY --from=build /app/target/observability-service-*.jar app.jar

EXPOSE 8080

# Healthcheck a nivel de contenedor (complementa las probes de Kubernetes).
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
