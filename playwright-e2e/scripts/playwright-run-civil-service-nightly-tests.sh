if [ "$RUN_FAILED_TESTS" = "true" ]; then
  PREV_PLAYWRIGHT_LAST_RUN_REPORT="${PLAYWRIGHT_FUNCTIONAL_TEST_RESULTS_DIR}/.prev-last-run.json"

  # Check if the previous last run json is not found or is empty.
  if [ ! -f "$PREV_PLAYWRIGHT_LAST_RUN_REPORT" ] || [ ! -s "$PREV_PLAYWRIGHT_LAST_RUN_REPORT" ]; then
    echo ".prev-last-run.json not found or is empty."
    exit 1

  # Check if the previous last run json has status passed.
  elif [ "$(jq -r '.status // empty' "$PREV_PLAYWRIGHT_LAST_RUN_REPORT")" = "passed" ]; then
    echo ".prev-last-run.json status is passed"
    exit 0
    
  # Check if the previous last run json has a status other than failed.
  elif [ "$(jq -r '.status // empty' "$PREV_PLAYWRIGHT_LAST_RUN_REPORT")" != "failed" ]; then
    PREV_LAST_RUN_STATUS=$(jq -r '.status // empty' "$PREV_PLAYWRIGHT_LAST_RUN_REPORT")
    echo ".last-run.json status is '$PREV_LAST_RUN_STATUS', expected 'passed' or 'failed'"
    exit 1

  else
    # Run the Playwright setup install and nightly tests for a failed last run.
    export PLAYWRIGHT_FUNCTIONAL=true
    yarn test:playwright:setup:install
    yarn test:playwright:civil-service-nightly:ci --last-failed
  fi
fi

# Run the Playwright setup install and nightly tests for a normal run.
export PLAYWRIGHT_FUNCTIONAL=true
yarn test:playwright:setup:install
yarn test:playwright:civil-service-nightly:ci
