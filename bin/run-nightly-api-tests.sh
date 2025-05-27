#!/bin/bash
set -ex

echo "Running Functional tests on ${ENVIRONMENT} env"

if [ "$FORCE_GREEN_BUILD" == "true" ]; then
  echo "Manually forced green build, no functional tests will be run."
  exit 0
fi


# Check if RUN_FAILED_AND_PREV_NOT_EXECUTED_TEST_FILES is set to "true"
if [ "$RUN_PREV_FAILED_AND_NOT_EXECUTED_TEST_FILES" != "true" ]; then
  yarn playwright install
  yarn test:api-nightly-prod
else 
  # Define path to failedTestFiles.json
  TEST_FILES_REPORT="test-results/functional/testFilesReport.json"
  PREV_TEST_FILES_REPORT="test-results/functional/prevTestFilesReport.json"

  if [ "$CI" != "true" ]; then
    [ -f "$TEST_FILES_REPORT" ] && mv "$TEST_FILES_REPORT" "$PREV_TEST_FILES_REPORT"
  fi

  # Check if prevTestFilesReport.json exists and is non-empty
  if [ ! -f "$PREV_TEST_FILES_REPORT" ] || [ ! -s "$PREV_TEST_FILES_REPORT" ]; then
    echo "prevTestFilesReport.json not found or is empty."
    exit 1

  # Check if the JSON array inside prevTestFilesReport.json is empty
  elif [ "$(jq '.failedTestFiles | length' "$PREV_TEST_FILES_REPORT")" -eq 0 ]; then
    echo "failedTestFiles in prevTestFilesReport.json contains an empty array."
    exit 1

  else
    # Collect array elements into a comma-separated string
    PREV_FAILED_TEST_FILES=$(jq -r '.failedTestFiles[]' "$PREV_TEST_FILES_REPORT" | paste -sd "," -)

    # Collect array elements into a comma-separated string
    PREV_NOT_EXECUTED_TEST_FILES=$(jq -r '.notExecutedTestFiles[]' "$PREV_TEST_FILES_REPORT" | paste -sd "," -)

    if [ -z "$PREV_FAILED_TEST_FILES" ]; then
      echo "No failed tests found."
      exit 1
    else
      # Export as environment variable
      export PREV_FAILED_TEST_FILES="$PREV_FAILED_TEST_FILES"
      export PREV_NOT_EXECUTED_TEST_FILES="$PREV_NOT_EXECUTED_TEST_FILES"
      
      yarn playwright install
      yarn test:api-nightly-prod
    fi
  fi
fi



