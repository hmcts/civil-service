#!/usr/bin/env bash

set -eu

ccd_branch=${1:-master}
camunda_branch=${2:-master}

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

echo "export ENVIRONMENT=devuser-preview"
echo "Loading Environment Variables"
echo "User directory: /Users/$(whoami)"

source .env.local

export ENVIRONMENT=preview
export URL=$XUI_WEBAPP_URL
export CIVIL_SERVICE_URL=$TEST_URL
export SERVICE_AUTH_PROVIDER_API_BASE_URL="http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
export IDAM_API_BASE_URL="https://idam-api.aat.platform.hmcts.net"
export IDAM_API_URL="https://idam-api.aat.platform.hmcts.net"
export CCD_IDAM_REDIRECT_URL="https://ccd-case-management-web-aat.service.core-compute-aat.internal/oauth2redirect"
export CCD_DEFINITION_STORE_API_BASE_URL=$CCD_DEFINITION_STORE_URL
export CAMUNDA_BASE_URL=$CAMUNDA_URL
export CCD_CONFIGURER_IMPORTER_USERNAME=$(az keyvault secret show --vault-name civil-aat --name ccd-importer-username --query value -o tsv)
export CCD_CONFIGURER_IMPORTER_PASSWORD=$(az keyvault secret show --vault-name civil-aat --name ccd-importer-password --query value -o tsv)
export CCD_API_GATEWAY_IDAM_CLIENT_SECRET=$(az keyvault secret show --vault-name ccd-aat --name ccd-api-gateway-oauth2-client-secret --query value -o tsv)
export CCD_API_GATEWAY_S2S_SECRET=$(az keyvault secret show --vault-name s2s-aat --name microservicekey-ccd-gw --query value -o tsv)
export S2S_SECRET=$(az keyvault secret show --vault-name civil-aat --name microservicekey-civil-service --query value -o tsv)
export CCD_DEF_CASE_SERVICE_BASE_URL=$TEST_URL

. ./bin/dev-setup/idam-get-tokens.sh

echo "Importing Roles to the CCD pod"
./bin/dev-setup/add-roles.sh

echo "Importing Camunda definitions"
./bin/pull-latest-camunda-files.sh "${camunda_branch}"
./bin/import-bpmn-diagram.sh .

echo "Importing CCD definitions"
./bin/pull-latest-civil-ccd-files.sh "${ccd_branch}"

if [ ! -d "ccd-definition/civil" ]; then
  echo "Unable to locate civil CCD definition directory at ccd-definition/civil."
  exit 1
fi

definition_input_dir=$(realpath "ccd-definition/civil")
definition_output_file="$(realpath ".")/build/ccd-development-config/ccd-civil-dev.xlsx"
./bin/dev-setup/import-ccd-definition.sh \
  "${definition_input_dir}" \
  "${definition_output_file}" \
  "-e *-prod.json,*HNL-nonprod.json,AuthorisationCaseType-shuttered.json"

rm -rf "$(pwd)/ccd-definition"
rm -rf "$(pwd)/build/ccd-development-config"
rm -rf "$(pwd)/camunda"

echo "ENV variables set for devuser-preview environment."
echo "XUI_URL: $XUI_WEBAPP_URL"
