@echo off
java -DJXTA.SIMULATION=true -classpath ..\lib\jxta.jar;..\lib\jxtasecurity.jar;..\lib\log4j.jar;..\lib\beepcore.jar;..\lib\jxtaptls.jar;..\lib\minimalBC.jar;..\lib\cryptix32.jar;..\lib\cryptix-asn1.jar;..\lib\javax.servlet.jar;..\lib\org.mortbay.jetty.jar;..\lib\jxtacast.jar;..\lib\picshare.jar net.jxta.picshare.PicShare