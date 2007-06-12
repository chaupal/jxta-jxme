/************************************************************************
 *
 * $Id: GameScreen.java,v 1.2 2001/11/06 00:39:19 akhil Exp $
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
import java.util.*;

/**
   The player places a bet with this screen
*/
public class GameScreen extends Canvas implements CommandListener, ResponseListener
{
    private static int SCREEN_WIDTH;
    private static int SCREEN_HEIGHT;

    //Coordinates of the screen title
    private int TITLE_X;
    private int TITLE_Y;

    //Coordinates of the information message 
    int INFO_X;
    int INFO_Y;
    //Coordinates of the line under the screen title
    private int LINE_X;
    private int LINE_Y;
    
    //Coordinates of the player image
    private int PLAYER_IMAGE_X;
    private int PLAYER_IMAGE_Y;
    
    //Dimensions of the player image
    private int PLAYER_IMAGE_WIDTH;
    private int PLAYER_IMAGE_HEIGHT;
    
    //Coordinates of the card for the current player
    private int BIG_CARD_X;
    private int BIG_CARD_Y;
    
    //Dimensions of the card for the current player
    private int BIG_CARD_WIDTH;
    private int BIG_CARD_HEIGHT;

    //Coordinate offset from big card coordinates of the suit icon for the current player
    private int BIG_IMAGE_XOFFSET = 3;
    private int BIG_IMAGE_YOFFSET = 13;
    
    //Dimensions of the suit icon for the current player
    private int BIG_IMAGE_WIDTH;
    private int BIG_IMAGE_HEIGHT;

    //Coordinates of the symbol icons for the current player
    private int BIG_SYM_X;
    private int BIG_SYM_Y;

    //Coordinates of name for other players
    private int NAME_X;
    private int NAME_Y;

    //Coordinates of bet for other players
    private int BET_X;
    private int BET_Y;
    
    //Coordinates of suit for other players
    private int SMALL_SUIT_X;
    private int SMALL_SUIT_Y;

    //Dimensions of the suit icons for the other players
    private int SMALL_SUIT_WIDTH;
    private int SMALL_SUIT_HEIGHT;

    //Coordinates of symbol for other players
    private int SMALL_SYM_X;
    private int SMALL_SYM_Y;

    //Dimensions of the symbol icons for the other players
    private int SMALL_SYM_WIDTH;
    private int SMALL_SYM_HEIGHT;

    //Coordiantes of the final status indication
    private int STATUS_X;
    private int STATUS_Y;
    
    private int colorBlack =   0x00000000;
    private int colorWhite =   0x00FFFFFF;

    private Command cmdCard = null;
    private Command cmdHold = null;
    private Command cmdNewGame = null;
    private Command cmdExit = null;
    
    private Image smallSuit = null;
    private Image smallSpade = null;
    private Image smallHeart = null;
    private Image smallDiamond = null;
    private Image smallClub = null;

    private Image bigSuit = null;
    private Image bigSpade = null;
    private Image bigHeart = null;
    private Image bigDiamond = null;
    private Image bigClub = null;

    //An information message
    private String info = "";
    
    public GameScreen()
    {
        super();
        SCREEN_WIDTH = getWidth();
        SCREEN_HEIGHT = getHeight();
        // Debug.println("width="+SCREEN_WIDTH+" height="+SCREEN_HEIGHT);

        cmdCard = new Command(Constants.STR_CARD, Command.SCREEN, 1);        
        cmdHold = new Command(Constants.STR_HOLD, Command.SCREEN, 1);        
        cmdNewGame = new Command(Constants.STR_NEW_GAME, Command.SCREEN, 1);      
        cmdExit = new Command(Constants.STR_EXIT, Command.SCREEN, 2);      
        
        addCommand(cmdExit);
        
        setCommandListener(this);
        //        loadImages();
    }
    
    public void setActive()
    {
        Game.instance.setResponseListener((ResponseListener)this);
        //Player 1 starts the game
        Game.instance.setTurn(1);
        Game.instance.checkBets();
        
        BlackjackMidlet.instance.display.setCurrent(this);
    }//setActive


