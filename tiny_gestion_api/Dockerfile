# Étape 1 : Builder avec Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml ./
# Télécharger les dépendances pour utiliser le cache Docker
RUN mvn dependency:resolve -B || true
COPY src ./src
RUN mvn clean package -B -DskipTests

# Étape 2 : Exécution avec JRE allégé
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
