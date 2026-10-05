#!/usr/bin/env bash

set -euo pipefail

readonly script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
readonly subject="${script_dir}/import-camunda-resources.sh"
readonly test_dir=$(mktemp -d "${TMPDIR:-/tmp}/camunda-import-test.XXXXXX")
trap 'rm -rf "${test_dir}"' EXIT

mkdir -p "${test_dir}/bin" "${test_dir}/resources"
touch "${test_dir}/resources/one.bpmn" "${test_dir}/resources/two.bpmn"

cat >"${test_dir}/bin/docker" <<'EOF'
#!/usr/bin/env bash
printf '123456'
EOF

cat >"${test_dir}/bin/curl" <<'EOF'
#!/usr/bin/env bash
if [[ "$*" == *'/lease'* ]]; then
  printf 'service-token'
  exit 0
fi
printf 'UPLOAD\n%s\n' "$*" >>"${CAMUNDA_IMPORT_TEST_LOG}"
printf '{"deployment":"ok"}\n200'
EOF
chmod +x "${test_dir}/bin/docker" "${test_dir}/bin/curl"

export CAMUNDA_IMPORT_TEST_LOG="${test_dir}/curl.log"
PATH="${test_dir}/bin:$PATH" IDAM_SERVICE_SECRET='not-a-real-secret' \
  "${subject}" "${test_dir}/resources" '*.bpmn' 'test BPMN' 'civil' 'civil-source' 'true' 'false'

[ "$(grep -c '^UPLOAD$' "${CAMUNDA_IMPORT_TEST_LOG}")" = '2' ] || { echo 'Expected two uploads.' >&2; exit 1; }
grep -q 'tenant-id=civil' "${CAMUNDA_IMPORT_TEST_LOG}" || { echo 'Tenant ID was not forwarded.' >&2; exit 1; }
grep -q 'deployment-source=civil-source' "${CAMUNDA_IMPORT_TEST_LOG}" || { echo 'Deployment source was not forwarded.' >&2; exit 1; }
grep -q 'deploy-changed-only=true' "${CAMUNDA_IMPORT_TEST_LOG}" || { echo 'Deploy-changed-only was not forwarded.' >&2; exit 1; }
! grep -q 'not-a-real-secret\|123456' "${CAMUNDA_IMPORT_TEST_LOG}" || { echo 'Secret or OTP material was logged.' >&2; exit 1; }
echo 'PASS: imports all matching resources with supported options'

set +e
output=$(PATH="${test_dir}/bin:$PATH" IDAM_SERVICE_SECRET='not-a-real-secret' \
  "${subject}" "${test_dir}/resources" '*.dmn' 'test DMN' 2>&1)
status=$?
set -e
[ "${status}" -ne 0 ] && [[ "${output}" == *'No test DMN resources'* ]] || { echo 'Missing-resource check failed.' >&2; exit 1; }
[[ "${output}" != *'not-a-real-secret'* ]] || { echo 'Missing-resource failure exposed the secret.' >&2; exit 1; }
echo 'PASS: fails safely when no matching resources exist'

mkdir -p "${test_dir}/civil/camunda" "${test_dir}/dmn/resources" "${test_dir}/wa/resources"
touch "${test_dir}/civil/camunda/civil.bpmn" \
  "${test_dir}/dmn/resources/decision.dmn" \
  "${test_dir}/wa/resources/wa.bpmn"

: >"${CAMUNDA_IMPORT_TEST_LOG}"
PATH="${test_dir}/bin:$PATH" S2S_SECRET='not-a-real-secret' \
  "${script_dir}/import-bpmn-diagram.sh" "${test_dir}/civil"
PATH="${test_dir}/bin:$PATH" S2S_SECRET='not-a-real-secret' \
  "${script_dir}/import-dmn-diagram.sh" "${test_dir}/dmn" civil civil
PATH="${test_dir}/bin:$PATH" S2S_SECRET='not-a-real-secret' \
  "${script_dir}/import-wa-bpmn-diagram.sh" "${test_dir}/wa"

[ "$(grep -c '^UPLOAD$' "${CAMUNDA_IMPORT_TEST_LOG}")" = '3' ] || { echo 'Expected each public importer to upload one resource.' >&2; exit 1; }
grep -q 'tenant-id=civil' "${CAMUNDA_IMPORT_TEST_LOG}" || { echo 'Civil tenant configuration is missing.' >&2; exit 1; }
grep -q 'deployment-source=civil' "${CAMUNDA_IMPORT_TEST_LOG}" || { echo 'DMN deployment source is missing.' >&2; exit 1; }
! grep -q 'not-a-real-secret\|123456' "${CAMUNDA_IMPORT_TEST_LOG}" || { echo 'Public importer exposed secret or OTP material.' >&2; exit 1; }
echo 'PASS: public Civil, DMN and WA importers delegate to the central engine'
