FROM eclipse-temurin:21-jdk-jammy
RUN addgroup --system appgroup && adduser --system --group appuser
WORKDIR /app
COPY build/libs/*.jar app.jar
RUN chown -R appuser:appgroup /app
EXPOSE 8082
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]