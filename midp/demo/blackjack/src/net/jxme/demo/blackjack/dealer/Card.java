/************************************************************************
 *
 * $Id: Card.java,v 1.1 2001/11/05 20:00:22 akhil Exp $
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

public class Card
{
    public static final int HEART = 0;
    public static final int DIAMOND = 1;
    public static final int SPADE = 2;
    public static final int CLUB = 3;
    
    public static final int ACE = 1;
    public static final int JACK = 11;
    public static final int QUEEN = 12;
    public static final int KING = 13;
    // data members
    private int suit;
    private int number;
    
    public Card(int suit, int number)
    {
        this.suit = suit;
        this.number = number;
    }
    
    public Card(Card c)
    {
        this.suit = c.getSuit();
        this.number = c.getNumber();
    }
    
    public int getSuit()
    {
        return this.suit;
    }
    
    public int getNumber()
    {
        return this.number;
    }   
    
    // DEBUG
    public void print()
    {
        String suit_str = null;
        String number_str = null;
        switch (number)
            {
            case ACE: number_str = "ACE";
                break;
            case JACK: number_str = "JACK";
                break;       
            case QUEEN: number_str = "QUEEN";
                break;
            case KING: number_str = "KING";
                break;
            default: number_str = (new Integer(number)).toString();
            }
        switch (suit) 
            {
            case HEART: suit_str = "HEART";
                break;
            case DIAMOND: suit_str = "DIAMOND";
                break;
            case SPADE: suit_str = "SPADE";
                break;
            case CLUB: suit_str = "CLUB";
                break;
            }
        System.out.println(number_str + "," + suit_str);
    } // end DEBUG
    
    public String printNumber()
    {
        switch (number)
            {
            case 10: return "0";
            case JACK: return "J";
            case QUEEN: return "Q";
            case KING: return "K";
            default: 
                return (new Integer(number)).toString();
            }
    }
    
    public char printSuit()
    {
        switch (suit) 
            {
            case HEART: return 'H';
            case DIAMOND: return 'D';
            case SPADE: return 'S';
            case CLUB: return 'C';
            }
        return 'e'; // will not reach here
    }
    
    public String toString()    // print number, suit
    {
        StringBuffer card_str = new StringBuffer();
        
        card_str.append(printNumber());
        card_str.append(',');
        card_str.append(printSuit());
        return card_str.toString();
    }
}
