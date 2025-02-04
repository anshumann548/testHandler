FROM maven:3.8.6-eclipse-temurin-17


# Set the working directory
WORKDIR /app

# Install Git
RUN apt-get update && apt-get install -y git

# Clone the frontend Spring Boot application repository
ARG GIT_REPO_URL=https://github.com/anshumann548/testHandler
RUN git clone ${GIT_REPO_URL} /app/test-project

# Clone the backend test code repository
ARG BACKEND_REPO_URL=https://github.com/anshumann548/restructuredonboarding
RUN git clone ${BACKEND_REPO_URL} /app/backend-test-code

# Configure Git to bypass TLS issues
RUN git config --global http.sslVerify false

# Set environment variables
ENV test.project.path=/app/test-project
ENV maven.path=mvn

# Command to run the application
CMD ["mvn", "spring-boot:run", "-f", "test-project/pom.xml"]
