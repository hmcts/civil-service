#!/bin/bash
set -e

source "$(dirname "${BASH_SOURCE[0]}")/../common.sh"

if should_run_failed_tests; then

  # Check if the previous last run json is not found or is empty.
  if report_missing_or_empty "$PREV_PLAYWRIGHT_LAST_RUN_REPORT"; then
    exit 1
    
  # Check if the previous last run json has status passed.
  elif previous_run_has_status_passed; then
    exit 0

  # Check if the previous last run json has a status other than failed.
  elif ! previous_run_has_status_failed; then
    exit 1
  fi
fi

# Run the Playwright teardown tests for a failed last run or a normal run.
yarn test:playwright:setup:install
yarn test:playwright:teardown:civil-service:ci
