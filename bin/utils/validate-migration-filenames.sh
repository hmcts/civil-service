#!/usr/bin/env bash

set -eu

BASE_MIGRATION_FILES="$1"
NEW_MIGRATION_FILES="$2"

echo "BASE_FILES!!"
echo "$BASE_MIGRATION_FILES"

LATEST_BASE_FILE=""
LATEST_BASE_TIMESTAMP=""

# Work out the latest timestamp from the base migration files
for BASE_FILE in $BASE_MIGRATION_FILES; do
  BASE_TIMESTAMP=$(echo "$BASE_FILE" | grep -oP 'V\K[0-9_]{10,14}')
  # Check if this base timestamp is later than the current latest
  if [[ -z "$LATEST_BASE_TIMESTAMP" ]] || [[ "$LATEST_BASE_TIMESTAMP" < "$BASE_TIMESTAMP" ]]; then
    LATEST_BASE_TIMESTAMP="$BASE_TIMESTAMP"
    LATEST_BASE_FILE="$BASE_FILE"
  fi
done

echo " "
echo "Base migration file with the latest timestamp:"
echo "    $LATEST_BASE_FILE"

# Log new migration files
echo " "
if [[ -z "$NEW_MIGRATION_FILES" ]]; then
  echo "No new migration files found."
  exit 0
else
  echo "New migration files in the current branch:"
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

# Check new migration file timestamps
for FILE in $NEW_MIGRATION_FILES; do
  TIMESTAMP=$(echo "$FILE" | grep -oP 'V\K[0-9_]{10,14}')
  # Ensure the timestamp is valid
  if [[ -z "$TIMESTAMP" ]]; then
    echo "Error: File $FILE does not have a valid timestamp."
    exit 1
  fi

  # Validate against the latest base file timestamp
  if [[ "$TIMESTAMP" < "$LATEST_BASE_TIMESTAMP" ]]; then
    ERROR_FILES+=("$FILE")
  fi
done

# Log timestamp errors
if [[ ${#ERROR_FILES[@]} -gt 0 ]]; then
  echo " "
  echo "Error: The new migration file names in the current branch:"
  for ERROR_FILE in "${ERROR_FILES[@]}"; do
    echo "    $ERROR_FILE"
  done
  echo "Contain timestamps earlier than the latest migration file from the base branch:"
  echo "    $LATEST_BASE_FILE"
  exit 1
fi

echo "All new migration files are valid."
