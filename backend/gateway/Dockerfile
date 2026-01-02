# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml pom.xml
COPY gateway/pom.xml gateway/pom.xml
# Récupération de toutes les dépendances avant de copier le code source et de compiler (utile pour la mise en cache docker sans dépendence au code source)
RUN mvn -B -f gateway/pom.xml dependency:go-offline
# Récupération du code source et compilation
COPY gateway/src gateway/src
RUN mvn -e -DskipTests -f gateway/pom.xml package
#RUN --mount=type=cache,target=/root/.m2 mvn -pl gateway -am -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /app/gateway/target/gateway-*.jar app.jar
COPY gateway/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/entrypoint.sh"]