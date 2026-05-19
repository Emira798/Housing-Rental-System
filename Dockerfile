# ============================================
# 阶段1: 构建前端 Vue3
# ============================================
FROM node:18-alpine AS frontend-build
WORKDIR /app/vue3
COPY vue3/package.json vue3/package-lock.json ./
RUN npm ci
COPY vue3/ ./
RUN npm run build

# ============================================
# 阶段2: 构建后端 Spring Boot（含前端静态文件）
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY springboot/pom.xml ./
RUN mvn dependency:go-offline -B
COPY springboot/src ./src
COPY --from=frontend-build /app/vue3/dist ./src/main/resources/static
RUN mvn package -DskipTests -B

# ============================================
# 阶段3: 运行（单个 JAR 包含前后端）
# ============================================
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
