#!/usr/bin/env bash
# Runs the CCD definition specs (ccd-definition/test) against this checkout's ccd-definition/: the mirror
# while civil-ccd-definition is the source of truth, the live definitions once ccd-definition/SOURCE_OF_TRUTH
# is here. They read only the JSON and exclusions.json, so they need no running services.
#
# Jenkins calls this at the start of the Build stage, while ccd-definition/ is still the checkout. The preview
# import later replaces it with civil-ccd-definition's copy (bin/pull-latest-civil-ccd-files.sh) until the
# switch, so running the specs after that would test civil-ccd-definition, not this repo.
#
# The npm dependencies go into a temporary folder, not ./node_modules, so the rest of the build sees the same
# workspace as before: later stages Yarn-install civil-ccd-definition's package.json into ./node_modules, and
# the dependency-check stage reads package-lock.json and would also analyse a ./node_modules if one existed.
#
# Needs node and npm on PATH (the version in .nvmrc).

set -euo pipefail

cd "$(dirname "$0")/.."

deps_dir=$(mktemp -d)
trap 'rm -rf "${deps_dir}"' EXIT

cp package.json package-lock.json "${deps_dir}/"
# --ignore-scripts stops puppeteer, which the BPMN rendering tooling uses, from downloading Chrome.
npm ci --prefix "${deps_dir}" --ignore-scripts --no-audit --no-fund

# The specs run from ccd-definition/test, so Node can't find the packages by walking up from there:
# NODE_PATH points require() at them, and PATH gives npm run the mocha binary.
NODE_PATH="${deps_dir}/node_modules" PATH="${deps_dir}/node_modules/.bin:${PATH}" npm run test:definitions
