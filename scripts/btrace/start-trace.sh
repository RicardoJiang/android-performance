#!/bin/zsh
set -e
BASE_DIR=$(cd $(dirname $0); pwd)
PROJECT_DIR=$(cd $BASE_DIR/../..; pwd)
PYTHON2=$(which python2)

echo $BASE_DIR
echo $PROJECT_DIR
echo $PYTHON2

if [ -z "$PYTHON2" ]
then
  echo "python2 is required for btrace."
  exit -1
fi

if [ -z "$1" ]
then
  DURATION=5
else
  DURATION=$1
fi

if [ -z "$2" ]
then
  DATE=$(date +"%Y%m%d%H%M%S")
  OUTPUT=$PROJECT_DIR/build/btrace/$DATE.html
else
  OUTPUT=$2
fi

echo "Trace duration is ${DURATION}s, the output file will be saved to $OUTPUT."

java -jar $BASE_DIR/rhea-trace-shell.jar -a com.zj.android.performance -t $DURATION -o $OUTPUT rhea.all sched -fullClassName -mainThreadOnly -m $PROJECT_DIR/app/build/generated/rhea_assets/methodMapping.txt
open $(dirname $OUTPUT)

