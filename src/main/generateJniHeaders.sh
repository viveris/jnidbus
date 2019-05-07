#!/usr/bin/env bash

JAVAH="/usr/lib/jvm/java-7-openjdk/bin/javah"

##Find every file in the folder and every subfolder with a limit of 10 subfolder
rm -rf ./h
echo "* generating headers for all classes"
for entry in $(find ./java -maxdepth 10 -type f)
do
    ##trim ".java"
    FILE="${entry%.java}"
    FILE="${FILE#./java/}"
    FILE=${FILE//[\/]/.}
    echo "  - processing class $FILE"
    $JAVAH -d ./h -jni -classpath ./java $FILE
done

echo "* filtering empty headers"
find ./h/* -type f -print0 | xargs --null grep -Z -L 'JNIEXPORT' | xargs --null rm