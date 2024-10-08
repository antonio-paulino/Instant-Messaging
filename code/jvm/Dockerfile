FROM postgres

USER postgres

WORKDIR /app

COPY im-database/src/schema.sql /docker-entrypoint-initdb.d/1_create.sql
COPY im-database/src/triggers.sql /docker-entrypoint-initdb.d/2_triggers.sql

COPY --chown=postgres:postgres ./scripts/wait-for-postgres.sh ./bin/wait-for-postgres.sh
RUN chmod +x ./bin/wait-for-postgres.sh

EXPOSE 5432