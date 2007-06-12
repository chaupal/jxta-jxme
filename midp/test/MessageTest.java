/************************************************************************
 *
 * $Id: MessageTest.java,v 1.3 2002/02/20 20:57:10 akhil Exp $
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache
 * Foundation.
 **********************************************************************/

/**
 * Unit test for Binary Message Wire Format.

 * This class needs to access private methods of
 * net.jxta.j2me.Message, which is why it is declared to be in the
 * net.jxta.j2me package.
 */

package net.jxta.j2me;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class MessageTest {

    private String testName = null;

    private Message msgOut = null;
    private final String[] msgNames = {
        Message.CMD_SEARCH,
        Message.CMD_CREATE,
        Message.CMD_LISTEN,
        Message.CMD_CLOSE,
        Message.CMD_SEND
    };
    private Message[] msg = new Message[msgNames.length];

    public static void main(String[] args) {
          MessageTest test = new MessageTest("MessageTest");
          test.setUp();
          test.testSerialization();
          test.testResponseApi();
          test.tearDown();
    }

    public MessageTest(String name) {
        testName = name;
    }

    public void setUp() {
        int requestId = 0;
        int msgIndex = 0;
        Element[] elm = null;

        elm = new Element[2];
        elm[0] = new Element("sample element", 
                           "sample element data".getBytes(),
                           "midp", "application/junk");
        elm[1] = new Element("sample element 2", 
                           "sample element 2 data".getBytes(),
                           null, null);
        msgOut = new Message(elm);

        elm = new Element[4];
        elm[0] = new Element(Message.TYPE_REQUEST, 
                             Message.CMD_SEARCH.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[1] = new Element(Message.ARG_ADV_TYPE, 
                             Message.ARG_PIPE_TYPE.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[2] = new Element(Message.ARG_QUERY, 
                             "some-pipe-name".getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[3] = new Element(Message.ARG_REQUEST_ID, 
                             Integer.toString(++requestId).getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        msg[msgIndex++] = new Message(elm);

        elm = new Element[4];
        elm[0] = new Element(Message.TYPE_REQUEST, 
                             Message.CMD_CREATE.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[1] = new Element(Message.ARG_PIPE_NAME, 
                             "create-name".getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[2] = new Element(Message.ARG_PIPE_TYPE, 
                             "pipe".getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[3] = new Element(Message.ARG_REQUEST_ID, 
                             Integer.toString(++requestId).getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        msg[msgIndex++] = new Message(elm);

        elm = new Element[5];
        elm[0] = new Element(Message.TYPE_REQUEST, 
                             Message.CMD_LISTEN.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[1] = new Element(Message.ARG_REQUEST_ID, 
                             Integer.toString(++requestId).getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[2] = new Element(Message.ARG_PIPE_NAME, 
                             "my-incoming-pipe".getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[3] = new Element(Message.ARG_PIPE_ID, 
                             "uuid-fake-pipe-id".getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[4] = new Element(Message.ARG_PIPE_TYPE, 
                             "JxtaUnicast".getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        msg[msgIndex++] = new Message(elm);

        elm[0] = new Element(Message.TYPE_REQUEST, 
                             Message.CMD_CLOSE.getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        elm[1] = new Element(Message.ARG_REQUEST_ID, 
                             Integer.toString(++requestId).getBytes(), 
                             Message.PROXY_NAME_SPACE, null);
        msg[msgIndex++] = new Message(elm);

        Element[] elmSend = new Element[6];
        for (int i=0; i < elm.length; i++) {
            elmSend[i] = elm[i];
        }
        elmSend[0] = new Element(Message.TYPE_REQUEST, 
                                 Message.CMD_SEND.getBytes(), 
                                 Message.PROXY_NAME_SPACE, null);
        elmSend[1] = new Element(Message.ARG_REQUEST_ID, 
                                 Integer.toString(++requestId).getBytes(), 
                                 Message.PROXY_NAME_SPACE, null);
        elmSend[elmSend.length - 1] = 
            new Element("application-data-sender", 
                        "whoami".getBytes(), 
                        "app", null);
        msg[msgIndex++] = new Message(elmSend);
    }

    public void tearDown() {
        msgOut = null;
        for (int i=0; i < msg.length; i++) {
            msg[i] = null;
        }
    }

    public void assertTrue(String message, boolean condition) {
        if (!condition) {
            fail(message);
        }
    }

    public void fail(String message) {
        throw new Error(testName + ": " + message);
    }

    public void testSerialization() {
        Message msgIn = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            msgOut.write(dos);
            baos.close();
            byte[] bits = baos.toByteArray();

            FileOutputStream fos = new FileOutputStream("testmsg.dump");
            fos.write(bits);
            fos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(bits);
            DataInputStream dis = new DataInputStream(bais);
            msgIn = Message.read(dis);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        int elementCount = msgIn.getElementCount();
        for (int i=0; i < elementCount; i++) {
            Element el = msgIn.getElement(i);
            Element ref = msgOut.getElement(i);
            assertTrue("element names mangled - " + 
                       ref.getName() + "/" + el.getName(), 
                       ref.getName().equals(el.getName()));
            assertTrue("element name space mangled - " +
                       ref.getNameSpace() + "/" + el.getNameSpace(),
                       ref.getNameSpace().equals(el.getNameSpace()));
            assertTrue("element mime type mangled - " +
                       ref.getMimeType() + "/" + el.getMimeType(),
                       ref.getMimeType().equals(el.getMimeType()));

            byte[] data1 = ref.getData();
            byte[] dat = el.getData();
            assertTrue("element data length mangled - " +
                       data1.length + "/" + dat.length,
                       data1.length == dat.length);
            for (int j=0; j < data1.length; j++) {
                assertTrue("element data mangled at offset " + j + " - " +
                           data1[j] + "/" + dat[j],
                           data1[j] == dat[j]);
            }
        }

        System.out.println(testName + ": testSerialization succeeded");
    }

    public void testResponseApi() {
        for (int i=0; i < msgNames.length; i++) {
            assertTrue(msgNames[i] + " type mangled - " + 
                       Integer.toString(msg[i].getType()) + "/" +
                       Integer.toString(Message.REQUEST | i+1),
                       msg[i].getType() == (Message.REQUEST | i+1));

            assertTrue(msgNames[i] + " query id mangled - " + 
                       Integer.toString(msg[i].getQueryId()) + "/" +
                       Integer.toString(i+1),
                       msg[i].getQueryId() == i+1);

            assertTrue("search name mangled - " + 
                       msg[i].getName() + "/" + msgNames[i],
                       msg[i].getName().equals(msgNames[i]));
        }

        System.out.println(testName + ": testResponseApi succeeded");
    }
}
