# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml pom.xml
COPY document-service/pom.xml document-service/pom.xml
# Récupération de toutes les dépendances avant de copier le code source et de compiler (utile pour la mise en cache docker sans dépendence au code source)
RUN mvn -B -f document-service/pom.xml dependency:go-offline
# Récupération du code source et compilation
COPY document-service/src document-service/src
RUN mvn -e -DskipTests -f document-service/pom.xml package
#RUN --mount=type=cache,target=/root/.m2 mvn -pl document-service -am -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /app/document-service/target/document-service-*.jar app.jar
COPY document-service/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/entrypoint.sh"]
