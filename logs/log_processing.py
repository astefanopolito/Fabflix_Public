import sys

TS = 0
TJ = 0
count = 0
for arg in sys.argv[1:]:
    f = open(arg, "r")
    for line in f:
        if line[0] == '0':
            count += 1
            TS += int(line[1:])
        else:
            TJ += int(line[1:])
print("Average TS: {}".format((TS / count)/1000000))
print("Average TJ: {}".format((TJ / count)/1000000))
