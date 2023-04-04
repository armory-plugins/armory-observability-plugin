FROM busybox:latest

RUN mkdir -p /opt/plugin

ADD build/distributions/armory-observability-plugin-* /opt/plugin/
ADD build-tools/install.sh /usr/local/bin/install.sh

RUN adduser -D spinnaker && addgroup spinnaker spinnaker

ENTRYPOINT ["sh", "/usr/local/bin/install.sh"]