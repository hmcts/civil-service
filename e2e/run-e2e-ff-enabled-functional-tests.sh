#!/bin/bash
set -ex

echo "Run E2E UI Functional tests enabling all feature flags"

echo "Run Api Functional tests for e2e-unspec"
yarn test:e2e-unspec

echo "Run Api Functional tests for e2e-spec"
yarn test:e2e-spec

echo "Run Api Functional tests for e2e-sdo"
yarn test:e2e-sdo
