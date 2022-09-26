#!/usr/bin/env bash

branchName=$1

#Checkout specific branch pf  civil camunda bpmn definition
git clone https://github.com/hmcts/civil-ccd-definition.git
cd civil-ccd-definition

echo "Switch to ${branchName} branch on civil-camunda-bpmn-definition"
git checkout ${branchName}
cd ..

#Copy ccd to civil-ccd-def
cp -r ./civil-ccd-definition/ccd-definition .
cp -r ./civil-ccd-definition/e2e .
rm -rf ./civil-ccd-definition
