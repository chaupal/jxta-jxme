/************************************************************************
 *
 * $Id: Game.java,v 1.2 2001/11/06 00:39:19 akhil Exp $
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

package net.jxme.demo.blackjack.client.logic;

import java.util.*;
import javax.microedition.lcdui.*;
import net.jxme.demo.blackjack.share.*;
import java.io.IOException;

/**
 * This singelton class handles the local management of the game that
 * is required to enable the gui to accurately represent the state of
 * the game.  It maintains a list of the players, and communicates
 * with the class that implements the protocol to the server.  When a
 * response is received from the server handling is delegated to the
 * currently active ResponseListener.
*/

public class Game
{
    /**
       The one and only instance of the Game 
    */
    public static final Game instance = new Game();
    
    /**
       The players in the current game
    */
    private Vector players = null;
    
    /**
       The player in turn
    */
    private int turn = ProtocolConstants.NO_ID;
    
    /**
       Indicates if the game has started yet or not
    */
    private boolean started = false;
        
    /**
       Indicates if all bets are in yet
    */
    private boolean betsIn = false;
    
    /**
       indicates if the game is over
    */
    //    private boolean gameOver = false;
    
    /**
       indicates if the game was reset
    */
    private boolean gameReset = true;
  
    /**
       Indicates which player is playing from this instance of the game
    */
    private int thisPlayer = ProtocolConstants.NO_ID;
    
    /**
       The current object that should handle responses from the server
    */
    private ResponseListener responseListener = null;
    
    /**
       Indicates which player is bust
    */
    private boolean[] bust = {false, false, false, false};
    
    /**
       Indicates which player has blackjack
    */
    private boolean[] blackjack = {false, false, false, false};

    /**
       Timer used to periodically query the server for various statuses    
    */
    private Timer statusTimer = null;

    /**
       The TimerTask that checks the application status
    */
    private StatusTask statusTask = null;
    
    /**
       The TimerTask that checks if the application has started
    */
    private StartStatusTask startStatusTask = null;

    private Game()
    {
        statusTimer = new Timer();
        players = new Vector(4);
    }

    /**
       Tells the game object where the servlet is found
    
       @param baseURL the base URL for requests from the servlet. The URL is of 
       the form "http://x.y.z/blackjack?"
    */
    public void setBaseURL(String baseURL)
    {
        Protocol.setBaseURL(baseURL);
    }

    /**
       Indicates that the player wants to start/join a game.
       Contacts the server and obtains an id for the player.
       If the id returned is 1, then this player is initiating a new game,
       otherwise the player is joining a game.
    */
    public void play()
    {
        //Switch off the reset flage when the game starts
        gameReset = false;
        Thread joinThread = new Thread()
            {
                public void run()
                {
                    try
                        {
                            String result = Protocol.join();
                            handleProtocolResult(result);
                            //get the response id and the data
                        }
                    catch(IOException e)
                        {
                            handleException(e);
                        }
                }
            };
        
        joinThread.start();

        // wait a bit to get the join response
        try {
            Thread.currentThread().sleep(1000);
        } catch(InterruptedException e) {}

        //Start a thread that periodically checks if the game should start
        try {
            startStatusTask = new StartStatusTask();
            statusTimer.schedule(startStatusTask, 0, 1000);
        } catch(IllegalStateException e) {
            Debug.println("Could not schedule StartStatusTask"+e);
        }
    }//end method

    /**
       Tells the server of this player's bet
    */
    public void bet(int theBet)
    {
        // Debug.println("Game.bet");
        final int betSum = theBet;
        Thread betThread = new Thread()
            {
                public void run()
                {
                    try
                        {
                            Protocol.bet(thisPlayer, betSum);
                        }
                    catch(IOException e)
                        {
                            handleException(e);
                        }
                }
            };
        betThread.start();

        try {
            Thread.currentThread().sleep(1000);
        } catch(InterruptedException e) {}

        //Start a thread that periodically checks game status
        try
            {
                statusTask = new StatusTask();
                statusTimer.schedule(statusTask, 0, 1000);
            }
        catch(IllegalStateException e)
            {
                Debug.println("Could not schedule StatusTask"+e);
            }
    }//end method
    
