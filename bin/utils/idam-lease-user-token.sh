#!/usr/bin/env bash

set -e

username=${1}
password=${2}

IDAM_API_URL=${IDAM_API_BASE_URL:-http://localhost:5000}
IDAM_URL=${IDAM_STUB_LOCALHOST:-$IDAM_API_URL}

clientSecret=${CCD_API_GATEWAY_IDAM_CLIENT_SECRET:-ccd_gateway_secret}
redirectUri=${CCD_IDAM_REDIRECT_URL:-http://localhost:3451/oauth2redirect}

if [ -z "$IDAM_STUB_LOCALHOST" ]; then
  curl --insecure --fail --show-error --silent -X POST \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password&username=${username}&password=${password}&client_id=ccd_gateway&client_secret=${clientSecret}&redirect_uri=${redirectUri}&scope=openid%20profile%20roles" \
    "${IDAM_URL}/o/token" | docker run --rm --interactive ghcr.io/jqlang/jq:latest -r .access_token
else
  curl --insecure --fail --show-error --silent -X POST -H "Content-Type: application/x-www-form-urlencoded" --user "ccd_gateway:${clientSecret}" "${IDAM_URL}/oauth2/token?code=stubbed-value&redirect_uri=${redirectUri}&grant_type=authorization_code" -d "" | docker run --rm --interactive ghcr.io/jqlang/jq:latest -r .access_token
fi
