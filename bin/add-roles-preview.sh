#!/usr/bin/env bash

# User used during the CCD import and ccd-role creation
./civil-unspecified-docker/bin/utils/ccd-add-role.sh "caseworker-civil"
./civil-unspecified-docker/bin/utils/ccd-add-role.sh "caseworker-caa"
./civil-unspecified-docker/bin/utils/ccd-add-role.sh "caseworker-approver"

roles=("solicitor" "systemupdate")
for role in "${roles[@]}"
do
  ./civil-unspecified-docker/bin/utils/ccd-add-role.sh "caseworker-civil-${role}"
done
