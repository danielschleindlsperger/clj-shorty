# Build FE code
FROM node:14-buster-slim as assets

WORKDIR /app

COPY package*.json /app/
COPY tailwind.config.js /app/
COPY resources /app/resources/

RUN npm ci

# src is required to prune the tailwind css classnames
COPY src /app/src

RUN npm run css:prod

##
## Build Clojure code
##

FROM clojure:openjdk-13-tools-deps-slim-buster as builder

WORKDIR /app

# add deps, change sometimes
COPY deps.edn /app/deps.edn

# add sources files, change often
COPY src/ /app/src

# config and other static resources
COPY resources /app/resources
# built assets
COPY --from=assets /app/resources /app/resources

RUN clj -A:uberjar

##
## Clean base image for distribution
##

FROM openjdk:13-slim-buster

WORKDIR /app

# copy java artifact, changes every time
COPY --from=builder /app/target/clj-shorty.jar /app/app.jar

# set the command, with proper container support
CMD ["java","-XX:+UseContainerSupport","-XX:+UnlockExperimentalVMOptions","-cp","/app/app.jar","clojure.main","-m","main"]