#!/bin/bash

# Launches a Docker container containing a Postgresql database.
# Requires Docker daemon (such as Docker Destop) to be running first.
# Non-default postgres port is used in case a system level Postgres instance is already launched.

# Environment variables:
#  - POSTGRES_PASSWORD (required)
#  - POSTGRES_VOLUME (required)
#  - POSTGRES_PORT (optional, default 5435)

POSTGRES_PORT=${POSTGRES_PORT:-5435}

docker run --rm --name rank-psql-db \
    -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
    -e POSTGRES_DB=$POSTGRES_DB \
    -e POSTGRES_USER=rank-psql-user \
    -d -p $POSTGRES_PORT:$POSTGRES_PORT \
    -v $POSTGRES_VOLUME:/var/lib/postgresql/data \
    postgres