    public void commandAction(Command c, Displayable s)
    {
        if(c == cmdCard)
            {
                info = Constants.STR_DEALING;
                repaint(0, LINE_Y, SCREEN_WIDTH, Font.getDefaultFont().getHeight() );
                serviceRepaints();
                Game.instance.card();
            }
        else if(c == cmdHold)
            {
                info = Constants.STR_HOLDING;
                repaint(0, LINE_Y, SCREEN_WIDTH, Font.getDefaultFont().getHeight() );
                serviceRepaints();
                Game.instance.hold();
            }
        else if(c == cmdExit)
            {
                info = "Exiting";
                repaint(0, LINE_Y, SCREEN_WIDTH, Font.getDefaultFont().getHeight() );
                serviceRepaints();
                Game.instance.reset();
                BlackjackMidlet.instance.notifyDestroyed();
            }
        else if(c == cmdNewGame)
            {
                info = "Restarting"; 
                repaint(0, LINE_Y, SCREEN_WIDTH, Font.getDefaultFont().getHeight() );
                serviceRepaints();
                Game.instance.newGame();
            }
        else
            {
                Debug.println("Unhandled command");
            }
        

    }
    
    public void onResponse(int code, String data)
    {
        // Debug.println("GameScreen.onResponse: code = "+code+" Data = "+data);
        switch(code)
            {
            
                //Server is notifying each player's bet
            case ProtocolConstants.R_BETS_IN: 
                {   
                    // Debug.println("GameScreen.onResponse: R_BETS_IN Data = "+data);
                    //Expecting data = ID,bet#ID,bet#....
                    //Parse the betting data and set it into the players
                    int beginIndex = 0;
                    int endIndex = data.indexOf(ProtocolConstants.FIELD_DELIMITER, beginIndex);
                    int id = ProtocolConstants.NO_ID;
                    int bet = 0;
                    int endData = data.length();
                    while(beginIndex < endData)
                        {
                            //if we're on the last card we adjust endIndex to the end of the string
                            if(endIndex == -1)
                                {
                                    endIndex = endData;
                                }
                    
                            String betString = data.substring(beginIndex, endIndex);
                            // Debug.println("Bet String is: "+betString);
                            //Bet String should be id,bet
                            //Get the id
                            id = Character.digit(betString.charAt(0), 10);
                            //Get the card
                            bet = Integer.parseInt(betString.substring(2));
                    
                            //                    Debug.println("id is: "+id);
                            //                    Debug.println("bet is: "+bet);
                    
                            Player player = Game.instance.getPlayer(id);
                            player.setBet(bet);
                    
                            beginIndex = endIndex+1;
                            endIndex = data.indexOf(ProtocolConstants.FIELD_DELIMITER, beginIndex);                    
                        }
                    repaint();
                }        
                break;

                //Server is notifying each player's cards
            case ProtocolConstants.R_CARDS: 
                {   
                    //                data = "0,9,H#0,K,S#1,K,D#1,A,S#2,8,C#2,1,H"; //simulates blackjack
                    //Expecting data = ID,card,suit#ID,card,suit#ID....
                    //Parse the data and create all the cards and add them to the players
                    int beginIndex = 0;
                    int endIndex = data.indexOf(ProtocolConstants.FIELD_DELIMITER, beginIndex);
                    int id = ProtocolConstants.NO_ID;
                    char card = 'a';
                    char suit = 's';
                    int endData = data.length();
                    while(beginIndex < endData)
                        {
                            //if we're on the last card we adjust endIndex to the end of the string
                            if(endIndex == -1)
                                {
                                    endIndex = endData;
                                }
                    
                            String cardString = data.substring(beginIndex, endIndex);
                            //Card String should be id,num,suit
                            //Get the id
                            id = Character.digit(cardString.charAt(0), 10);
                            //Get the card
                            card = cardString.charAt(2);
                            suit = cardString.charAt(4);
                    
                            Player player = Game.instance.getPlayer(id);
                            player.addCard(new Card(card, suit));
                    
                            //do all then...
                            beginIndex = endIndex+1;
                            endIndex = data.indexOf(ProtocolConstants.FIELD_DELIMITER, beginIndex);                    
                        }
                    //When we receive cards we check to see if we are the first player
                    //If we are we add the Card and Hold commands
                    int thisPlayer = Game.instance.getThisPlayer();
                    if(thisPlayer == 1)
                        {
                            info = Constants.STR_YOUR_TURN;
                            addCommand(cmdCard);
                            addCommand(cmdHold);
                        }
                
                    repaint();
                }        
                break;
            
            case ProtocolConstants.R_BLACKJACK:
                {
                    //Expecting data = id
                    // Debug.println("R_BLACKJACK: "+data);
                    int id = Character.digit(data.charAt(0), 10);
                    Game.instance.setBlackjack(id);
                
                }
                break; 

            case ProtocolConstants.R_BUST:
                {
                    // Debug.println("R_BUST: "+data);
                }
                //break; fall through
            case ProtocolConstants.R_CARD:
                {
                    //expecting data = ID#card,suit#Total
                    // Debug.println("R_CARD "+data);
                    //Get the id
                    int id = Character.digit(data.charAt(0), 10);
                    //Get the card
                    char card = data.charAt(2); //card
                    char suit = data.charAt(4); //suit
                    
                    Player player = Game.instance.getPlayer(id);
                    player.addCard(new Card(card, suit));
                    //do a repaint to update the screen
                    info = "";
                    repaint();
                }
                break;

            case ProtocolConstants.R_TURN:
                {
                    //expecting data = ID 
                    //where ID is the next player ID
                    // Debug.println("R_TURN: "+data);
                    //When turn changes update the game object and do a repaint
                    int turnID = Integer.parseInt(data);
                    Game.instance.setTurn(turnID);
                
                    int thisPlayer = Game.instance.getThisPlayer();
                    if(turnID == thisPlayer)
                        {
                            addCommand(cmdCard);
                            addCommand(cmdHold);
                            info = Constants.STR_YOUR_TURN;
                        }
                    else
                        {
                            removeCommand(cmdCard);
                            removeCommand(cmdHold);
                            info = "";
                        }
                    repaint();
                }
                break;
            
            case ProtocolConstants.R_END: 
                {
                    //expecting data = ID,outcome#ID,outcome#...
                    ///where outcome: 0-lost, 1-win, 2-draw
                    // Debug.println("R_END: "+data);
                
                    int beginIndex = 0;
                    int endIndex = data.indexOf(ProtocolConstants.FIELD_DELIMITER, beginIndex);
                    int id = ProtocolConstants.NO_ID;
                    int endStatus = 0;
                    int endData = data.length();
                    // Debug.println("beginIndex, endData "+ beginIndex +" "+endData);
                    while(beginIndex < endData)
                        {
                            //if we're on the last outcome we adjust endIndex to the end of the string
                            if(endIndex == -1)
                                {
                                    endIndex = endData;
                                }
                    
                            String endStatusString = data.substring(beginIndex, endIndex);
                    
                            //CendStatusString should be id,status
                            //Get the id
                            id = Character.digit(endStatusString.charAt(0), 10);
                            //Get the status
                            endStatus = Character.digit(endStatusString.charAt(2), 10);
                    
                            Player player = Game.instance.getPlayer(id);
                            player.setEndStatus(endStatus);
                    
                            //do all then...
                            beginIndex = endIndex+1;
                            endIndex = data.indexOf(ProtocolConstants.FIELD_DELIMITER, beginIndex);                    
                        }
                    repaint();
                    //If we are the initiating player we get the option to start a new game
                    int thisPlayer = Game.instance.getThisPlayer();
                    if(thisPlayer ==1)
                        {
                            addCommand(cmdNewGame);
                        }
                }
                break;

            case ProtocolConstants.R_NO_CHANGE: 
                //do nothing
                break;
                
            case ProtocolConstants.R_NEW_GAME: 
                //expecting data: NO_DATA
                //If we're receiving this response it means the game controller
                //has pressed the New game command. This has reset the server.
                //All we have to do is reset the players and go directly to the
                //betting screen
                Game.instance.startNewGame();
                new PlaceBet().setActive();
                break;

            case ProtocolConstants.R_ERROR: 
                {
                    Debug.println("GameScreen: Error"+data);
                    //TBD alert screen
                }
                break;
            
            default:
                System.out.println("Unhandled response: id = "+code+", Data = "+data);
                break;
            }//end switc            
    }//end method

