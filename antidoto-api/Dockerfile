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

# O plano grátis do Render tem 512 MB e 0,1 de CPU: heap em até 60% da memória,
# coletor de lixo simples e JIT mais leve (sobe mais rápido e gasta menos memória)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60", "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1", "-Xss512k", "-jar", "app.jar"]
