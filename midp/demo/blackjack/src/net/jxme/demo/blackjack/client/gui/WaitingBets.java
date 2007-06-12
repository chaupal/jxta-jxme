/************************************************************************
 *
 * $Id: WaitingBets.java,v 1.2 2001/11/06 00:39:19 akhil Exp $
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

package net.jxme.demo.blackjack.client.gui;

import javax.microedition.lcdui.*;
import net.jxme.demo.blackjack.share.*;
import net.jxme.demo.blackjack.client.logic.*;
import java.io.IOException;

/**
 * The player places a bet with this screen
 */

public class WaitingBets extends Form implements ResponseListener
{
    //The form screen to display
    private AutoGauge gauge = null;
    private StringItem playersItem = null;
    private StringItem betsInItem = null;
    
    public WaitingBets()
    {
        super(Constants.STR_WAITING);
        //set up a gauge control to show processing
        gauge = new AutoGauge(Constants.STR_WAITING_BETS, 10, 0, 1, 500);

        playersItem = new StringItem(Constants.STR_TOTAL_PLAYERS,"" );
        betsInItem = new StringItem(Constants.STR_BETS_IN,"" );
        append(gauge);
        append(playersItem);
        append(betsInItem);
    }
    
    public void setActive()
    {
        Game.instance.setResponseListener((ResponseListener)this);
        
        BlackjackMidlet.instance.display.setCurrent(this);
    }//setActive


    public void onResponse(int code, String data)
    {
        // Debug.println("WaitingBets.onResponse: code = "+code+" data = "+data);
        switch(code)
            {
                //Server is notifying of this player's id
            case ProtocolConstants.R_WAITING_BETS: 
                {   
                    // Debug.println("R_WAITING_BETS: data = "+data+ " Player = "+Game.instance.getThisPlayer());
                    //Expecting data = numBetsIn#totalPlayers
                    int delim = data.indexOf(ProtocolConstants.FIELD_DELIMITER);
                    String betsIn = data.substring(0, delim);
                    String totalPlayers = data.substring(delim+1);
                    playersItem.setText(totalPlayers);
                    betsInItem.setText(betsIn);
                }        
                break;
            
            case ProtocolConstants.R_GAME_STARTED:
                // Debug.println("R_GAME_STARTED: data = "+data);
                new GameScreen().setActive();
                break;

            case ProtocolConstants.R_ERROR: 
                {
                    Debug.println("Waiting bets: Error"+data);
                    //TBD alert screen
                }
                break;
            
            default:
                System.out.println("Unhandled response: id = "+code+", Data = "+data);
                break;
            }//end switc            
    }//end method

}//end class
