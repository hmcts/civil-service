#!/bin/bash
set -ex

echo "Run Api Functional tests for api-unspec"
yarn test:api-unspec

echo "Run Api Functional tests for e2e-unspec"
yarn test:e2e-unspec
