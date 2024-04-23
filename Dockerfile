# Use an official Maven base image
FROM maven:3.8.1-jdk-11 as build

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project files into the container
COPY my-app/pom.xml ./my-app/
COPY my-app/src ./my-app/src/

# Build the application within the my-app directory
RUN mvn -f ./my-app/pom.xml clean package

# Use OpenJDK for the runtime base image
FROM openjdk:11-jre-slim

# Set the working directory in the Docker image
WORKDIR /app

# Copy over the built artifact from the Maven image
COPY --from=build /app/my-app/target/my-app.jar my-app.jar

# Command to run the application
CMD ["java", "-jar", "my-app.jar"]
