# FROM caprover/cli-caprover:v2.1.1
FROM node:12-alpine

RUN npm install -g caprover

COPY entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]