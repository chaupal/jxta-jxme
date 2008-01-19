/*
 *
 *  Copyright (c) 2001-2008 Sun Microsystems, Inc.  All rights
 *  reserved.
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
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *  not be used to endorse or promote products derived from this
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
 *  =========================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 *
 *  $Id: $
 *
 *
 */


package net.jxta.util.java.lang;

/**
 * This interface should be implemented by classes wishing to
 * support of override <code>Object.clone()</code>.  The default
 * behaviour of <code>clone()</code> performs a shallow copy, but
 * subclasses often change this to perform a deep copy.  Therefore,
 * it is a good idea to document how deep your clone will go.
 * If <code>clone()</code> is called on an object which does not
 * implement this interface, a <code>CloneNotSupportedException</code>
 * will be thrown.
 * <p/>
 * <p>This interface is simply a tagging interface; it carries no
 * requirements on methods to implement.  However, it is typical for
 * a Cloneable class to implement at least <code>equals</code>,
 * <code>hashCode</code>, and <code>clone</code>, sometimes
 * increasing the accessibility of clone to be public. The typical
 * implementation of <code>clone</code> invokes <code>super.clone()</code>
 * rather than a constructor, but this is not a requirement.
 * <p/>
 * <p>If an object that implement Cloneable should not be cloned,
 * simply override the <code>clone</code> method to throw a
 * <code>CloneNotSupportedException</code>.
 * <p/>
 * <p>All array types implement Cloneable, and have a public
 * <code>clone</code> method that will never fail with a
 * <code>CloneNotSupportedException</code>.
 *
 * @author Paul Fisher
 * @author Eric Blake (ebb9@email.byu.edu)
 * @author Warren Levy (warrenl@cygnus.com)
 * @status updated to 1.4
 * @see Object#clone()
 * @see CloneNotSupportedException
 * @since 1.0
 */
public interface Cloneable {
    // Tagging interface only.
}
