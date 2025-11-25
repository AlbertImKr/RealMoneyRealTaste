# Spring Boot Kotlin 애플리케이션을 위한 멀티-스테이지 빌드
# 스테이지 1: 빌드 단계
FROM eclipse-temurin:21-jdk-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# Git 및 기타 빌드 의존성 설치
RUN apk add --no-cache git

# Gradle wrapper와 설정 파일들 복사
COPY gradlew ./
COPY gradle/ gradle/
COPY build.gradle* ./
COPY settings.gradle* ./
COPY gradle.properties* ./

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 다운로드
RUN ./gradlew build --no-daemon --stacktrace || true

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew build -x test --no-daemon --stacktrace

# 스테이지 2: 실행 단계
FROM eclipse-temurin:21-jre-alpine

# 실행에 필요한 패키지 설치
RUN apk add --no-cache tzdata curl

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/"$TZ" /etc/localtime \
    && echo "$TZ" > /etc/timezone \
    && addgroup -g 1001 appgroup \
    && adduser -u 1001 -G appgroup -s /bin/sh -D appuser

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# appuser로 소유권 변경
RUN chown -R appuser:appgroup /app

# 비-root 사용자로 전환
USER appuser

# 포트 노출
EXPOSE 8080

# 헬스 체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 프로덕션용 JVM 옵션
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
