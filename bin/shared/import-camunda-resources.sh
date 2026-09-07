#!/usr/bin/env bash

set -euo pipefail

fail() {
  echo "ERROR: $1" >&2
  exit 1
}

[ "$#" -ge 3 ] || fail 'Usage: import-camunda-resources.sh <directory> <pattern> <resource-label> [tenant-id] [deployment-source] [deploy-changed-only] [timestamp-name].'

readonly resource_dir=$1
readonly pattern=$2
readonly resource_label=$3
readonly tenant_id=${4:-}
readonly deployment_source=${5:-}
readonly deploy_changed_only=${6:-false}
readonly timestamp_name=${7:-false}
readonly script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

[ -d "${resource_dir}" ] || fail "${resource_label} resource directory not found: ${resource_dir}"
[ -n "${IDAM_SERVICE_SECRET:-}" ] || fail 'IDAM_SERVICE_SECRET is required.'

service_token=$("${script_dir}/idam-lease-service-token.sh" civil_service)
[ -n "${service_token}" ] || fail 'Service-token generation returned empty output.'

resource_files=()
while IFS= read -r -d '' resource_file; do
  resource_files+=("${resource_file}")
done < <(find "${resource_dir}" -type f -name "${pattern}" -print0 | sort -z)
[ "${#resource_files[@]}" -gt 0 ] || fail "No ${resource_label} resources matching ${pattern} found in ${resource_dir}."

failed=0
for resource_file in "${resource_files[@]}"; do
  filename=$(basename "${resource_file}")
  deployment_name="${filename}"
  if [ "${timestamp_name}" = 'true' ]; then
    deployment_name="$(date +'%Y%m%d-%H%M%S')-${filename}"
  fi

  curl_args=(
    --insecure --silent --show-error
    -w $'\n%{http_code}'
    -X POST "${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create"
    -H 'Accept: application/json'
    -H "ServiceAuthorization: Bearer ${service_token}"
    -F "deployment-name=${deployment_name}"
  )
  [ "${deploy_changed_only}" = 'true' ] && curl_args+=(-F 'deploy-changed-only=true')
  [ -n "${deployment_source}" ] && curl_args+=(-F "deployment-source=${deployment_source}")
  [ -n "${tenant_id}" ] && curl_args+=(-F "tenant-id=${tenant_id}")
  curl_args+=(-F "file=@${resource_file}")

  set +e
  upload_response=$(curl "${curl_args[@]}")
  curl_status=$?
  set -e

  if [ "${curl_status}" -ne 0 ]; then
    echo "ERROR: ${filename} upload request failed (curl status ${curl_status})." >&2
    failed=$((failed + 1))
    continue
  fi

  upload_http_code=$(tail -n1 <<<"${upload_response}")
  upload_response_content=$(sed '$d' <<<"${upload_response}")
  if [ "${upload_http_code}" = '200' ]; then
    echo "${filename} uploaded successfully."
    continue
  fi

  echo "ERROR: ${filename} upload failed with HTTP ${upload_http_code}: ${upload_response_content}" >&2
  failed=$((failed + 1))
done

[ "${failed}" -eq 0 ] || fail "${failed} ${resource_label} resource(s) failed to upload."
