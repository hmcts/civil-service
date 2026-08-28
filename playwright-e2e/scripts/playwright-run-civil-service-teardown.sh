if [ "$RUN_FAILED_TESTS" = "true" ]; then
  PLAYWRIGHT_LAST_RUN_REPORT="${PLAYWRIGHT_FUNCTIONAL_TEST_RESULTS_DIR}/${PLAYWRIGHT_FUNCTIONAL_TEST_RESULTS_PROJECT_DIR}/.last-run.json"

  # Check if the last run json is not found or is empty.
  if [ ! -f "$PLAYWRIGHT_LAST_RUN_REPORT" ] || [ ! -s "$PLAYWRIGHT_LAST_RUN_REPORT" ]; then
    echo ".prev-last-run.json not found or is empty."
    exit 1
    
  # Check if the last run json has status passed.
  elif [ "$(jq -r '.status // empty' "$PLAYWRIGHT_LAST_RUN_REPORT")" = "passed" ]; then
    echo ".prev-last-run.json status is passed"
    exit 0

  # Check if the last run json has a status other than failed.
  elif [ "$(jq -r '.status // empty' "$PLAYWRIGHT_LAST_RUN_REPORT")" != "failed" ]; then
    LAST_RUN_STATUS=$(jq -r '.status // empty' "$PLAYWRIGHT_LAST_RUN_REPORT")
    echo ".last-run.json status is '$LAST_RUN_STATUS', expected 'passed' or 'failed'"
    exit 1
  fi
fi

# Run the Playwright setup install and teardown tests for a failed last run or a normal run.
yarn test:playwright:setup:install
yarn test:playwright:teardown:civil-service-nightly:ci
