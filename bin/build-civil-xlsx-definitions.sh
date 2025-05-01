#!/usr/bin/env bash

basePath=${PWD%/*}
definitionsPath=${basePath}/civil-ccd-definition/ccd-definition/
outputPath=${basePath}/civil-service/build/ccd-def/
xlsxOutputFile=${outputPath}civil-ccd-definition.xlsx
ccdDefArgs="-e *-prod.json,AuthorisationCaseType-shuttered.json"

mkdir -p $outputPath
touch "$xlsxOutputFile"

echo "${basePath}/civil-ccd-definition/bin/utils/process-definition.sh ${definitionsPath} ${xlsxOutputFile} ${ccdDefArgs}"
sh ${basePath}/civil-ccd-definition/bin/utils/process-definition.sh ${definitionsPath} ${xlsxOutputFile} "${ccdDefArgs}"
