FROM amazoncorretto:21

RUN yum -y install shadow-utils

EXPOSE 8080

RUN groupadd -r -g 1007 templateuser
RUN useradd -r -u 1007 -g templateuser templateuser

RUN mkdir /app

ADD build/distributions/ktor-template-0.0.1.tar /app/

RUN chown -R templateuser:templateuser /app

USER lk20user

ENTRYPOINT ["/app/ktor-template-0.0.1/bin/ktor-template"]
