/************************************************************************
 *
 * $Id: ProtocolConstants.java,v 1.1 2001/11/05 20:00:22 akhil Exp $
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

/*
 * Created: Tue May 1 2001 by Rami Honig
 */

package net.jxme.demo.blackjack.share;

public interface ProtocolConstants
{
    public static final int DEALER = 0;

    public static final int CMD_JOIN = 1;
    public static final int CMD_START_STATUS = 2;
    public static final int CMD_START = 3;
    public static final int CMD_STATUS = 4;
    public static final int CMD_CARD = 5;
    public static final int CMD_BET = 6;
    public static final int CMD_BETS_IN = 7;
    public static final int CMD_HOLD = 8;
    public static final int CMD_RESTART = 9;
    public static final int CMD_NEW_GAME = 10;

    public static final int R_ID = 1;
    public static final int R_START_STATUS = 2;
    public static final int R_CARDS = 3;
    public static final int R_CARD = 4;
    public static final int R_TURN = 5;
    public static final int R_BUST = 6;
    public static final int R_END = 7;
    public static final int R_BLACKJACK = 8;
    public static final int R_BETS_IN = 9;
    public static final int R_WAITING_BETS = 10;
    public static final int R_NO_CHANGE = 11;
    public static final int R_GAME_STARTED = 12;
    public static final int R_NEW_GAME = 13;
    public static final int R_ERROR = 99;
    
    public static final int NO_ID = 99;
    
    public static final char FIELD_DELIMITER = '#';
    
    public static final int START_TRUE = 1;
    public static final int START_FALSE = 0;
    
    public static final String baseURLProperty = "BASE_URL";

    /* Modify this to define a default URL that the client should try
    to find Blackjack server.  If the device running the client
    supports optional jad file entries then it is easier to just add
    an entery to the jad file called "BASE_URL".  If optional jad file
    entries are not supported, modify the url here, and then recompile
    the client */

    public static final String defaultBaseURL = "http://127.0.0.1/blackjack";

}//end class
