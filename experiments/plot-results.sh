#!/bin/bash


mkdir -p results-agg/sched4

for dat in results/sched4-*/*.dat; do
	cat "$dat" >>results-agg/sched4/`basename "$dat"`
done

mkdir -p results-agg/pots2
cp -r results/pots2/*.dat results-agg/pots2
mkdir -p results-agg/peterson2
cp -r results/peterson2/*.dat results-agg/peterson2


for f in `find results-agg -name '*.dat'`; do
	./aggregate.py "$f"
done


for t in sched4 pots2 peterson2; do
	pushd .
	cd results-agg/"$t"
	gnuplot ../../plot.gpl
	popd
done
