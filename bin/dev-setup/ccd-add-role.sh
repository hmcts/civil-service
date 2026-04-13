#!/usr/bin/env bash

set -ex

dir=$(dirname "${0}")
utils_dir="${dir}/../utils"

role=${1}

if [ -z "${USER_TOKEN:-}" ]; then
  userToken=$(${utils_dir}/idam-lease-user-token.sh ${CCD_CONFIGURER_IMPORTER_USERNAME} ${CCD_CONFIGURER_IMPORTER_PASSWORD})
else
  userToken=${USER_TOKEN}
fi

if [ -z "${SERVICE_TOKEN:-}" ]; then
  serviceToken=$(${utils_dir}/idam-lease-service-token.sh ccd_gw $(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool --totp -b ${CCD_API_GATEWAY_S2S_SECRET:-AAAAAAAAAAAAAAAC}))
else
  serviceToken=${SERVICE_TOKEN}
fi

echo "Creating CCD role: ${role} using ${CCD_DEFINITION_STORE_API_BASE_URL}"

curl --insecure --fail --show-error --silent -X PUT \
  ${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}/api/user-role \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "'${role}'",
    "security_classification": "PUBLIC"
  }'
