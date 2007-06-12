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
 * Created: Tue May 1 2001 by Rami Honig
 */

package net.jxme.demo.blackjack.client.logic;

import java.util.*;

/**
 * A player in the blackjack game
 */
public class Card
{
    public static final int ACE = 1;
    public static final int JACK = 11;
    public static final int QUEEN = 12;
    public static final int KING = 13;

    /**
       An identifier for the card
    */
    int cardNumber;
    
    /**
       The symbol for the card (A1-9JQK)
    */
    private String symbol;
    
    /**
       The suit of the card (shdc)
    */
    private char suit;

    public Card(char aSymbol, char aSuit)
    {
        suit = aSuit;
        switch(aSymbol)
            {
            case '0':
                symbol = "10";
                cardNumber = 10;
                break;

            case '1':
                symbol = "A";
                cardNumber = ACE;
                break;
               
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                symbol = ""+ aSymbol;
                cardNumber = Character.digit(aSymbol, 10);
                break;
            case 'j':
            case 'J':
                symbol = "J";
                cardNumber = JACK;
                break;
            case 'q':
            case 'Q':
                symbol = "Q";
                cardNumber = QUEEN;
                break;
            case 'k':
            case 'K':
                symbol = "K";
                cardNumber = KING;
                break;
                
            default:
                System.out.println("Invalid card created - symbol: "+aSymbol+", suit: "+aSuit);
                break;
            }
        
    }

    
    public String getSymbol()
    {
        return symbol;
    }
    
    public char getSuit()
    {
        return suit;
    }
    
    public int getNumber()
    {
        return cardNumber;
    }
}//end class
