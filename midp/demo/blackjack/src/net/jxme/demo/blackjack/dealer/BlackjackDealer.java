/************************************************************************
 *
 * $Id: BlackjackDealer.java,v 1.2 2001/11/05 21:59:33 akhil Exp $
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
 * Created: Tue May 1 2001 by Yael Gavish
 */

package net.jxme.demo.blackjack.dealer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;

import net.jxme.demo.blackjack.share.ProtocolConstants;

public class BlackjackDealer
{
    // different states of the game
    public static final int NOT_STARTED = 0;
    public static final int NEW_GAME = 1;   // a new game was started with the same players
    public static final int BETTING = 2;    // waiting for all bets
    public static final int START_PLAYING = 3;    // server informs all players that the game started
    public static final int PLAYING = 4;    // initial hands were already dealt
    public static final int NEW_TURN = 5;
    public static final int AWAITING_PLAYER = 6; // waiting for instructions: card/hold
    public static final int CARD_DEALT = 7; // a card was dealt
    public static final int HOLD = 8;
    public static final int BUST = 9;
    public static final int BLACKJACK = 10;
    public static final int DEALER_BLACKJACK = 11;
    public static final int ENDED = 12;
    // outcome of the game
    public static final int LOST = 0;
    public static final int WON = 1;
    public static final int DRAW = 2;
    public static final int NO_OUTCOME = 3;
    public static final int PLAYER_HOLD = 4;
    
    private Deck deck = null;
    private Player dealer = null;
    private Player currentPlayer = null;
    private Vector players = null;
    private int numOfPlayers = 0;
    private int numOfPlayersLeft = 0;       // not busted / blackjack
    private int numOfPlayersHolding = 0;    // chose "hold" on their last turn
    private int numOfBetsIn = 0;
    private int gameStatus = NOT_STARTED;
    private int lastDealOutcome = NOT_STARTED;
    // keep track of who was informed of a status change
    StringBuffer statusReply = null;        // contains the latest status reply
    StringBuffer cardDealtReply = null;     // contains the reply with the latest card dealt
    private int numOfRepliesSent = 0;       // how many players received the latest update
    private boolean[] playerReceivedReply;  // an array to keep track of who received 
                                            // the latest status update
    private boolean nextGameWithSamePlayers = false;

    // stub, for now
    private static class Config {
    }
    
    // stub, for now
    private static class Request {

        String id = "";
        String cmd = "";
        String sum = "";

        /**
         * @param message must be a URL of the form 
         * <pre>baseURL+"?ID="+id+"&cmd="+CMD_BET+"&sum="+sum</pre>
         */
        public Request(String message) {
            int len = message.length();
            for (int i=0; i < len; i++) {
                if (i+3 < len &&
                    message.charAt(i) == 'I' &&
                    message.charAt(i+1) == 'D' &&
                    message.charAt(i+2) == '=') {
                    int end = message.indexOf('&');
                    if (end < 0) {
                        end = len;
                    }
                    id = message.substring(i+3, end);
                } else if (i+4 < len &&
                           message.charAt(i) == 'c' &&
                           message.charAt(i+1) == 'm' &&
                           message.charAt(i+2) == 'd' &&
                           message.charAt(i+3) == '=') {
                    int end = message.indexOf('&', i+4);
                    if (end < 0) {
                        end = len;
                    }
                    cmd = message.substring(i+4, end);
                } else if (i+4 < len &&
                           message.charAt(i) == 's' &&
                           message.charAt(i+1) == 'u' &&
                           message.charAt(i+2) == 'm' &&
                           message.charAt(i+3) == '=') {
                    int end = message.indexOf('&', i+4);
                    if (end < 0) {
                        end = len;
                    }
                    sum = message.substring(i+4, end);
                }
            }
        }

        public String getParameter(String name) { 
            if ("ID".equals(name)) {
                return id;
            } else if ("cmd".equals(name)) {
                return cmd;
            } else if ("sum".equals(name)) {
                return sum;
            }
            return null;
        }
    }
    
    // stub, for now
    private static class Response {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos); 

