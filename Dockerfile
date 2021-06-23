FROM adoptopenjdk/openjdk8:ubi
ARG JAR_FILE=target/*.jar
ENV TOKEN=token
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Ddiscord.bot.token=${TOKEN}", "-jar", "/app.jar"]