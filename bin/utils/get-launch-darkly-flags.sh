#!/usr/bin/env bash
#this script gets the latest status for launchdarkly flags and shows where it has saved them, see application-dev.yml

filename="$(dirname ${0})/launchdarkly-flags.json"

curl -H "Authorization: ${LAUNCH_DARKLY_SDK_KEY}" https://app.launchdarkly.com/sdk/latest-all | node -e \
 "\
 s=process.openStdin();\
 d=[];\
 s.on('data',function(c){\
   d.push(c);\
 });\
 s.on('end',function(){\
   console.log(JSON.stringify(JSON.parse(d.join('')),null,2));\
 });\
 " > ${filename}

echo "The last launch darkly flags have been saved in ${filename}"