    public void paint(Graphics g)
    {
        try
            {
                //clear the screen first
                g.setColor(colorWhite);
                g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
                g.setColor(colorBlack);
                drawTitle(g);
                drawCurrentPlayer(g);
                drawOtherPlayers(g);
            }
        catch(Exception e)
            {
                Debug.println("Exception in Paint:"+e);
            }
    }

    /**
       Draws the title and the underline
    */
    private void drawTitle(Graphics g)
    {
        Font titleFont = Font.getDefaultFont();
        int titleWidth = titleFont.stringWidth(Constants.STR_BLACKJACK);
        TITLE_X = (SCREEN_WIDTH - titleWidth)/2;
        TITLE_Y = 0;
        //Do drawString twice for "Bold" effect
        g.drawString(Constants.STR_BLACKJACK, TITLE_X, TITLE_Y, g.TOP | g.LEFT);
        g.drawString(Constants.STR_BLACKJACK, TITLE_X+1, TITLE_Y, g.TOP | g.LEFT);
            
        LINE_X = 5;
        LINE_Y = TITLE_Y + titleFont.getHeight()-2;
        //Draw double underline for "Bold" effect
        g.drawLine(LINE_X, LINE_Y, SCREEN_WIDTH-LINE_X, LINE_Y );
        g.drawLine(LINE_X, LINE_Y+1, SCREEN_WIDTH-LINE_X, LINE_Y+1 );
    }

