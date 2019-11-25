#!/bin/bash
JOB=$1

CURRENTYEAR=`date +"%Y"`
CURRENTMONTH=`date +"%m"`
CURRENTDATE=`date +"%d"`
CURRENTEPOCTIME=`date +"%s"`
EXECUTIONDATE=${CURRENTYEAR}-${CURRENTMONTH}-${CURRENTDATE}

echo executed at $EXECUTIONDATE
echo $CURRENTEPOCTIME
echo --job-name=$JOB --execution-date=$EXECUTIONDATE

#./pants run src/main/scala/apiparser --jvm-run-jvm-program-args="--job-name=$JOB --execution-date=$EXECUTIONDATE"
#java -cp apiparser.jar apiparser.Main "--job-name=$JOB" "--execution-date=$EXECUTIONDATE"

#sh executor.sh GrabMatch
#sh executor.sh SaveFlattenedMatch

#gcloud beta compute --project "praxis-backup-158705" ssh --zone "us-central1-a" "instance-1"
