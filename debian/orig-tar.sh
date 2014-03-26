#!/bin/sh -e

VERSION=$2
TAR=../bsaf_$VERSION.orig.tar.xz
DIR=bsaf-$VERSION
TAG=bsaf-$VERSION

svn export https://svn.kenai.com/svn/bsaf~main/framework/tags/$TAG $DIR
tar -c -J -f $TAR --exclude '*.jar' $DIR
rm -rf $DIR ../$TAG $3

# move to directory 'tarballs'
if [ -r .svn/deb-layout ]; then
  . .svn/deb-layout
  mv $TAR $origDir
  echo "moved $TAR to $origDir"
fi
