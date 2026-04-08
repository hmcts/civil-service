#!/usr/bin/env bash

set -eu
user=$(whoami)
echo "User directory: /Users/$user"

source .env.local

export ENVIRONMENT=preview
# urls
export URL=$XUI_WEBAPP_URL
export CIVIL_SERVICE_URL=$TEST_URL
export SERVICE_AUTH_PROVIDER_API_BASE_URL="http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
export IDAM_API_BASE_URL="https://idam-api.aat.platform.hmcts.net"
export IDAM_API_URL="https://idam-api.aat.platform.hmcts.net"
export CCD_IDAM_REDIRECT_URL="https://ccd-case-management-web-aat.service.core-compute-aat.internal/oauth2redirect"
export CCD_DEFINITION_STORE_API_BASE_URL=$CCD_DEFINITION_STORE_URL
export DEFINITION_STORE_URL_BASE=$CCD_DEFINITION_STORE_API_BASE_URL
export CAMUNDA_BASE_URL=$CAMUNDA_URL
export DEFINITION_IMPORTER_USERNAME=$(az keyvault secret show --vault-name civil-aat --name ccd-importer-username --query value -o tsv)
export DEFINITION_IMPORTER_PASSWORD=$(az keyvault secret show --vault-name civil-aat --name ccd-importer-password --query value -o tsv)
export CCD_CONFIGURER_IMPORTER_USERNAME=$DEFINITION_IMPORTER_USERNAME
export CCD_CONFIGURER_IMPORTER_PASSWORD=$DEFINITION_IMPORTER_PASSWORD
export CCD_API_GATEWAY_IDAM_CLIENT_SECRET=$(az keyvault secret show --vault-name ccd-aat --name ccd-api-gateway-oauth2-client-secret --query value -o tsv)
export CCD_API_GATEWAY_S2S_SECRET=$(az keyvault secret show --vault-name s2s-aat --name microservicekey-ccd-gw --query value -o tsv)
export S2S_SECRET=$(az keyvault secret show --vault-name civil-aat --name microservicekey-civil-service --query value -o tsv)
# definition placeholders
export CCD_DEF_CASE_SERVICE_BASE_URL=$TEST_URL
