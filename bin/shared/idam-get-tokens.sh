#!/usr/bin/env sh

# Source this script to export USER_TOKEN and SERVICE_TOKEN for reuse.
# Usage: . ./bin/shared/idam-get-tokens.sh

set -e

# Support both naming conventions (CCD_CONFIGURER_IMPORTER_* is canonical)
IMPORTER_USERNAME=${CCD_CONFIGURER_IMPORTER_USERNAME:-${DEFINITION_IMPORTER_USERNAME}}
IMPORTER_PASSWORD=${CCD_CONFIGURER_IMPORTER_PASSWORD:-${DEFINITION_IMPORTER_PASSWORD}}

export USER_TOKEN=$(./bin/shared/idam-lease-user-token.sh ${IMPORTER_USERNAME} ${IMPORTER_PASSWORD})
export SERVICE_TOKEN=$(./bin/shared/idam-lease-service-token.sh ccd_gw $(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool --totp -b ${CCD_API_GATEWAY_S2S_SECRET:-AAAAAAAAAAAAAAAC}))
