#!/bin/bash
set -ex

 echo "Run Functional tests with prod ccd def file"

dir=$(dirname ${0})

${dir}/run-unspec-functional-tests.sh