    /**
       Draws the section of the screen showing the current player
    */
    private void drawCurrentPlayer(Graphics g)
    {
        //get the current player in turn
        int currentIndex = Game.instance.getTurn();
        int thisPlayer = Game.instance.getThisPlayer();
        Font defaultFont = Font.getDefaultFont();
        int fontHeight = defaultFont.getHeight();
        
        Player currentPlayer = Game.instance.getPlayer(currentIndex);
        
        int currentBet = currentPlayer.getBet();
        
        String currentInfo = null;
        if(currentIndex == 0)
            {
                //Current player is dealer
                currentInfo = Constants.STR_DEALER;
            }
        else
            {
                currentInfo = Constants.STR_BET+": $"+currentBet;
            }
        g.drawString(currentInfo, LINE_X, LINE_Y+3, g.TOP | g.LEFT);
        
        //Draw the info message
        INFO_X = LINE_X+defaultFont.stringWidth(currentInfo)+3;
        INFO_Y = LINE_Y+3;
        //erase the old message
        g.setColor(colorWhite);
        g.fillRect(INFO_X, INFO_Y, defaultFont.stringWidth(Constants.STR_DEALING),fontHeight );
        g.setColor(colorBlack);
        //write the new one
        //Do drawString twice for "Bold" effect
        g.drawString(info, INFO_X, INFO_Y, g.TOP | g.LEFT);
        g.drawString(info, INFO_X+1, INFO_Y, g.TOP | g.LEFT);

        //Draw the image representing the player
        Image currentPlayerImage = currentPlayer.getImage();
        PLAYER_IMAGE_X = 0;//LINE_X;
        PLAYER_IMAGE_Y = LINE_Y+fontHeight+2;
        PLAYER_IMAGE_WIDTH = currentPlayerImage.getWidth();
        PLAYER_IMAGE_HEIGHT = currentPlayerImage.getHeight();
        g.drawImage(currentPlayerImage, PLAYER_IMAGE_X, PLAYER_IMAGE_Y, g.TOP | g.LEFT);
        //Get the player's cards
        Vector cards = currentPlayer.getCards();
        BIG_CARD_X = PLAYER_IMAGE_X+PLAYER_IMAGE_WIDTH;
        BIG_CARD_Y = PLAYER_IMAGE_Y;
        BIG_CARD_WIDTH = 31;
        BIG_CARD_HEIGHT = 40;
        BIG_SYM_Y = BIG_CARD_Y + BIG_CARD_HEIGHT+2;
        int numCards = cards.size();
        for(int i = 0; i < numCards; i++)
            {
                BIG_SYM_X = BIG_CARD_X+2;
                BIG_SYM_Y = BIG_CARD_Y;
                Card currentCard = (Card)cards.elementAt(i);
                char currentSuit = currentCard.getSuit();
                bigSuit = Images.getInstance().getImage(currentSuit, true);
                String currentSymbol = currentCard.getSymbol();

                g.drawRoundRect(BIG_CARD_X, BIG_CARD_Y, BIG_CARD_WIDTH, BIG_CARD_HEIGHT, 4, 4);
                g.drawString(currentSymbol, BIG_SYM_X, BIG_SYM_Y,  g.TOP | g.LEFT);
                g.drawImage(bigSuit, BIG_CARD_X+BIG_IMAGE_XOFFSET, BIG_CARD_Y+BIG_IMAGE_YOFFSET, g.TOP | g.LEFT);
                //Now "erase" the right half of the card by drawing filled white rectangle 
                //over it for all but the last card
                if(i != numCards-1)
                    {    
                        g.setColor(colorWhite);
                        g.fillRect(BIG_CARD_X+BIG_CARD_WIDTH/2, BIG_CARD_Y, BIG_CARD_WIDTH, BIG_CARD_HEIGHT);
                        g.setColor(colorBlack);
                    }
            
                BIG_CARD_X += BIG_CARD_WIDTH/2;
            }
        int currentOutcome = currentPlayer.getOutcome();
        int OUTCOME_X = BIG_CARD_X+BIG_CARD_WIDTH/2+2;
        int OUTCOME_Y = BIG_CARD_Y+BIG_CARD_HEIGHT-fontHeight;
        if(currentOutcome != 0)
            {
                if(currentOutcome <= 21)
                    {
                        g.drawString(" "+currentOutcome, OUTCOME_X, OUTCOME_Y,  g.TOP | g.LEFT);
                    }
                else
                    {
                        g.drawString(" "+currentOutcome+Constants.STR_BUST, OUTCOME_X, OUTCOME_Y,  g.TOP | g.LEFT);
                    }
            }//if(currentOutcome != 0)
        
    }//end method

