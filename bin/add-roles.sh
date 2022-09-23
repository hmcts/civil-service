#!/usr/bin/env bash

# User used during the CCD import and ccd-role creation
./bin/utils/ccd-add-role.sh "caseworker-civil"
./bin/utils/ccd-add-role.sh "caseworker-caa"
./bin/utils/ccd-add-role.sh "caseworker-approver"
./bin/utils/ccd-add-role.sh "prd-admin"
./bin/utils/ccd-add-role.sh "judge-profile"
./bin/utils/ccd-add-role.sh "basic-access" #remove after GS_profile is added.
./bin/utils/ccd-add-role.sh "GS_profile"
./bin/utils/ccd-add-role.sh "legal-adviser"
./bin/utils/ccd-add-role.sh "caseworker-ras-validation"
./bin/utils/ccd-add-role.sh "admin-access"
./bin/utils/ccd-add-role.sh "full-access"
./bin/utils/ccd-add-role.sh "civil-administrator-standard"
./bin/utils/ccd-add-role.sh "civil-administrator-basic"
./bin/utils/ccd-add-role.sh "hearing-schedule-access"

roles=("solicitor" "systemupdate" "admin" "staff")
for role in "${roles[@]}"
do
  ./bin/utils/ccd-add-role.sh "caseworker-civil-${role}"
done
