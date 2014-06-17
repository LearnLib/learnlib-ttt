#!/bin/bash

MVN=`which mvn`

PROFILE="$1"

if [ -z "$PROFILE" -o ! -f profiles/"$PROFILE" ]; then
	echo >&2 Must specify a valid profile. Exiting ...
	exit 1
fi

source profiles/"$PROFILE"
export TARGET_SYSTEM CE_LENGTH_MIN CE_LENGTH_MAX CE_LENGTH_STEP REPEAT_COUNT
export NUM_THREADS LEARNERS OUTPUT_NAME RANDOM_SEED

"$MVN" -f ../pom.xml compile
"$MVN" -f ../pom.xml exec:java \
	-Dexec.mainClass=de.learnlib.algorithms.ttt.dfa.TTTExperiment \
	-Dexec.classpathScope=test

