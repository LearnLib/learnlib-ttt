#!/usr/bin/env python

import math
import sys

def read(file):
	by_key = dict()
	with open(file, 'r') as f:
		for line in f:
			strdata = line.split()
			if len(strdata) == 0:
				continue
			fdata = [float(x) for x in strdata]
			if fdata[0] not in by_key:
				by_key[fdata[0]] = list()
			by_key[fdata[0]].append(fdata[1:])

	return by_key


def statisticize(data):
	items = len(data)
	size = len(data[0])
	result = [0]*size*2

	for d in data:
		for i in xrange(size):
			result[2*i] += d[i]

	for i in xrange(size):
		result[2*i] /= items

	for d in data:
		for i in xrange(size):
			result[2*i + 1] += (d[i] - result[2*i])**2

	for i in xrange(size):
		result[2*i + 1] = math.sqrt(result[2*i + 1] / items)

	return result

def aggregate(by_key):
	dataset = list()
	for key, data in by_key.items():
		stat_data = list()
		stat_data.append(key)
		stat_data.extend(statisticize(data))
		dataset.append(stat_data)

	return dataset

def write(dataset, file):
	with open(file, 'w') as f:
		for d in dataset:
			f.write('\t'.join([str(x) for x in d]) + '\n')


def read_and_aggregate(file, output_file = None):
	if output_file is None:
		output_file = file + ".agg"

	by_key = read(file)
	dataset = aggregate(by_key)
	dataset = sorted(dataset)
	write(dataset, output_file)


def main():
	if len(sys.argv) < 2:
		print "Need to specify at least one argument"

	input = sys.argv[1]
	output = None
	if len(sys.argv) > 2:
		output = sys.argv[2]

	read_and_aggregate(input, output)


if __name__ == '__main__':
	main()

