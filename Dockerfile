# 1단계  빌드 이미지
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# 프로젝트 전체 복사
COPY . .

# 스프링 부트 jar 빌드
RUN ./gradlew clean bootJar -x test

# 2단계  실행 이미지
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드된 jar 복사
COPY --from=build /app/build/libs/ict05_final_admin-0.0.1-SNAPSHOT.jar app.jar

# Firebase 서비스 계정 파일 복사
# 컨테이너 안 경로: /app/fcm-secret/firebase-admin.json
RUN mkdir -p fcm-secret
COPY fcm-secret/firebase-admin.json fcm-secret/firebase-admin.json

# 스프링 부트 포트 (지금 8081 이니까 이렇게)
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
