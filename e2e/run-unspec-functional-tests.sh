#!/bin/bash
set -ex

yarn test:api-unspec
yarn test:e2e-unspec
