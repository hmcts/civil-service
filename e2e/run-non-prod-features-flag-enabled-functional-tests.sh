#!/bin/bash
set -ex

echo "Run Api Functional tests with non prod ccd def "
yarn test:api-unspec
yarn test:api-spec
yarn test:api-dj
yarn test:api-cos
yarn test:api-sdo

echo "Run E2E Functional tests with non prod ccd def file"

yarn test:master-e2e-ft
yarn test:e2e-nightly-prod
yarn test:e2e-nightly-nonprod


