#!/bin/bash
set -e
echo "Running smoke tests on ${ENVIRONMENT} env"

cd civil-ccd-definition
yarn install --immutable --silent
yarn test:smoke --silent
cd ../civil-ga-ccd-definition
yarn install --immutable --silent
yarn test:ga-smoke --silent
cd ..

