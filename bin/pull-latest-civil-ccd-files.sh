#!/usr/bin/env bash

set -eu

ccdRepoName="civil-ccd-definition"
branchName=${1:-master}

find_first_existing_dir() {
  for candidate in "$@"; do
    if [ -d "${candidate}" ]; then
      echo "${candidate}"
      return 0
    fi
  done
  return 1
}

# Checkout specific branch of CCD definitions.
git clone https://github.com/hmcts/${ccdRepoName}.git
cd "${ccdRepoName}"

echo "Switch to ${branchName} branch on ${ccdRepoName}"
git checkout "${branchName}"
cd ..

mergedDefinitionsDir=$(find_first_existing_dir \
  "./${ccdRepoName}/ccd-definition" \
  "./${ccdRepoName}/civil-ccd-definition/ccd-definition" || true)

if [ -z "${mergedDefinitionsDir:-}" ]; then
  echo "Unable to locate merged CCD definition directory in ${ccdRepoName}."
  exit 1
fi

# ccd-definition/ is also tracked in this repo, as a mirror that nothing builds from yet (see
# ccd-definition/README.md). The build uses the branch copied here, so the mirror is replaced.
rm -rf ./ccd-definition
cp -r "${mergedDefinitionsDir}" ./ccd-definition

cp -r "./${ccdRepoName}/e2e" .
cp -r "./${ccdRepoName}/playwright-e2e" .
cp -r "./${ccdRepoName}/playwright-categories.json" .
cp -r "./${ccdRepoName}/tsconfig.json" .
cp -r "./${ccdRepoName}/plugins" .
cp -r "./${ccdRepoName}/package.json" .
cp -r "./${ccdRepoName}/bin/run-commands-series.sh" ./bin/run-commands-series.sh
cp -r "./${ccdRepoName}/yarn.lock" .
cp -r "./${ccdRepoName}/.yarnrc.yml" .
cp -r "./${ccdRepoName}/.yarn" .
cp -r "./${ccdRepoName}/codecept.conf.js" .
cp -r "./${ccdRepoName}/playwright.config.ts" .
cp -r "./${ccdRepoName}/saucelabs.conf.js" .

rm -rf "./${ccdRepoName}"

# Jenkins sets BUILD_NUMBER and throws its workspace away. Locally, the tracked mirror now shows as
# modified, so say how to put it back rather than leave it to be committed by accident.
if [ -z "${BUILD_NUMBER:-}" ] && git ls-files --error-unmatch ccd-definition/.source-sha >/dev/null 2>&1; then
  echo "ccd-definition/ now holds ${ccdRepoName}@${branchName}, not the tracked mirror. Do not commit it."
  echo "Restore the mirror with: git checkout -- ccd-definition && git clean -fdq -- ccd-definition"
fi
