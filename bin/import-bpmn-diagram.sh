#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
filepath="$(realpath ".")/src/main/resources"
filename=${1}

uploadResponse=$(curl --insecure --silent -w "\n%{http_code}" --show-error -X POST \
  ${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create \
  -H "Accept: application/json" \
  -F "deployment-name=$(date +"%Y%m%d-%H%M%S")-${filename}" \
  -F "file=@${filepath}/${filename}")

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [[ "${upload_http_code}" == '200' ]]; then
  echo "${filename} diagram uploaded successfully (${upload_response_content})"
  exit 0
fi

echo "${filename} upload failed with http code ${upload_http_code} and response (${upload_response_content})"
exit 1;
