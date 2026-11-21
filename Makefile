FLAGS=-cp .
JAR=jar
JAVA=java
JAVAC=javac
JAVACC=javacc

PARSEDIR=Parse
MJ_PARSER=MiniJavaParser

DEFAULT_JAVACCGEN = $(PARSEDIR)/SimpleCharStream.java \
	$(PARSEDIR)/ParseException.java \
	$(PARSEDIR)/Token.java \
	$(PARSEDIR)/TokenMgrError.java

MJ_JAVACCGEN = $(DEFAULT_JAVACCGEN) \
	$(PARSEDIR)/$(MJ_PARSER).java \
	$(PARSEDIR)/$(MJ_PARSER)Constants.java \
	$(PARSEDIR)/$(MJ_PARSER)TokenManager.java


# Project 8 - Instruction selector (builds on the translator)

ASTSRC = $(wildcard Absyn/*.java)
TYPESRC = $(wildcard Types/*.java)
SYMBOLSRC = $(wildcard Symbol/*.java)
SEMANTSRC = Semant/Main.java Semant/TypeChecker.java
TEMPSRC = $(wildcard Temp/*.java)
TREESRC = $(wildcard Tree/*.java)
FRAMESRC = $(wildcard Frame/*.java)
RISCVSRC = $(wildcard Riscv/*.java)
TRANSLATESRC = $(wildcard Translate/*.java)
ASSEMSRC = $(wildcard Assem/*.java)
CANONSRC = $(wildcard Canon/*.java)
READSRC = Parse/ReadFrags.java

PARSESRC = Parse/Main.java $(ASTSRC)
PROJECTSRC = $(ASTSRC) $(TYPESRC) $(SYMBOLSRC) $(SEMANTSRC) \
	$(TEMPSRC) $(TREESRC) $(FRAMESRC) $(RISCVSRC) $(TRANSLATESRC) \
	$(CANONSRC) $(ASSEMSRC) $(READSRC)

all: 	$(PARSEDIR)/$(MJ_PARSER).java $(PROJECTSRC:.java=.class)

$(PARSEDIR)/$(MJ_PARSER).java: $(PARSEDIR)/$(MJ_PARSER).jj
	cd $(PARSEDIR); ${JAVACC} $(MJ_PARSER).jj;
	${JAVAC} ${JFLAGS} ${MJ_JAVACCGEN}

%.class : %.java
	${JAVAC} ${JFLAGS} $<

clean:
	rm -f $(PARSESRC:.java=.class) $(TYPESRC:.java=.class) \
		$(SYMBOLSRC:.java=.class) $(SEMANTSRC:.java=.class) \
		$(TEMPSRC:.java=.class) $(TREESRC:.java=.class) \
		$(FRAMESRC:.java=.class) $(RISCVSRC:.java=.class) \
		$(TRANSLATESRC:.java=.class) $(CANONSRC:.java=.class) \
		$(ASSEMSRC:.java=.class) $(READSRC:.java=.class) \
		$(MJ_JAVACCGEN) $(MJ_JAVACCGEN:.java=.class)\
                '$(PARSEDIR)/$(MJ_PARSER)$$JJCalls.class' \
                '$(PARSEDIR)/$(MJ_PARSER)$$LookaheadSuccess.class' 

realclean: clean
	rm -f */*.class *~ */*~ *.jar
