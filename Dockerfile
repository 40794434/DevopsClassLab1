FROM openjdk:latest
COPY ./target/DevopsClassRoom1-0.1.0.2-jar-with-dependencies.jar /tmp
WORKDIR /tmp
ENTRYPOINT ["java", "-jar", "DevopsClassRoom1-0.1.0.2-jar-with-dependencies.jar"]