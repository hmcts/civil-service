#!/usr/bin/env bash

MAPPINGS_DIR="./mappings"
FILES_DIR="./__files"

if [ ! -d "$MAPPINGS_DIR" ]; then
  echo "Mappings folder not found: $MAPPINGS_DIR"
  exit 1
fi

echo "Loading mappings into WireMock at $WIREMOCK_URL"

for file in "$MAPPINGS_DIR"/*.json; do
  if [ -f "$file" ]; then
    echo "Posting: $file"

    # Extract bodyFileName if it exists
    BODY_FILE_NAME=$(jq -r '.response.bodyFileName // empty' "$file")
    if [[ -n "$BODY_FILE_NAME" ]]; then
      BODY_FILE_PATH="$FILES_DIR/$BODY_FILE_NAME"
      if [[ -f "$BODY_FILE_PATH" ]]; then
        echo "Inlining body from: $BODY_FILE_PATH"

        if [[ "$BODY_FILE_PATH" == *.pdf ]]; then
          echo "Inlining PDF as base64"
          BASE64_CONTENT=$(base64 -w 0 "$BODY_FILE_PATH")
          INLINE_MAPPING=$(jq --arg base64_content "$BASE64_CONTENT" '
            del(.response.bodyFileName) |
            .response.base64Body = $base64_content
          ' "$file")
        else
          echo "Inlining JSON/text body"
          BODY_CONTENT=$(<"$BODY_FILE_PATH")
          INLINE_MAPPING=$(jq --arg body "$BODY_CONTENT" '
            del(.response.bodyFileName) |
            .response.body = $body
          ' "$file")
        fi
      else
        echo "Missing body file: $BODY_FILE_PATH"
        continue
      fi
    else
      # No bodyFileName: just load the file directly
      INLINE_MAPPING=$(cat "$file")
    fi

    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$WIREMOCK_URL/__admin/mappings" \
      -H "Content-Type: application/json" \
      --data "$INLINE_MAPPING")

    if [ "$RESPONSE" == "201" ]; then
      echo "Mapping loaded: $file"
    else
      echo "Failed to load $file (HTTP $RESPONSE)"
    fi
  fi
done

echo "All mappings processed."
