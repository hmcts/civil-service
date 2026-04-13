#!/usr/bin/env bash

set -eu
workspace=${1}
tenant_id=${2}
product=${3}
camundaBaseUrl=${CAMUNDA_BASE_URL:-http://localhost:9404}
dmnFilepath="$(realpath $workspace)/resources"

s2sSecret=${S2S_SECRET:-AABBCCDDEEFFGGHH}

echo "Importing Civil WA DMN definitions"
echo "Workspace: $(realpath $workspace)"
echo "Camunda base URL: ${camundaBaseUrl}"
echo "Tenant ID: ${tenant_id}"
echo "Deployment source: ${product}"
echo "S2S_SECRET present: $([[ -n "${S2S_SECRET:-}" ]] && echo "yes" || echo "no")"
echo "DMN file count: $(find "${dmnFilepath}" -name '*.dmn' | wc -l | tr -d ' ')"

#if [[ "${env}" == 'prod' ]]; then
#  s2sSecret=${S2S_SECRET_PROD}-
#fi

serviceToken=$($(realpath ".")/bin/utils/idam-lease-service-token.sh civil_service \
  $(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool --totp -b ${s2sSecret}))
echo "Service token generated: $([[ -n "${serviceToken}" ]] && echo "yes" || echo "no")"

for file in $(find ${dmnFilepath} -name '*.dmn')
do
  echo "Uploading DMN diagram: $(basename ${file})"
  uploadResponse=$(curl --insecure -v --silent -w "\n%{http_code}" --show-error -X POST \
    ${camundaBaseUrl}/engine-rest/deployment/create \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=$(basename ${file})" \
    -F "deploy-changed-only=true" \
    -F "deployment-source=$product" \
    ${tenant_id:+'-F' "tenant-id=$tenant_id"} \
    -F "file=@${dmnFilepath}/$(basename ${file})")

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [[ "${upload_http_code}" == '200' ]]; then
  echo "$(basename ${file}) diagram uploaded successfully (${upload_response_content})"
  continue;
fi

echo "$(basename ${file}) upload failed with http code ${upload_http_code} and response (${upload_response_content})"
continue;

done

