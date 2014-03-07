JAVAC=javac

default: client server

server:
	javac src/mn/jhn/server/*.java

client:
	javac src/mn/jhn/client/*.java

all: clean server client

clean:
	rm -f src/mn/jhn/server/*.class src/mn/jhn/client/*.class
