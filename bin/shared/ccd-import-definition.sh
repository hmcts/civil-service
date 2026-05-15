#!/usr/bin/env bash

#export IDAM_STUB_LOCALHOST=http://localhost:5555

set -eu

dir=$(dirname ${0})
filepath=${1}
filename=$(basename ${filepath})
uploadFilename="$(date +"%Y%m%d-%H%M%S")-${filename}"

# Support both naming conventions (CCD_CONFIGURER_IMPORTER_* is canonical)
IMPORTER_USERNAME=${CCD_CONFIGURER_IMPORTER_USERNAME:-${DEFINITION_IMPORTER_USERNAME}}
IMPORTER_PASSWORD=${CCD_CONFIGURER_IMPORTER_PASSWORD:-${DEFINITION_IMPORTER_PASSWORD}}

if [ -z "${USER_TOKEN:-}" ]; then
  userToken=$(${dir}/idam-lease-user-token.sh ${IMPORTER_USERNAME} ${IMPORTER_PASSWORD})
else
  userToken=${USER_TOKEN}
fi

if [ -z "${SERVICE_TOKEN:-}" ]; then
  serviceToken=$(${dir}/idam-lease-service-token.sh ccd_gw $(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool --totp -b ${CCD_API_GATEWAY_S2S_SECRET:-AAAAAAAAAAAAAAAC}))
else
  serviceToken=${SERVICE_TOKEN}
fi

version="n/a"
newVersion="n/a"
curlCommonOptions=(--insecure --silent --show-error --http1.1)
definitionStoreUrl=${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}

if [ "${ENVIRONMENT}" == "preview" ] || [ "${ENVIRONMENT}" == "aat" ]; then
  version=$(curl "${curlCommonOptions[@]}" -X GET \
    ${definitionStoreUrl}/api/data/case-type/CIVIL/version \
    -H "Authorization: Bearer ${userToken}" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" || echo 'bypass-if-error')

  echo "Current version is ${version}"
fi

uploadResponse=$(curl "${curlCommonOptions[@]}" -w "\n%{http_code}" --connect-timeout 10 --max-time 300 --retry 2 --retry-delay 10 --retry-all-errors -X POST \
  ${definitionStoreUrl}/import \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -F "file=@${filepath};filename=${uploadFilename}" || echo 'bypass-if-error')

echo "Definition Upload response is ${uploadResponse}"

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [ "${ENVIRONMENT}" == "preview" ] || [ "${ENVIRONMENT}" == "aat" ]; then
 if [ "${upload_http_code}" != "201" ]; then
  echo "Bypassing audit check as on preview - will verify the import completed"

  for try in {1..20}
  do
    sleep 15
    echo "Checking CCD definition import completed (Try ${try})"

    newVersion=$(curl "${curlCommonOptions[@]}" -X GET \
      ${definitionStoreUrl}/api/data/case-type/CIVIL/version \
      -H "Authorization: Bearer ${userToken}" \
      -H "ServiceAuthorization: Bearer ${serviceToken}" || echo 'bypass-if-error')

    echo "Current version is ${newVersion}"
    if [[ "${newVersion}" == *'"version"'* && "${newVersion}" != "${version}" ]]; then
      echo "CCD definition version has changed, definition successfully uploaded"
      exit 0
    fi

    audit_response=$(curl "${curlCommonOptions[@]}" -X GET \
      ${definitionStoreUrl}/api/import-audits \
      -H "Authorization: Bearer ${userToken}" \
      -H "ServiceAuthorization: Bearer ${serviceToken}" || echo 'bypass-if-error')

    if [[ ${audit_response} == *"${uploadFilename}"* ]]; then
      echo "${filename} (${uploadFilename}) uploaded"
      exit 0
    fi
  done

  echo "Import audit entry was not found and version has not changed - the definition was not imported successfully"
  exit 1
 fi
  echo "CCD definition version has changed, definition successfully uploaded"
  exit 0
fi

if [[ "${upload_http_code}" == '504' ]]; then
  for try in {1..10}
  do
    sleep 5
    echo "Checking status of ${filename} (${uploadFilename}) upload (Try ${try})"
    audit_response=$(curl "${curlCommonOptions[@]}" -X GET \
      ${definitionStoreUrl}/api/import-audits \
      -H "Authorization: Bearer ${userToken}" \
      -H "ServiceAuthorization: Bearer ${serviceToken}")

    if [[ ${audit_response} == *"${uploadFilename}"* ]]; then
      echo "${filename} (${uploadFilename}) uploaded"
      exit 0
    fi
  done
else
  if [[ "${upload_response_content}" == 'Case Definition data successfully imported' ]]; then
    echo "${filename} (${uploadFilename}) uploaded"
    exit 0
  fi
fi

echo "${filename} (${uploadFilename}) upload failed (${upload_response_content})"
exit 1;
