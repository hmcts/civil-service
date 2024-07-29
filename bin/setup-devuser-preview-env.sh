camundaBranch=${1:-master}
ccdBranch=${2:-master}

echo "export ENVIRONMENT=devuser-preview"
source ./bin/variables/load-dev-user-preview-environment-variables.sh
./bin/add-roles.sh
#./bin/pull-latest-camunda-files.sh ${camundaBranch}
#./bin/pull-latest-ccd-files.sh ${ccdBranch}
#./bin/import-ccd-definition.sh "-e *-prod.json,*HNL-nonprod.json,AuthorisationCaseType-shuttered.json"

echo "ENV variables set for devuser-preview environment."
echo "CDAM_REDIRECT_URL: $CCD_IDAM_REDIRECT_URL"
