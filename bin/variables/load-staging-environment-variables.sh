#!/usr/bin/env bash

set -eu

echo 'export ENVIRONMENT=aat'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
echo "export IDAM_API_BASE_URL=https://idam-api.aat.platform.hmcts.net"
echo "export CCD_IDAM_REDIRECT_URL=https://ccd-case-management-web-aat.service.core-compute-aat.internal/oauth2redirect"
echo "export CCD_DEFINITION_STORE_API_BASE_URL=https://ccd-definition-store-civil-damages-service-staging.service.core-compute-preview.internal"
echo "export CAMUNDA_BASE_URL=https://camunda-civil-damages-service-staging.service.core-compute-preview.internal"

# definition placeholders
echo "export CCD_DEF_CASE_SERVICE_BASE_URL=http://civil-damages-service-staging.service.core-compute-aat.internal"
