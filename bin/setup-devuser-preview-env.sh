ccdBranch=${1:-master}
camundaBranch=${2:-master}

echo "export ENVIRONMENT=devuser-preview"
echo "Loading Environment Variables"
source ./bin/variables/load-dev-user-preview-environment-variables.sh

echo "Importing Roles to the CCD pod"
./bin/add-roles.sh

echo "Preparing CCD definitions"
./bin/pull-latest-civil-ccd-files.sh ${ccdBranch}
./bin/build-release-ccd-definition.sh preview

echo "Running high-level CCD data setup"
./gradlew --no-daemon highLevelDataSetup

echo "Importing Camunda definitions"
./bin/pull-latest-camunda-files.sh ${camundaBranch}

rm -rf $(pwd)/ccd-definition
rm -rf $(pwd)/build/ccd-release-config
rm -rf $(pwd)/camunda

echo "ENV variables set for devuser-preview environment."
echo "XUI_URL: $XUI_WEBAPP_URL"
