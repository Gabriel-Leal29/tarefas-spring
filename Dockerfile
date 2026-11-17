FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --no-create-home spring
USER spring

COPY --from=build /build/target/tarefas-spring.war app.war

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
