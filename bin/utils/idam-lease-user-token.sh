#!/usr/bin/env bash

set -e

username=${1}
password=${2}

IDAM_API_URL=${IDAM_API_BASE_URL:-http://localhost:5000}
IDAM_URL=${IDAM_STUB_LOCALHOST:-$IDAM_API_URL}

clientSecret=${CCD_API_GATEWAY_IDAM_CLIENT_SECRET:-ccd_gateway_secret}
redirectUri=${CCD_IDAM_REDIRECT_URL:-https://ccd-case-management-web-aat.service.core-compute-aat.internal/oauth2redirect}
echo "redirectUri: $redirectUri"
if [ -z "$IDAM_STUB_LOCALHOST" ]; then
  code=$(curl --insecure --fail --show-error --silent -X POST --user "${username}:${password}" "${IDAM_URL}/oauth2/authorize?redirect_uri=${redirectUri}&response_type=code&client_id=ccd_gateway" -d "" | docker run --rm --interactive ghcr.io/jqlang/jq:latest -r .code)
else
  code=stubbed-value
fi

curl --insecure --fail --show-error --silent -X POST -H "Content-Type: application/x-www-form-urlencoded" --user "ccd_gateway:${clientSecret}" "${IDAM_URL}/oauth2/token?code=${code}&redirect_uri=${redirectUri}&grant_type=authorization_code" -d "" | docker run --rm --interactive ghcr.io/jqlang/jq:latest -r .access_token
