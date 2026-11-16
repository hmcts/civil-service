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

# ccd-definition/SOURCE_OF_TRUTH in this checkout means this repo is the source of truth for the CCD
# definitions (see ccd-definition/README.md). The build then uses the in-repo ccd-definition/ as it is,
# and only the test code still comes from civil-ccd-definition.
# The same path is checked in bin/dev-setup/start-devuser-preview-environment.sh and
# bin/shared/pull-ccd-definitions.sh. Change all three together.
toggleFile="ccd-definition/SOURCE_OF_TRUTH"
if [ -f "${toggleFile}" ]; then
  definitionsFromThisRepo=true
  echo "CCD definitions: source of truth is civil-service (${toggleFile} is present). Using the in-repo ccd-definition/."
  echo "CCD definitions: only the test code comes from ${ccdRepoName}@${branchName}."
  if [ "${branchName}" != "master" ]; then
    echo "WARNING: ${ccdRepoName} branch ${branchName} is not used for the definitions, only for the test code. Make definition changes in this repo's ccd-definition/."
  fi
else
  definitionsFromThisRepo=false
  echo "CCD definitions: source of truth is civil-ccd-definition (no ${toggleFile}). Copying them from ${ccdRepoName}@${branchName}."
fi

# Checkout specific branch of CCD definitions.
git clone https://github.com/hmcts/${ccdRepoName}.git
cd "${ccdRepoName}"

echo "Switch to ${branchName} branch on ${ccdRepoName}"
git checkout "${branchName}"
cd ..

mergedDefinitionsDir=$(find_first_existing_dir \
  "./${ccdRepoName}/ccd-definition" \
  "./${ccdRepoName}/civil-ccd-definition/ccd-definition" || true)

if [ "${definitionsFromThisRepo}" = "false" ] && [ -z "${mergedDefinitionsDir:-}" ]; then
  echo "Unable to locate merged CCD definition directory in ${ccdRepoName}."
  exit 1
fi

# ccd-definition/ is also tracked in this repo: a mirror that nothing builds from yet, and the specs in
# ccd-definition/test (see ccd-definition/README.md). The build uses the branch copied here, so replace
# only the folders that branch provides (civil/ and generalapplication/). Deleting all of ccd-definition/
# would also delete test/ and any uncommitted work in it.
replacedPaths=""
trackedPaths=""
if [ "${definitionsFromThisRepo}" = "false" ]; then
  mkdir -p ./ccd-definition
  for source in "${mergedDefinitionsDir}"/* "${mergedDefinitionsDir}"/.[!.]*; do
    [ -e "${source}" ] || continue
    target="ccd-definition/$(basename "${source}")"
    if [ -n "$(git ls-files -- "${target}" 2>/dev/null | head -1)" ]; then
      trackedPaths="${trackedPaths} ${target}"
    fi
    replacedPaths="${replacedPaths} ${target}"
    rm -rf "./${target}"
    cp -r "${source}" "./${target}"
  done
fi

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
# modified, so say how to put it back rather than leave it to be committed by accident. Callers that
# restore it themselves set CCD_DEFINITION_MIRROR_RESTORED_BY_CALLER=true to skip this.
if [ -z "${BUILD_NUMBER:-}" ] && [ "${CCD_DEFINITION_MIRROR_RESTORED_BY_CALLER:-}" != "true" ] \
  && [ -n "${trackedPaths}" ]; then
  echo "${replacedPaths# } now hold ${ccdRepoName}@${branchName}, not the tracked mirror. Do not commit them."
  echo "Restore the mirror with: git checkout --${trackedPaths} && git clean -fdq --${replacedPaths}"
fi
