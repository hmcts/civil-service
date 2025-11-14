#!/usr/bin/env bash

ccdRepoName="civil-general-apps-ccd-definition"
branchName=$1
functionalTestType=$2
directoryName="civil-ga-ccd-definition"

#Checkout specific branch of CCD definitions
git clone https://github.com/hmcts/${ccdRepoName}.git
cd ${ccdRepoName}

echo "Switch to ${branchName} branch on ${ccdRepoName}"
git checkout ${branchName}
cd ..

#Copy ccd definition files  to civil-ccd-def which contains ccd def files
cp -r ./${ccdRepoName}/ga-ccd-definition .
if [ "$functionalTestType" = "GENERAL_APPS_FT" ]; then
  cp -r ./${ccdRepoName}/e2e .
  cp -r ./${ccdRepoName}/package.json .
  cp -r ./${ccdRepoName}/yarn.lock .
  cp -r ./${ccdRepoName}/codecept.conf.js .
  cp -r ./${ccdRepoName}/saucelabs.conf.js .
  cp -r ./bin/yarn/.yarnrc.yml .
fi
echo *
rm -rf ./${ccdRepoName}
