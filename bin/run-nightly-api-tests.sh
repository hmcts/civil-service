#!/bin/bash
set -ex

yarn playwright install

echo "Running API tests on ${ENVIRONMENT} env"

if [ "$FORCE_GREEN_BUILD" == "true" ]; then
  echo "Manually forced green build, no functional tests will be run."
  exit 0
fi


# Define path to failedTestFiles.json
FAILED_TEST_FILES_JSON="./test-results/functional/failedTestFiles.json"
NOT_EXECUTED_TEST_FILES_JSON="./test-results/functional/notExecutedTestFiles.json"

# Check if RUN_FAILED_AND_NOT_EXECUTED_TEST_FILES is set to "true"
if [ "$RUN_FAILED_AND_NOT_EXECUTED_TEST_FILES" != "true" ]; then
  yarn test:api-nightly-prod

# Check if failedTestFiles.json exists and is non-empty
elif [ ! -f "$FAILED_TEST_FILES_JSON" ] || [ ! -s "$FAILED_TEST_FILES_JSON" ]; then
  echo "failedTestFiles.json not found or is empty."
  exit 1

# Check if the JSON array inside failedTestFiles.json is empty
elif [ "$(jq 'length' "$FAILED_TEST_FILES_JSON")" -eq 0 ]; then
  echo "failedTestFiles.json contains an empty array."
  exit 1

else
  # Collect array elements into a comma-separated string
  FAILED_TEST_FILES=$(jq -r '.[]' "$FAILED_TEST_FILES_JSON" | paste -sd "," -)

  # If no failed tests, set FAILED_TEST_FILES to empty string
  FAILED_TEST_FILES=${FAILED_TEST_FILES:-""}

  # Collect array elements into a comma-separated string
  NOT_EXECUTED_TEST_FILES=$(jq -r '.[]' "$NOT_EXECUTED_TEST_FILES_JSON" | paste -sd "," -)

  # If no failed tests, set NOT_EXECUTED_TEST_FILES to empty string
  NOT_EXECUTED_TEST_FILES=${NOT_EXECUTED_TEST_FILES:-""}

  if [ -z "$FAILED_TEST_FILES" ]; then
    echo "No failed tests found."
    exit 1
  else
    # Export as environment variable
    export FAILED_TEST_FILES
    export NOT_EXECUTED_TEST_FILES

    yarn test:api-nightly-prod
  fi
fi
