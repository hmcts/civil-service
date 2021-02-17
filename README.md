# civil-damages-service

Civil Damages CCD Callback Service.

### Contents:
- [Building and deploying application](#building-and-deploying-the-application)
- [Pact or contract testing](#pact-or-contract-testing)

## Building and deploying the application

### Dependencies

The project is dependent on other Civil Damages repositories:
- [civil-damages-ccd-definition](https://github.com/hmcts/civil-damages-ccd-definition)
- [civil-damages-camunda-bpmn-definition](https://github.com/hmcts/civil-damages-camunda-bpmn-definition)

To set up complete local environment for Civil Damages check [civil-damages-sdk](https://github.com/hmcts/civil-damages-sdk)

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/unspec-service` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `4000` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4000/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Preview environment

Preview environment will be created when opening new PR.
CCD and Camunda BPMN definitions will be pulled from the latest GitHub releases.

To access XUI visit url (make sure that it starts with `https`, otherwise IDAM won't let you log in):
- `https://xui-civil-damages-service-pr-PR_NUMBER.service.core-compute-preview.internal`

To access Camunda visit url (login and password are both `admin`):
- `https://camunda-civil-damages-service-pr-PR_NUMBER.service.core-compute-preview.internal`

## Pact or contract testing

### Run and generate pact

You can run contract or pact tests as follows:

```
./gradlew contract
```
### Run pact broker local docker
You can then publish your pact tests locally by first running the pact docker-compose:

```
docker-compose -f docker-pactbroker-compose.yml up -d
```
### Publish pact to broker
and then using it to publish your tests:

```
./gradlew pactPublish
```
if you want to publish the pact to hmcts pact broker, please set this env variable accordingly before running the publish command.
```
export PACT_BROKER_FULL_URL=http://pact-broker.platform.hmcts.net/
./gradlew pactPublish
```

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

