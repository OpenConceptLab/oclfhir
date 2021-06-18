#!/bin/bash
set -e

SOURCE_COMMIT=$(git rev-parse HEAD)
export SOURCE_COMMIT=${SOURCE_COMMIT:0:8}

./set_build_version.sh

PROJECT_VERSION=$(mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)

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
  mvn --batch-mode release:update-versions -DautoVersionSubmodules=true
  mvn versions:commit
fi



