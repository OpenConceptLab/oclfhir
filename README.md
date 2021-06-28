# oclfhir
FHIR Terminology Service for Open Concept Lab

## versions
FHIR        v4.0.1

HAPI FHIR   v5.0.0

Java        OpenJDK 14.0.2

## Building

In order to build run: `docker-compose build`

## Running

Before running make sure that oclapi2 is running. Check the oclapi2 network by running: `docker network list`

If it is different than `oclapi2_default`, please prepend network variable `OCLAPI_NETWORK=oclapi` to the following run commands.

In order to startup the FHIR service in development mode run: `SERVER_PORT=9000 docker-compose up`

To run in production run: `SERVER_PORT=9000 docker-compose -f docker-compose.yml up`

### Release

Every build is a candidate for release.

In order to release please trigger the release build step in [our CI](https://ci.openmrs.org/browse/OCL-OF/latest). Please note
that the maintenance version will be automatically increased after a successful release. It is desired only, if you are releasing the latest build and
should be turned off by setting the increaseMaintenanceRelease variable to false on the Run stage "Release" popup in other cases.

You also need to create a deployment release [here](https://ci.openmrs.org/deploy/createDeploymentVersion.action?deploymentProjectId=205619205).
Please make sure the release version matches the version defined in core/__init__.py (except the extended GIT SHA in the release version).

### Deployment

In order to deploy please trigger the deployment [here](https://ci.openmrs.org/deploy/viewDeploymentProjectEnvironments.action?id=205619205).
Please use an existing deployment release.


