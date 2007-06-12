/************************************************************************
 *
 * $Id: Images.java,v 1.1 2001/11/05 20:00:22 akhil Exp $
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

import java.util.*;
import javax.microedition.lcdui.*;
import java.io.IOException;
import net.jxme.demo.blackjack.share.Debug;

/**
 * This singelton class manages bitmaps used in the game.  It's
 * purpose os to cut down on overhead required in loading images.  By
 * using this class we have much more control over when images are
 * loaded and can do it at some convenient time in the game when the
 * player is waiting anyway.
 */

public class Images
{
    /**
       The one and only instance of the Game 
    */
    private static Images instance = null;
    
    private Image smallSpade= null;
    private Image smallHeart= null;
    private Image smallDiamond= null;
    private Image smallClub= null;

    private Image bigSpade= null;
    private Image bigHeart= null;
    private Image bigDiamond= null;
    private Image bigClub= null;

    private Images()
    {
        try
            {
                String useColor = BlackjackMidlet.instance.getAppProperty("USE_COLOR");
                String bmpPath = "/";
                if(useColor != null && useColor.toLowerCase().equals("true"))
                    {
                        bmpPath = "/color/";
                    }
            
                smallSpade = Image.createImage(bmpPath+"SmallSpade.png");
                Debug.println("Loaded:"+bmpPath+"SmallSpade.png");
                smallHeart = Image.createImage(bmpPath+"SmallHeart.png");
                smallDiamond = Image.createImage(bmpPath+"SmallDiamond.png");
                smallClub = Image.createImage(bmpPath+"SmallClub.png");

                bigSpade = Image.createImage(bmpPath+"BigSpade.png");
                bigHeart = Image.createImage(bmpPath+"BigHeart.png");
                bigDiamond = Image.createImage(bmpPath+"BigDiamond.png");
                bigClub = Image.createImage(bmpPath+"BigClub.png");
                Debug.println("Loaded:"+bmpPath+"BigClub.png");
            }
        catch(IOException e)
            {
                System.out.println("Error loading image\n"+e);
            }
    }
    
    /**
       Returns a reference to the one and only instance of this class
    */
    public static Images getInstance()
    {
        if(instance == null)
            {
                instance = new Images();
            }
        return instance;
    }
    
    /**
       Returns a referene to the appropriate image according to the suit letter
    
       @param suit the letter defining the suit
       @param big true if the caller wants the big image
       @return a reference to the appropriate image
    */
    public Image getImage(char suit, boolean big)
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

}//end class
