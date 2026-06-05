#!/usr/bin/env bash

branchName=${1:-master}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

#Checkout specific branch of civil wiremock mappings
rm -rf ./civil-wiremock-mappings ./mappings ./__files
git clone https://github.com/hmcts/civil-wiremock-mappings.git
cd civil-wiremock-mappings

echo "Switch to ${branchName} branch on civil-wiremock-mappings"
git checkout "${branchName}"
cd ..

#Copy mappings, response files, and load script
cp -r ./civil-wiremock-mappings/mappings .
cp -r ./civil-wiremock-mappings/__files .
cp -r ./civil-wiremock-mappings/bin/. ./bin/
rm -rf ./civil-wiremock-mappings

# Include civil-service postcode stubs until they are published in civil-wiremock-mappings
if compgen -G "${REPO_ROOT}/mappings/postcode-lookup*.json" > /dev/null; then
  cp -f "${REPO_ROOT}"/mappings/postcode-lookup*.json ./mappings/
  echo "Copied civil-service postcode WireMock mappings"
fi
