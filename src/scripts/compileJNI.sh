#!/usr/bin/env bash

JAVAH="/usr/lib/jvm/java-7-openjdk/bin/javah"

echo "* going into source folder"
cd ./src/main

##Find every file in the folder and every subfolder with a limit of 10 subfolder, do it asynchronously
rm -rf ./h
echo "* generating headers for all classes"

for entry in $(find ./java/fr/viveris/vizada/jnidbus/bindings -maxdepth 10 -type f)
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

echo "* moving files in jni folder"
mv ./h/*.h ./jni/src/headers/
rmdir ./h

echo "* generating Makefile"
cd jni
cmake .

echo "* compiling sources"
make

echo "* copying native library in resources"
cp ./libjnidbus.so ../resources/libjnidbus.so