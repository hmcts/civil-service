#!/usr/bin/env bash
#
# Copies the Civil CCD definitions (civil/ and generalapplication/) from whichever repo is the source of
# truth for them.
#
# The toggle is the file ccd-definition/SOURCE_OF_TRUTH in civil-service, at the ref given:
#   - present: civil-service is the source of truth, and the definitions come from that ref.
#   - absent:  civil-ccd-definition is, and the definitions come from its branch, as before the move.
#
# Usage: pull-ccd-definitions.sh <civil-service ref> [civil-ccd-definition ref] [target dir]
#        pull-ccd-definitions.sh --check [civil-service ref]
#   civil-service ref:         branch, tag, full commit SHA, or pr-N (or prN) for the head of pull
#                              request N. Default master.
#   civil-ccd-definition ref:  same forms. Default master. Pass - to leave the target as it is when
#                              civil-ccd-definition is the source (for civil-ccd-definition's own pipeline,
#                              whose checkout already holds the definitions it is building).
#   target dir:                default ./ccd-definition. Only its civil/ and generalapplication/ folders are
#                              replaced; anything else in it is left alone.
#   --check:                   copy nothing. Print only the source of truth at the civil-service ref,
#                              civil-service or civil-ccd-definition, on stdout. civil-ccd-definition's
#                              highLevelDataSetup uses it to decide whether to import, so the import and
#                              the copy can't disagree.
#
# Prints the source it used. Any clone or fetch failure is an error. It never falls back to the other
# repo, so a network failure can't quietly select the wrong source.
#
# Other repos fetch this one file from civil-service master and run it, so it must not depend on
# anything else in this repo.

set -euo pipefail

checkOnly=false
if [ "${1:-}" = "--check" ]; then
  checkOnly=true
  shift
fi

civilServiceRef=${1:-master}
ccdDefinitionRef=${2:-master}
targetDir=${3:-./ccd-definition}

# The same path is checked in bin/pull-latest-civil-ccd-files.sh and
# bin/dev-setup/start-devuser-preview-environment.sh. Change all three together.
toggleFile="ccd-definition/SOURCE_OF_TRUTH"
definitionDirs=(civil generalapplication)

workDir=$(mktemp -d)
trap 'rm -rf "${workDir}"' EXIT

# Fetches one commit of a GitHub repo into a new, blob-less repository at $3, so that checking the
# toggle downloads no file contents. Accepts pr-N or prN for the head of pull request N, as the
# camunda and shared-script pulls in civil-ccd-definition and civil-citizen-ui do.
fetch_ref() {
  local repo=$1 ref=$2 dir=$3 refspec
  if [[ "${ref}" =~ ^pr-?([0-9]+)$ ]]; then
    refspec="refs/pull/${BASH_REMATCH[1]}/head"
  else
    refspec="${ref}"
  fi
  git init -q "${dir}"
  git -C "${dir}" remote add origin "https://github.com/hmcts/${repo}.git"
  if ! git -C "${dir}" fetch -q --depth 1 --filter=blob:none origin "${refspec}"; then
    echo "ERROR: could not fetch ${ref} from hmcts/${repo}." >&2
    exit 1
  fi
}

# Replaces civil/ and generalapplication/ in the target with the ones from the fetched commit.
copy_definitions() {
  local dir=$1 label=$2 name
  for name in "${definitionDirs[@]}"; do
    if [ -z "$(git -C "${dir}" ls-tree --name-only FETCH_HEAD "ccd-definition/${name}")" ]; then
      echo "ERROR: ${label} has no ccd-definition/${name}." >&2
      exit 1
    fi
  done
  # A sparse checkout downloads the missing file contents in one batch. Checking out paths directly from
  # the blob-less commit fetches them one at a time, which takes minutes.
  git -C "${dir}" sparse-checkout set --no-cone "${definitionDirs[@]/#/ccd-definition/}"
  git -C "${dir}" checkout -q FETCH_HEAD
  mkdir -p "${targetDir}"
  for name in "${definitionDirs[@]}"; do
    rm -rf "${targetDir:?}/${name}"
    cp -R "${dir}/ccd-definition/${name}" "${targetDir}/${name}"
  done
}

fetch_ref civil-service "${civilServiceRef}" "${workDir}/civil-service"

if [ -n "$(git -C "${workDir}/civil-service" ls-tree --name-only FETCH_HEAD "${toggleFile}")" ]; then
  sourceOfTruth=civil-service
else
  sourceOfTruth=civil-ccd-definition
fi

if [ "${checkOnly}" = true ]; then
  echo "${sourceOfTruth}"
elif [ "${sourceOfTruth}" = civil-service ]; then
  echo "CCD definitions: source of truth is civil-service (${toggleFile} is present at ${civilServiceRef})."
  copy_definitions "${workDir}/civil-service" "civil-service@${civilServiceRef}"
  echo "CCD definitions: copied from civil-service@${civilServiceRef} into ${targetDir}."
elif [ "${ccdDefinitionRef}" = "-" ]; then
  echo "CCD definitions: source of truth is civil-ccd-definition (no ${toggleFile} at civil-service@${civilServiceRef})."
  echo "CCD definitions: leaving ${targetDir} as it is."
else
  echo "CCD definitions: source of truth is civil-ccd-definition (no ${toggleFile} at civil-service@${civilServiceRef})."
  fetch_ref civil-ccd-definition "${ccdDefinitionRef}" "${workDir}/civil-ccd-definition"
  copy_definitions "${workDir}/civil-ccd-definition" "civil-ccd-definition@${ccdDefinitionRef}"
  echo "CCD definitions: copied from civil-ccd-definition@${ccdDefinitionRef} into ${targetDir}."
fi
