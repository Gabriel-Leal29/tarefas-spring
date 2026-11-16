# ---------- Etapa 1: build ----------
# Imagem com Maven + JDK 21, usada só para compilar o projeto e gerar o .war
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Primeiro só o pom.xml, para baixar as dependências. Enquanto o pom não mudar, o Docker
# reaproveita esta camada do cache e não baixa tudo de novo a cada alteração no código.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Depois o código-fonte, que é o que muda com frequência
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: execução ----------
# Imagem final só com o JRE: sem Maven, sem JDK e sem o código-fonte, por isso bem menor
FROM eclipse-temurin:21-jre
WORKDIR /app

# Roda com um usuário comum, e não como root
RUN useradd --system --no-create-home spring
USER spring

COPY --from=build /build/target/tarefas-spring.war app.war

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
