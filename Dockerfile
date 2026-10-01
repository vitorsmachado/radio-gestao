# =============================================================================
# radio-gestao — imagem de produção
# Build em duas etapas: compila com Maven e roda só o jar numa JRE enxuta.
# =============================================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
ENV TZ=America/Sao_Paulo
RUN useradd --system --uid 1001 app
COPY --from=build /app/target/radio-gestao-*.jar app.jar
USER app
EXPOSE 8080
# Limita o heap a uma fração da memória do container (planos pequenos têm
# 512 MB–1 GB); sem isso a JVM pode passar do limite e ser morta.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "app.jar"]
