#!/usr/bin/env bash

set -eu

user=$(whoami | tr -cd '[:alnum:]' | tr '[:upper:]' '[:lower:]' | cut -c1-8)
service_name="civil-service-${user}"

dev_env_file=$(mktemp)
additional_values_template="values.devuser-preview-local.yaml"
additional_values_file="charts/civil-service/${additional_values_template}"
trap 'rm -f "${dev_env_file}" "${additional_values_file}"' EXIT

cat > "${dev_env_file}" <<EOF
PREVIEW_ACA_SERVICE_API_BASEURL=http://${service_name}-aac-manage-case-assignment
PREVIEW_HMC_API_URL=http://hmc-cft-hearing-service-aat.service.core-compute-aat.internal
PREVIEW_RD_PROFESSIONAL_API_URL=http://rd-professional-api-aat.service.core-compute-aat.internal
PREVIEW_RD_COMMONDATA_API_URL=http://rd-commondata-api-aat.service.core-compute-aat.internal
PREVIEW_DOCMOSIS_TORNADO_URL=https://docmosis.aat.platform.hmcts.net
PREVIEW_SEND_LETTER_URL=http://rpe-send-letter-service-aat.service.core-compute-aat.internal
PREVIEW_FEES_API_URL=http://fees-register-api-aat.service.core-compute-aat.internal
PREVIEW_CLAIM_STORE_URL=http://cmc-claim-store-aat.service.core-compute-aat.internal
PREVIEW_ROLE_ASSIGNMENT_URL=http://am-role-assignment-service-aat.service.core-compute-aat.internal
PREVIEW_GENAPP_LRD_URL=http://rd-location-ref-api-aat.service.core-compute-aat.internal
PREVIEW_RTL_API_URL=http://localhost:4000
PREVIEW_SENDGRID_HOST=
PREVIEW_SENDGRID_TEST=false
EOF

cat > "${additional_values_file}" <<EOF
wiremock:
  enabled: false
EOF

npx @hmcts/dev-env@latest --env "${dev_env_file}" --template "${additional_values_template}"
./bin/setup-devuser-preview-env.sh "$@"
