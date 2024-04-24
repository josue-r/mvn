# Use Alpine Linux as the base image for a lightweight container
FROM alpine:latest

# Install necessary dependencies for Corretto installation
RUN apk --no-cache add curl binutils

# Download and install Amazon Corretto
RUN curl -L https://corretto.aws/downloads/latest/amazon-corretto-11-x64-alpine-linux-jdk.tar.gz -o /tmp/corretto.tar.gz \
    && tar -xzf /tmp/corretto.tar.gz -C /opt \
    && rm /tmp/corretto.tar.gz \
    && ln -s /opt/amazon-corretto-*/bin/* /usr/bin/

# Set JAVA_HOME environment variable
ENV JAVA_HOME=/opt/amazon-corretto-11
ENV PATH=$PATH:$JAVA_HOME/bin

# Verify installation
RUN java -version

# Command to keep the container running
CMD ["sh"]
