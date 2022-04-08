#!/bin/bash
set -ex

echo "Run E2E UI Functional tests enabling all feature flags"

yarn test:e2e-unspec
yarn test:e2e-spec
yarn test:e2e-sdo