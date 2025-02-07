# Use a base image with Maven and Java installed
FROM maven:3.8.6-eclipse-temurin-17

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# Install Google Chrome
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Install ChromeDriver
RUN wget -N https://storage.googleapis.com/chrome-for-testing-public/133.0.6943.53/linux64/chromedriver-linux64.zip && \
    unzip chromedriver-linux64.zip && \
    mv chromedriver-linux64/chromedriver /usr/local/bin/ && \
    chmod +x /usr/local/bin/chromedriver && \
    rm chromedriver-linux64.zip

# Install Git
RUN apt-get update && apt-get install -y git

# Configure Git to bypass TLS issues
RUN git config --global http.sslVerify false

# Clone the frontend Spring Boot application repository
ARG GIT_REPO_URL=https://github.com/anshumann548/testHandler
RUN git clone ${GIT_REPO_URL} /app/test-project

# Clone the backend test code repository
ARG BACKEND_REPO_URL=https://github.com/anshumann548/restructuredonboarding
RUN git clone ${BACKEND_REPO_URL} /app/backend-test-code

# Set environment variables
ENV test.project.path=/app/test-project
ENV maven.path=mvn

# Set the working directory
WORKDIR /app

# Expose any ports if necessary
EXPOSE 8080

# Command to run the application
CMD ["mvn", "spring-boot:run", "-f", "test-project/pom.xml"]