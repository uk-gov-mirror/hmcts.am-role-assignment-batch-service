# am-role-assignment-batch-service

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=am-role-assignment-batch-service&metric=alert_status)](https://sonarcloud.io/summary/overall?id=am-role-assignment-batch-service)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=am-role-assignment-batch-service&metric=security_rating)](https://sonarcloud.io/summary/overall?id=am-role-assignment-batch-service)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=am-role-assignment-batch-service&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=am-role-assignment-batch-service)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=am-role-assignment-batch-service&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=am-role-assignment-batch-service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=am-role-assignment-batch-service&metric=coverage)](https://sonarcloud.io/summary/overall?id=am-role-assignment-batch-service)

Role Assignment Batch Service

## Purpose

Scheduled batch job for removing the expired assignment records from role-assignment-service database. 
This is spring batch application scheduled with Kubernetes and runs once in a day per cluster.

### Prerequisites

To run the project you will need to have the following installed:

* Java 21
* Docker

please ensure the following application components are already running:
* am-role-assignment-service
* am-role-assignment-database

### Running the application

To run the applicaiton quickly use the docker helper script as follows:

```
./bin/run-in-docker.sh install
```
or

```
docker-compose up
```


Alternatively, you can start the application from the current source files using Gradle as follows:

```
./gradlew clean bootRun
```

If required, to run with a low memory consumption, the following can be used:

```
./gradlew --no-daemon assemble && java -Xmx384m -jar build/libs/rd-case-worker-api.jar
```


### Running unit tests tests:

If you have some time to spare, you can run the *unit tests* as follows:

```
./gradlew test
```

### Running mutation tests tests:

If you have some time to spare, you can run the *mutation tests* as follows:

```
./gradlew pitest
```

As the project grows, these tests will take longer and longer to execute but are useful indicators of the quality of the test suite.

More information about mutation testing can be found here:
http://pitest.org/

### Contract testing with pact

Please refer to the confluence on how to run and publish PACT tests.
https://tools.hmcts.net/confluence/display/RTRD/PACT+testing
