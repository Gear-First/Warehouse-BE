# --- 1단계: 빌드(Build) 환경 ---
# Gradle과 JDK 21이 포함된 이미지를 'builder'라는 이름의 임시 빌드 환경으로 사용합니다.
# gradle:8.7.0-jdk21 로 버전을 변경했습니다.
FROM gradle:8.7.0-jdk21 AS builder

# 컨테이너 내부에 작업 디렉터리를 만듭니다.
WORKDIR /app

# 현재 디렉터리의 모든 소스 코드를 컨테이너의 작업 디렉터리로 복사합니다.
COPY . .

# Gradle을 사용하여 스프링 부트 애플리케이션을 빌드하고 실행 가능한 .jar 파일을 생성합니다.
RUN ./gradlew bootJar


# --- 2단계: 최종 실행(Runtime) 환경 ---
# 실제 애플리케이션을 실행할 환경입니다. JDK 21만 포함된 가벼운 이미지를 사용합니다.
# openjdk:21-jdk-slim 으로 버전을 변경했습니다.
FROM openjdk:21-jdk-slim

# 컨테이너 내부에 작업 디렉터리를 만듭니다.
WORKDIR /app

# 1단계(builder)에서 생성된 .jar 파일을 지금의 최종 환경으로 복사해옵니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너가 시작될 때 실행될 명령어입니다. 'java -jar app.jar'를 실행하여 스프링 부트 애플리케이션을 시작합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]