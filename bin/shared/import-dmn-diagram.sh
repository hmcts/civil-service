#!/usr/bin/env bash

set -euo pipefail

readonly workspace=${1:?Workspace is required.}
readonly tenant_id=${2:-}
readonly product=${3:-}
readonly script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
readonly resolved_workspace=$(realpath "${workspace}")

IDAM_SERVICE_SECRET="${S2S_SECRET:-AABBCCDDEEFFGGHH}" \
  "${script_dir}/import-camunda-resources.sh" \
  "${resolved_workspace}/resources" '*.dmn' 'DMN' "${tenant_id}" "${product}" 'true' 'false'

if [ -d "${resolved_workspace}/camunda" ]; then
  IDAM_SERVICE_SECRET="${S2S_SECRET:-AABBCCDDEEFFGGHH}" \
    "${script_dir}/import-camunda-resources.sh" \
    "${resolved_workspace}/camunda" '*.bpmn' 'embedded BPMN' '' "${product}" 'true' 'false'
fi
