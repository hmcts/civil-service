#!/usr/bin/env bash

set -eu

CURRENT_BRANCH=${1}
BASE_BRANCH=${2}
JENKINS_MODE=false
MIGRATION_DIR="src/main/resources/db/migration"
TEMP_DIR="tmp"
WORKING_DIR=$(pwd)
JENKINS_MODE=false

if [[ "${3:-}" == "--jenkins" ]]; then
  JENKINS_MODE=true
fi

echo "Retrieving migration files..."
if [[ "${JENKINS_MODE}" = true ]]; then
  mkdir ${TEMP_DIR}
  cd ${TEMP_DIR}
  git clone --no-checkout https://github.com/HMCTS/civil-service.git > /dev/null 2>&1
  cd civil-service
fi

git fetch origin > /dev/null 2>&1

NEW_MIGRATION_FILES=$(git diff --name-only origin/${BASE_BRANCH}...origin/${CURRENT_BRANCH} -- ${MIGRATION_DIR})
BASE_MIGRATION_FILES=$(git ls-tree -r --name-only origin/${BASE_BRANCH} -- ${MIGRATION_DIR})

if [[ "${JENKINS_MODE}" = true ]]; then
  cd ${WORKING_DIR}
  rm -rf ${TEMP_DIR}
fi

if [[ -z "${NEW_MIGRATION_FILES}" ]]; then
  echo -e "\nNo new migration files found."
  exit 0
else
  echo -e "\nNew migration files in the current branch:"
  for FILE in ${NEW_MIGRATION_FILES}; do
    echo "    ${FILE}"
  done
fi

LATEST_BASE_FILE=""
LATEST_BASE_TIMESTAMP=""

for BASE_FILE in ${BASE_MIGRATION_FILES}; do
  BASE_TIMESTAMP=$(echo ${BASE_FILE} | grep -oP 'V\K[0-9]{4}_[0-9]{2}_[0-9]{2}_[0-9]{4}')
  if [[ "${LATEST_BASE_TIMESTAMP}" < "${BASE_TIMESTAMP}" ]]; then
    LATEST_BASE_TIMESTAMP="${BASE_TIMESTAMP}"
    LATEST_BASE_FILE="${BASE_FILE}"
  fi
done

echo -e "\nBase migration file with the latest timestamp:"
echo "    ${LATEST_BASE_FILE}"

echo -e "\nValidating new migration filename format..."
FORMAT_ERROR_FILES=()

for FILE in ${NEW_MIGRATION_FILES}; do
  FILENAME=$(basename "${FILE}")
  if ! [[ "${FILENAME}" =~ ^V[0-9]{4}_[0-9]{2}_[0-9]{2}_[0-9]{4}__.*$ ]]; then
    FORMAT_ERROR_FILES+=("${FILE}")
  fi
done

if [[ ${#FORMAT_ERROR_FILES[@]} -gt 0 ]]; then
  echo -e "\nError: The following migration file names do not have a valid timestamp format:"
  for FORMAT_ERROR_FILE in "${FORMAT_ERROR_FILES[@]}"; do
    echo "    ${FORMAT_ERROR_FILE}"
  done
fi

echo "Validating new migration files timestamps are newer than the latest base migration file timestamp..."
ERROR_FILES=()

for FILE in ${NEW_MIGRATION_FILES}; do
  TIMESTAMP=$(echo "${FILE}" | grep -oP 'V\K[0-9]{4}_[0-9]{2}_[0-9]{2}_[0-9]{4}')
  if [[ "${TIMESTAMP}" < "${LATEST_BASE_TIMESTAMP}" || "${TIMESTAMP}" == "${LATEST_BASE_TIMESTAMP}" ]]; then
    ERROR_FILES+=("${FILE}")
  fi
done

if [[ ${#ERROR_FILES[@]} -gt 0 ]]; then
  echo -e "\nError: The following migration file names in the current branch [${CURRENT_BRANCH}]:"
  for ERROR_FILE in "${ERROR_FILES[@]}"; do
    echo "    ${ERROR_FILE}"
  done

  echo "Contain timestamps earlier than the latest migration file from the base branch [${BASE_BRANCH}]:"
  echo "    ${LATEST_BASE_FILE}"
  exit 1
fi

echo "All new migration files are valid."
exit 0
