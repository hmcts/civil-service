#!/usr/bin/env bash

set -euo pipefail
workspace=${1}
env=${2}

s2sSecret=${S2S_SECRET:-AABBCCDDEEFFGGHH}

if [[ "${env}" == 'prod' ]]; then
  s2sSecret=${S2S_SECRET_PROD}
fi

IDAM_SERVICE_SECRET="${s2sSecret}" \
  "$(realpath "$workspace")/bin/shared/import-camunda-resources.sh" \
  "$(realpath "$workspace")/src/main/resources/camunda" '*.bpmn' 'Civil BPMN' \
  'civil' '' 'true' 'false'
