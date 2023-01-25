FROM eclipse-temurin:8

ENV JMETER_VERSION=5.5
ENV JMETER_HOME=/opt/jmeter
ENV JMETER_TGZ_PATH=/tmp/jmeter.tgz

WORKDIR ${JMETER_HOME}

RUN curl -L -s -o ${JMETER_TGZ_PATH} https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz \
  && tar -xzf ${JMETER_TGZ_PATH} \
  && mv apache-jmeter-${JMETER_VERSION}/* . \
  && rm -r apache-jmeter-${JMETER_VERSION} \
  && rm ${JMETER_TGZ_PATH}

ENV PATH $PATH:$JMETER_HOME/bin
