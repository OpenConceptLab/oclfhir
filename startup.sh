#!/bin/bash

set -e

./wait_for_it.sh ${OCLAPI_HOST}:${OCLAPI_PORT} -t 0

java -Dhibernate.types.print.banner=false -jar ocl-fhir-ts/target/oclfhir.jar
