# Dropwizard  org.belgif.be 
FROM amd64/openjdk:11-jre-slim
MAINTAINER Bart Hanssens <bart.hanssens@bosa.fgov.be>

RUN adduser --system cbe 
COPY target/cbe-proxy-1.4-runner.jar /usr/local/bin/cbe-proxy.jar

USER cbe 
WORKDIR /home/cbe

CMD java -server  -Dhttp.proxyHost=dc-proxy.names.belgium.be -Dhttp.proxyPort=3128 -Dhttps.proxyHost=dc-proxy.names.belgium.be -Dhttps.proxyPort=3128 -jar /usr/local/bin/cbe-proxy.jar

EXPOSE 8080
 
