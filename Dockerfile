ARG REGISTRY=uhub.service.ucloud.cn/myblog_docker

# Stage 1: Build
FROM ${REGISTRY}/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# Download dependencies first (cache layer)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Run
FROM ${REGISTRY}/eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
