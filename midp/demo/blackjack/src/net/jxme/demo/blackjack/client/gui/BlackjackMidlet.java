/************************************************************************
 *
 * $Id: BlackjackMidlet.java,v 1.1 2001/11/05 20:00:22 akhil Exp $
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

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import net.jxme.demo.blackjack.client.logic.*;
import net.jxme.demo.blackjack.share.*;

/**
 * The main Midlet class for the Blackjack demo application
 */
public class BlackjackMidlet extends MIDlet
{
    /**
       The Display object for the midlet
    */
    public static Display display = null;
    
    /**
       Gives public access to the  MIDlet object
    */
    public static BlackjackMidlet instance = null;
    
    public BlackjackMidlet()
    {
        display = Display.getDisplay(this);
        instance = this;
    }
    /**
       Start the midlet. 
    */
    public void startApp()
    {
        String debugOn = getAppProperty("DEBUG");
        System.out.println("DEBUG = "+debugOn);
        if(debugOn != null)
            {
                debugOn = debugOn.trim();
                if(debugOn.equals("true"))
                    {
                        System.out.println("Setting debug printout on");
                        Debug.debug = true;
                    }
            }//if(debugOn != null)
        
        Canvas can = new Canvas() 
	    {
	        public void paint(Graphics g) 
	        {
	            int width = getWidth();
	            int height = getHeight();
	            String message = "Loading, please wait...";
	            Font font = Font.getDefaultFont();
	            int msgLen = font.stringWidth(message);
	            int x = (width-msgLen)/2;
	            int y = height/2;
                    g.drawString(message,x,   y, g.TOP|g.LEFT);
                    g.drawString(message,x+1, y, g.TOP|g.LEFT);
	        }
	    };
	    
        display.setCurrent(can);
        can.serviceRepaints();

        try
            {
                //instantiate the Images class so that bitmaps are loaded now while
                //player is waiting anyway. Saves time later
                Images.getInstance();
                Debug.println("Images loaded");
                NewGame newGame = new NewGame();
                newGame.setActive();
            }
        catch(Exception e)
            {
                System.out.println();
                System.out.println("Loading - Exception: "+e);
            }
            
    }

    /**
       Pause is a no-op since there are no background activities to do
    */
    public void pauseApp()
    {
    }
    
    /**
       Reset the game - resets the servlet
    */
    public void destroyApp(boolean unconditional)
    {
        Debug.println("BlackjackMidlet.destroyApp: Resetting");
        Game.instance.reset();
    }
    }//end class
