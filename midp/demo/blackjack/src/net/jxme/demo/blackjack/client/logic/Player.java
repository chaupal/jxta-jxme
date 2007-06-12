/************************************************************************
 *
 * $Id: Player.java,v 1.1 2001/11/05 20:00:22 akhil Exp $
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
import java.io.IOException;
import javax.microedition.lcdui.*;
import net.jxme.demo.blackjack.share.*;

/**
 * A player in the blackjack game
 */
public class Player
{
    /**
       The cards that the player holds
    */
    private Vector cards = null;
    
    /**
       The bet that the player placed
    */
    private int bet;
    
    /**
       The Player's id
    */
    private int id;
    
    /**
       The player label
    */
    private String label;
    
    /**
       The outcome of the player's cards. 
    */
    private int outcome = 0;
    
    /**
       The image used to display this player when he is in turn
    */
    private Image image = null;
    
    /**
       The player's status at the end of the game (WIN/LOSE/DRAW)
    */
    private String endStatus = Constants.STATUS_UNDEFINED;

    /**
       Constructor. Player label and image are directly related to player id
       unless this is the local player
    */
    public Player(int anId)
    {
        id = anId;
        int thisPlayer = Game.instance.getThisPlayer();
        String imageFile = null;
        if(id == thisPlayer)
            {
                label = Constants.thisPlayerName;
                imageFile = Constants.thisPlayerImageFile;
            }
        else
            {
                label = Constants.playerNames[id];
                imageFile = Constants.playerImageFiles[id];
            }

        try
            {
                image = Image.createImage(imageFile);
            }
        catch(IOException e)
            {
                //if for some reason we can not load the player image file
                //then create a blank image and print a message
                Debug.println("Could not create image for player "+id);
                Debug.println(e.toString());
                image = Image.createImage(50,30);
            }
        
        cards = new Vector(7);
    }

    public int getId()
    {
        return id;
    }
    
    public Image getImage()
    {
        return image;
    }
    
    public int getBet()
    {
        return bet;
    }
    
    public String getLabel()
    {
        return label;
    }

    public void setBet(int aBet)
    {
        bet = aBet;
    }
    
    public void setID(int anID)
    {
        id = anID;
    }
        
    /**
       Add a card and calculate the new outcome.  We first add the
       first possible value of the card. If this busts the player then
       we deduct the value just added, and try adding the second value
       instead.  Note that this can only help if the card is an ace
    
       @param aCard the card added
       @return the outcome after adding the card
    */
    public int addCard(Card aCard)
    {
        cards.addElement(aCard);
        outcome = calculateOutcome();

        return outcome;
    }
    
    /**
       Returns the vector of cards for this player
    
       @return the vector of cards for this player
    */
    public Vector getCards()
    {
        return cards;
    }
    
    public boolean equals(Object other)
    {
        boolean eq = false;
        if(other instanceof Player)
            {
                int otherId = ((Player)other).getId();
            
                if(id == otherId)
                    {
                        eq = true;
                    }
            }//if
        return eq;
    }
    
    public void setEndStatus(int status)
    {
        switch(status)
            {
            case Constants.LOSE:
                Debug.println("Player setEndStatus - LOSE");
                endStatus = Constants.STATUS_LOSE;
                break;

            case Constants.WIN:
                Debug.println("Player setEndStatus - WIN");
                endStatus = Constants.STATUS_WIN;
                break;

            case Constants.DRAW:
                Debug.println("Player setEndStatus - DRAW");
                //fall through
            default:
                endStatus = Constants.STATUS_DRAW;
                break;
            }
    }

    public void setEndStatus(String status)
    {
        endStatus = status;
    }
    
    public String getEndStatus()
    {
        return endStatus;
    }
    
    public String toString()
    {
        return "Player: "+label+"id: "+id;
    }
    
    public int getOutcome()
    {
        return outcome;
    }
    
    /**
       Resets the player object in preparation for a new game
    */
    public void reset()
    {
        bet = 0;
        endStatus = Constants.STATUS_UNDEFINED;
        cards = new Vector(7);
        outcome = 0;
    }
    /**
       Helper method to calculate the outcome of the cards.
       Calculates the highest outcome without going bust
    */
    private int calculateOutcome()
    {
        int num_of_aces = 0;
        int total_value = 0;
        Enumeration all_cards = cards.elements();
            
        while (all_cards.hasMoreElements())
            {
                Card curr_card = (Card)(all_cards.nextElement());
                int card_number = curr_card.getNumber();
                if (card_number == Card.ACE)
                    {
                        num_of_aces++;
                    }
                else 
                    {
                        if (card_number > 10)
                            {
                                card_number = 10;
                            }
                        total_value += card_number;
                    }
            }
        // now handle aces if found
        if (num_of_aces > 0)
            {
                if (num_of_aces > 1) // at most one ace can count as 11
                    {
                        total_value += num_of_aces-1;   // add all aces but one
                    }
                // add last ace
                if (total_value + 11 > 21)
                    {
                        total_value++;  // last ace counts as 1
                    }
                else
                    {
                        total_value += 11;  // last ace counts as 11
                    }
            }
        return total_value;
    }
    
    }//end class
