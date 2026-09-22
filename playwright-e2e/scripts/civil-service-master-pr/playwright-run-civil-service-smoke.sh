#!/bin/bash
set -e

source "$(dirname "${BASH_SOURCE[0]}")/../common.sh"

common_script="$(dirname "${BASH_SOURCE[0]}")/../common.sh"
echo "Loaded common.sh: ${common_script}"
if grep -n "write_empty_smoke_results_xml" "${common_script}"; then
  echo "write_empty_smoke_results_xml exists in common.sh"
else
  echo "write_empty_smoke_results_xml does not exist in common.sh"
fi
if declare -F write_empty_smoke_results_xml; then
  echo "write_empty_smoke_results_xml function is loaded"
else
  echo "write_empty_smoke_results_xml function is not loaded"
fi

run_playwright_setup() {
  echo "Running playwright setup tests on ${ENVIRONMENT} env"
  yarn test:playwright:setup:install
  if ! yarn test:playwright:setup:civil-service:ci; then
    write_report_flags true false
    run_playwright_teardown
    exit 1
  fi
}

run_smoke_tests() {
  echo "Running playwright smoke tests on ${ENVIRONMENT} env"
  yarn test:playwright:setup:install
  if ! yarn test:playwright:civil-service-smoke:ci; then
    write_report_flags true true
    run_playwright_teardown
    exit 1
  fi
  exit 0
}

#MAIN SCRIPT

# Check if SKIP_FUNCTIONAL_TESTS is set to true
if should_skip_functional_tests; then
  write_empty_smoke_results_xml
  exit 0
fi

write_report_flags false false

#Check if RUN_ALL_FUNCTIONAL_TESTS is set to true
if should_run_all_functional_tests; then
  run_playwright_setup
  run_smoke_tests
fi

# Check if the playwrightTestFilesReport json or last run json is not found or is empty.
if report_missing_or_empty "$PLAYWRIGHT_TEST_FILES_REPORT" || report_missing_or_empty "$PLAYWRIGHT_LAST_RUN_REPORT"; then
  run_playwright_setup
  run_smoke_tests
fi

mv "$PLAYWRIGHT_TEST_FILES_REPORT" "$PREV_PLAYWRIGHT_TEST_FILES_REPORT"
cp "$PLAYWRIGHT_LAST_RUN_REPORT" "$PREV_PLAYWRIGHT_LAST_RUN_REPORT"

# Check if the previous last run json is not found or is empty.
if report_missing_or_empty "$PREV_PLAYWRIGHT_LAST_RUN_REPORT"; then
  run_playwright_setup
  run_smoke_tests

#Check if latest current git commit is the not the same as git commit of prev playwright test files report 
elif previous_commit_changed; then 
  run_playwright_setup
  run_smoke_tests

#Check if ft_groups of test files report is the same as current ft_groups.
elif ! compare_ft_groups; then
  run_playwright_setup
  run_smoke_tests

# Check if latest current git commit has not changed, ft_groups match and previous last run json has status passed.
elif previous_run_has_status_passed; then
  write_empty_smoke_results_xml
  exit 0

# Check if the previous last run json has a status other than failed.
elif ! previous_run_has_status_failed; then
  run_playwright_setup
  run_smoke_tests

else 
  run_playwright_setup
  run_smoke_tests
fi
