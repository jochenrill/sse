.PHONY: all clean jar compile

all:
	ant clean
	ant build
	ant -buildfile build.ant
clean:
	ant clean
jar:
	ant -buildfile build.ant
compile:
	ant build
