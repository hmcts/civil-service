#!/usr/bin/env bash

basePath=${PWD%/*}
definitionsPath=$basePath/civil-ccd-definition/ccd-definition/
xlsxOutputFile=$basePath/civil-service/build/civil-definitions.xlsx

if [[ ! -e $(dirname ${xlsxOutputFile}) ]]; then
  mkdir $(dirname ${xlsxOutputFile})
fi

sh ${basePath}/civil-ccd-definition/bin/utils/process-definition.sh ${definitionsPath} ${xlsxOutputFile}