    /**
       See what bets are in
    */
    public void checkBets()
    {
        new Thread()
        {
            public void run()
            {
                try
                    {
                        String result = Protocol.checkBets(thisPlayer);
                        handleProtocolResult(result);
                    }
                catch(IOException e)
                    {
                        handleException(e);
                    }
            }
        }.run();
    }//end method

    /**
       Used by the first player who joins the game aka the initiating player, 
       to notify that enough users have joined the game and the game should start.
    */
    public void start()
    {
        try
            {
                Protocol.start(thisPlayer);
            }
        catch(IOException e)
            {
                handleException(e);
            }
    }

    /**
       Ask the server for a card
    */
    public void card()
    {
        try
            {
                Protocol.card(thisPlayer);
            }
        catch(IOException e)
            {
                handleException(e);
            }
    }

    /**
       Tell the server that you are holding
    */
    public void hold()
    {
        try
            {
                Protocol.hold(thisPlayer);
            }
        catch(IOException e)
            {
                handleException(e);
            }
    }


    /**
       Used to reset the servlet
    */
    public void reset()
    {
        doReset();
        //        players = new Vector(4);
        
        try
            {
                String result = Protocol.reset();
                handleProtocolResult(result);
            }
        catch(IOException e)
            {
                handleException(e);
            }
    }

    /**
       Used to notify the servlet that the contoller wants to start a new game
    */
    public void newGame()
    {
        try
            {
                Protocol.newGame();
            }
        catch(IOException e)
            {
                handleException(e);
            }
        
    }

    /**
       Helper method that parses a protocol result to a response ID and data,
       and invokes the response listener
    
       @param result the result to handle
    */
    private void handleProtocolResult(String result)
    {
        // Debug.println("handleProtocolResult: result = "+result);
        //DEBUG - if there is an internal server error print a message and return
        if(-1 != result.indexOf("html"))
            {
                Debug.println("************************** Got Internal Server Error");
                return;
            }
        
        int delim = result.indexOf(ProtocolConstants.FIELD_DELIMITER);

        String ridString = null;
        String data = null;
        if(delim != -1)
            {
                ridString = result.substring(0,delim);
                data = result.substring(delim+1);
            }
        else
            {
                ridString = result;
                data = "NO_DATA";
            }
        // Debug.println("\nridString: "+ridString);

        try
            {
                int rid = Integer.parseInt(ridString.trim());

                responseListener.onResponse(rid, data.trim());
            }
        catch(NumberFormatException e)
            {
                // Debug.println("Non integer response ID returned: "+ ridString);
            }
    }

    /**
       Helper method to handle exceptions when communicating with server
    */
    private void handleException(Exception e)
    {
        e.printStackTrace();
        String msg = e.getMessage();
        System.out.println(msg);
        responseListener.onResponse(ProtocolConstants.R_ERROR, msg);
    }

    /**
       Sets the appropriate response listener object
    
       @param ResopnseListener the response listener object
    */
    public void setResponseListener(ResponseListener l)
    {
        responseListener = l;
    }

    /**
       Gets the index of the local player
    
       @return the index of the local player
    */
    public int getThisPlayer()
    {
        return thisPlayer;
    }
    
    /**
       Adds a player to the list of players in this game
    
       @param Player the player to add
    */
    public void addPlayer(Player player)
    {
        players.addElement(player);
    }
    
    /**
       Returns a player with the specified id. The id happens to match the index
       in the vector of players
    
       @return the player with the specified id
    */
    public Player getPlayer(int id)
    {
        return (Player)players.elementAt(id);
    }
    
    /**
       Returns the players in this game
    
       @return the Vector of players
    */
    public Vector getPlayers()
    {
        return players;
    }
    
    /**
       Sets whose turn it is
    
       @param int the index of the player whose turn it is
    */
    public void setTurn(int playerIndex)
    {
        turn = playerIndex;
    }
    
    /**
       Returns the index of the player whose turn it currently is
    
       @return the index of the player whose turn it currently is
    */
    public int getTurn()
    {
        return turn;
    }
    
    /**
       Indicates if the game has started
    
       @return true if the game has started, false otherwise
    */
    public boolean isStarted()
    {
        return started;
    }
    
