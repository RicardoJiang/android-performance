#!/bin/zsh
set -e

BASE_DIR=$(cd $(dirname $0); pwd)
PROJECT_DIR=$(cd $BASE_DIR/../..; pwd)

cd $PROJECT_DIR
./gradlew -PisBtraceEnabled=true :app:assembleDebug
adb install -t -r $PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk