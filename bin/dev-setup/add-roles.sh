#!/usr/bin/env bash

# Fetch tokens once if not already set, so ccd-add-role.sh reuses them
if [ -z "${USER_TOKEN:-}" ] || [ -z "${SERVICE_TOKEN:-}" ]; then
  . ./bin/shared/idam-get-tokens.sh
fi

# User used during the CCD import and ccd-role creation
./bin/shared/ccd-add-role.sh "caseworker-civil"
./bin/shared/ccd-add-role.sh "caseworker-caa"
./bin/shared/ccd-add-role.sh "caseworker-approver"
./bin/shared/ccd-add-role.sh "prd-admin"
./bin/shared/ccd-add-role.sh "judge-profile"
./bin/shared/ccd-add-role.sh "APP-SOL-UNSPEC-PROFILE"
./bin/shared/ccd-add-role.sh "APP-SOL-SPEC-PROFILE"
./bin/shared/ccd-add-role.sh "APPLICANT-PROFILE-SPEC"
./bin/shared/ccd-add-role.sh "RESPONDENT-ONE-PROFILE-SPEC"
./bin/shared/ccd-add-role.sh "RES-SOL-ONE-UNSPEC-PROFILE"
./bin/shared/ccd-add-role.sh "RES-SOL-ONE-SPEC-PROFILE"
./bin/shared/ccd-add-role.sh "RES-SOL-TWO-UNSPEC-PROFILE"
./bin/shared/ccd-add-role.sh "RES-SOL-TWO-SPEC-PROFILE"
./bin/shared/ccd-add-role.sh "ga-basic-access"
./bin/shared/ccd-add-role.sh "GS_profile"
./bin/shared/ccd-add-role.sh "legal-adviser"
./bin/shared/ccd-add-role.sh "caseworker-ras-validation"
./bin/shared/ccd-add-role.sh "admin-access"
./bin/shared/ccd-add-role.sh "full-access"
./bin/shared/ccd-add-role.sh "civil-administrator-standard"
./bin/shared/ccd-add-role.sh "civil-administrator-basic"
./bin/shared/ccd-add-role.sh "hearing-schedule-access"
./bin/shared/ccd-add-role.sh "payment-access"
./bin/shared/ccd-add-role.sh "caseflags-admin"
./bin/shared/ccd-add-role.sh "caseflags-viewer"
./bin/shared/ccd-add-role.sh "caseworker-wa-task-configuration"
./bin/shared/ccd-add-role.sh "CITIZEN-CLAIMANT-PROFILE"
./bin/shared/ccd-add-role.sh "CITIZEN-DEFENDANT-PROFILE"
./bin/shared/ccd-add-role.sh "cui-admin-profile"
./bin/shared/ccd-add-role.sh "cui-nbc-profile"
./bin/shared/ccd-add-role.sh "citizen-profile"
./bin/shared/ccd-add-role.sh "citizen"
./bin/shared/ccd-add-role.sh "caseworker-civil-citizen-ui-pcqextractor"
./bin/shared/ccd-add-role.sh "judge"
./bin/shared/ccd-add-role.sh "caseworker-civil-judge"
./bin/shared/ccd-add-role.sh "hearing-centre-admin"
./bin/shared/ccd-add-role.sh "national-business-centre"
./bin/shared/ccd-add-role.sh "hearing-centre-team-leader"
./bin/shared/ccd-add-role.sh "next-hearing-date-admin"
./bin/shared/ccd-add-role.sh "court-officer-order"
./bin/shared/ccd-add-role.sh "nbc-team-leader"
./bin/shared/ccd-add-role.sh "ctsc"
./bin/shared/ccd-add-role.sh "ctsc-team-leader"
./bin/shared/ccd-add-role.sh "caseworker-civil-doc-removal"
./bin/shared/ccd-add-role.sh "caseworker-civil-system-field-reader"
./bin/shared/ccd-add-role.sh "caseworker-civil-rparobot"
./bin/shared/ccd-add-role.sh "wlu-admin"

roles=("solicitor" "systemupdate" "admin" "staff")
for role in "${roles[@]}"
do
  ./bin/shared/ccd-add-role.sh "caseworker-civil-${role}"
done
