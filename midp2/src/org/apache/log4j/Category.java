/*
 *  Copyright (c) 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Sun Microsystems, Inc. for Project JXTA."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *  must not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact Project JXTA at http://www.jxta.org.
 *
 *  5. Products derived from this software may not be called "JXTA",
 *  nor may "JXTA" appear in their name, without prior written
 *  permission of Sun.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 */
package org.apache.log4j;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Iterator;

public class Category {

    protected String name;
    protected volatile Priority priority;
    protected volatile Category parent;
    protected static boolean debug = true;
    protected Category() {}

    public void assertLog(boolean assertion, String msg) {}
    public static void setDebug(boolean debugOn) {
        debug = debugOn;
    }
    /**
     *
     * @param message
     */
    public void debug(Object message) { print(message); }

    /**
     *
     * @param message
     * @param t
     */
    public void debug(Object message, Throwable t) { print(message); t.printStackTrace(); }

    /**
     *
     * @param messagePattern
     * @param arg
     */
    public void debug(Object messagePattern, Object arg) { print(arg); }

    /**
     *
     * @param messagePattern
     * @param arg1
     * @param arg2
     */
    public void debug(String messagePattern, Object arg1, Object arg2) { print(arg1); }

    /**
     *
     * @return
     */
    public boolean isErrorEnabled() { return false; }

    /**
     *
     * @param message
     */
    public void error(Object message) { print(message); }

    /**
     *
     * @param message
     * @param t
     */
    public void error(Object message, Throwable t) { print(message); t.printStackTrace(); }

    /**
     *
     * @param messagePattern
     * @param arg
     */
    public void error(Object messagePattern, Object arg) { print(arg); }

    /**
     *
     * @param messagePattern
     * @param arg1
     * @param arg2
     */
    public void error(String messagePattern, Object arg1, Object arg2) { print(arg1); }

    /**
     *
     * @param name
     * @return
     */
    public static Logger exists(String name) { return Logger.getInstance("stub"); }

    /**
     *
     * @param message
     */
    public void fatal(Object message) { print(message); }

    /**
     *
     * @param messagePattern
     * @param arg
     */
    public void fatal(Object messagePattern, Object arg) { print(messagePattern, arg); }

    /**
     *
     * @param message
     * @param t
     */
    public void fatal(Object message, Throwable t) { print(message); t.printStackTrace(); }

    /**
     *
     */
    protected void forcedLog() {}

    /**
     *
     * @return
     */
    public boolean getAdditivity() { return false; }

    /**
     *
     * @return
     */
    public Iterator getAllAppenders() { return new ArrayList().iterator(); }

    /**
     *
     * @return
     *
     * If reached will cause an NullPointerException.
     */
    public Priority getEffectivePriority() { return null; }

    /**
     *
     * @return
     */
    public static Iterator getCurrentCategories() { return new ArrayList().iterator(); }

    /**
     *
     * @return
     */
    public final String getName() { return "stub"; }

    /**
     *
     * @return
     */
    public final Category getParent() { return this; }

    /**
     *
     * @return
     */
    public final Priority getPriority() { return this.priority; }

    /**
     *
     * @param key
     * @return
     */
    protected String getResourceBundleString(String key) { return null; }

    /**
     *
     * @param message
     */
    public void info(Object message) { print(message); }

    /**
     *
     * @param messagePattern
     * @param arg
     */
    public void info(Object messagePattern, Object arg) { print(arg);}

    /**
     *
     * @param messagePattern
     * @param arg1
     * @param arg2
     */
    public void info(String messagePattern, Object arg1, Object arg2) { print(arg1, arg2);}

    /**
     *
     * @param message
     * @param t
     */
    public void info(Object message, Throwable t) { print(message); t.printStackTrace(); }

    /**
     *
     * @return
     */
    public boolean isDebugEnabled() { return false; }

    /**
     *
     * @return
     */
    public boolean isTraceEnabled() { return false; }

    /**
     *
     * @param priority
     * @return
     */
    public boolean isEnabledFor(Priority priority) { return debug; }

    /**
     *
     * @return
     */
    public boolean isInfoEnabled() { return false; }

    /**
     *
     * @param priority
     * @param key
     * @param t
     */
    public void l7dlog(Priority priority, String key, Throwable t) {}

    /**
     *
     * @param priority
     * @param key
     * @param params
     * @param t
     */
    public void l7dlog(Priority priority, String key, Object[] params, Throwable t) {}

    /**
     * This generic form is intended to be used by wrappers.
     */

    /**
     *
     * @param priority
     * @param message
     * @param t
     */
    public void log(Priority priority, Object message, Throwable t) {}

    /**
     *
     * @param priority
     * @param message
     */
    public void log(Priority priority, Object message) {}

    /**
     *
     * @param callerFQCN
     * @param priority
     * @param message
     * @param t
     */
    public void log(String callerFQCN, Priority priority, Object message, Throwable t) {}

    /**
     *
     */
    public void removeAllAppenders() {}

    /**
     *
     * @param name
     */
    public void removeAppender(String name) {}

    /**
     *
     * @param additive
     */
    public void setAdditivity(boolean additive) {}

    /**
     *
     * @param priority
     */
    public void setPriority(Priority priority) {}

    /**
     *
     * @return
     */
    public boolean isWarnEnabled() { return false; }

    /**
     *
     * @param message
     */
    public void warn(Object message) { print(message);}

    /**
     *
     * @param message
     * @param t
     */
    public void warn(Object message, Throwable t) { print(message); t.printStackTrace(); }

    /**
     *
     * @param messagePattern
     * @param arg
     */
    public void warn(Object messagePattern, Object arg) { print(messagePattern, arg);}

    /**
     *
     * @param messagePattern
     * @param arg1
     * @param arg2
     */
    public void warn(String messagePattern, Object arg1, Object arg2) { print(arg1, arg2);}

    /**
     *
     * @param message
     */
    private void print(Object message) { System.out.println(message.toString()); }

    /**
     *
     * @param message
     * @param arg
     */
    private void print(Object message, Object arg) {
        if(null == message) {
            System.out.println("Cannot print null message!");
            return;
        }

        if(null == arg) {
            System.out.println(message.toString());
            return;
        }

        if(! (arg instanceof Throwable))
            System.out.println(message.toString() + " " + arg.toString());
        else {
            Throwable t = (Throwable) arg;
            t.printStackTrace();
            System.out.println(message.toString() + " " + arg.toString());
        }
    }

//    /**
//     *
//     * @param message
//     * @param arg1
//     * @param arg2
//     */
//    private void print(Object message, Object arg1, Object arg2) {
//        System.out.println(message.toString() + " " + arg1.toString() + " " + arg2.toString());
//    }
}
