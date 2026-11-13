#!/usr/bin/env bash

# Equivalence gate for moving the CCD definitions from civil-ccd-definition into this repo.
#
# Builds the civil and general application spreadsheets for every environment in
# ccd-definition/exclusions.json, plus the shuttered prod variant, twice:
#   - with civil-ccd-definition's bin/build-release-ccd-definition.sh, in a checkout of civil-ccd-definition
#   - with this repo's bin/build-civil-xlsx-definitions.sh, against this repo's ccd-definition/
# then unzips every spreadsheet and diffs the two sides file by file (one XML file per sheet, plus the
# shared strings). The processor writes no timestamps, so the same input gives byte-identical output and
# any difference fails the script.
#
# Usage: ./bin/compare-ccd-definition-builds.sh [civil-ccd-definition ref]
#   The ref defaults to the commit in ccd-definition/.source-sha. Both sides must hold the same
#   definitions, so the script stops if ccd-definition/civil or ccd-definition/generalapplication differ
#   from civil-ccd-definition at that ref.
#
# Needs git, jq, unzip and Docker. Work files go under build/, because Docker must be able to mount them.
# Delete this script at the cutover, together with build-release-ccd-definition.sh in civil-ccd-definition.

set -eu

rootDir=$(realpath "$(dirname "${0}")/..")
sourceRef=${1:-$(tr -d '[:space:]' < "${rootDir}/ccd-definition/.source-sha")}
workDir="${rootDir}/build/ccd-definition-equivalence"
oldRepo="${workDir}/civil-ccd-definition"

# Both sides must render the same callback URLs.
export CCD_DEF_CASE_SERVICE_BASE_URL=http://civil-service
export CCD_DEF_GEN_APP_SERVICE_BASE_URL=http://civil-service

rm -rf "${workDir}"
mkdir -p "${oldRepo}"

echo "Fetching hmcts/civil-ccd-definition at ${sourceRef}"
git -C "${oldRepo}" init -q
git -C "${oldRepo}" remote add origin https://github.com/hmcts/civil-ccd-definition.git
git -C "${oldRepo}" fetch -q --depth 1 origin "${sourceRef}"
git -C "${oldRepo}" checkout -q FETCH_HEAD

for definition in civil generalapplication; do
  if ! diff -r --brief "${oldRepo}/ccd-definition/${definition}" "${rootDir}/ccd-definition/${definition}"; then
    echo "ccd-definition/${definition} differs from civil-ccd-definition at ${sourceRef}. Compare builds of the same definitions."
    exit 1
  fi
done

# civil-ccd-definition's pipeline pulls bin/shared from this repo before it builds.
mkdir -p "${oldRepo}/bin/shared"
cp "${rootDir}/bin/shared/process-definition.sh" "${oldRepo}/bin/shared/process-definition.sh"

unpack() {
  local xlsx=${1}
  local outputDir=${2}
  if [ ! -s "${xlsx}" ]; then
    echo "${xlsx} is missing or empty"
    exit 1
  fi
  mkdir -p "${outputDir}"
  unzip -q "${xlsx}" -d "${outputDir}"
}

variants="$(jq -r 'keys_unsorted[] + ":false"' "${rootDir}/ccd-definition/exclusions.json") prod:true"
failed=""

for variant in ${variants}; do
  environment=${variant%%:*}
  shutter=${variant#*:}
  variantDir="${workDir}/${environment}-shutter-${shutter}"
  echo "== ${environment}, shutter ${shutter}"

  # Don't let a build that writes nothing be compared against the previous variant's output.
  for prefix in civil-ccd civil-ga-ccd; do
    rm -f "${oldRepo}/build/ccd-release-config/${prefix}-${environment}.xlsx" \
      "${rootDir}/build/ccd-release-config/${prefix}-${environment}.xlsx"
  done

  (cd "${oldRepo}" && ./bin/build-release-ccd-definition.sh "${environment}" "${shutter}") > "${variantDir}.old.log" 2>&1 \
    || { echo "civil-ccd-definition build failed, see ${variantDir}.old.log"; exit 1; }
  "${rootDir}/bin/build-civil-xlsx-definitions.sh" "${environment}" "${shutter}" > "${variantDir}.new.log" 2>&1 \
    || { echo "civil-service build failed, see ${variantDir}.new.log"; exit 1; }

  for prefix in civil-ccd civil-ga-ccd; do
    unpack "${oldRepo}/build/ccd-release-config/${prefix}-${environment}.xlsx" "${variantDir}/old/${prefix}"
    unpack "${rootDir}/build/ccd-release-config/${prefix}-${environment}.xlsx" "${variantDir}/new/${prefix}"
  done

  if diff -r "${variantDir}/old" "${variantDir}/new" > "${variantDir}.diff"; then
    echo "   identical ($(find "${variantDir}/new" -path '*/worksheets/*.xml' | wc -l | tr -d ' ') sheets)"
  else
    echo "   DIFFERENT, see ${variantDir}.diff"
    failed="${failed} ${environment}:${shutter}"
  fi
done

if [ -n "${failed}" ]; then
  echo "Builds differ for:${failed}"
  exit 1
fi
echo "All builds identical."
