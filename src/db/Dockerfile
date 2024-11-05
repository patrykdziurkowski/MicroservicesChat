FROM mcr.microsoft.com/mssql/server:2022-CU15-GDR1-ubuntu-22.04
EXPOSE 1433
USER root

ENV ACCEPT_EULA=Y
ARG password
RUN test -n "${password}" || (echo "password not set" && false)
ENV MSSQL_SA_PASSWORD=${password}

RUN mkdir -p /usr/src/app/
COPY ./entrypoint.sh /usr/src/app
COPY ./run-initialization.sh /usr/src/app
RUN chmod -R a+x /usr/src/app
CMD /bin/bash /usr/src/app/entrypoint.sh ${MSSQL_SA_PASSWORD}

HEALTHCHECK --interval=5s --timeout=1s --start-period=10s --retries=10 \
    CMD /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P ${MSSQL_SA_PASSWORD} -Q "SELECT 1" -b -No
