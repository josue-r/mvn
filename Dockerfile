# This assume you have built the jar file with mvn clean package
FROM amazoncorretto:21 as build
WORKDIR /build
COPY target/*.jar .
RUN mkdir -p dependency && (cd dependency; jar -xf ../*.jar)

FROM ghcr.io/valvoline-llc/vioc-amazoncorretto:21-alpine-latest

# Run as a non-root user for pod security policy non-root check
# According to https://github.com/amazonlinux/container-images/issues/28#issuecomment-492800501, the user id doesn't have to exist to use it
USER 1000:1000
# NEW_RELIC_AGENT_OPTS is set in the vioc-amazoncorretto, so that the new relic agent version is not maintained in the APIs individually
# Adding it to the JAVA_TOOL_OPTIONS, allows the agent options to be executed with the below java command
ENV JAVA_TOOL_OPTIONS=$NEW_RELIC_AGENT_OPTS

ARG DEPENDENCY=/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENV SPRING_PROFILES_ACTIVE=store

ARG DB_USER
ARG DB_PASS
ENV SPRING_DATASOURCE_USERNAME=$DB_USER
ENV SPRING_DATASOURCE_PASSWORD=$DB_PASS

CMD ["java", "-cp", "app:app/lib/*", "com.vioc.central.motor.MotorApiApplication"]
