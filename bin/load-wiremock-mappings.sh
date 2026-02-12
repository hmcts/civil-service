#!/usr/bin/env bash

set -e

MAPPINGS_DIR="./mappings"
FILES_DIR="./__files"
MAX_RETRIES=${MAX_RETRIES:-30}
RETRY_INTERVAL=${RETRY_INTERVAL:-10}

if [ -z "$WIREMOCK_URL" ]; then
  echo "Error: WIREMOCK_URL environment variable is not set"
  exit 1
fi

if [ ! -d "$MAPPINGS_DIR" ]; then
  echo "Mappings folder not found: $MAPPINGS_DIR"
  exit 1
fi

echo "Loading mappings into WireMock at $WIREMOCK_URL"

# Wait for WireMock to be ready
echo "Waiting for WireMock to be ready..."
attempt=1
while [ $attempt -le $MAX_RETRIES ]; do
  response=$(curl -sk -o /dev/null -w "%{http_code}" "$WIREMOCK_URL/__admin/mappings" 2>/dev/null || echo "000")

  if [ "$response" = "200" ]; then
    echo "WireMock is ready"
    break
  fi

  if [ $attempt -eq $MAX_RETRIES ]; then
    echo "Error: WireMock not ready after $MAX_RETRIES attempts"
    exit 1
  fi

  echo "WireMock not ready (HTTP $response), attempt $attempt/$MAX_RETRIES, retrying in ${RETRY_INTERVAL}s..."
  sleep $RETRY_INTERVAL
  attempt=$((attempt + 1))
done

# Reset all existing mappings first to avoid duplicates
echo "Resetting existing WireMock mappings..."
RESET_RESPONSE=$(curl -sk -o /dev/null -w "%{http_code}" -X DELETE "$WIREMOCK_URL/__admin/mappings")
if [ "$RESET_RESPONSE" == "200" ]; then
  echo "Existing mappings cleared successfully"
else
  echo "Warning: Failed to clear existing mappings (HTTP $RESET_RESPONSE)"
fi

for file in "$MAPPINGS_DIR"/*.json; do
  if [ -f "$file" ]; then
    echo "Posting: $file"

    BODY_FILE_NAME=$(jq -r '.response.bodyFileName // empty' "$file")
    if [[ -n "$BODY_FILE_NAME" ]]; then
      BODY_FILE_PATH="$FILES_DIR/$BODY_FILE_NAME"
      if [[ -f "$BODY_FILE_PATH" ]]; then
        echo "Inlining body from: $BODY_FILE_PATH"

        if [[ "$BODY_FILE_PATH" == *.pdf ]]; then
          echo "Inlining PDF as base64"
          TMP_BASE64=$(mktemp)
          base64 -w 0 "$BODY_FILE_PATH" > "$TMP_BASE64"
          TMP_JSON=$(mktemp)
          jq --rawfile base64_content "$TMP_BASE64" '
            del(.response.bodyFileName) |
            .response.base64Body = $base64_content
          ' "$file" > "$TMP_JSON"
          rm "$TMP_BASE64"
        else
          echo "Inlining JSON/text body"
          BODY_CONTENT=$(<"$BODY_FILE_PATH")
          TMP_JSON=$(mktemp)
          jq --arg body "$BODY_CONTENT" '
            del(.response.bodyFileName) |
            .response.body = $body
          ' "$file" > "$TMP_JSON"
        fi
      else
        echo "Missing body file: $BODY_FILE_PATH"
        continue
      fi
    else
      TMP_JSON=$(mktemp)
      cat "$file" > "$TMP_JSON"
    fi

    RESPONSE=$(curl -sk -o /dev/null -w "%{http_code}" -X POST "$WIREMOCK_URL/__admin/mappings" \
      -H "Content-Type: application/json" \
      --data-binary "@$TMP_JSON")

    rm "$TMP_JSON"

    if [ "$RESPONSE" == "201" ]; then
      echo "Mapping loaded: $file"
    else
      echo "Failed to load $file (HTTP $RESPONSE)"
    fi
  fi
done

echo "All mappings processed."
