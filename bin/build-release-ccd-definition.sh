#!/bin/bash

set -eu

environment=${1:-prod}

if [[ ${environment} != "prod" && ${environment} != "staging" && ${environment} != "aat" && ${environment} != "demo" ]]; then
  echo "Environment '${environment}' is not supported!"
  exit 1
fi

if [ ${environment} == "prod" ]; then
  excludedFilenamePatterns="-e UserProfile.json,*-nonprod.json"
else
  excludedFilenamePatterns="-e UserProfile.json"
fi

root_dir=$(realpath $(dirname ${0})/..)
config_dir=${root_dir}/ccd-definition
build_dir=${root_dir}/build/ccd-release-config
release_definition_output_file=${build_dir}/ccd-unspec-${environment}.xlsx

mkdir -p ${build_dir}

$(${root_dir}/bin/variables/load-${environment}-environment-variables.sh)

# build the ccd definition file
${root_dir}/civil-unspecified-docker/bin/utils/process-definition.sh ${config_dir} ${release_definition_output_file} "${excludedFilenamePatterns}"
