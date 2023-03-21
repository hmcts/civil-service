#!/usr/bin/env bash

set -ex
echo "hiya"
dir=$(dirname ${0})


echo "111111"
#definition_input_dir=${1}
#definition_output_file=${2}
definition_input_dir='/home/elfasij/workspace/HMCTS-Damages/civil-service/build/ccd-definitionIndu'
definition_output_file='/home/elfasij/workspace/HMCTS-Damages/civil-service/build/ccd-development-config/ccd-civil-dev.xlsx'
echo "22222"
additionalParameters="${@:3}"
echo "333333"

echo "Definition directory: ${definition_input_dir}"
echo "Definition spreadsheet ${definition_output_file}"
echo "Additional parameters: ${additionalParameters}"

mkdir -p $(dirname ${definition_output_file})

${dir}/process-definition.sh ${definition_input_dir} ${definition_output_file} "${additionalParameters}"
${dir}/ccd-import-definition.sh ${definition_output_file}
