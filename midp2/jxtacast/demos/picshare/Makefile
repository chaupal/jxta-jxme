#
# $Id: Makefile,v 1.5 2002/11/14 06:48:09 jclark Exp $
#

# If you need to use a particular JDK set JAVA_HOME in your env.
# If you reference additional libraries they need to be set in the
# CLASSPATH.
#
# If you need to use a particular Core classes set JXTACORELIB in your env.
# Default value of JXTACORELIB points to the .jar file in Platform pkg.
# 

ifneq ($(JAVA_HOME),)
 JAVAHOMEBIN      = $(JAVA_HOME)/bin/
else
 JAVAHOMEBIN      =
endif

ifeq ($(JXTACORELIB),)
 JXTACORELIB =$(TOP)/../../../../../platform/binding/java/dist/jxta.jar
endif

JAVA          = $(JAVAHOMEBIN)java
JAVAC         = $(JAVAHOMEBIN)javac
JAR           = $(JAVAHOMEBIN)jar
CP	          = cp -f
JAVASRCFILES  = $(shell find src -name 'CVS' -prune -o -name '*.java' -print)
TOP           = $(shell pwd)
CLASSDIR      = $(TOP)/classes
DISTDIR       = $(TOP)/dist
EXTLIB        = $(JXTACORELIB):$(TOP)/../../dist/jxtacast.jar
JARPKGS       = net/jxta/picshare

JXTA_PROJECT  = "PicShare"
JXTA_PKG_NAME = "picshare"

ifneq ($(CLASSPATH),)
 JXTACLASSPATH      = $(CLASSPATH):$(CLASSDIR):$(EXTLIB)
else
 JXTACLASSPATH      = $(CLASSDIR):$(EXTLIB)
endif


ifeq (true,$(OPTIMIZE))
 JAVACOPT=-O -g:none
else
 JAVACOPT=-g
endif

#
# Cygwin uses the MS-Windows style classpath, so "fix" it for them
#
ifeq (cygwin,$(OSTYPE))
  JXTACLASSPATH := "$(shell cygpath --path --windows "$(JXTACLASSPATH)")"
  CLASSDIRx     := "$(shell cygpath --path --windows "$(CLASSDIR)")"
  DISTDIRx      := "$(shell cygpath --path --windows "$(DISTDIR)")"
else
  CLASSDIRx := "$(CLASSDIR)"
  DISTDIRx  := "$(DISTDIR)"
endif

.PHONY: all compile clean clobber help

all: jar

compile:
	@echo building $(JXTA_PROJECT) using $(JAVAC)
	@echo CLASSPATH = $(JXTACLASSPATH)
	@if [ '!' -d $(CLASSDIR) ]; then mkdir $(CLASSDIR); fi;
	@$(JAVAC) $(JAVACOPT) -d $(CLASSDIRx) -deprecation -classpath $(JXTACLASSPATH) $(JAVASRCFILES)
	@$(CP) $(TOP)/src/net/jxta/picshare/*.gif $(TOP)/classes/net/jxta/picshare
	@$(CP) $(TOP)/src/net/jxta/picshare/*.jpg $(TOP)/classes/net/jxta/picshare
	@echo Done building $(JXTA_PROJECT).

jar: compile
	@echo "Creating $(CLASSDIR)/$(JXTA_PKG_NAME).jar"
	@if [ '!' -d $(DISTDIR) ]; then mkdir $(DISTDIR); fi;
	@cd $(CLASSDIR); $(JAR) -cf $(DISTDIRx)/$(JXTA_PKG_NAME).jar $(JARPKGS); unzip -l $(DISTDIRx)/$(JXTA_PKG_NAME).jar |grep files

clean:
	@rm -rf $(CLASSDIR)
	@rm -rf $(DISTDIR)

clobber: clean

help:
	@echo "# Usage : gnumake [all] [compile] [jar] [clean] [clobber] [help]"
