/************************************************************************
 *
 * $Id: Deck.java,v 1.1 2001/11/05 20:00:22 akhil Exp $
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

import java.util.Random;
import java.util.Calendar;

public class Deck
{
    Card[] cards;
    int nextCard = 0;
    
    public Deck()
    {
        cards = new Card[52];
        
        for (int suit=0; suit<4; suit++)
            {
                for (int number=1; number<=13; number++)
                    {
                        cards[getIndex(suit,number)] = new Card(suit,number);
                    }
            }
    }
        
    public Card drawCard()
    {
        return cards[nextCard++];
    }
    
    public void shuffle()
    {
        long timeSeed = Calendar.getInstance().getTime().getTime();
        Random number_generator = new Random(timeSeed);
        
        for (int i=0; i<1000; i++)
            {
                // choose first card
                int random_number = java.lang.Math.abs(number_generator.nextInt()%52);
                int first_suit = random_number%4;
                int first_number = random_number%13 + 1;  //number range 1-13 
                // choose second card
                random_number = java.lang.Math.abs(number_generator.nextInt()%52);
                int second_suit = random_number%4;
                int second_number = random_number%13 + 1; 
        
                swapCards(getIndex(first_suit, first_number),
                          getIndex(second_suit, second_number));
            }
        nextCard = 0;
    }
 
    private void swapCards(int first_index, int second_index)
    {
        Card temp_card = new Card(cards[first_index]);
        cards[first_index] = new Card(cards[second_index]);
        cards[second_index] = new Card(temp_card);
    }
    
    private int getIndex(int suit, int number)
    {
        return suit*13 + number-1;  // for array of 52 cards
    }
    
    // DEBUG
    public void print()
    {
        for (int i=0; i<4; i++)
            {
                for (int j=1; j<=13; j++)
                    {
                        cards[getIndex(i,j)].print();
                    }
            }
    }
}
