#!/usr/bin/env bash

cd ${PWD%/*}/civil-ccd-definition

echo "Running Functional tests on ${ENVIRONMENT} env"

if [ "$ENVIRONMENT" = "aat" ] || [ -z "$PR_FT_GROUPS" ]; then
  yarn test:api-nonprod
else
  command="yarn test:api-nonprod --grep "
  pr_ft_groups=$(echo "$PR_FT_GROUPS" | awk '{print tolower($0)}')
  
  regex_pattern=""

  IFS=',' read -ra ft_groups_array <<< "$pr_ft_groups"

  for ft_group in "${ft_groups_array[@]}"; do
      if [ -n "$regex_pattern" ]; then
          regex_pattern+="|"
      fi
      regex_pattern+="@api-$ft_group"
  done

  command+="'$regex_pattern'"
  echo "Executing: $command"
  eval "$command"
fi

echo "Current directory: $(pwd)"