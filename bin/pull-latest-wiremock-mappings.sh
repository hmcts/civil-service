#!/usr/bin/env bash

branchName=${1:-master}

#Checkout specific branch of civil wiremock mappings
git clone https://github.com/hmcts/civil-wiremock-mappings.git
cd civil-wiremock-mappings

echo "Switch to ${branchName} branch on civil-wiremock-mappings"
git checkout ${branchName}
cd ..

#Copy mappings, response files, and load script
cp -r ./civil-wiremock-mappings/mappings .
cp -r ./civil-wiremock-mappings/__files .
cp -r ./civil-wiremock-mappings/bin/. ./bin/
rm -rf ./civil-wiremock-mappings
