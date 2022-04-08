#!/bin/bash
set -ex

echo "Run Api Functional tests enabling all feature flags"

yarn test:api-unspec
yarn test:api-spec
yarn test:api-sdo
