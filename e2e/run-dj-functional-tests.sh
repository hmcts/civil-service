#!/bin/bash
set -ex

echo "Run Api Functional tests for api-dj"
yarn test:api-dj

echo "Run Api Functional tests for e2e-dj"
yarn test:e2e-dj
