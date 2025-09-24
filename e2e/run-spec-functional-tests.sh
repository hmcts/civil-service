#!/bin/bash
set -ex

echo "Run Api Functional tests for api-spec"
yarn test:api-spec

echo "Run Api Functional tests for e2e-spec"
yarn test:e2e-spec

#echo "Run Api Functional tests for api-spec-cui"
#yarn test:api-spec-cui
