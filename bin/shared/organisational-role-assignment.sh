#!/usr/bin/env bash
## Usage: ./organisational-role-assignment.sh [username] [password] [role_classification] [role_name] [role_attributes] [role_category] [authorisations]
##
## Options:
##    - username: Email for user. Default to `ccd-import@fake.hmcts.net`.
##    - password: Password for user. Default to `London01`.
##    - role_classification: Role assignment classification. Default to `PUBLIC`.
##    - role_name: Name of the role for role-assignment. Default to `tribunal-caseworker`.
##    - role_attributes: JSON attributes for the role. Default to `{"jurisdiction":"CIVIL"}`.
##    - role_category: Category of the role. Default to `LEGAL_OPERATIONS`.
##    - authorisations: JSON array of authorisation codes for the role. Default to `[]`.
##

USERNAME=${1:-ccd-import@fake.hmcts.net}
PASSWORD=${2:-London01}
ROLE_CLASSIFICATION="${3:-PUBLIC}"
ROLE_NAME="${4:-"tribunal-caseworker"}"
ROLE_ATTRIBUTES="${5:-'{"jurisdiction":"CIVIL"}'}"
ROLE_CATEGORY="${6:-"LEGAL_OPERATIONS"}"
AUTHORISATIONS=${7:-'[]'}

# hmcts-* roles (such as hmcts-admin) are BASIC grants: the role assignment service rejects them as STANDARD
GRANT_TYPE="STANDARD"
if [[ "${ROLE_NAME}" == hmcts-* ]]; then
  GRANT_TYPE="BASIC"
fi

BASEDIR=$(dirname "$0")

# When ORG_ROLE_TOKEN_CACHE names a directory (add-org-roles-to-users.sh creates a private one for each run), a
# user's token and id and the civil_service S2S token are fetched once and reused for that user's other roles.
# Without it, every call fetches its own tokens, as before.
USER_CACHE=""
SERVICE_CACHE=""
if [ -n "${ORG_ROLE_TOKEN_CACHE:-}" ] && [ -d "${ORG_ROLE_TOKEN_CACHE}" ]; then
  USER_CACHE="${ORG_ROLE_TOKEN_CACHE}/user-${USERNAME//[^A-Za-z0-9._@-]/_}"
  SERVICE_CACHE="${ORG_ROLE_TOKEN_CACHE}/service-civil_service"
fi

is_usable() {
  [ -n "$1" ] && [ "$1" != "null" ]
}

if [ -n "${USER_CACHE}" ] && [ -s "${USER_CACHE}" ]; then
  { read -r USER_TOKEN; read -r USER_ID; } < "${USER_CACHE}"
else
  USER_TOKEN=$($BASEDIR/idam-user-token.sh $USERNAME $PASSWORD)
  USER_ID=$($BASEDIR/idam-user-id.sh $USER_TOKEN)
  if [ -n "${USER_CACHE}" ] && is_usable "${USER_TOKEN}" && is_usable "${USER_ID}"; then
    (umask 077 && printf '%s\n%s\n' "${USER_TOKEN}" "${USER_ID}" > "${USER_CACHE}")
  fi
fi

if [ -n "${SERVICE_CACHE}" ] && [ -s "${SERVICE_CACHE}" ]; then
  read -r SERVICE_TOKEN < "${SERVICE_CACHE}"
else
  SERVICE_TOKEN=$(IDAM_SERVICE_SECRET="${S2S_SECRET:-AABBCCDDEEFFGGHH}" \
    "$BASEDIR/idam-lease-service-token.sh" civil_service)
  if [ -n "${SERVICE_CACHE}" ] && is_usable "${SERVICE_TOKEN}"; then
    (umask 077 && printf '%s\n' "${SERVICE_TOKEN}" > "${SERVICE_CACHE}")
  fi
fi

echo "\n\nCreating role assignment: \n User: ${USER_ID}\n Role name: ${ROLE_NAME}\n ROLE_CLASSIFICATION: ${ROLE_CLASSIFICATION}\n"
echo "\n\nROLE ASSIGNMENT URL: \n Url: ${ROLE_ASSIGNMENT_URL}\n"

curl --silent --show-error -X POST "${ROLE_ASSIGNMENT_URL}/am/role-assignments" \
  -H "accept: application/vnd.uk.gov.hmcts.role-assignment-service.create-assignments+json;charset=UTF-8;version=1.0" \
  -H "Authorization: Bearer ${USER_TOKEN}" \
  -H "ServiceAuthorization: Bearer ${SERVICE_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{ "roleRequest": {
          "assignerId": "'"${USER_ID}"'",
          "process": "staff-organisational-role-mapping",
          "reference": "'"${USER_ID}/${ROLE_NAME}"'",
          "replaceExisting": true
        },
        "requestedRoles": [
          {
            "actorIdType": "IDAM",
            "actorId": "'"${USER_ID}"'",
            "roleType": "ORGANISATION",
            "roleName": "'"${ROLE_NAME}"'",
            "classification": "'"${ROLE_CLASSIFICATION}"'",
            "grantType": "'"${GRANT_TYPE}"'",
            "roleCategory": "'"${ROLE_CATEGORY}"'",
            "readOnly": false,
            "attributes": '${ROLE_ATTRIBUTES}',
            "authorisations": '"${AUTHORISATIONS}"'
          }
        ]
      }'
