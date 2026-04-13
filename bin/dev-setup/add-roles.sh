#!/usr/bin/env bash

# Fetch tokens once if not already set, so ccd-add-role.sh reuses them
if [ -z "${USER_TOKEN:-}" ] || [ -z "${SERVICE_TOKEN:-}" ]; then
  . ./bin/dev-setup/idam-get-tokens.sh
fi

# User used during the CCD import and ccd-role creation
./bin/dev-setup/ccd-add-role.sh "caseworker-civil"
./bin/dev-setup/ccd-add-role.sh "caseworker-caa"
./bin/dev-setup/ccd-add-role.sh "caseworker-approver"
./bin/dev-setup/ccd-add-role.sh "prd-admin"
./bin/dev-setup/ccd-add-role.sh "judge-profile"
./bin/dev-setup/ccd-add-role.sh "APP-SOL-UNSPEC-PROFILE"
./bin/dev-setup/ccd-add-role.sh "APP-SOL-SPEC-PROFILE"
./bin/dev-setup/ccd-add-role.sh "APPLICANT-PROFILE-SPEC"
./bin/dev-setup/ccd-add-role.sh "RESPONDENT-ONE-PROFILE-SPEC"
./bin/dev-setup/ccd-add-role.sh "RES-SOL-ONE-UNSPEC-PROFILE"
./bin/dev-setup/ccd-add-role.sh "RES-SOL-ONE-SPEC-PROFILE"
./bin/dev-setup/ccd-add-role.sh "RES-SOL-TWO-UNSPEC-PROFILE"
./bin/dev-setup/ccd-add-role.sh "RES-SOL-TWO-SPEC-PROFILE"
./bin/dev-setup/ccd-add-role.sh "ga-basic-access"
./bin/dev-setup/ccd-add-role.sh "GS_profile"
./bin/dev-setup/ccd-add-role.sh "legal-adviser"
./bin/dev-setup/ccd-add-role.sh "caseworker-ras-validation"
./bin/dev-setup/ccd-add-role.sh "admin-access"
./bin/dev-setup/ccd-add-role.sh "full-access"
./bin/dev-setup/ccd-add-role.sh "civil-administrator-standard"
./bin/dev-setup/ccd-add-role.sh "civil-administrator-basic"
./bin/dev-setup/ccd-add-role.sh "hearing-schedule-access"
./bin/dev-setup/ccd-add-role.sh "payment-access"
./bin/dev-setup/ccd-add-role.sh "caseflags-admin"
./bin/dev-setup/ccd-add-role.sh "caseflags-viewer"
./bin/dev-setup/ccd-add-role.sh "caseworker-wa-task-configuration"
./bin/dev-setup/ccd-add-role.sh "CITIZEN-CLAIMANT-PROFILE"
./bin/dev-setup/ccd-add-role.sh "CITIZEN-DEFENDANT-PROFILE"
./bin/dev-setup/ccd-add-role.sh "cui-admin-profile"
./bin/dev-setup/ccd-add-role.sh "cui-nbc-profile"
./bin/dev-setup/ccd-add-role.sh "citizen-profile"
./bin/dev-setup/ccd-add-role.sh "citizen"
./bin/dev-setup/ccd-add-role.sh "caseworker-civil-citizen-ui-pcqextractor"
./bin/dev-setup/ccd-add-role.sh "judge"
./bin/dev-setup/ccd-add-role.sh "caseworker-civil-judge"
./bin/dev-setup/ccd-add-role.sh "hearing-centre-admin"
./bin/dev-setup/ccd-add-role.sh "national-business-centre"
./bin/dev-setup/ccd-add-role.sh "hearing-centre-team-leader"
./bin/dev-setup/ccd-add-role.sh "next-hearing-date-admin"
./bin/dev-setup/ccd-add-role.sh "court-officer-order"
./bin/dev-setup/ccd-add-role.sh "nbc-team-leader"
./bin/dev-setup/ccd-add-role.sh "ctsc"
./bin/dev-setup/ccd-add-role.sh "ctsc-team-leader"
./bin/dev-setup/ccd-add-role.sh "caseworker-civil-doc-removal"
./bin/dev-setup/ccd-add-role.sh "caseworker-civil-system-field-reader"
./bin/dev-setup/ccd-add-role.sh "caseworker-civil-rparobot"
./bin/dev-setup/ccd-add-role.sh "wlu-admin"

roles=("solicitor" "systemupdate" "admin" "staff")
for role in "${roles[@]}"
do
  ./bin/dev-setup/ccd-add-role.sh "caseworker-civil-${role}"
done
