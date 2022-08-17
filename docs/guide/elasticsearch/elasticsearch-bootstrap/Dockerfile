FROM ubuntu:22.04

RUN apt-get update \
	&& apt-get install -y curl

RUN curl -O https://raw.githubusercontent.com/toschneck/wait-for-it/master/wait-for-it.sh \
  && chmod +x wait-for-it.sh

COPY index-template.json .

CMD ["sh", "-c", "/wait-for-it.sh -t 60 elasticsearch:9200 && curl -X PUT 'elasticsearch:9200/_template/jmeter?pretty' -H 'Content-Type: application/json' --data-binary '@/index-template.json' && curl -X PUT elasticsearch:9200/jmeter"]
