#!/bin/bash
## Usage: ./idam-user-token.sh [user] [password]
##
## Options:
##    - username: Role assigned to user in generated token. Default to `ccd-import@fake.hmcts.net`.
##    - password: ID assigned to user in generated token. Default to `London01`
##

USERNAME=${1:-ccd-import@fake.hmcts.net}
PASSWORD=${2:-London01}
REDIRECT_URI="https://manage-case.aat.platform.hmcts.net/oauth2/callback"
CLIENT_ID="xuiwebapp"
CLIENT_SECRET=${IDAM_CLIENT_SECRET}
SCOPE="profile%20openid%20roles%20manage-user%20create-user%20search-user"

echo "debugging IDAM_API_URL- ${IDAM_API_URL}"
echo "debugging REDIRECT_URI- ${REDIRECT_URI}"
echo "debugging CLIENT_ID- ${CLIENT_ID}"
echo "debugging CLIENT_SECRET- ${CLIENT_SECRET}"
echo "debugging USERNAME- ${USERNAME}"
echo "debugging SCOPE- ${SCOPE}"
echo "debugging IDAM_CLIENT_SECRET- ${IDAM_CLIENT_SECRET}"


curl --silent -v --show-error \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -X POST "${IDAM_API_URL}/o/token?grant_type=password&redirect_uri=${REDIRECT_URI}&client_id=${CLIENT_ID}&client_secret=${CLIENT_SECRET}&username=${USERNAME}&password=${PASSWORD}&scope=${SCOPE}" -d "" | jq -r .access_token
