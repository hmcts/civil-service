#!/usr/bin/env bash

set -eu

ccdBranch=${1:-master}
camundaBranch=${2:-master}

echo "export ENVIRONMENT=devuser-preview"
echo "Loading Environment Variables"
source ./bin/devuser/load-preview-environment-variables.sh
. ./bin/utils/idam-get-tokens.sh
echo "Importing Roles to the CCD pod"
./bin/devuser/add-roles.sh
echo "Importing Camunda definitions"
./bin/pull-latest-camunda-files.sh "${camundaBranch}"
./bin/import-bpmn-diagram.sh .
echo "Importing CCD definitions"
./bin/pull-latest-civil-ccd-files.sh "${ccdBranch}"
./bin/devuser/import-ccd-definition.sh "-e *-prod.json,*HNL-nonprod.json,AuthorisationCaseType-shuttered.json"

rm -rf "$(pwd)/ccd-definition"
rm -rf "$(pwd)/build/ccd-development-config"
rm -rf "$(pwd)/camunda"

echo "ENV variables set for devuser-preview environment."
echo "XUI_URL: $XUI_WEBAPP_URL"
