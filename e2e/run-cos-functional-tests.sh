#!/bin/bash
set -ex

echo "Run Api Functional tests for cos"
yarn test:api-cos

echo "Run Functional tests for cos"
yarn test:e2e-cos
