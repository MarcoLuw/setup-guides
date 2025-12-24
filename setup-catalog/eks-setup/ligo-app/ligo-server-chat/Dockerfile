FROM amd64/amazoncorretto:21-alpine AS jar

# curl 설치 (Alpine Linux)
#RUN apk update && apk add --no-cache curl

# 애플리케이션 JAR 파일 복사
COPY ./build/libs/ligo-0.0.1-SNAPSHOT.jar /app/ligo-server-chat.jar

# 작업 디렉터리 설정
WORKDIR /app

# 컨테이너 실행 시 Java 애플리케이션 실행
CMD ["java", "-jar", "ligo-server-chat.jar"]