#!/bin/bash

# Launches a Docker container containing a Mongo database.
# Requires Docker daemon (such as Docker Destop) to be running first.

# Environment variables:
#  - MONGO_PASSWORD (required)
#  - MONGO_VOLUME (required)
#  - MONGO_PORT (optional, default 27017)

MONGO_PORT=${MONGO_PORT:-27017}

docker run --rm --name rank_mongodb \
    -e MONGO_INITDB_ROOT_USERNAME=rank-mongo-user \
    -e MONGO_INITDB_ROOT_PASSWORD=$MONGO_PASSWORD \
    -p $MONGO_PORT:$MONGO_PORT \
    -v $MONGO_VOLUME:/data/db \
    -d mongo
