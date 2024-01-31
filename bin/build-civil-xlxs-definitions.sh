#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))
basePath=$(dirname $scriptPath)
ccdDefinitionRepoPath=$basePath/civil-ccd-definition

definition_processor_version=latest
additionalParameters=${1-}

ccdDefinitionPath=$ccdDefinitionRepoPath/ccd-definition
definitionOutputFile="${scriptPath}/build/ccd-development-config/ccd-civil-dev.xlsx"

echo "Path to CCD Definitions: ${ccdDefinitionPath}"
echo "Path to output xlsx file: ${definitionOutputFile}"

cd $basePath
if [[ ! -d civil-service || ! -d civil-ccd-definition || ! -d civil-camunda-bpmn-definition ]]; then
  echo "Error: make sure all civil repos are in the same directory"
  exit 1
fi

mkdir -p $(dirname ${definitionOutputFile})

if [[ ! -e ${definitionOutputFile} ]]; then
   touch ${definitionOutputFile}
fi

docker run --rm --name json2xlsx \
  -v ${ccdDefinitionPath}:/tmp/ccd-definition \
  -v ${definitionOutputFile}:/tmp/ccd-definition.xlsx \
  -e CCD_DEF_CASE_SERVICE_BASE_URL=${CCD_DEF_CASE_SERVICE_BASE_URL:-http://docker.for.mac.localhost:4000} \
  -e CCD_DEF_GEN_APP_SERVICE_BASE_URL=${CCD_DEF_GEN_APP_SERVICE_BASE_URL:-http://docker.for.mac.localhost:4550} \
  -e CCD_DEF_AAC_URL=${CCD_DEF_AAC_URL:-http://manage-case-assignment:4454} \
  hmctspublic.azurecr.io/ccd/definition-processor:${definition_processor_version} \
  json2xlsx -D /tmp/ccd-definition -o /tmp/ccd-definition.xlsx -e "*-prod.json" ${additionalParameters}

echo "NEED TO CHANGE env ports"
echo "Might need to run with AuthorisationCaseType-shuttered.json as additionalParameters"
