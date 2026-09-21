#!/bin/bash
set -e

source "$(dirname "${BASH_SOURCE[0]}")/../common.sh"

run_functional_test_groups() {
  command="yarn test:playwright:civil-service-pr:ci --grep "
  playwright_pr_ft_groups=$(echo "$PLAYWRIGHT_PR_FT_GROUPS" | awk '{print tolower($0)}')
  
  regex_pattern=""

  IFS=',' read -ra playwright_ft_groups_array <<< "$playwright_pr_ft_groups"

  for ft_group in "${playwright_ft_groups_array[@]}"; do
      if [ -n "$regex_pattern" ]; then
          regex_pattern+="|"
      fi
      regex_pattern+="@$ft_group"
  done

  command+="'$regex_pattern'"
  echo "Executing: $command"
  eval "$command"
}

run_failed_functional_tests() {
  echo "Running failed playwright functional tests on ${ENVIRONMENT} env"
  if [ "$ENVIRONMENT" = "aat" ]; then
    if ! yarn test:playwright:civil-service-master:ci --last-failed; then
      run_playwright_teardown
      exit 1
    fi
  else
    if ! yarn test:playwright:civil-service-pr:ci --last-failed; then
      run_playwright_teardown
      exit 1
    fi
  fi
}

run_functional_tests() {
  echo "Running functional playwright tests on ${ENVIRONMENT} env"
  if [ "$ENVIRONMENT" = "aat" ]; then
    if ! yarn test:playwright:civil-service-master:ci; then
      run_playwright_teardown
      exit 1
    fi
  elif [ -z "$PLAYWRIGHT_PR_FT_GROUPS" ]; then
    if ! yarn test:playwright:civil-service-pr:ci; then
      run_playwright_teardown
      exit 1
    fi
  else
    if ! run_functional_test_groups; then
      run_playwright_teardown
      exit 1
    fi
  fi
}

#MAIN SCRIPT

# Check if SKIP_FUNCTIONAL_TESTS is set to true
if should_skip_functional_tests; then
  exit 0

#Check if RUN_ALL_FUNCTIONAL_TESTS is set to true
elif should_run_all_functional_tests; then
  run_functional_tests
  run_playwright_teardown

# Check if the previous last run json is not found or is empty.
elif report_missing_or_empty "$PREV_PLAYWRIGHT_LAST_RUN_REPORT"; then
  run_functional_tests
  run_playwright_teardown

#Check if latest current git commit is the not the same as git commit of prev playwright test files report 
elif previous_commit_changed; then 
  run_functional_tests
  run_playwright_teardown

#Check if ft_groups of test files report is the same as current ft_groups.
elif ! compare_ft_groups; then
  run_functional_tests
  run_playwright_teardown

# Check if latest current git commit has not changed, ft_groups match and previous last run json has status passed.
elif previous_run_has_status_passed; then
  exit 0

# Check if the previous last run json has a status other than failed.
elif ! previous_run_has_status_failed; then
  run_functional_tests
  run_playwright_teardown

else
  run_failed_functional_tests
  run_playwright_teardown
fi
