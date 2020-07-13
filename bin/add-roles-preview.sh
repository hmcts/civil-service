#!/usr/bin/env bash

# User used during the CCD import and ccd-role creation
./civil-unspecified-docker/bin/utils/ccd-add-role.sh "caseworker-civil"

roles=("solicitor")
for role in "${roles[@]}"
do
  ./civil-unspecified-docker/bin/utils/ccd-add-role.sh "caseworker-civil-${role}"
done
