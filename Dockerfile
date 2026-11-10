# ============================================
# Antídoto — API Java (imagem para o Render)
# Compila com o Gradle Wrapper e roda só com a JRE.
# ============================================

# --- Etapa 1: build ---
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Dependências primeiro: mudar só o código não baixa tudo de novo
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null

COPY src ./src
RUN ./gradlew --no-daemon bootJar

# --- Etapa 2: execução ---
FROM eclipse-temurin:21-jre-jammy

LABEL application="antidoto-api"

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

# Roda sem root
RUN useradd -ms /bin/bash antidoto
USER antidoto

EXPOSE 8080

# O plano grátis do Render tem 512 MB: a JVM usa até 75% disso
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
