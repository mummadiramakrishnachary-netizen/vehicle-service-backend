
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw && ./mvnw -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

# Import the Aiven CA certificate into Java's truststore
COPY ca.pem /tmp/ca.pem
RUN keytool -importcert -noprompt \
    -alias aiven-mysql-ca \
    -file /tmp/ca.pem \
    -cacerts \
    -storepass changeit \
    && rm /tmp/ca.pem

COPY --from=build /app/target/vehicle-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]
