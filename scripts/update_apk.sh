#!/bin/bash

set -e

if [ "${TRAVIS_PULL_REQUEST_BRANCH}" == "" ]; then
    echo "We only work with pull request";
    exit 0;
fi

git config --global user.name "Travis CI"
git config --global user.email "noreply+travis@robotutor.org"

export DEPLOY_BRANCH=${DEPLOY_BRANCH:-development}
export PUBLISH_BRANCH=${PUBLISH_BRANCH:-master}
DATE_TODAY=$(date +%Y-%m-%d)

echo $TRAVIS_REPO_SLUG, $TRAVIS_PULL_REQUEST;

release_apk_build () {
    echo "Building release apk";
    ./gradlew bundlePlayStoreRelease;
}

debug_apk_build () {
    echo "Building debug apk";
    ./gradlew assembleDebug;
}

# yes | sdkmanager --licenses
if [ "${TRAVIS_BRANCH}" == "${PUBLISH_BRANCH}" ]; then
    release_apk_build
else
    debug_apk_build
fi



git clone --quiet --branch=apk https://robotutor:$GH_TOKEN@github.com/RoboTutorLLC/RoboTutor_2020 apk > /dev/null
cd apk

echo `ls`
find ../app/build/outputs/apk/debug -type f -name '*.apk' -exec mv -v {} temp.apk \;

#APK Version extraction

# finding the exact line in the gradle file
ORIGINAL_STRING=$(cat ../../../../../build.gradle | grep -E '\d\.\d\.\d\.\d')
echo ORIGINAL_STRING
# extracting the exact parts but with " around
TEMP_STRING=$(echo $ORIGINAL_STRING | grep -Eo '"(.*)"')
echo TEMP_STRING
# the exact numbering scheme
FINAL_VERSION=$(echo $TEMP_STRING | sed 's/"//g') # 3.5.0.1
echo FINAL_VERSION

major=0
minor=0
build=0
assets=0

regex="([0-9]+).([0-9]+).([0-9]+).([0-9]+)"
if [[ $FINAL_VERSION =~ $regex ]]; then
  major="${BASH_REMATCH[1]}"
  minor="${BASH_REMATCH[2]}"
  build="${BASH_REMATCH[3]}"
  assets="${BASH_REMATCH[4]}"
fi

mv temp.apk RoboTutor-${TRAVIS_PULL_REQUEST_BRANCH}-${DATE_TODAY}-v${major}.${minor}.${build}.${assets}.apk

ls
echo `ls -al`
git status
echo $(git status)
# Create a new branch that will contains only latest apk
# git checkout --orphan temporary


# Add generated APK
git add .
git commit -am " ${TRAVIS_BRANCH} : ($(git rev-parse --short HEAD)) : ($(date +%Y-%m-%d.%H:%M:%S))"

# Delete current apk branch
# git branch -D apk
# Rename current branch to apk
# git branch -m apk

# Force push to origin since histories are unrelated
git push origin apk > /dev/null

# Publish App to Play Store
# if [ "$TRAVIS_BRANCH" != "$PUBLISH_BRANCH" ]; then
#     echo "We publish apk only for changes in master branch. So, let's skip this shall we ? :)"
#     exit 0
# fi

# cd ..
# gem install fastlane
# fastlane supply --aab ./apk/eventyay-organizer-master-app-playStore-release.aab --skip_upload_apk true --track alpha --json_key ./scripts/fastlane.json --package_name $PACKAGE_NAME
