ccdBranch=${1:-master}
camundaBranch=${2:-master}

echo "export ENVIRONMENT=devuser-preview"
echo "Loading Environment Variables"
source ./bin/variables/load-dev-user-preview-environment-variables.sh
echo "Importing Roles to the CCD pod"
./bin/add-roles.sh
echo "Importing Camunda definitions"
./bin/pull-latest-camunda-files.sh ${camundaBranch}
echo "Importing CCD definitions"
./bin/pull-latest-ccd-files.sh ${ccdBranch}
./bin/import-ccd-definition.sh "-e *-prod.json,*HNL-nonprod.json,AuthorisationCaseType-shuttered.json"

rm -rf $(pwd)/ccd-definition
rm -rf $(pwd)/build/ccd-development-config
rm -rf $(pwd)/camunda

echo "ENV variables set for devuser-preview environment."
echo "XUI_URL: $XUI_WEBAPP_URL"
