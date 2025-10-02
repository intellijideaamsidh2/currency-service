FROM eclipse-temurin:21-jre-jammy

# Add a non-root user for security
RUN useradd -u 1001 spring

WORKDIR /currency-exchange

# Copy built jar from build stage
COPY /target/*.jar currency-exchange.jar

# Change ownership to non-root user
RUN chown spring:spring /currency-exchange/currency-exchange.jar
USER spring

# Expose application port
EXPOSE 8181

# JVM tuning (optional, safe defaults for containers)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /currency-exchange/currency-exchange.jar"]
