#!/usr/bin/env bash

ccdRepoName="civil-ccd-definition"
directoryName="civil-test"
branchName=$1
functionalTestType=$2

#Checkout specific branch of CCD definitions
git clone https://github.com/hmcts/${ccdRepoName}.git
cd ${ccdRepoName}

echo "Switch to ${branchName} branch on ${ccdRepoName}"
git checkout ${branchName}
cd ..

#Copy ccd definition files  to civil-ccd-def which contains ccd def files
cp -r ./${ccdRepoName}/ccd-definition .
if [ "$functionalTestType" = "CIVIL_FT" ]; then
  cp -r ./civil-ccd-definition/e2e .
  cp -r ./civil-ccd-definition/playwright-e2e .
  cp -r ./civil-ccd-definition/plugins .
  cp -r ./civil-ccd-definition/package.json .
  cp -r ./civil-ccd-definition/yarn.lock .
  cp -r ./civil-ccd-definition/.yarnrc.yml .
  cp -r ./civil-ccd-definition/.yarn .
  cp -r ./civil-ccd-definition/codecept.conf.js .
  cp -r ./civil-ccd-definition/playwright.config.ts .
  cp -r ./civil-ccd-definition/saucelabs.conf.js .
fi
echo *
rm -rf ./${ccdRepoName}
