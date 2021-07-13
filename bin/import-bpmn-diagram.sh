#!/usr/bin/env bash

set -eu
workspace=${1}

serviceToken=$($(realpath $workspace)/bin/utils/idam-lease-service-token.sh civil_service \
  $(docker run --rm toolbelt/oathtool --totp -b ${S2S_SECRET:-AABBCCDDEEFFGGHH}))
filepath="$(realpath $workspace)/camunda"

for file in $(find ${filepath} -name '*.bpmn')
do
  uploadResponse=$(curl --insecure -v --silent -w "\n%{http_code}" --show-error -X POST \
    ${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=$(date +"%Y%m%d-%H%M%S")-$(basename ${file})" \
    -F "file=@${filepath}/$(basename ${file})")

  upload_http_code=$(echo "$uploadResponse" | tail -n1)
  echo "${file} upload, http code: ${upload_http_code}"

  if [[ ${upload_http_code} != '200' ]]; then
    echo $uploadResponse
    continue;
  fi

done
exit 0;
