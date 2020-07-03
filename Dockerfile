# Build FE code
FROM node:14-buster-slim as assets

WORKDIR /app

COPY package*.json /app/
COPY tailwind.config.js /app/
COPY resources /app/resources/

RUN npm ci
RUN npm run css:prod

# Build Clojure code

FROM clojure:openjdk-13-tools-deps-slim-buster as builder

WORKDIR /app

# install main deps, sometimes change
COPY deps.edn /app/deps.edn
# add files and build, change often
COPY resources/ /app/resources
COPY --from=assets /app/resources /app/resources
COPY src/ /app/src
RUN clj -A:uberjar

# use clean base image
FROM openjdk:13-slim-buster

WORKDIR /app

# set the command, with proper container support
CMD ["java","-XX:+UseContainerSupport","-XX:+UnlockExperimentalVMOptions","-cp","/app/app.jar","clojure.main","-m","main"]
# copy the ever changing artifact
COPY --from=builder /app/target/clj-shorty.jar /app/app.jar