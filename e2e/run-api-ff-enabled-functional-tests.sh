#!/bin/bash
set -ex

echo "Run Api Functional tests enabling all feature flags"

echo "Run Api Functional tests for api-unspec"
yarn test:api-unspec

echo "Run Api Functional tests for api-unspec"
yarn test:api-spec

echo "Run Api Functional tests for api-sdo"
yarn test:api-sdo

#echo "Run Api Functional tests for api-spec-cui"
#yarn test:api-spec-cui
