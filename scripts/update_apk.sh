#!/bin/bash

set -e

if [ "${TRAVIS_PULL_REQUEST_BRANCH}" == "false" ]; then
    echo "We only work with pull request";
    exit 0;
fi

git config --global user.name "Travis CI"
git config --global user.email "noreply+travis@robotutor.org"

export DEPLOY_BRANCH=${DEPLOY_BRANCH:-development}
export PUBLISH_BRANCH=${PUBLISH_BRANCH:-master}
DATE_TODAY=$(date +%Y-%m-%d)

echo $TRAVIS_REPO_SLUG, $TRAVIS_PULL_REQUEST;
# if [ "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_REPO_SLUG" != "RoboTutorLLC/RoboTutor_2020" ] ; then
#     echo "We upload apk only for changes in development or master, and not PRs. So, let's skip this shall we ? :)"
#     exit 0
# fi


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


mv temp.apk RoboTutor-${TRAVIS_PULL_REQUEST_BRANCH}-${DATE_TODAY}.apk

ls
echo `ls -al`
git status
echo $(git status)
# Create a new branch that will contains only latest apk
# git checkout --orphan temporary


# Add generated APK
git add .
git commit -am "[Auto] Update Test Apk ($(date +%Y-%m-%d.%H:%M:%S))"

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

