#!/usr/bin/env bash

# Builds the civil and general application CCD definition spreadsheets for one environment:
#   build/ccd-release-config/civil-ccd-<environment>.xlsx
#   build/ccd-release-config/civil-ga-ccd-<environment>.xlsx
#
# Which JSON files each environment leaves out is set in ccd-definition/exclusions.json, which the
# definition specs in ccd-definition/test read too. Change the rules there, not here.
#
# The second argument picks the shuttered AuthorisationCaseType file instead of the unshuttered one.

set -eu

environment=${1:-}
activateShutter=${2:-false}
rootDir=$(realpath "$(dirname "${0}")/..")
definitionsPath="${rootDir}/ccd-definition"
exclusionsFile="${definitionsPath}/exclusions.json"

usage() {
  echo "Usage: ./bin/build-civil-xlsx-definitions.sh <environment> [true|false]"
  echo "  environment: one of the keys in ccd-definition/exclusions.json ($(jq -r 'keys_unsorted | join(", ")' "${exclusionsFile}"))"
  echo "  true builds the shuttered variant; the default, false, builds the unshuttered one"
}

if [ -z "${environment}" ]; then
  usage
  exit 1
fi

if [ "${activateShutter}" != true ] && [ "${activateShutter}" != false ]; then
  echo "Unsupported shutter flag: ${activateShutter}"
  usage
  exit 1
fi

if ! excludedFilenamePatterns=$(jq -er --arg env "${environment}" '.[$env] // empty | join(",")' "${exclusionsFile}"); then
  echo "Unsupported environment: ${environment}"
  usage
  exit 1
fi

if [ "${activateShutter}" = true ]; then
  echo "Building the shuttered definitions for ${environment}"
  excludedFilenamePatterns="${excludedFilenamePatterns},AuthorisationCaseType-unshuttered.json"
else
  echo "Building the unshuttered definitions for ${environment}"
  excludedFilenamePatterns="${excludedFilenamePatterns},AuthorisationCaseType-shuttered.json"
fi
ccdDefArgs="-e ${excludedFilenamePatterns}"

outputPath="${rootDir}/build/ccd-release-config"
civilOutputFile="${outputPath}/civil-ccd-${environment}.xlsx"
generalApplicationOutputFile="${outputPath}/civil-ga-ccd-${environment}.xlsx"

mkdir -p "${outputPath}"

for definition in "civil:${civilOutputFile}" "generalapplication:${generalApplicationOutputFile}"; do
  definitionDir="${definitionsPath}/${definition%%:*}"
  outputFile="${definition#*:}"
  touch "${outputFile}"
  echo "${rootDir}/bin/shared/process-definition.sh ${definitionDir} ${outputFile} ${ccdDefArgs}"
  sh "${rootDir}/bin/shared/process-definition.sh" "${definitionDir}" "${outputFile}" "${ccdDefArgs}"
done
