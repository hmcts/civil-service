#!/bin/bash

set -eu

environment=${1:-preview}

if [ "${environment}" = "preview" ]; then
  civil_excluded_filename_patterns="-e *-prod.json,*HNL-nonprod.json,AuthorisationCaseType-shuttered.json"
  ga_excluded_filename_patterns="-e *-nonprod.json"
elif [ "${environment}" = "aat" ]; then
  civil_excluded_filename_patterns="-e *-nonprod.json,*COS-nonprod.json,AuthorisationCaseType-shuttered.json"
  ga_excluded_filename_patterns="-e *-nonprod.json"
else
  echo "ERROR: unsupported environment '${environment}'. Supported values: preview, aat."
  exit 1
fi

root_dir=$(realpath "$(dirname "${0}")/..")
civil_config_dir=${root_dir}/ccd-definition/civil
civil_ga_config_dir=${root_dir}/ccd-definition/generalapplication
build_dir=${root_dir}/build/ccd-release-config
civil_release_definition_output_file=${build_dir}/civil-ccd-${environment}.xlsx
civil_ga_release_definition_output_file=${build_dir}/civil-ga-ccd-${environment}.xlsx

mkdir -p "${build_dir}"

"${root_dir}/bin/utils/process-definition.sh" "${civil_config_dir}" "${civil_release_definition_output_file}" "${civil_excluded_filename_patterns}"
"${root_dir}/bin/utils/process-definition.sh" "${civil_ga_config_dir}" "${civil_ga_release_definition_output_file}" "${ga_excluded_filename_patterns}"
