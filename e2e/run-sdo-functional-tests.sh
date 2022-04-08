#!/bin/bash
set -ex

yarn test:api-sdo
yarn test:e2e-sdo
