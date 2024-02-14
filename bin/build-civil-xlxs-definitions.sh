#!/usr/bin/env bash

set -eu

scriptPath=$(dirname $(realpath $0))
basePath=$(dirname $scriptPath)
ccdDefinitionRepoPath=$basePath/civil-ccd-definition

definition_processor_version=latest
additionalParameters=${1-}

ccdDefinitionPath="/Users/ellis/Documents/WorkFiles/civil-ccd-definition/ccd-definition"
definitionOutputFile="/Users/ellis/Documents/WorkFiles/civil-service/build/ccd-civil-dev.xlsx"

sh /Users/ellis/Documents/WorkFiles/civil-ccd-definition/bin/utils/process-definition.sh ${ccdDefinitionPath} ${definitionOutputFile}
