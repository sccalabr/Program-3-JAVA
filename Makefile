JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
        $(JC) $(JFLAGS) $*.java

CLASSES = \
        MemSim.java \
        PageAndFrameNumber.java \
        MemBlock.java

default: memSim

MimSim: $(CLASSES:.java=.class)

clean:
        $(RM) *.class
