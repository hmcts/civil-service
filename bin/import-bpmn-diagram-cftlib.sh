#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))
basePath=$(dirname $(dirname $scriptPath))
camundaDiagramsPath=$basePath/civil-camunda-bpmn-definition/src/main/resources/camunda

cd $basePath
if [[ ! -d civil-service || ! -d civil-ccd-definition || ! -d civil-camunda-bpmn-definition ]]; then
  echo "Error: make sure all civil repos are in the same directory"
  exit 1
fi

cd $scriptPath
serviceToken=$(utils/idam-lease-service-token-cftlib.sh civil_service)

cd $camundaDiagramsPath
for filePath in $(find $PWD -name '*.bpmn')
do
  filename=$(basename $filePath)
  uploadResponse=$(curl --insecure --silent -w "\n%{http_code}" --show-error -X POST \
    ${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=${filename}" \
    -F "deploy-changed-only=true" \
    -F "tenant-id=civil" \
    -F "file=@${filePath}")

  uploadHttpCode=$(echo "$uploadResponse" | tail -n1)
  echo "${filename} upload, http code: ${uploadHttpCode}"

  if [[ ${uploadHttpCode} != '200' ]]; then
    echo $uploadResponse
    continue;
  fi
done
exit 0;
