FROM maven:3.6.3

WORKDIR /tmp/build

ADD pom.xml .
ADD src/main/java/com/gzqylc/BootApplication.java src/main/java/com/gzqylc/BootApplication.java
RUN mvn -q  package

ADD src ./src
RUN mvn -q -DskipTests=true package \
        && mv target/*.jar /app.jar \
        && cd / && rm -rf /tmp/build

FROM openjdk:8
COPY --from=0 /app.jar /app.jar

EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Duser.timezone=Asia/Shanghai","-jar","/app.jar"]