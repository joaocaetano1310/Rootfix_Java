FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system rootfix && useradd --system --gid rootfix rootfix

COPY --from=build /app/target/rootfix-api.jar app.jar
USER rootfix

ENV JAVA_OPTS="-XX:MaxRAMPercentage=70"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
