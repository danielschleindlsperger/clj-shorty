FROM debian:stable

RUN apt-get update -y
RUN apt-get -y install sshpass

RUN apt-get -y install sqitch libdbd-pg-perl postgresql-client

COPY entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]