        PrintStream getWriter() { 
            return ps; 
        }
    }
    
    public void init(Config config)
    {
        resetGame();
    }

    public void destroy()
    {
    }

    public void doGet(Request request, Response response)
	throws IOException
    {
        String cmd = request.getParameter("cmd");
        int command = Integer.valueOf(cmd).intValue();
        String player_id = request.getParameter("ID");
        int id = Integer.valueOf(player_id).intValue();
        PrintStream out = response.getWriter();
        Player player = findPlayer(id); // null when id = NO_ID
        
        switch (command) 
            {
            case ProtocolConstants.CMD_JOIN:
                id = addPlayer();   // returns new id
                // reply: new id
                out.print(ProtocolConstants.R_ID);
                out.print(ProtocolConstants.FIELD_DELIMITER);
                out.println(id);
                break;
            case ProtocolConstants.CMD_START_STATUS:
                // reply: started-true/false, num of players so far
                out.print(ProtocolConstants.R_START_STATUS);
                out.print(ProtocolConstants.FIELD_DELIMITER);
                if (gameStatus == NOT_STARTED)
                    out.print(ProtocolConstants.START_FALSE);
                else
                    out.print(ProtocolConstants.START_TRUE);
                out.print(ProtocolConstants.FIELD_DELIMITER);
                out.println(numOfPlayers);
                break;
            case ProtocolConstants.CMD_START:
                // 1st player is starting the game
                gameStatus = BETTING;
                // reply: none
                break;
            case ProtocolConstants.CMD_STATUS:
                // what is the current status of the game
                sendStatus(out, player);
                break;
            case ProtocolConstants.CMD_CARD:
                // player requests a card
                if (currentPlayer == null ||
                    currentPlayer.getId() != player.getId())
                    {
                        log("Player " + player.getId() + 
                            " should wait for his turn");
                        return;
                    }
                dealCardAtPlayersRequest();
                // reply: none
                break;
            case ProtocolConstants.CMD_BET:
                // user is placing a bet
                String bet = request.getParameter("sum");
                int bet_sum = Integer.valueOf(bet).intValue();
                placeBet(player, bet_sum);
                // reply: none
                break;
            case ProtocolConstants.CMD_BETS_IN:
                // reply: how many bets are in
                sendBetsIn(out, player);
                break;
            case ProtocolConstants.CMD_HOLD:
                if (currentPlayer == null ||
                    currentPlayer.getId() != player.getId())
                    {
                        log("Player " + player.getId() + 
                            " should wait for his turn");
                        return;
                    }
                // player wants no more cards
                gameStatus = HOLD;
                player.setOutcome(PLAYER_HOLD);
                numOfPlayersHolding++;
                numOfPlayersLeft--;
                // reply: none
                break;
            case ProtocolConstants.CMD_RESTART:
                resetGame();
                out.println("Game reset!");
                break;
            case ProtocolConstants.CMD_NEW_GAME:
                if (gameStatus == ENDED && 
                    numOfRepliesSent == numOfPlayers)   // all players received final game result
                    {
                        startNewGameWithSamePlayers(); 
                    }
                else
                    nextGameWithSamePlayers = true;  // after all players receive end status, 
                                                     // game will be reset with the same players
                // reply: none
                break;
                // default: error, ignore
            }
        out.close();
    }

    private int addPlayer()
    {
        Player new_player = new Player(++numOfPlayers);
        players.addElement(new_player);
        return new_player.getId();
    }
    
    private Player findPlayer(int id)
    {
        Enumeration all_players = players.elements();
        while (all_players.hasMoreElements())
            {
                Player curr_player = (Player)(all_players.nextElement());
                if (curr_player.getId() == id)
                    return curr_player;
            }
        return null;
    }
    
    private void placeBet(Player player, int bet)
    {
        player.placeBet(bet);
        if (++numOfBetsIn == numOfPlayers)
            {
                startPlaying();
            }
    }
    
    private void startPlaying()
    {
        // initialize array of received replies
        playerReceivedReply = new boolean[numOfPlayers+1];  // index 0 not used
        zeroRepliesInfo();
        numOfPlayersLeft = numOfPlayers;
        gameStatus = START_PLAYING;
        setStatusReplyToGameStarted();  // message will be sent on next status inquiry
    }
    
    // deal initial hands to player + dealer
    private void dealInitialHands()
    {
        Enumeration all_players = players.elements();
        
        statusReply.append(ProtocolConstants.R_CARDS);
        statusReply.append(ProtocolConstants.FIELD_DELIMITER);
        
        dealCard(dealer);
        dealCard(dealer);        
        if (dealer.getCardsValue() == 21)
            gameStatus = DEALER_BLACKJACK;
        statusReply.append(dealer.listCards()); // prepare message with list of dealer's cards
        
        while (all_players.hasMoreElements())
            {
                statusReply.append(ProtocolConstants.FIELD_DELIMITER);
                Player curr_player = (Player)(all_players.nextElement());
                dealCard(curr_player);
                dealCard(curr_player);
                statusReply.append(curr_player.listCards());    // add current player's cards to message
            }
    }
    
    private Card dealCard(Player player)
    {
        Card card = deck.drawCard();
        player.addCard(card);
        return card;
    }
    
    private void sendBetsIn(PrintStream out, Player player)
    {
        Enumeration all_players = players.elements();
        
        out.print(ProtocolConstants.R_BETS_IN);
        //        out.print(ProtocolConstants.FIELD_DELIMITER);
        //        out.print(numOfBetsIn);
        
        while (all_players.hasMoreElements())
            {
                Player curr_player = (Player)(all_players.nextElement());
                if (curr_player.getBet() != 0)
                    {
                        out.print(ProtocolConstants.FIELD_DELIMITER);
                        out.print(curr_player.getId());
                        out.print(',');
                        out.print(curr_player.getBet());
                    }
            }
        out.println("");
    }
    
    private void setStatusReplyToGameStarted()
    {
        statusReply.append(ProtocolConstants.R_GAME_STARTED);
    }
    
    private void sendStatus(PrintStream out, Player player)
    {
        int player_id = 0;
        
        try     // in case player asks for status after game was reset
            {
                player_id = player.getId();
            } 
        catch (java.lang.NullPointerException e)
            {
                log("player asked for status after game ended");
            }
        if (gameStatus == NOT_STARTED || 
            (gameStatus == ENDED && numOfRepliesSent == numOfPlayers))  // waiting to start next game
            {
                sendNoChange(out, player_id);
                return;
            }
        if (gameStatus == BETTING)
            {
                sendBettingStatus(out);
                return;
            }
        // in all the other cases we must count how many players 
        // received the status reply
        if (numOfRepliesSent != numOfPlayers) 
            {
                if (playerReceivedReply[player_id] == false)
                    {
                        sendStatusReply(out, player_id);
                        if (gameStatus == ENDED && 
                            numOfRepliesSent == numOfPlayers &&   // last player received status ENDED
                            nextGameWithSamePlayers == true)
                            {
                                startNewGameWithSamePlayers();
                            }
                    }
                else
                    sendNoChange(out, player_id);
                return;
            }
        // all recipients received the latest status, move to a new one
        
        // was a card dealt while players were informed that turn has changed?
        if (cardDealtReply != null) 
            {
                gameStatus = lastDealOutcome;
                zeroRepliesInfo();
                statusReply.append(cardDealtReply.toString());
                cardDealtReply = null;
                sendStatusReply(out, player_id);
                return;
            }
        zeroRepliesInfo();
        changeStatus(); // move to the next state of the game
        // if we reached here all players received the previous status 
        // and now it can change
        switch (gameStatus)
            {
            case BETTING:   // reached only if the a new game started with the same players
                sendBettingStatus(out);
                return;
            case AWAITING_PLAYER:
                sendNoChange(out, player_id);
                return;
                // in all other cases send reply in sendStatusReply()
            case PLAYING:
                dealInitialHands();
                break;
            case DEALER_BLACKJACK:
            case NEW_TURN:
                statusReply.append(ProtocolConstants.R_TURN);
                statusReply.append(ProtocolConstants.FIELD_DELIMITER);
                statusReply.append(currentPlayer.getId());
                break;
            case BLACKJACK:
                statusReply.append(ProtocolConstants.R_BLACKJACK);
                statusReply.append(ProtocolConstants.FIELD_DELIMITER);
                statusReply.append(currentPlayer.getId());
                break;
            case ENDED:
                statusReply.append(ProtocolConstants.R_END);
                statusReply.append(ProtocolConstants.FIELD_DELIMITER);
                statusReply.append(calculateGameOutcome());
                break;
            }
        sendStatusReply(out, player_id);
    }
    
    
    private void sendBettingStatus(PrintStream out)
    {
        out.print(ProtocolConstants.R_WAITING_BETS);
        out.print(ProtocolConstants.FIELD_DELIMITER);
        out.print(numOfBetsIn);
        out.print(ProtocolConstants.FIELD_DELIMITER);
        out.println(numOfPlayers);
    }
    
    private String calculateGameOutcome()
    {
        Enumeration all_players = players.elements();
        StringBuffer outcome_str = new StringBuffer();
        int dealerCardsValue = dealer.getCardsValue();
        
        outcome_str = new StringBuffer();
        // if player is busted it will be discovered when the last card was
        // drawn, and the outcome will already be set
        for (int i=0; i<numOfPlayers; i++)
            {
                Player player = (Player)(all_players.nextElement());
                int playerCardsValue;
                int outcome = player.getOutcome();
            
                if (outcome == PLAYER_HOLD || outcome == NO_OUTCOME)
                    {
                        playerCardsValue = player.getCardsValue();
                        if (dealerCardsValue == playerCardsValue)
                            player.setOutcome(DRAW);
                        else if (playerCardsValue < dealerCardsValue &&
                                 dealerCardsValue <= 21)
                            player.setOutcome(LOST);
                        else
                            player.setOutcome(WON);
                    }
                outcome_str.append(player.getId());
                outcome_str.append(',');
                outcome_str.append(player.getOutcome());
                if (i < numOfPlayers-1)
                    outcome_str.append(ProtocolConstants.FIELD_DELIMITER);
            }
        return outcome_str.toString();
    }
    
    private void sendStatusReply(PrintStream out, int player_id)
    {
        // servlet log
        if (statusReply != null)
            log("game status = " + gameStatus + 
                " player " + player_id + 
                " status reply = " + statusReply.toString());
        else 
            log("game status = " + gameStatus + 
                " player " + player_id);
        
        playerReceivedReply[player_id] = true;
        numOfRepliesSent++;
        out.println(statusReply);
    }
    
    private void sendNoChange(PrintStream out, int player_id)
    {
        out.print(ProtocolConstants.R_NO_CHANGE);
        out.print(ProtocolConstants.FIELD_DELIMITER);
        out.println(player_id);
    }
    
    private void zeroRepliesInfo()
    {
        numOfRepliesSent = 0;
        statusReply = new StringBuffer();
        for (int i=0; i<=numOfPlayers; i++)
            {
                playerReceivedReply[i] = false;
            }
    }
    
    private void changeStatus()
    {
        log("status changed from " + gameStatus);
        switch (gameStatus)
            {
            case NEW_GAME:
                gameStatus = BETTING;
                break;
            case START_PLAYING:     // all players know we started the game, now cards can be dealt
                gameStatus = PLAYING;
                break;
            case NEW_TURN:      // fall through - we only reach here when all players know about the new turn or
            case CARD_DEALT:    // the card that has been dealt, but the player still hasn't sent his next move
                if (currentPlayer.getId() != ProtocolConstants.DEALER)  // current player cannot be null
                    {
                        gameStatus = AWAITING_PLAYER;
                        break;
                    }
                // else card was dealt to dealer - fall through to continue dealer's game
            case PLAYING:       // fall through - if cards were dealt find out if player has blackjack
            case BUST:          // fall through
            case HOLD:          // fall through
            case BLACKJACK:     // fall through
                // move on to next player's turn
                if (numOfPlayersLeft > 0)
                    {
                        int next_player_id;
                        if (currentPlayer == null)
                            next_player_id = 1;
                        else
                            {
                                next_player_id = currentPlayer.getId()+1;
                            }
                        currentPlayer = findPlayer(next_player_id);
                        // blackjack is discovered
                        if (currentPlayer.getCardsValue() == 21)
                            {
                                currentPlayer.setOutcome(WON);  // YG - is this the place?
                                numOfPlayersLeft--;
                                gameStatus = BLACKJACK;
                            }
                        else 
                            {
                                gameStatus = NEW_TURN;
                                log("new turn: player "+currentPlayer.getId());
                            }
                    }
                else    // dealer should play now
                    {
                        if (currentPlayer.getId() != ProtocolConstants.DEALER)  // we haven't moved to the dealer yet
                            {
                                currentPlayer = dealer;
                                gameStatus = NEW_TURN;
                            }
                        else
                            {
                                if (numOfPlayersHolding > 0)    // not all players are busted or have blackjack
                                    {
                                        dealerPlays();
                                    } 
                                else
                                    {
                                        gameStatus = ENDED;
                                    }
                            }
                    }
                break;
            case DEALER_BLACKJACK:
                if (currentPlayer == null)  // a NEW_TURN message hasn't been sent yet
                    {
                        currentPlayer = dealer;
                    }
                else    // message has been sent, now end game
                    {
                        gameStatus = ENDED;
                    }
                break;
            }
        log(" to " + gameStatus);
    }
    
    private void dealerPlays()
    {
        int dealerCardsValue = dealer.getCardsValue();
        if (dealerCardsValue < 17)
            {
                log("deal card to the dealer");
                dealCardAtPlayersRequest();
                log("dealer cards value is now " + dealer.getCardsValue());
                // change of status to CARD_DEALT will occur when the cardDealtReply is seen
            }
        else
            {
                gameStatus = ENDED;
            }
    }
    
    private void dealCardAtPlayersRequest()
    {
        Card card = dealCard(currentPlayer);
        int playerCardsValue = currentPlayer.getCardsValue();
        cardDealtReply = new StringBuffer();                   
        
        if (playerCardsValue > 21)
            {
                currentPlayer.setOutcome(LOST);
                numOfPlayersLeft--;
                cardDealtReply.append(ProtocolConstants.R_BUST);
                lastDealOutcome = BUST;
            }
        else
            {
                cardDealtReply.append(ProtocolConstants.R_CARD);
                lastDealOutcome = CARD_DEALT;
            }
        cardDealtReply.append(ProtocolConstants.FIELD_DELIMITER);
        cardDealtReply.append(currentPlayer.getId());
        cardDealtReply.append(ProtocolConstants.FIELD_DELIMITER);
        cardDealtReply.append(card.toString());
        cardDealtReply.append(ProtocolConstants.FIELD_DELIMITER);
        cardDealtReply.append(playerCardsValue);            
    }
    
    private void resetGame()
    {
        deck = new Deck();
        deck.shuffle();
        dealer = new Player(ProtocolConstants.DEALER);
        players = new Vector();
        currentPlayer = null;
        numOfPlayers = 0;
        numOfPlayersHolding = 0;
        numOfBetsIn = 0;
        gameStatus = NOT_STARTED;
        // keep track of who was informed of a status change
        statusReply = null;
        cardDealtReply = null;
        numOfRepliesSent = 0;
        nextGameWithSamePlayers = false;
    }
    
    private void startNewGameWithSamePlayers()
    {
        deck = new Deck();
        deck.shuffle();
        dealer = new Player(ProtocolConstants.DEALER);
        currentPlayer = null;
        numOfPlayersHolding = 0;
        numOfBetsIn = 0;
        gameStatus = NEW_GAME;
        cardDealtReply = null;
        // keep track of who was informed of a status change
        zeroRepliesInfo();
        resetPlayers();
        setStatusReplyToNewGame();
        nextGameWithSamePlayers = false;
    }
    
    private void resetPlayers() // used for new game with the same players
    {
        Enumeration all_players = players.elements();
        while (all_players.hasMoreElements())
            {
                Player curr_player = (Player)(all_players.nextElement());
                curr_player.reset();
            }
    }
    
    private void setStatusReplyToNewGame()
    {
        statusReply = new StringBuffer();
        statusReply.append(ProtocolConstants.R_NEW_GAME);
    }

    private void log(String msg) 
    {
        System.out.println(msg);
    }
}
