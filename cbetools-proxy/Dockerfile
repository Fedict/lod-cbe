FROM registry.access.redhat.com/ubi8/ubi-minimal:8.4 AS temp

RUN curl -L -o /usr/local/bin/dumb-init https://github.com/Yelp/dumb-init/releases/download/v1.2.5/dumb-init_1.2.5_x86_64 && \
  chmod +x /usr/local/bin/dumb-init

FROM registry.access.redhat.com/ubi8/openjdk-17-runtime:1.14

LABEL org.opencontainers.image.authors="Bart Hanssens <bart.hanssens@bosa.fgov.be>" \
  io.k8s.display-name="CBE LOD Proxy" \
  org.opencontainers.image.title="CBE LOD Proxy" \
  io.k8s.description="Linked open data proxy for Crossroadbank Enterprises, based on RedHat UBI 8 with OpenJDK JRE 17." \
  org.opencontainers.image.description="Linked open data proxy for Crossroadbank Enterprises, based on RedHat UBI 8 with OpenJDK JRE 17" \
  org.opencontainers.image.documentation="https://github.com/Fedict/lod-cbe/tree/master/cbetools-proxy#readme" \
  org.opencontainers.image.licenses="Copyright (C) 2022 FPS BOSA." \
  org.opencontainers.image.source="https://github.com/Fedict/lod-cbe/tree/master/cbetools-proxy" \
  io.openshift.tags="bosa, lod, CBE" \
  io.openshift.min-cpu="1" \
  io.openshift.min-memory="400Mi" \
  io.openshift.non-scalable="true" \
  io.openshift.wants=""

USER root

RUN echo 'nonroot:x:1949000151:0:non-root user:/home/nonroot:/sbin/nologin' >> /etc/passwd && \
  mkdir /home/nonroot && \
  chgrp -R 0 /home/nonroot && \
  chmod -R g=u /home/nonroot

COPY --from=temp /usr/local/bin/dumb-init /usr/local/bin/dumb-init
COPY cbetools-proxy-*.jar /home/nonroot/cbetools-proxy.jar

WORKDIR /home/nonroot

USER nonroot

WORKDIR /home/nonroot

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["java", "-jar", "/home/nonroot/cbetools-proxy.jar" ]

EXPOSE 8080
 
