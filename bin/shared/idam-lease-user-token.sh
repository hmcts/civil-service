#!/usr/bin/env bash

set -e

username=${1}
password=${2}

IDAM_URL=${IDAM_API_BASE_URL:-http://localhost:5000}
clientSecret=${CCD_API_GATEWAY_IDAM_CLIENT_SECRET:-ccd_gateway_secret}
redirectUri=${CCD_IDAM_REDIRECT_URL:-http://localhost:3451/oauth2redirect}

curl --insecure --fail --show-error --silent -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "grant_type=password" \
  --data-urlencode "username=${username}" \
  --data-urlencode "password=${password}" \
  --data-urlencode "client_id=ccd_gateway" \
  --data-urlencode "client_secret=${clientSecret}" \
  --data-urlencode "redirect_uri=${redirectUri}" \
  --data-urlencode "scope=openid profile roles" \
  "${IDAM_URL}/o/token" | docker run --rm --interactive ghcr.io/jqlang/jq:latest -r .access_token
