/************************************************************************
 *
 * $Id: Constants.java,v 1.1 2001/11/05 20:00:22 akhil Exp $
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

public interface Constants
{
    public static final String STR_BET = "Bet";
    public static final String STR_EXIT = "Exit";
    public static final String STR_BLACKJACK = "Blackjack";
    public static final String STR_STARTED_GAME = " You have started a new game.";  
    public static final String STR_JOINED_GAME = " You have joined a new game as: Player ";
    public static final String STR_WAITING_FOR_PLAYERS = " Waiting for others to join.";
    public static final String STR_TOTAL_PLAYERS = " Total players: ";
    public static final String STR_PLACE_BET = " Place bet";
    public static final String STR_PLACE = "Place";
    public static final String STR_WAITING = " Waiting";
    public static final String STR_WAITING_BETS = " Waiting for bets...";
    public static final String STR_BETS_IN = " Bets in: ";
    public static final String STR_YOU_BET = "You bet";
    public static final String STR_CARD = "Card";
    public static final String STR_HOLD = "Hold";
    public static final String STR_BUST = " Bust";
    public static final String STR_YOUR_TURN = "Your turn";
    public static final String STR_DEALING =   "Dealing...";
    public static final String STR_HOLDING =   "Holding";
    public static final String STR_DEALER = "Dealer";
    public static final String STR_NEW_GAME = "New game";
    
    public static final String STATUS_UNDEFINED = "Undefined";
    public static final String STATUS_LOSE = "Lose";
    public static final String STATUS_WIN = "Win";
    public static final String STATUS_DRAW = "Draw";
    
    public static final String[] playerNames = {"Dlr", "P1", "P2", "P3"};
    public static final String[] playerImageFiles = {"/Dlr.png", "/P1.png", "/P2.png", "/P3.png"};
    public static final String thisPlayerName = "You";
    public static final String thisPlayerImageFile = "/You.png";
    
    public static final int INITIATOR_ID = 1;
    public static final int LOSE = 0;
    public static final int WIN = 1;
    public static final int DRAW = 2;

}//end class
