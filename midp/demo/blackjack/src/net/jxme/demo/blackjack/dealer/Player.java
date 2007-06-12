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
 * Created: Tue May 1 2001 by Yael Gavish
 */

package net.jxme.demo.blackjack.dealer;

import java.io.PrintStream;
import java.util.Vector;
import java.util.Enumeration;

import net.jxme.demo.blackjack.share.ProtocolConstants;

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
       The outcome of the player's cards. 
    */
    private int outcome;

    public Player(int id)
    {
        this.id = id;
        this.reset();
    }
    
    public int getId()
    {
        return this.id;
    }
    
    public int getOutcome()
    {
        return this.outcome;
    }
    
    public void placeBet(int bet)
    {
        this.bet = bet;
    }
    
    public int getBet()
    {
        return this.bet;
    }
    
    public void setOutcome(int outcome)
    {
        this.outcome = outcome;
    }
    
    public void addCard(Card new_card)
    {
        cards.addElement(new_card);
    }
    
    public int getCardsValue()
    {
        int num_of_aces = 0, total_value = 0;
        Enumeration all_cards = cards.elements();
        
        while (all_cards.hasMoreElements())
            {
                Card curr_card = (Card)(all_cards.nextElement());
                int card_number = curr_card.getNumber();
                if (card_number == Card.ACE)
                    num_of_aces++;
                else 
                    {
                        if (card_number > 10)
                            card_number = 10;
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
                    total_value++;  // last ace counts as 1
                else
                    total_value += 11;  // last ace counts as 11
            }
        // YG - set outcome here?
        return total_value;
    }
    
    // print all cards in the format (id,number,suit)#
    public String listCards()
    {
        if (cards.isEmpty())
            return new String();
        // we have at least one card
        Enumeration all_cards = cards.elements();
        StringBuffer cards_str = new StringBuffer();
        
        do
            {
                Card card = (Card)(all_cards.nextElement());
                cards_str.append(id + "," + 
                                 card.toString());
                if (!(all_cards.hasMoreElements())) // we don't want to print the field delimiter
                    break;
                cards_str.append(ProtocolConstants.FIELD_DELIMITER);
            } while (true);
        return cards_str.toString();
    }

    public void reset()
    {
        this.cards = new Vector();
        this.bet = 0;
        this.outcome = BlackjackDealer.NO_OUTCOME;
    }
}//end class
