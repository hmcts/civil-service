#!/usr/bin/env bash

set -eu

delete() {
  curl -X DELETE http://localhost:9404/engine-rest/${1}/{$2}
}

cleanup() {
  curl -X GET http://localhost:9404/engine-rest/${1} | jq '.[] | .id' | xargs -n 1 -P 10 -I {} bash -c 'delete '"$1"' "$@"' _ {}
}

export -f delete
export -f cleanup

cleanup process-instance
cleanup deployment