    /**
       Indicates if all bets are in or not
    
       @return true if all bets are in, false otherwise
    */
    public boolean areBetsIn()
    {
        return betsIn;
    }

    /**
       Sets the game to be over
        
       public void setGameOver()
       {
       gameOver = true;
       }
    */
    /**
       Sets the player with the indicated id to be bust
    */
    public void bustPlayer(int id)
    {
        bust[id] = true;
    }
    
    /**
       Indicates if the player with id is bust
    */
    public boolean isBust(int id)
    {
        return bust[id];
    }
    
    /**
       Raises a flag to show that the player with the specified ID got blackjack

       @param id the id of the player to set
    */
    public void setBlackjack(int id)
    {
        blackjack[id] = true;
    }
    
    /**
       Checks if a player has blackjack
    
       @param id the id of the player to check
    */
    public boolean hasBlackjack(int id)
    {
        return blackjack[id];
    }
    
    /**
       Sets which player is using this instance of the game
    
       @param int the index of the player that is using this instance of the game
    */
    public void setThisPlayer(int playerIndex)
    {
        thisPlayer = playerIndex;
    }
    
    /**
       Sets the started flag to indicate that the game has started
    */
    public void setStarted(boolean set)
    {
        started = set;
    }

    public void startNewGame()
    {
        //        doReset();
        turn = 1;
        gameReset = false;
        //        gameOver = false;
        started = true;
        betsIn = false;
        ResponseListener responseListener = null;
        statusTask.cancel();
        for(Enumeration e = players.elements(); e.hasMoreElements();)
            {
                Player p = (Player)e.nextElement();
                p.reset();
            }
        for(int i = 0; i < bust.length; i++)
            {
                bust[i] = false;
            }
        for(int j = 0; j < blackjack.length; j++)
            {
                blackjack[j] = false;
            }
    }//end method


    /**
       Helper method that resets the Game object for a new game
    */
    private void doReset()
    {
        statusTimer = new Timer();
        players = new Vector(4);
        
        turn = ProtocolConstants.NO_ID;
        gameReset = true;
        started = false;
        betsIn = false;
        //        gameOver = false;
        //        thisPlayer = ProtocolConstants.NO_ID;
        ResponseListener responseListener = null;
        for(int i = 0; i < bust.length; i++)
            {
                bust[i] = false;
            }
        for(int j = 0; j < blackjack.length; j++)
            {
                blackjack[j] = false;
            }
    }
    
    /**
       Inner class encapsulating the timer task that checks if the game should start
    */
    private class StartStatusTask extends TimerTask
    {
        private int busy = 0;
        public void run()
        {
            
            if(started || gameReset)
                {
                    cancel();
                }
            else
                {

                    try
                        {
                            if(busy != 0)
                                {
                                    System.out.println("Timeouts for START status timer:"+busy);
                                    return;
                                }
                            // increment busy counter
                            busy++;

                            // System.out.println("Sending START status request.");
                            String result = Protocol.startStatus(thisPlayer);
                            handleProtocolResult(result);
                            //get the response id and the data
                        }
                    catch(IOException e)
                        {
                            handleException(e);
                        }//catch

                    // decrement busy counter
                    busy--;
                }//else
        }//end method
    }//end inner class

    /**
       Inner class encapsulating the timer task that checks the game status
       This task is started when the user places his bet and should continue 
       throughout the rest of the game
    */
    private class StatusTask extends TimerTask
    {
        private int busy = 0;
        public void run()
        {
            
            //            if(gameOver || gameReset)
            if(gameReset)
                {
                    Debug.println("StatusTask: Cancelling");
                    //                Debug.println("gameOver = "+gameOver);
                    Debug.println("gameReset = "+gameReset);
                    cancel();
                }
            else
                {
            
                    try
                        {
                            if(busy != 0)
                                {
                                    System.out.println("Timeouts for STATUS timer:"+busy);
                                    return;
                                }
                            // increment busy counter
                            busy++;

                            // System.out.println("Sending status request");
                            String result = Protocol.status(thisPlayer);
                            handleProtocolResult(result);
                        }
                    catch(IOException e)
                        {
                            handleException(e);
                        }//catch

                    // decrement busy counter
                    busy--;
                }
        }//end method
    }//end inner class
    }//end class
