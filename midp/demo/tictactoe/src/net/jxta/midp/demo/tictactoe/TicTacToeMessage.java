/************************************************************************
 *
 * $Id: TicTacToeMessage.java,v 1.1 2002/04/08 19:03:17 akhil Exp $
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

package net.jxta.midp.demo.tictactoe;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public final class TicTacToeMessage {

    private static final int STATE_NOT_CONNECTED = 0;
    private static final int STATE_WAITING_FOR_OPPONENT = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_OBSERVING = 3;
    private static final int STATE_GAME_OVER = 4;

    private static final boolean DEBUG = true;

    private int state = 0; 
    private int peerBoard = 0;
    private int senderBoard = 0; 
    private String sender = "";

    public TicTacToeMessage(int state,
                            int peerBoard,
                            int senderBoard, 
                            String sender) {
        this.state = state;
        this.peerBoard = peerBoard;
        this.senderBoard = senderBoard;
        this.sender = sender;
    }

    public TicTacToeMessage(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        state = dis.readInt();
        peerBoard = dis.readInt();
        senderBoard = dis.readInt();
        sender = dis.readUTF();
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(state);
        dos.writeInt(peerBoard);
        dos.writeInt(senderBoard);
        dos.writeUTF(sender);
        return baos.toByteArray();
    }

    public int getState() {
        return state;
    }

    public int getPeerBoard() {
        return peerBoard;
    }

    public int getSenderBoard() {
        return senderBoard;
    }

    public String getSender() {
        return sender;
    }

    public String toString() {
        return 
            "sender=" + sender + 
            " state=" + Integer.toHexString(state) +
            " peerBoard=" + Integer.toHexString(peerBoard) +
            " senderBoard=" + Integer.toHexString(senderBoard);
    }
}
