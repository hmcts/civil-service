#!/usr/bin/env bash

#export IDAM_STUB_LOCALHOST=http://localhost:5555

set -eu

dir=$(dirname ${0})
filepath=${1}
filename=$(basename ${filepath})
uploadFilename="$(date +"%Y%m%d-%H%M%S")-${filename}"

userToken=$(${dir}/idam-lease-user-token.sh ${CCD_CONFIGURER_IMPORTER_USERNAME:-ccd.docker.default@hmcts.net} ${CCD_CONFIGURER_IMPORTER_PASSWORD:-Password12!})
serviceToken=$(${dir}/idam-lease-service-token.sh ccd_gw $(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool --totp -b ${CCD_API_GATEWAY_S2S_SECRET:-AAAAAAAAAAAAAAAC}))

version="n/a"
newVersion="n/a"

if [ "${ENVIRONMENT}" == "preview" ] || [ "${ENVIRONMENT}" == "aat" ]; then
  version=$(curl --insecure --silent --show-error -X GET \
    ${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}/api/data/case-type/CIVIL/version \
    -H "Authorization: Bearer ${userToken}" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" || echo 'bypass-if-error')

  echo "Current version is ${version}"
fi

uploadResponse=$(curl --insecure --silent -w "\n%{http_code}"  --show-error --max-time 60  -X POST \
  ${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}/import \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -F "file=@${filepath};filename=${uploadFilename}" || echo 'bypass-if-error')

echo "Definition Upload response is ${uploadResponse}"

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [ "${ENVIRONMENT}" == "preview" ] || [ "${ENVIRONMENT}" == "aat" ]; then
 if [ "${upload_http_code}" != "201" ]; then
  echo "Bypassing audit check as on preview - will wait 45s and then verify the version has changed"
  sleep 45

  newVersion=$(curl --insecure --silent --show-error -X GET \
    ${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}/api/data/case-type/CIVIL/version \
    -H "Authorization: Bearer ${userToken}" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" || echo 'bypass-if-error')

    echo "Current version is ${newVersion}"
    if [[ "$newVersion" == "$version" ]]; then
      echo "Version has not changed - the definition was not imported successfully"
      exit 1
    fi
 fi
  echo "CCD definition version has changed, definition successfully uploaded"
  exit 0
fi

if [[ "${upload_http_code}" == '504' ]]; then
  for try in {1..10}
  do
    sleep 5
    echo "Checking status of ${filename} (${uploadFilename}) upload (Try ${try})"
    audit_response=$(curl --insecure --silent --show-error -X GET \
      ${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}/api/import-audits \
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
