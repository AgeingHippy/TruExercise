#!/bin/bash

VERSION=""

# get parameters
while getopts v: flag
do
  case "${flag}" in
    v) VERSION=${OPTARG};;
  esac
done

# get highest tag number, and add v0.1.0 if doesn't exist
git fetch --prune --unshallow 2>/dev/null
CURRENT_VERSION=`git describe --abbrev=0 --tags 2>/dev/null`

if [[ $CURRENT_VERSION == '' ]]
then
  CURRENT_VERSION='v0.1.0'
fi
echo "Current Version: $CURRENT_VERSION"

#verify tag format is as expected
if [[ $CURRENT_VERSION =~ ^v([0-9]+\.[0-9]+\.[0-9]+)$ ]]
then
  # replace . with space so can split into an array and only work on numeric part
  CURRENT_VERSION_PARTS=(${BASH_REMATCH[1]//./ })
  # get number parts
  VNUM1=${CURRENT_VERSION_PARTS[0]}
  VNUM2=${CURRENT_VERSION_PARTS[1]}
  VNUM3=${CURRENT_VERSION_PARTS[2]}
else
  echo "Invalid tag format $CURRENT_VERSION expecting '^v([0-9]+\.[0-9]+\.[0-9]+)$'"
  exit 1
fi

if [[ $VERSION == 'major' ]]
then
  VNUM1=$((VNUM1+1))
  VNUM2=0
  VNUM3=0
elif [[ $VERSION == 'minor' ]]
then
  VNUM2=$((VNUM2+1))
  VNUM3=0
elif [[ $VERSION == 'patch' ]]
then
  VNUM3=$((VNUM3+1))
else
  echo "No version type (https://semver.org/) or incorrect type specified, try: -v [major, minor, patch]"
  exit 1
fi

# create new tag
NEW_TAG="v$VNUM1.$VNUM2.$VNUM3"
echo "($VERSION) updating $CURRENT_VERSION to $NEW_TAG"

# get current hash and see if it already has a tag
GIT_COMMIT=`git rev-parse HEAD`
NEEDS_TAG=`git describe --contains $GIT_COMMIT 2>/dev/null`

# only tag if no tag already
if [ -z "$NEEDS_TAG" ]; then
  echo "Tagged with $NEW_TAG"
  git tag $NEW_TAG
  git push --tags
  git push
else
  echo "Already a tag on this commit. Returning existing tag. $CURRENT_VERSION"
  NEW_TAG=$CURRENT_VERSION
fi

echo ::set-output name=git-tag::$NEW_TAG

exit 0