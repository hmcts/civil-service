#!/usr/bin/env bash

# Builds the demo CCD definitions for a manual import: build/ccd-release-config/civil-ccd-demo.xlsx and
# civil-ga-ccd-demo.xlsx, with callbacks to demo civil-service.
#
# It builds from this checkout's ccd-definition/, so check out the commit you mean to import first.
# Moved here from civil-ccd-definition with the definitions. It refuses to run until
# ccd-definition/SOURCE_OF_TRUTH is here: before that, civil-ccd-definition owns the definitions and its
# own bin/build-definitions-demo.sh builds them.

set -eu

rootDir=$(realpath "$(dirname "${0}")/..")

if [ ! -e "${rootDir}/ccd-definition/SOURCE_OF_TRUTH" ]; then
  echo "civil-ccd-definition is still the source of truth for the CCD definitions." >&2
  echo "Run bin/build-definitions-demo.sh in civil-ccd-definition instead." >&2
  exit 1
fi

export CCD_DEF_CASE_SERVICE_BASE_URL=http://civil-service-demo.service.core-compute-demo.internal
"${rootDir}/bin/build-civil-xlsx-definitions.sh" demo
