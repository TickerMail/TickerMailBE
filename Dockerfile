FROM openjdk:17-jdk-slim
WORKDIR /app

# 인증서 파일 복사
COPY certificate.pem /app/certificate.pem

# 인증서를 Java 키스토어에 추가
RUN keytool -importcert -file /app/certificate.pem -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -alias customcert

COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]