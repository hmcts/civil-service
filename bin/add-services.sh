#!/usr/bin/env bash

IFS=','; read -ra FILES <<< "${ADDITIONAL_COMPOSE_FILES}"
for FILE in "${FILES[@]}"; do
  # process "$i"
  echo "$FILE"
  docker compose -f "${BASE_COMPOSE_FILES_PATH}$FILE" up -d
done
