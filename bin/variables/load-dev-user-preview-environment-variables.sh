#!/usr/bin/env bash

set -eu
user=$(whoami)
echo "User directory: /Users/$user"

export ENVIRONMENT=preview
# urls
export URL="https://xui-civil-service-dev-${user}.preview.platform.hmcts.net"
export CIVIL_SERVICE_URL="https://civil-service-dev-${user}.preview.platform.hmcts.net"
export SERVICE_AUTH_PROVIDER_API_BASE_URL="http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
export IDAM_API_BASE_URL="https://idam-api.aat.platform.hmcts.net"
export IDAM_API_URL="https://idam-api.aat.platform.hmcts.net"
export CCD_IDAM_REDIRECT_URL="https://ccd-case-management-web-aat.service.core-compute-aat.internal/oauth2redirect"
export CCD_DEFINITION_STORE_API_BASE_URL="https://ccd-definition-store-civil-service-dev-${user}.preview.platform.hmcts.net"
export CAMUNDA_BASE_URL="https://camunda-civil-service-dev-${user}.preview.platform.hmcts.net"
export CCD_CONFIGURER_IMPORTER_USERNAME=$(az keyvault secret show --vault-name civil-aat --name ccd-importer-username --query value -o tsv)
export CCD_CONFIGURER_IMPORTER_PASSWORD=$(az keyvault secret show --vault-name civil-aat --name ccd-importer-password --query value -o tsv)
export CCD_API_GATEWAY_IDAM_CLIENT_SECRET=$(az keyvault secret show --vault-name ccd-aat --name ccd-api-gateway-oauth2-client-secret --query value -o tsv)
export CCD_API_GATEWAY_S2S_SECRET=$(az keyvault secret show --vault-name s2s-aat --name microservicekey-ccd-gw --query value -o tsv)
export S2S_SECRET=$(az keyvault secret show --vault-name civil-aat --name microservicekey-civil-service --query value -o tsv)
# definition placeholders
export CCD_DEF_CASE_SERVICE_BASE_URL="http://civil-service-dev-${user}-java"

