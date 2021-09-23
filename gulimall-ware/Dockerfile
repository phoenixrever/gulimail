FROM java:8
EXPOSE 8080

VOLUME /tmp
ADD target/*.jar  /app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar","-Xms128m","-Xmx300m","--spring.profiles.active=prod"]