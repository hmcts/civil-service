#!/bin/bash
set -ex

yarn test:api-spec
yarn test:e2e-spec