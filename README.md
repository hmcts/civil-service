# civil-service

Civil CCD Callback Service.

### Contents:

- [Building and deploying application](#building-and-deploying-the-application)
- [Pact or contract testing](#pact-or-contract-testing)

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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
