# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml pom.xml
COPY rag-service/pom.xml rag-service/pom.xml
# Récupération de toutes les dépendances avant de copier le code source et de compiler (utile pour la mise en cache docker sans dépendence au code source)
RUN mvn -B -f rag-service/pom.xml dependency:go-offline
# Récupération du code source et compilation
COPY rag-service/src rag-service/src
RUN mvn -e -DskipTests -f rag-service/pom.xml package
#RUN --mount=type=cache,target=/root/.m2 mvn -pl rag-service -am -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /app/rag-service/target/rag-service-*.jar app.jar
COPY rag-service/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/entrypoint.sh"]