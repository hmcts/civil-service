#!/usr/bin/env bash

set -euo pipefail

readonly OATHTOOL_IMAGE_DEFAULT='hmctspublic.azurecr.io/imported/toolbelt/oathtool@sha256:ee73b804168ffaf4e00a1bf03240aa9f508ddabdd998587c0e114a336e2529ca'

fail() {
  echo "ERROR: $1" >&2
  exit 1
}

generate_one_time_password() {
  local error_file error_output output status image
  image="${OATHTOOL_IMAGE_DEFAULT}"
  error_file=$(mktemp "${TMPDIR:-/tmp}/idam-oathtool.XXXXXX")

  set +e
  output=$(docker run --rm "${image}" --totp -b "${IDAM_SERVICE_SECRET}" 2>"${error_file}")
  status=$?
  set -e
  error_output=$(<"${error_file}")
  rm -f "${error_file}"

  if [ "${status}" -ne 0 ]; then
    if grep -Eqi 'unauthorized|authentication required|denied|forbidden' <<<"${error_output}"; then
      fail "OTP tool registry access denied for ${image}. Verify the Jenkins agent can pull from hmctspublic."
    fi
    if grep -Eqi 'manifest unknown|not found|no such manifest' <<<"${error_output}"; then
      fail "OTP tool image not found: ${image}. Verify the approved image digest."
    fi
    fail "OTP tool exited non-zero (status ${status}) for ${image}."
  fi

  [ -n "${output}" ] || fail 'OTP tool returned empty output.'
  [[ "${output}" =~ ^[0-9]{6}$ ]] || fail 'OTP tool returned malformed output.'
  printf '%s' "${output}"
}

[ "$#" -ge 1 ] || fail 'Microservice name is required.'
microservice=$1
[ -n "${microservice}" ] || fail 'Microservice name is required.'

if [ "$#" -ge 2 ]; then
  oneTimePassword=$2
  [ -n "${oneTimePassword}" ] || fail 'One-time password is empty.'
  [[ "${oneTimePassword}" =~ ^[0-9]{6}$ ]] || fail 'One-time password is malformed.'
else
  [ -n "${IDAM_SERVICE_SECRET:-}" ] || fail 'IDAM_SERVICE_SECRET is required to generate a one-time password.'
  oneTimePassword=$(generate_one_time_password)
fi

if [ "${IDAM_VALIDATE_OTP_TOOL_ONLY:-false}" = 'true' ]; then
  echo 'OTP tool validation succeeded.'
  exit 0
fi

curl --insecure --fail --show-error --silent -X POST \
  "${SERVICE_AUTH_PROVIDER_API_BASE_URL:-http://localhost:4502}/lease" \
  -H "Content-Type: application/json" \
  -d "{\"microservice\":\"${microservice}\",\"oneTimePassword\":\"${oneTimePassword}\"}"
