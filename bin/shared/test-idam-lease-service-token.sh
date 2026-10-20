#!/usr/bin/env bash

set -euo pipefail

readonly script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
readonly subject="${script_dir}/idam-lease-service-token.sh"
readonly test_dir=$(mktemp -d "${TMPDIR:-/tmp}/idam-service-token-test.XXXXXX")
trap 'rm -rf "${test_dir}"' EXIT

make_docker() {
  cat >"${test_dir}/docker" <<EOF
#!/usr/bin/env bash
$1
EOF
  chmod +x "${test_dir}/docker"
}

cat >"${test_dir}/curl" <<'EOF'
#!/usr/bin/env bash
printf 'service-token'
EOF
chmod +x "${test_dir}/curl"

assert_failure() {
  local name=$1 expected=$2 output status
  set +e
  output=$(PATH="${test_dir}:$PATH" IDAM_SERVICE_SECRET='not-a-real-secret' "${subject}" civil_service 2>&1)
  status=$?
  set -e
  if [ "${status}" -eq 0 ] || [[ "${output}" != *"${expected}"* ]]; then
    echo "${name} failed: status=${status}, output=${output}" >&2
    exit 1
  fi
  [[ "${output}" != *'not-a-real-secret'* ]] || { echo "${name} exposed the secret" >&2; exit 1; }
  echo "PASS: ${name}: ${expected}"
}

make_docker "echo 'unauthorized: authentication required' >&2; exit 1"
assert_failure 'registry denial' 'registry access denied'

make_docker "echo 'manifest unknown: not found' >&2; exit 1"
assert_failure 'image not found' 'image not found'

make_docker "echo 'runtime failure' >&2; exit 42"
assert_failure 'tool non-zero' 'exited non-zero (status 42)'

make_docker "exit 0"
assert_failure 'empty output' 'returned empty output'

make_docker "echo 'not-an-otp'"
assert_failure 'malformed output' 'returned malformed output'

make_docker "echo '123456'"
output=$(PATH="${test_dir}:$PATH" IDAM_SERVICE_SECRET='not-a-real-secret' "${subject}" civil_service)
[ "${output}" = 'service-token' ] || { echo 'successful token generation failed' >&2; exit 1; }
echo 'PASS: successful token generation'

output=$(PATH="${test_dir}:$PATH" IDAM_SERVICE_SECRET='not-a-real-secret' \
  IDAM_VALIDATE_OTP_TOOL_ONLY=true "${subject}" civil_service)
[ "${output}" = 'OTP tool validation succeeded.' ] || { echo 'OTP tool validation mode failed' >&2; exit 1; }
echo 'PASS: OTP tool validation mode'

output=$(PATH="${test_dir}:$PATH" "${subject}" civil_service 123456)
[ "${output}" = 'service-token' ] || { echo 'pre-generated OTP compatibility failed' >&2; exit 1; }
echo 'PASS: pre-generated OTP compatibility'
