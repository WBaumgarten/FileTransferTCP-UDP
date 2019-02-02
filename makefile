JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
		Tester.java\
        FilePacket.java \
        SenderRBUDP.java \
        ReceiverRBUDP.java \
		SenderGUI.java \
		ReceiverGUI.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
