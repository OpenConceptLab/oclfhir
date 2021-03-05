#Stage 1: Build
FROM openjdk:14 as build

RUN mkdir /code

ARG MAVEN_VERSION=3.6.3
ARG USER_HOME_DIR="/code"
ARG SHA=c35a1803a6e70a126e80b2b3ae33eed961f83ed74d18fcd16909b2d44d7dada3203f1ffe726c17ef8dcca2dcaa9fca676987befeadc9b9f759967a8cb77181c0
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && echo "Downlaoding maven" \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
  \
  && echo "Checking download file hash" \
  && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - \
  \
  && echo "Unziping maven" \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  \
  && echo "Cleaning and setting links" \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

COPY . /code

RUN cd /code \
  && echo "Building oclfhir project" \
  && mvn clean install

#Stage 2: Runtime  
FROM openjdk:14-jdk-alpine as runtime

RUN apk add --update bash curl && rm -rf /var/cache/apk/*

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the jar file from the build environment
COPY --from=build /code/ocl-fhir-ts/target/*.jar ocl-fhir-ts/target/

COPY startup.sh startup.sh
COPY wait_for_it.sh wait_for_it.sh

EXPOSE 7000

ENTRYPOINT ["bash", "-c", "./startup.sh"]

