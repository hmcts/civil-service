#!/bin/bash

# Shared report paths; source after configuring the results directory and project.
PLAYWRIGHT_TEST_FILES_REPORT="${PLAYWRIGHT_TEST_RESULTS_DIR}/${PLAYWRIGHT_FUNCTIONAL_TEST_RESULTS_DIR}/playwrightTestFilesReport.json"
PREV_PLAYWRIGHT_TEST_FILES_REPORT="${PLAYWRIGHT_TEST_RESULTS_DIR}/prevPlaywrightTestFilesReport.json"
PLAYWRIGHT_LAST_RUN_REPORT="${PLAYWRIGHT_TEST_RESULTS_DIR}/${PLAYWRIGHT_FUNCTIONAL_TEST_RESULTS_DIR}/.last-run.json"
PREV_PLAYWRIGHT_LAST_RUN_REPORT="${PLAYWRIGHT_TEST_RESULTS_DIR}/.prev-last-run.json"
PLAYWRIGHT_TESTS_FLAGS="${PLAYWRIGHT_TEST_RESULTS_DIR}/playwrightTestsFlags.properties"

write_report_flags() {
  local setup_tests_failed="${1:-false}"
  local smoke_tests_failed="${2:-false}"
  local tests_skipped="${3:-false}"

  mkdir -p "$(dirname "$PLAYWRIGHT_TESTS_FLAGS")"
  {
    echo "PLAYWRIGHT_SETUP_TESTS_FAILED=$setup_tests_failed"
    echo "PLAYWRIGHT_SMOKE_TESTS_FAILED=$smoke_tests_failed"
    echo "PLAYWRIGHT_TESTS_SKIPPED=$tests_skipped"
  } > "$PLAYWRIGHT_TESTS_FLAGS"
}

get_report_flag() {
  local flag_name="$1"
  local default_value="${2:-false}"

  if [ ! -f "$PLAYWRIGHT_TESTS_FLAGS" ]; then
    echo "$default_value"
    return
  fi

  local flag_value
  flag_value=$(grep "^${flag_name}=" "$PLAYWRIGHT_TESTS_FLAGS" | tail -n 1 | cut -d '=' -f 2-) || true
  echo "${flag_value:-$default_value}"
}

write_tests_skipped_flag() {
  local setup_tests_failed
  local smoke_tests_failed

  setup_tests_failed=$(get_report_flag "PLAYWRIGHT_SETUP_TESTS_FAILED")
  smoke_tests_failed=$(get_report_flag "PLAYWRIGHT_SMOKE_TESTS_FAILED")

  write_report_flags "$setup_tests_failed" "$smoke_tests_failed" true

  echo "Skipping smoke and functionaltests. PLAYWRIGHT_TESTS_SKIPPED=true"
}

run_playwright_teardown() {
  echo "Running playwright teardown tests on ${ENVIRONMENT} env"
  yarn test:playwright:setup:install
  yarn test:playwright:teardown:civil-ccd:ci
}

# Returns success when the report is unavailable so callers can choose the action.
report_missing_or_empty() {
  local report="$1"

  if [ ! -f "$report" ] || [ ! -s "$report" ]; then
    echo "${report##*/} not found or is empty."
    return 0
  fi

  return 1
}

# Predicates are called from if/elif; callers retain control of exits and test runs.
previous_commit_changed() {
  if [ "$(jq -r 'if .gitCommitId == null then "__NULL__" else .gitCommitId end' "$PREV_PLAYWRIGHT_TEST_FILES_REPORT")" != "$GIT_COMMIT" ]; then
    echo "The gitCommitId does not match the current GIT_COMMIT."
    return 0
  fi
  return 1
}

previous_run_has_status() {
  local expected_status="$1"
  [ "$(jq -r '.status // empty' "$PREV_PLAYWRIGHT_LAST_RUN_REPORT")" = "$expected_status" ]
}

previous_run_has_status_passed() {
  if previous_run_has_status passed; then
    echo ".prev-last-run.json status is passed"
    write_tests_skipped_flag
    return 0
  fi
  return 1
}

previous_run_has_status_failed() {
  if previous_run_has_status failed; then
    return 0
  fi

  local previous_status
  # Preserve the caller's failure on unreadable JSON, even inside an if condition.
  previous_status=$(jq -r '.status // empty' "$PREV_PLAYWRIGHT_LAST_RUN_REPORT") || exit $?
  echo ".prev-last-run.json status is '$previous_status', expected 'passed' or 'failed'"
  return 1
}

should_skip_functional_tests() {
  if [ "$SKIP_FUNCTIONAL_TESTS" = "true" ]; then
    echo "The label 'pr-values:skip-functional-tests' exists on the PR."
    echo "Skipping functional tests."
    write_tests_skipped_flag
    return 0
  fi
  return 1
}

should_run_failed_tests() {
  [ "$RUN_FAILED_TESTS" = "true" ]
}

should_run_all_functional_tests() {
  if [ "$RUN_ALL_FUNCTIONAL_TESTS" = "true" ]; then
    echo "The label 'runAllFunctionalTests' exists on the PR."
    echo "Running all functional tests."
    return 0
  fi
  return 1
}

compare_ft_groups() {
  local ft_groups_csv pr_ft_groups_csv

  # Extract ftGroups array as a comma-separated string (sorted).
  ft_groups_csv=$(jq -r '
    if (.ftGroups == null or (.ftGroups | length == 0))
    then ""
    else (.ftGroups | sort | join(","))
    end
  ' "$PREV_PLAYWRIGHT_TEST_FILES_REPORT")

  # Normalize PLAYWRIGHT_PR_FT_GROUPS (sort, trim spaces, split by comma, then rejoin sorted).
  pr_ft_groups_csv=""
  if [ -n "$PLAYWRIGHT_PR_FT_GROUPS" ]; then
    pr_ft_groups_csv=$(echo "$PLAYWRIGHT_PR_FT_GROUPS" | tr ',' '\n' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//' | sort | paste -sd "," -)
  fi

  if [ "$ft_groups_csv" = "$pr_ft_groups_csv" ]; then
    echo "ftGroups do match PLAYWRIGHT_PR_FT_GROUPS"
    return 0
  fi

  echo "ftGroups do NOT match PLAYWRIGHT_PR_FT_GROUPS"
  return 1
}
