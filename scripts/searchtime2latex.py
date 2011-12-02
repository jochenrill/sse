#encoding=utf-8

import csv
import sys
import math


def avg(values):
	return reduce(lambda x,y: x+y, values) / float(len(values))

def sigma(values):
	mu = avg(values)

	x = 0
	for i in values:
		x += (i - mu)**2
	
	return math.sqrt(x/float(len(values)))

reader = csv.reader(open(sys.argv[1], 'r'), delimiter=' ')


times = {}
for row in reader:
	length = int(row[0])
	time = float(row[1])

	if not length in times.keys():
		times[length] = []
	
	times[length].append(time)

print """\\begin{tabular}{lll}
	$|w|$ & $n$ & $t \pm \sigma$ \\\\\hline"""

keys = times.keys()
keys.sort()
for l in keys:
	print "\t$%d$ & $%d$ & $%.2f \\pm %.2f$ \\\\" % (l, len(times[l]), avg(times[l]), sigma(times[l]))



print """\end{tabular}"""
