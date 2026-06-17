#!/usr/bin/env bash
set -eo pipefail

# Centralized CCD role creation script.
# This is the single source of truth for all civil CCD roles.
# Consumer repos pull this via bin/pull-latest-civil-shared.sh.

dir=$(dirname ${0})

if [ -z "${USER_TOKEN:-}" ] || [ -z "${SERVICE_TOKEN:-}" ]; then
  . ${dir}/idam-get-tokens.sh
fi

${dir}/ccd-add-role.sh "caseworker-civil"
${dir}/ccd-add-role.sh "caseworker-caa"
${dir}/ccd-add-role.sh "caseworker-approver"
${dir}/ccd-add-role.sh "prd-admin"
${dir}/ccd-add-role.sh "judge-profile"
${dir}/ccd-add-role.sh "APP-SOL-UNSPEC-PROFILE"
${dir}/ccd-add-role.sh "APP-SOL-SPEC-PROFILE"
${dir}/ccd-add-role.sh "APPLICANT-PROFILE-SPEC"
${dir}/ccd-add-role.sh "RESPONDENT-ONE-PROFILE-SPEC"
${dir}/ccd-add-role.sh "RES-SOL-ONE-UNSPEC-PROFILE"
${dir}/ccd-add-role.sh "RES-SOL-ONE-SPEC-PROFILE"
${dir}/ccd-add-role.sh "RES-SOL-TWO-UNSPEC-PROFILE"
${dir}/ccd-add-role.sh "RES-SOL-TWO-SPEC-PROFILE"
${dir}/ccd-add-role.sh "basic-access"
${dir}/ccd-add-role.sh "ga-basic-access"
${dir}/ccd-add-role.sh "GS_profile"
${dir}/ccd-add-role.sh "legal-adviser"
${dir}/ccd-add-role.sh "caseworker-ras-validation"
${dir}/ccd-add-role.sh "admin-access"
${dir}/ccd-add-role.sh "full-access"
${dir}/ccd-add-role.sh "civil-administrator-standard"
${dir}/ccd-add-role.sh "civil-administrator-basic"
${dir}/ccd-add-role.sh "hearing-schedule-access"
${dir}/ccd-add-role.sh "payment-access"
${dir}/ccd-add-role.sh "caseflags-admin"
${dir}/ccd-add-role.sh "caseflags-viewer"
${dir}/ccd-add-role.sh "caseworker-wa-task-configuration"
${dir}/ccd-add-role.sh "CITIZEN-CLAIMANT-PROFILE"
${dir}/ccd-add-role.sh "CITIZEN-DEFENDANT-PROFILE"
${dir}/ccd-add-role.sh "cui-admin-profile"
${dir}/ccd-add-role.sh "cui-nbc-profile"
${dir}/ccd-add-role.sh "citizen-profile"
${dir}/ccd-add-role.sh "citizen"
${dir}/ccd-add-role.sh "caseworker-civil-citizen-ui-pcqextractor"
${dir}/ccd-add-role.sh "judge"
${dir}/ccd-add-role.sh "caseworker-civil-judge"
${dir}/ccd-add-role.sh "hearing-centre-admin"
${dir}/ccd-add-role.sh "national-business-centre"
${dir}/ccd-add-role.sh "hearing-centre-team-leader"
${dir}/ccd-add-role.sh "next-hearing-date-admin"
${dir}/ccd-add-role.sh "court-officer-order"
${dir}/ccd-add-role.sh "nbc-team-leader"
${dir}/ccd-add-role.sh "ctsc"
${dir}/ccd-add-role.sh "ctsc-team-leader"
${dir}/ccd-add-role.sh "tribunal-caseworker"
${dir}/ccd-add-role.sh "senior-tribunal-caseworker"
${dir}/ccd-add-role.sh "caseworker-civil-doc-removal"
${dir}/ccd-add-role.sh "caseworker-civil-system-field-reader"
${dir}/ccd-add-role.sh "caseworker-civil-rparobot"
${dir}/ccd-add-role.sh "wlu-admin"

roles=("solicitor" "systemupdate" "admin" "staff")
for role in "${roles[@]}"
do
  ${dir}/ccd-add-role.sh "caseworker-civil-${role}"
done
