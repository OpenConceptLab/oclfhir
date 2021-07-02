#!/bin/bash
set -e

MAVEN_BIN="$MAVEN_HOME/bin/mvn"
if [[ "$MAVEN_HOME" == "" ]]; then
  echo "[INFO] MAVEN_HOME variable is unset. Trying to use 'mvn' from the system path."
  MAVEN_BIN="mvn"
fi

SOURCE_COMMIT=$(git rev-parse HEAD)
export SOURCE_COMMIT=${SOURCE_COMMIT:0:8}

./set_build_version.sh

GET_VERSION_CMD="$MAVEN_BIN -Dexec.executable='echo' -Dexec.args='\${project.version}' --non-recursive exec:exec -q"
PROJECT_VERSION=$(eval "$GET_VERSION_CMD")

TAG="$PROJECT_VERSION-$SOURCE_COMMIT"

git add .
git commit -m "Set build version"

git tag "$TAG"

git remote set-url origin ${GIT_REPO_URL}
git push origin --tags

docker pull $DOCKER_IMAGE_ID
docker tag $DOCKER_IMAGE_ID $DOCKER_IMAGE_NAME:$TAG
docker push $DOCKER_IMAGE_NAME:$TAG

if [[ "$INCREASE_MAINTENANCE_VERSION" = true ]]; then
  $MAVEN_BIN --batch-mode release:update-versions -DautoVersionSubmodules=true
  $MAVEN_BIN versions:commit
fi



