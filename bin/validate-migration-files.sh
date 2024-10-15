#!/usr/bin/env bash

set -eu

CURRENT_BRANCH=${1}
BASE_BRANCH=${2}
MIGRATION_DIR="src/main/resources/db/migration"
TEMP_DIR="tmp"
WORKING_DIR=$(pwd)

mkdir ${TEMP_DIR}
cd ${TEMP_DIR}
git clone --no-checkout https://github.com/HMCTS/civil-service.git
cd civil-service
git fetch origin

BASE_MIGRATION_FILES=$(git ls-tree -r --name-only origin/${BASE_BRANCH} -- $MIGRATION_DIR)
NEW_MIGRATION_FILES=$(git diff --name-only origin/${BASE_BRANCH}..origin/${CURRENT_BRANCH} -- $MIGRATION_DIR)

# Get back to work dir and remove temp directory
cd ${WORKING_DIR}
rm -rf ${TEMP_DIR}

sh ./bin/utils/validate-migration-filenames.sh "${BASE_MIGRATION_FILES}" "${NEW_MIGRATION_FILES}"

echo "Exiting on purpose!!"
exit 1
