/************************************************************************
 *
 * $Id: NewGame.java,v 1.2 2001/11/06 00:39:19 akhil Exp $
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
 * The first screen the user sees.
 * The main menu of the PIM service
 */

public class NewGame extends Form implements CommandListener, ResponseListener
{
    //The form screen to display
    private StringItem welcomeTextItem = null;
    private AutoGauge gauge = null;
    private StringItem playersItem = null;
        
    private Command cmdBet = null;
    private Command cmdExit = null;
    
    public NewGame()
    {
        super(Constants.STR_BLACKJACK);
        cmdBet = new Command(Constants.STR_BET, Command.SCREEN, 1);
        cmdExit = new Command(Constants.STR_EXIT, Command.SCREEN, 1);
    }
    
    public void setActive()
    {
        Game.instance.setResponseListener((ResponseListener)this);
        //Initialize the Game class with the base URL
        //The Game class passes this on to the Protocol
        String baseURL = BlackjackMidlet.instance.getAppProperty(ProtocolConstants.baseURLProperty);
        System.out.println("Base URL read from property: "+baseURL);
        if(baseURL == null)
            {
                //use a default hard coded value if none is found in the jad file
                baseURL = ProtocolConstants.defaultBaseURL;
                System.out.println("Using default Base URL: "+baseURL);
            }
        Game.instance.setBaseURL(baseURL);

        //start/join a game
        Game.instance.play();
        BlackjackMidlet.instance.display.setCurrent(this);
    }//setActive

    
    public void commandAction(Command c, Displayable s)
    {
        if(c == cmdExit)
            {
                try
                    {           
                        Game.instance.reset();
                        BlackjackMidlet.instance.notifyDestroyed();
                        Debug.println("NewGame - Exited");
                    }
                catch(Exception e)
                    {
                        Debug.println("NewGame Exit Exception: "+e);
                    }
            }
        else if(c == cmdBet)
            {
                Game.instance.start();
            }
    }

    public void onResponse(int code, String data)
    {
        // Debug.println("NewGame onResponse: code = "+code+" data = "+data);
        switch(code)
            {
                //Server is notifying of this player's id
            case ProtocolConstants.R_ID: 
                {   
                    //Expecting data = PlayerID
                    //parse the id from the data
                    int id = Integer.parseInt(data);
                
                    //Tell the game of this player's id
                    Game.instance.setThisPlayer(id);
                    
                    //We display the appropriate message, and if this player
                    //is initiating the game we display the "Bet" and "Exit" commands
                    if(id == Constants.INITIATOR_ID)
                        {
                            welcomeTextItem = new StringItem("", Constants.STR_STARTED_GAME);
                            addCommand(cmdBet);
                            addCommand(cmdExit);
                        }
                    else
                        {
                            welcomeTextItem = new StringItem("", Constants.STR_JOINED_GAME+id);
                            addCommand(cmdExit);
                        }
                    
                    //set up a gauge control to show processing
                    gauge = new AutoGauge(Constants.STR_WAITING_FOR_PLAYERS, 10, 0, 1, 500);

                    playersItem = new StringItem("", Constants.STR_TOTAL_PLAYERS+id);
                    
                    append(welcomeTextItem);
                    append(gauge);
                    append(playersItem);
                    setCommandListener(this);
                }
                break;

            case ProtocolConstants.R_START_STATUS:
                {
                    //Expecting data = [1|0]#NumPlayers
                    // Debug.println("New Game R_START_STATUS data = "+data);
                    //Check if the game has started
                    int delim = data.indexOf(ProtocolConstants.FIELD_DELIMITER);
                    String startString = data.substring(0,delim);
                    int startValue = Integer.parseInt(startString);
                    String playersString = data.substring(delim+1);
                    int numPlayers = Integer.parseInt(playersString);
                    playersItem.setText(Constants.STR_TOTAL_PLAYERS+numPlayers);
                    if(startValue == ProtocolConstants.START_TRUE)
                        {
                            //Now that we know how many players there are we can
                            //create the Player objects and add them to the game
                            //We add an additional player (numPlayers+1) to account 
                            //for the dealer
                    
                            for(int i = 0; i < numPlayers+1; i++)
                                {
                                    Game.instance.addPlayer(new Player(i));
                                }
                    
                            Game.instance.setStarted(true);
                            //Move on to the "Place bet" screen
                            new PlaceBet().setActive();
                        }
                }
                break;

            case ProtocolConstants.R_ERROR: 
                {
                    Debug.println("New Game: Error"+data);
                    //TBD alert screen
                }
                break;
            
            default:
                System.out.println("Unhandled response: id = "+code+", Data = "+data);
                break;
                
            }//switch
    }//end method
    
}//end class
