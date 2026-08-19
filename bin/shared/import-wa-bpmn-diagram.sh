#!/usr/bin/env bash

set -euo pipefail

readonly workspace=${1:?Workspace is required.}
readonly script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

IDAM_SERVICE_SECRET="${S2S_SECRET:-AABBCCDDEEFFGGHH}" \
  "${script_dir}/import-camunda-resources.sh" \
  "$(realpath "${workspace}")/resources" '*.bpmn' 'WA BPMN' '' '' 'false' 'true'