    private void drawOtherPlayers(Graphics g)
    {
        SMALL_SUIT_WIDTH = 10;
        SMALL_SUIT_HEIGHT = 10;
        SMALL_SYM_WIDTH = SMALL_SUIT_WIDTH;
        SMALL_SYM_HEIGHT = SMALL_SUIT_HEIGHT;
        
        NAME_X = LINE_X;
        NAME_Y = BIG_CARD_Y+BIG_CARD_HEIGHT+6;
        //The longest name to display is "You" i.e. thisPlayerName
        Font defaultFont = Font.getDefaultFont();
        int maxNameLength = defaultFont.stringWidth(Constants.thisPlayerName);
        BET_X = NAME_X+maxNameLength+2;
        BET_Y = NAME_Y;

        Vector players = Game.instance.getPlayers();
        int numPlayers = players.size();
        
        //count to numPlayers-1 because the current player is displayed in large
        //figures at the upper part of the screen
        for(Enumeration e = players.elements(); e.hasMoreElements();)
            {
                Player p = (Player)e.nextElement();
                boolean isDealer = (p.getId() == ProtocolConstants.DEALER);
            
                //Check if this player is in turn right now.
                //If he is we don't display him here, because he is displayed
                //above.
                int turn = Game.instance.getTurn();
                Player turnPlayer = Game.instance.getPlayer(turn);
                if(p.equals(turnPlayer))
                    {
                        continue;
                    }
                String label = p.getLabel();
                int bet = p.getBet();
                g.drawString(label+": ", NAME_X, NAME_Y, g.TOP | g.LEFT);
            
                if(!isDealer)
                    {
                        g.drawString("$"+bet, BET_X, BET_Y, g.TOP | g.LEFT);
                    }

                //Determine where the final status will go here, where we know
                //the x coordinate of the first card
                STATUS_X = BET_X;
                SMALL_SUIT_X = BET_X+15;
                SMALL_SUIT_Y = BET_Y+3;
                SMALL_SYM_X = SMALL_SUIT_X+SMALL_SUIT_WIDTH+2;
                SMALL_SYM_Y = BET_Y;

                Vector cards = p.getCards();
                if(isDealer)
                    {
                        drawDealerCards(g, cards);
                    }
                else
                    {
                        drawPlayerCards(g, cards);
                    }
            
                int outcome = p.getOutcome();
                int numCards = cards.size();
                String status = p.getEndStatus();
                if(status != Constants.STATUS_UNDEFINED)
                    {
                        //Put the status on the next line
                        STATUS_Y = SMALL_SYM_Y+defaultFont.getHeight();
                        //Update the location for the next player
                        NAME_Y += defaultFont.getHeight();
                        //Draw twice for bold effect
                        g.drawString(status, STATUS_X, STATUS_Y, g.TOP | g.LEFT);
                        g.drawString(status, STATUS_X+1, STATUS_Y, g.TOP | g.LEFT);
                    }
            
                NAME_Y += SMALL_SUIT_HEIGHT+4;
                BET_Y = NAME_Y;
            }//for ... players
        
    }//end method
    
