# civil-service

Civil CCD Callback Service.

### Contents:

- [Building and deploying application](#building-and-deploying-the-application)
- [Pact or contract testing](#pact-or-contract-testing)
- [Adding Git Conventions](#adding-git-conventions)

## Building and deploying the application

### Dependencies

The project is dependent on other Civil repositories:

- [civil-ccd-definition](https://github.com/hmcts/civil-ccd-definition)
- [civil-camunda-bpmn-definition](https://github.com/hmcts/civil-camunda-bpmn-definition)

To set up complete local environment for Civil check [civil-sdk](https://github.com/hmcts/civil-sdk)

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

#### Environment variables

You will need the following environment variables setup in your bashrc/zshrc or IntelliJ run configuration. API keys can
be found in the Azure key store.

| Name | Use | Value |
| ---- | --- | ----- |
| `DOCMOSIS_TORNADO_KEY` | [Docmosis](https://www.docmosis.com/) is our document generation service. For development purposes we have been using trial keys which can be obtained [here](https://www.docmosis.com/products/tornado/try.html). **Note:** These expire after a month. | |
| `GOV_NOTIFY_API_KEY` | [GOV.UK Notify](https://www.notifications.service.gov.uk/) is our notification service for sending emails.  | |
| `LAUNCH_DARKLY_SDK_KEY` | [LaunchDarkly](https://launchdarkly.com/) is our platform for managing feature toggles. | |
| `LAUNCH_DARKLY_OFFLINE_MODE` | Sets LaunchDarkly to use local values for flags rather than connecting to the service | `true` |
| `SPRING_PROFILES_ACTIVE` | Sets the active Spring profile | `local` |

#### Running through IntelliJ

IntelliJ will create a Spring Boot run configuration for you. Which, after setting up your environment variables, can be
run from there.

They are also base64 encoded and stored in the aat-env keystore in azure, in civil-service-dot-env secret.

The loadEnvSecrets gradle task downloads them to a local .aat-env file which is read in the gradle bootRun task.

#### Create a Docker image

While not necessary for local development, you can create the image of the application by executing the following
command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/civil-service` directory) by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port (set to `4000` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4000/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Preview environment

Preview environment will be created when opening new PR. CCD and Camunda BPMN definitions will be pulled from the latest
GitHub releases.

To access XUI visit url (make sure that it starts with `https`, otherwise IDAM won't let you log in):

- `https://xui-civil-service-pr-PR_NUMBER.service.core-compute-preview.internal`

To access Camunda visit url (login and password are both `admin`):

- `https://camunda-civil-service-pr-PR_NUMBER.service.core-compute-preview.internal`


### Functional test labels for targeted testing
There are a variety of labels that can be added to PRs for the purpose of running sub-groups of functional tests, relevant to specific journeys. All functional test labels begin with the pr_ft_ prefix.

Adding a functional test label allows the user to run a greater amount of tests relevant to the journey where changes are being made, and reduces the amount of time taken to run a build.

For example, if the label pr_ft_spec-part-admit is added to a PR, the PR will run only the API tests relevant to the Spec Part Admit journey.

It is also possible to add two labels to a PR to target multiple journeys. For example, if the labels pr_ft_spec-part-admit, and pr_ft_spec-part-admit are added to a PR, the PR will run only the API tests relevant to the Spec Part Admit, and Spec Full Admit journeys.

If no functional test label has been added to a PR, the full set of regression tests (api-nonprod) will be run.

For more details about the functional test labels available, the tests that run under each label, and the guidelines for using them, please refer to this confluence page: https://tools.hmcts.net/confluence/display/ROC/GitHub+Labels+for+Testing


## Contract testing

### Generate contracts

You can generate contracts as follows:

```
./gradlew contract
```

### Publish contracts

If you want to publish the contracts to hmcts pact broker, please set this env variable accordingly before running the
publish command.

```
export PACT_BROKER_FULL_URL=http://pact-broker.platform.hmcts.net/
```

If you want to publish the RPA contract to the PactFlow pact broker, please set this env variable accordingly before
running the publish command. By setting your env variable to this, the IDAM contract will be ignored and only the RPA
contract will be published to PactFlow.

```
export PACT_BROKER_FULL_URL=https://civil-claims.pactflow.io/
```

Before running, you should set the API token to connect to the pactflow portal as follows:

```bash
export PACT_BROKER_TOKEN=<api token here>
```

The API Token can be obtained on [Confluence](https://tools.hmcts.net/confluence/display/CU/Pactflow).

To publish your contracts:

```
./gradlew pactPublish
```

* If connecting to Pactflow, please disable the HMCTS VPN.

## Adding Git Conventions

### Include the git conventions.
* Make sure your git version is at least 2.9 using the `git --version` command
* Run the following command:
```
git config --local core.hooksPath .git-config/hooks
```
Once the above is done, you will be required to follow specific conventions for your commit messages and branch names.

If you violate a convention, the git error message will report clearly the convention you should follow and provide
additional information where necessary.

*Optional:*
* Install this plugin in Chrome: https://github.com/refined-github/refined-github

  It will automatically set the title for new PRs according to the first commit message, so you won't have to change it manually.

  Note that it will also alter other behaviours in GitHub. Hopefully these will also be improvements to you.

*In case of problems*

1. Get in touch with your Technical Lead so that they can get you unblocked
2. If the rare eventuality that the above is not possible, you can disable enforcement of conventions using the following command

   `git config --local --unset core.hooksPath`

   Still, you shouldn't be doing it so make sure you get in touch with a Technical Lead soon afterwards.

## Gradle Dependency Check - Running Locally
In the local environment, as of 15/12/2023 where dependency check is mandated to use version 9 or above:
https://github.com/jeremylong/DependencyCheck?tab=readme-ov-file#900-upgrade-notice

Users will now need to generate a NVD API key for themselves in order to run some gradle dependency commands:
https://nvd.nist.gov/developers/request-an-api-key

Example
```
./gradlew -DdependencyCheck.failBuild=true -Dnvd.api.check.validforhours=24 -Dnvd.api.key=<YOUR_API_KEY_HERE> dependencyCheckAggregate
```

## CFTLib- Running Locally
```
./gradlew bootWithCCD
```
If you're seeing errors when pulling images, run the following command:

```shell
az acr login --name hmctspublic --subscription 8999dec3-0104-4a27-94ee-6588559729d1
```
If you're seeing errors after importing bpmn files then run it again.
XUI will be running on http://localhost:3000/

After creating a case in XUI, complete the payment using service request.
To update the payment success callback on a created case, please use [[civil-operation]](https://github.com/hmcts/civil-operations/)
http://rpe-service-auth-provider-aat.service.core-compute-aat.internal/testing-support/lease to generate token
then use http://localhost:4000/service-request-update-claim-issued endpoint with above token and then
update body with case id and payment reference no

## Development / Debugging Environment - Preview with Mirrord

As an alternative for a development environment there is a procedure in place where after running the command
below the required services for Civil are created in Preview under the developer's name, so these will be exclusively
for the named developer use.

While connected to the VPN simply run one of the below commands from your project's (civil-service) folder:
Note: be sure to have Docker running
```shell
npx @hmcts/dev-env@latest && ./bin/setup-devuser-preview-env.sh
```
You can optionally specify a branch for CCD definitions and Camunda definitions like below or leave it blank to use master.

```shell
npx @hmcts/dev-env@latest && ./bin/setup-devuser-preview-env.sh ccdBranchName camundaBranchName
```
If you want to clean up the environment just run:

```shell
npx @hmcts/dev-env@latest --delete
```

Once the pods are up and running you can connect to them using a plugin called Mirrord on Intellij.
https://mirrord.dev

Most times, by just enabling the Mirrord plugin and running the application in debug mode a popup will come for you to select the target pod running civil-service.
In some setups you will need a mirrord config file specifying the pod as below.

The script should create a /.mirrord/.mirrord.json config file under the project's root directory.

Specifically for civil-service you might need to exclude a flyway environment variable to allow the startup process to run without errors.
For that you should have such file as this:

```json
{
  "feature": {
    "network": {
      "incoming": "steal",
      "outgoing": true
    },
    "fs": "read",
    "env": {
      "override": {
        "REFERENCE_DATABASE_MIGRATION": "false"
      }
    }
  },
  "target": {
    "path": {
      "pod": "Your civil-service pod ID"
    },
    "namespace": "civil"
  },
  "operator": false,
  "agent": {
    "flush_connections": false
  }
}
```
## Point CCD definitions to a specific branch

Add the following label to your GitHub PR.

```
civilDefinitionBranch:????

where ???? is the branch name you want to point to. e.g civilDefinitionBranch:DTSCCI-1699
```


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
