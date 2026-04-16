#!/usr/bin/env bash

set -eu
workspace=${1}
camundaBaseUrl=${CAMUNDA_BASE_URL:-http://localhost:9404}
filepath="$(realpath $workspace)/camunda"

if [[ -n "${BEFTA_S2S_CLIENT_ID:-}" && -n "${BEFTA_S2S_CLIENT_SECRET:-}" ]]; then
  s2sClientId=${BEFTA_S2S_CLIENT_ID}
  s2sSecret=${BEFTA_S2S_CLIENT_SECRET}
elif [[ -n "${CCD_API_GATEWAY_S2S_ID:-}" && -n "${CCD_API_GATEWAY_S2S_KEY:-}" ]]; then
  s2sClientId=${CCD_API_GATEWAY_S2S_ID}
  s2sSecret=${CCD_API_GATEWAY_S2S_KEY}
else
  s2sClientId=civil_service
  s2sSecret=${S2S_SECRET:-AABBCCDDEEFFGGHH}
fi

echo "Importing Civil Camunda BPMN definitions"
echo "Workspace: $(realpath $workspace)"
echo "Camunda base URL: ${camundaBaseUrl}"
echo "S2S client ID: ${s2sClientId}"
echo "BEFTA_S2S_CLIENT_SECRET present: $([[ -n "${BEFTA_S2S_CLIENT_SECRET:-}" ]] && echo "yes" || echo "no")"
echo "CCD_API_GATEWAY_S2S_KEY present: $([[ -n "${CCD_API_GATEWAY_S2S_KEY:-}" ]] && echo "yes" || echo "no")"
echo "S2S_SECRET present: $([[ -n "${S2S_SECRET:-}" ]] && echo "yes" || echo "no")"
echo "BPMN file count: $(find "${filepath}" -name '*.bpmn' | wc -l | tr -d ' ')"

serviceToken=$($(realpath $workspace)/bin/shared/idam-lease-service-token.sh "${s2sClientId}" \
  $(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool --totp -b ${s2sSecret}))
echo "Service token generated: $([[ -n "${serviceToken}" ]] && echo "yes" || echo "no")"

for file in $(find ${filepath} -name '*.bpmn')
do
  echo "Uploading BPMN diagram: $(basename ${file})"
  uploadResponse=$(curl --insecure --silent -w "\n%{http_code}" --show-error -X POST \
    ${camundaBaseUrl}/engine-rest/deployment/create \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=$(date +"%Y%m%d-%H%M%S")-$(basename ${file})" \
    -F "tenant-id=civil" \
    -F "file=@${filepath}/$(basename ${file})")

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [[ "${upload_http_code}" == '200' ]]; then
  echo "$(basename ${file}) diagram uploaded successfully (${upload_response_content})"
  continue;
fi

echo "$(basename ${file}) upload failed with http code ${upload_http_code} and response (${upload_response_content})"
continue;

done
exit 0;
