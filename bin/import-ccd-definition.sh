#!/usr/bin/env bash

#definition_input_dir=$(realpath 'build/ccd-definitionIndu')
#definition_output_file="$(realpath ".")/build/ccd-development-config/ccd-civil-dev.xlsx"

definition_input_dir='/home/elfasij/workspace/HMCTS-Damages/civil-service/build/ccd-definitionIndu'
definition_output_file='/home/elfasij/workspace/HMCTS-Damages/civil-service/build/ccd-development-config/ccd-civil-dev.xlsx'


params="$@"

echo "Input directory $definition_input_dir"
echo "output directory $definition_output_file"

echo "hiya $params"

#./bin/utils/import-ccd-definition.sh  #"${definition_input_dir}" "${definition_output_file}" "${params}"

./home/elfasij/workspace/HMCTS-Damages/civil-service/bin/utils/import-ccd-definition.sh
