/*
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
 */
package net.jxta.util.java.net;

public class URISyntaxException
        extends Exception {
    private String input;
    private int index;

    /**
     * Constructs an instance from the given input string, reason, and error
     * index.
     *
     * @param input  The input string
     * @param reason A string explaining why the input could not be parsed
     * @param index  The index at which the parse error occurred,
     *               or <tt>-1</tt> if the index is not known
     * @throws NullPointerException     If either the input or reason strings are <tt>null</tt>
     * @throws IllegalArgumentException If the error index is less than <tt>-1</tt>
     */
    public URISyntaxException(String input, String reason, int index) {
        super(reason);
        if ((input == null) || (reason == null))
            throw new NullPointerException();
        if (index < -1)
            throw new IllegalArgumentException();
        this.input = input;
        this.index = index;
    }

    /**
     * Constructs an instance from the given input string and reason.  The
     * resulting object will have an error index of <tt>-1</tt>.
     *
     * @param input  The input string
     * @param reason A string explaining why the input could not be parsed
     * @throws NullPointerException If either the input or reason strings are <tt>null</tt>
     */
    public URISyntaxException(String input, String reason) {
        this(input, reason, -1);
    }

    /**
     * Returns the input string.
     *
     * @return The input string
     */
    public String getInput() {
        return input;
    }

    /**
     * Returns a string explaining why the input string could not be parsed.
     *
     * @return The reason string
     */
    public String getReason() {
        return super.getMessage();
    }

    /**
     * Returns an index into the input string of the position at which the
     * parse error occurred, or <tt>-1</tt> if this position is not known.
     *
     * @return The error index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns a string describing the parse error.  The resulting string
     * consists of the reason string followed by a colon character
     * (<tt>':'</tt>), a space, and the input string.  If the error index is
     * defined then the string <tt>" at index "</tt> followed by the index, in
     * decimal, is inserted after the reason string and before the colon
     * character.
     *
     * @return A string describing the parse error
     */
    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append(getReason());
        if (index > -1) {
            sb.append(" at index ");
            sb.append(index);
        }
        sb.append(": ");
        sb.append(input);
        return sb.toString();
    }

}
