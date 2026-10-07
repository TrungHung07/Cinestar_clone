# ============ Build stage ============
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Tai dependency truoc de tang toc build lan sau
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ============ Runtime stage ============
# Dung base Ubuntu (co san tzdata) de TZ=Asia/Ho_Chi_Minh co hieu luc -
# alpine thieu tzdata se van chay UTC -> expiresAt lech 7h so voi frontend
FROM eclipse-temurin:21-jre-noble
ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
