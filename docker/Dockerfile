FROM openjdk:17-alpine

EXPOSE 8080

ARG Xmx="-Xmx500m"
ARG Xms="-Xmx500m"

ENV JAVA_OPTS "$Xmx \
              $Xms \
              -XX:+UseParallelGC"

ADD target/entry*.jar entry.jar

CMD [ "sh", "-c", "java $JAVA_OPTS -jar entry.jar" ]