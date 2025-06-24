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

    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$WIREMOCK_URL/__admin/mappings" \
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
