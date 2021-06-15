#!/usr/bin/env bash

repoName=$1
assetName=$2

retries=0
while [ -z "$latestRelease" ]
do
  latestRelease=$(curl https://api.github.com/repos/hmcts/${repoName}/releases/latest)
  retries=$((retries+1))

  if [ "$retries" -eq 5 ]
  then
      echo "Unable to get latest release from GitHub API"
      exit 1
  fi
done

latestAssetId=$(echo "$latestRelease" | docker run --rm --interactive stedolan/jq ".assets[] | select(.name==\"${assetName}\") | .id")

curl -L \
  -H "Accept: application/octet-stream" \
  --output $assetName \
  https://api.github.com/repos/hmcts/${repoName}/releases/assets/${latestAssetId} \

unzip $assetName
rm $assetName
