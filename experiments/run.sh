#!/bin/bash

MVN=`which mvn`


"$MVN" -f ../pom.xml clean compile test-compile

for profile in "$@"; do
	if [ -z "$profile" -o ! -f profiles/"$profile" ]; then
		echo >&2 "$profile" is not a valid profile
	else
		unset TARGET_SYSTEM CE_LENGTH_MIN CE_LENGTH_MAX CE_LENGTH_STEP REPEAT_COUNT
		unset NUM_THREADS LEARNERS OUTPUT_NAME RANDOM_SEED
		source profiles/"$profile"
		export TARGET_SYSTEM CE_LENGTH_MIN CE_LENGTH_MAX CE_LENGTH_STEP REPEAT_COUNT
		export NUM_THREADS LEARNERS OUTPUT_NAME RANDOM_SEED

	"$MVN" -f ../pom.xml exec:java \
		-Dexec.mainClass=de.learnlib.algorithms.ttt.dfa.TTTExperiment \
		-Dexec.classpathScope=test
	fi
done
