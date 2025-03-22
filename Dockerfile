FROM amazoncorretto:21-al2023
WORKDIR /app

# Copy the jar file
COPY build/libs/SecretStash-0.0.1-SNAPSHOT.jar ./app.jar

# Expose the application port
EXPOSE 8081

# Set a default profile
ENV SPRING_PROFILES_ACTIVE=default

# Start the application using the environment variable
ENTRYPOINT ["java", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]