FROM openjdk:latest
COPY ./target/DevopsClassRoom1-0.1.0.4-jar-with-dependencies.jar /tmp
WORKDIR /tmp
ENTRYPOINT ["java", "-jar", "DevopsClassRoom1-0.1.0.4-jar-with-dependencies.jar"]