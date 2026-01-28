#!/bin/bash
##############################################################################
## Gradle start up script for POSIX
##############################################################################

APP_HOME=$( cd "${0%/*}" && pwd -P ) || exit
APP_BASE_NAME=${0##*/}

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use
if [ -n "$JAVA_HOME" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
else
    JAVACMD=java
fi

# Collect all arguments
set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        -classpath "$CLASSPATH" \
        org.gradle.wrapper.GradleWrapperMain \
        "$@"

# Execute Gradle
exec "$JAVACMD" "$@"