    /**
       Helper method to simplify drawOthers
    */
    private void drawDealerCards(Graphics g, Vector cards)
    {
        int numCards = cards.size();
        for( int i = 0; i < numCards; i++)
            {
                Card card = (Card)cards.elementAt(i);
                String symbol = card.getSymbol();
                char suit = card.getSuit();
                smallSuit = Images.getInstance().getImage(suit, false);

                if(i == 0)
                    {
                        //draw the first card as an unknown
                        g.fillRect(SMALL_SUIT_X, SMALL_SUIT_Y, 7, 10);
                        g.drawString("?", SMALL_SYM_X, SMALL_SYM_Y, g.TOP | g.LEFT);
                    }
                else
                    {
                        g.drawImage(smallSuit, SMALL_SUIT_X, SMALL_SUIT_Y, g.TOP | g.LEFT);
                        g.drawString(symbol, SMALL_SYM_X, SMALL_SYM_Y, g.TOP | g.LEFT);
                    }
                
                SMALL_SUIT_X = SMALL_SYM_X+SMALL_SYM_WIDTH+4;
                SMALL_SYM_X = SMALL_SUIT_X+SMALL_SUIT_WIDTH+2;
            }//for ... cards
    }

    /**
       Helper method to simplify drawOthers
    */
    private void drawPlayerCards(Graphics g, Vector cards)
    {
        int numCards = cards.size();
        for( int i = 0; i < numCards; i++)
            {
                Card card = (Card)cards.elementAt(i);
                String symbol = card.getSymbol();
                char suit = card.getSuit();
                smallSuit = Images.getInstance().getImage(suit, false);

                g.drawImage(smallSuit, SMALL_SUIT_X, SMALL_SUIT_Y, g.TOP | g.LEFT);
                g.drawString(symbol, SMALL_SYM_X, SMALL_SYM_Y, g.TOP | g.LEFT);
                
                SMALL_SUIT_X = SMALL_SYM_X+SMALL_SYM_WIDTH+4;
                SMALL_SYM_X = SMALL_SUIT_X+SMALL_SUIT_WIDTH+2;
            }//for ... cards
    }
    
    /**
       Maps a suit letter to the appropriate image
    
       @param suit the letter defining the suit
       @param big true if the caller wants the big image
    */
    /*    
          private Image getImage(char suit, boolean big)
          {
          Image img = null;
          if(big)
          {
          switch(suit)
          {
          case 's':
          case 'S':
          img = bigSpade;
          break;

          case 'h':
          case 'H':
          img = bigHeart;
          break;
                    
          case 'd':
          case 'D':
          img = bigDiamond;
          break;
                    
          case 'c':
          case 'C':
          img = bigClub;
          break;
                    
          default:
          img = bigSpade;
          break;
          }
          }
          else
          {
          switch(suit)
          {
          case 's':
          case 'S':
          img = smallSpade;
          break;

          case 'h':
          case 'H':
          img = smallHeart;
          break;
                    
          case 'd':
          case 'D':
          img = smallDiamond;
          break;
                    
          case 'c':
          case 'C':
          img = smallClub;
          break;
                    
          default:
          img = smallSpade;
          break;
          }
          }
        
          return img;
          }//end method


          private void loadImages()
          {
          try
          {
          smallSpade = Image.createImage("/SmallSpade.png");
          smallHeart = Image.createImage("/SmallHeart.png");
          smallDiamond = Image.createImage("/SmallDiamond.png");
          smallClub = Image.createImage("/SmallClub.png");

          bigSpade = Image.createImage("/BigSpade.png");
          bigHeart = Image.createImage("/BigHeart.png");
          bigDiamond = Image.createImage("/BigDiamond.png");
          bigClub = Image.createImage("/BigClub.png");
          }
          catch(IOException e)
          {
          System.out.println("Error loading image\n"+e);
          }
          }//end method
    */
    }//end class
