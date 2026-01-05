# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml pom.xml
COPY user-service/pom.xml user-service/pom.xml
# Récupération de toutes les dépendances avant de copier le code source et de compiler (utile pour la mise en cache docker sans dépendence au code source)
RUN mvn -B -f user-service/pom.xml dependency:go-offline
# Récupération du code source et compilation
COPY user-service/src user-service/src
RUN mvn -e -DskipTests -f user-service/pom.xml package
#RUN --mount=type=cache,target=/root/.m2 mvn -pl user-service -am -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /app/user-service/target/user-service-*.jar app.jar
COPY user-service/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
RUN apk add --no-cache curl
EXPOSE 8080
ENTRYPOINT ["/entrypoint.sh"]
