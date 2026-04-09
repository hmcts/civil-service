#!/usr/bin/env bash

set -eu

environment=${1:-}
rootDir=$(realpath "$(dirname "${0}")/..")
definitionsPath="${rootDir}/ccd-definition"

if [ -z "${environment}" ]; then
  echo "Usage: ./bin/build-civil-xlsx-definitions.sh <preview|aat>"
  exit 1
fi

case "${environment}" in
  preview)
    outputPath="${rootDir}/build/ccd-release-config"
    civilOutputFile="${outputPath}/civil-ccd-preview.xlsx"
    generalApplicationOutputFile="${outputPath}/civil-ga-ccd-preview.xlsx"
    ccdDefArgs="-e *-prod.json,AuthorisationCaseType-shuttered.json"
    ;;
  aat)
    outputPath="${rootDir}/build/ccd-release-config"
    civilOutputFile="${outputPath}/civil-ccd-aat.xlsx"
    generalApplicationOutputFile="${outputPath}/civil-ga-ccd-aat.xlsx"
    ccdDefArgs="-e UserProfile.json,*-nonprod.json,AuthorisationCaseType-shuttered.json"
    ;;
  *)
    echo "Unsupported environment: ${environment}"
    exit 1
    ;;
esac

mkdir -p "${outputPath}"
touch "${civilOutputFile}"

echo "${rootDir}/bin/utils/process-definition.sh ${definitionsPath}/civil ${civilOutputFile} ${ccdDefArgs}"
sh "${rootDir}/bin/utils/process-definition.sh" "${definitionsPath}/civil" "${civilOutputFile}" "${ccdDefArgs}"

if [ -n "${generalApplicationOutputFile}" ]; then
  touch "${generalApplicationOutputFile}"
  echo "${rootDir}/bin/utils/process-definition.sh ${definitionsPath}/generalapplication ${generalApplicationOutputFile} ${ccdDefArgs}"
  sh "${rootDir}/bin/utils/process-definition.sh" "${definitionsPath}/generalapplication" "${generalApplicationOutputFile}" "${ccdDefArgs}"
fi
