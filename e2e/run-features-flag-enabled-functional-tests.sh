#!/bin/bash
set -ex

echo "Run Functional tests with all feature flags enabled ccd def file"

dir=$(dirname ${0})

${dir}/run-unspec-functional-tests.sh
#${dir}/run-spec-functional-tests.sh
#${dir}/run-sdo-functional-tests.sh
