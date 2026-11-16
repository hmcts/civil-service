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
  serviceToken=$(IDAM_SERVICE_SECRET="${CCD_API_GATEWAY_S2S_SECRET:-AAAAAAAAAAAAAAAC}" \
    "${dir}/idam-lease-service-token.sh" ccd_gw)
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

# Uploads the definition once. Sets upload_http_code (000 if there was no response), upload_bytes_sent (how much of the
# upload was sent, so 0 if the definition store was never reached) and upload_response_content.
# Any extra arguments are passed to curl.
uploadDefinition() {
  local response
  response=$(curl "${curlCommonOptions[@]}" -w "\n%{http_code} %{size_upload}" --connect-timeout 10 --max-time 300 "$@" -X POST \
    ${definitionStoreUrl}/import \
    -H "Authorization: Bearer ${userToken}" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "file=@${filepath};filename=${uploadFilename}" || echo 'bypass-if-error')
  echo "Definition Upload response is ${response}"
  local status
  status=$(echo "$response" | grep -oE '[0-9]{3} [0-9]+' | tail -n1)
  upload_http_code=${status% *}
  upload_bytes_sent=${status#* }
  upload_http_code=${upload_http_code:-000}
  upload_bytes_sent=${upload_bytes_sent:-0}
  upload_response_content=$(echo "$response" | sed '$d')
}

# Whether the definition has been imported since this script started: the CIVIL version has changed, or the import
# audit lists this upload
importCompleted() {
  newVersion=$(curl "${curlCommonOptions[@]}" -X GET \
    ${definitionStoreUrl}/api/data/case-type/CIVIL/version \
    -H "Authorization: Bearer ${userToken}" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" || echo 'bypass-if-error')
  echo "Current version is ${newVersion}"
  if [[ "${newVersion}" == *'"version"'* && "${newVersion}" != "${version}" ]]; then
    echo "CCD definition version has changed, definition successfully uploaded"
    return 0
  fi

  audit_response=$(curl "${curlCommonOptions[@]}" -X GET \
    ${definitionStoreUrl}/api/import-audits \
    -H "Authorization: Bearer ${userToken}" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" || echo 'bypass-if-error')
  if [[ ${audit_response} == *"${uploadFilename}"* ]]; then
    echo "${filename} (${uploadFilename}) uploaded"
    return 0
  fi
  return 1
}

if [ "${ENVIRONMENT}" == "preview" ] || [ "${ENVIRONMENT}" == "aat" ]; then
  # On a new preview the import can take longer than the connection to the definition store stays open, so the
  # upload can fail on the client while the import carries on and succeeds. Retrying the upload straight away (as
  # curl's --retry did) imported the definition again for each retry. So after a failed upload that reached the
  # definition store, the script first checks whether the import completed, and only uploads again if it did not.
  # An upload that never reached the definition store (e.g. it was not ready) is retried after a short wait.
  # How many times to upload (CCD_IMPORT_UPLOAD_ATTEMPTS), and after a failed upload how many times to check for the
  # import (CCD_IMPORT_CHECK_TRIES) and how many seconds apart (CCD_IMPORT_CHECK_INTERVAL), then how long to wait
  # before uploading again (CCD_IMPORT_RETRY_DELAY)
  maxUploadAttempts=${CCD_IMPORT_UPLOAD_ATTEMPTS:-3}
  for attempt in $(seq 1 "${maxUploadAttempts}")
  do
    uploadDefinition

    if [ "${upload_http_code}" == "201" ]; then
      echo "CCD definition version has changed, definition successfully uploaded"
      exit 0
    fi

    if [ "${upload_bytes_sent}" != "0" ] || [ "${upload_http_code}" != "000" ]; then
      echo "Upload attempt ${attempt} of ${maxUploadAttempts} did not succeed (HTTP ${upload_http_code}) - checking whether the definition was imported anyway"
      for try in $(seq 1 "${CCD_IMPORT_CHECK_TRIES:-36}")
      do
        echo "Checking CCD definition import completed (Try ${try})"
        if importCompleted; then
          exit 0
        fi
        sleep "${CCD_IMPORT_CHECK_INTERVAL:-5}"
      done
    else
      echo "Upload attempt ${attempt} of ${maxUploadAttempts} did not reach the definition store"
    fi

    if [ "${attempt}" -lt "${maxUploadAttempts}" ]; then
      echo "The definition was not imported - uploading it again in ${CCD_IMPORT_RETRY_DELAY:-10}s"
      sleep "${CCD_IMPORT_RETRY_DELAY:-10}"
    fi
  done

  echo "Import audit entry was not found and version has not changed - the definition was not imported successfully"
  exit 1
fi

uploadResponse=$(curl "${curlCommonOptions[@]}" -w "\n%{http_code}" --connect-timeout 10 --max-time 300 --retry 2 --retry-delay 10 --retry-all-errors -X POST \
  ${definitionStoreUrl}/import \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -F "file=@${filepath};filename=${uploadFilename}" || echo 'bypass-if-error')

echo "Definition Upload response is ${uploadResponse}"

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

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
