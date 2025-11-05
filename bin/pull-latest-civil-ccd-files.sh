#!/usr/bin/env bash

ccdRepoName="civil-ccd-definition"
branchName=$1
directoryName="civil-test"

#Checkout specific branch of CCD definitions
git clone https://github.com/hmcts/${ccdRepoName}.git
cd ${ccdRepoName}

echo "Switch to ${branchName} branch on ${ccdRepoName}"
git checkout ${branchName}
cd ..

#Copy ccd definition files  to civil-ccd-def which contains ccd def files
cp -r ./${ccdRepoName}/ccd-definition .
mkdir -p ${directoryName}/e2e && mv ./${ccdRepoName}/e2e ./${directoryName}
mkdir -p ${directoryName}/playwright-e2e && cp -r ./civil-ccd-definition/playwright-e2e ./${directoryName}
mkdir -p ${directoryName}/plugins && cp -r ./civil-ccd-definition/plugins ./${directoryName}
cp -r ./${ccdRepoName}/package.json ./${directoryName}
cp -r ./${ccdRepoName}/yarn.lock ./${directoryName}
cp -r ./${ccdRepoName}/codecept.conf.js ./${directoryName}
cp -r ./${ccdRepoName}/saucelabs.conf.js ./${directoryName}
cp -r ./${ccdRepoName}/playwright.config.ts ./${directoryName}
echo *
rm -rf ./${ccdRepoName}
mv ./${directoryName} ./${ccdRepoName}
