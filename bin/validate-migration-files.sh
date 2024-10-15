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

# Get existing migration files from the base branch
BASE_MIGRATION_FILES=$(git ls-tree -r --name-only origin/${BASE_BRANCH} -- $MIGRATION_DIR)
LATEST_BASE_FILE=""
LATEST_BASE_TIMESTAMP=""

# Work out the latest timestamp from the base branch
for BASE_FILE in $BASE_MIGRATION_FILES; do
  BASE_TIMESTAMP=$(echo $BASE_FILE | grep -oP 'V\K[0-9_]{10,14}')
  # Check if this base timestamp is later than the current latest
  if [[ "$LATEST_BASE_TIMESTAMP" < "$BASE_TIMESTAMP" ]]; then
    LATEST_BASE_TIMESTAMP="$BASE_TIMESTAMP"
    LATEST_BASE_FILE="$BASE_FILE"
  fi
done

echo " "
echo "Base migration file with the latest timestamp:"
echo "    $LATEST_BASE_FILE"

# Get the new migration files added in the current branch
NEW_MIGRATION_FILES=$(git diff --name-only origin/${BASE_BRANCH}..origin/${CURRENT_BRANCH} -- $MIGRATION_DIR)

# Log new migration files from current branch
echo " "
if [[ -z "$NEW_MIGRATION_FILES" ]]; then
  echo "No new migration files found."
  exit 0
else
  echo "New migration files in the current branch:"
  # Output the new migration files with indentation
  for FILE in $NEW_MIGRATION_FILES; do
    echo "    $FILE"
  done
fi
FORMAT_ERROR_FILES=()

# Validate timestamp format
for FILE in $NEW_MIGRATION_FILES; do
  FILENAME=$(basename "$FILE")
  if ! [[ "$FILENAME" =~ ^V[0-9]{4}_[0-9]{2}_[0-9]{2}_[0-9]{4}__.*$ ]]; then
    FORMAT_ERROR_FILES+=("$FILE")
  fi
 done

# Log errors for format issues
if [[ ${#FORMAT_ERROR_FILES[@]} -gt 0 ]]; then
  echo " "
  echo "Error: The following migration file names do not have a valid timestamp format:"
  for FORMAT_ERROR_FILE in "${FORMAT_ERROR_FILES[@]}"; do
    echo "    $FORMAT_ERROR_FILE"
  done
  exit 1
fi
ERROR_FILES=()

# Check for new migration files and their timestamps
for FILE in $NEW_MIGRATION_FILES; do
  TIMESTAMP=$(echo $FILE | grep -oP 'V\K[0-9_]{10,14}')
  # Ensure the timestamp is valid
  if [[ -z "$TIMESTAMP" ]]; then
    echo "Error: File $FILE does not have a valid timestamp."
    cd ../../
    rm -rf ${TEMP_DIR}
    exit 1
  fi
  # Validate against the latest base file timestamp
  if [[ "$TIMESTAMP" < "$LATEST_BASE_TIMESTAMP" ]]; then
    ERROR_FILES+=("$FILE")  # Collect the file name for later output
  fi
done

# Log timestamp errors
if [[ ${#ERROR_FILES[@]} -gt 0 ]]; then
  echo " "
  echo "Error: The following migration file names in the current branch [$CURRENT_BRANCH]:"
  for ERROR_FILE in "${ERROR_FILES[@]}"; do
    echo "    $ERROR_FILE"
  done
  echo "Contain timestamps earlier than the latest migration file from the base branch [$BASE_BRANCH]:"
  echo "    $LATEST_BASE_FILE"
  exit 1
fi
echo "All new migration files are valid."

# Get back to original location and remove temp directory
cd ${WORKING_DIR}
rm -rf ${TEMP_DIR}

echo "Exiting on purpose!!"
exit 1
