/************************************************************************
 *
 * $Id: Protocol.java,v 1.2 2001/11/06 00:39:19 akhil Exp $
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

package net.jxme.demo.blackjack.share;

import java.io.*;
import java.util.Date;
import javax.microedition.io.*;

/**
 * This class handles communication with the server
 */

public class Protocol implements ProtocolConstants
{
    private static byte[] data = new byte[50];
    private static String baseURL = null;

    /**
       Sets the base URL where the servlet is found
    */
    public static void setBaseURL(String url)
    {
        baseURL = url;
    }

    /**
       Tells the server that someone is joining or starting the game
    */
    public static String join() throws IOException
    {
        String url = baseURL+"?ID="+NO_ID+"&cmd="+CMD_JOIN;
        String result = send(url);
        return result;
    }
    
    /**
       Check if the game has started
    */
    public static String startStatus(int id) throws IOException
    {
        
        String url = baseURL+"?ID="+id+"&cmd="+CMD_START_STATUS;
        String result = send(url);
        return result;
    }
    
    /**
       Start the game
    */
    public static String start(int id) throws IOException
    {
        
        String url = baseURL+"?ID="+id+"&cmd="+CMD_START;
        String result = send(url);
        return result;
    }

    public static void bet(int id, int bet) throws IOException
    {
        String url = baseURL+"?ID="+id+"&cmd="+CMD_BET+"&sum="+bet;
        String result = send(url);
    }

    public static void card(int id) throws IOException
    {
        String url = baseURL+"?ID="+id+"&cmd="+CMD_CARD;
        String result = send(url);
    }

    public static void hold(int id) throws IOException
    {
        String url = baseURL+"?ID="+id+"&cmd="+CMD_HOLD;
        String result = send(url);
    }

    public static String checkBets(int id) throws IOException
    {
        String url = baseURL+"?ID="+id+"&cmd="+CMD_BETS_IN;
        String result = send(url);
        return result;
    }

    /**
       Tells the server that someone is joining or starting the game
    */
    public static String reset() throws IOException
    {
        String url = baseURL+"?ID="+NO_ID+"&cmd="+CMD_RESTART;
        String result = send(url);
        return result;
    }

    /**
       Tells the server that the players want to play another game
    */
    public static String newGame() throws IOException
    {
        String url = baseURL+"?ID="+NO_ID+"&cmd="+CMD_NEW_GAME;
        String result = send(url);
        return result;
    }

    /**
       Requests the server for a status update
    
       @param id the player id
    */
    public static String status(int id) throws IOException
    {
        String url = baseURL+"?ID="+id+"&cmd="+CMD_STATUS;
        String result = send(url);
        return result;
    }

    /**
       Helper method to handle common actions on sending a command
    
       @param url the url to open
    */
    private static String send(String url) throws IOException
    {
        String result = null;
        HttpConnection c = null;
        InputStream is = null;

        Debug.println("Protocol.send: "+url);
        //        Debug.println("Starting comm timer);
        //        long start = new Date().getTime();
        try
            {
                c = (HttpConnection)Connector.open(url);
                is = c.openInputStream();
                //        long open = new Date().getTime();
                //        Debug.println("Elapsed time (ms) to open comm: "+(open - start));
                // Get the length and process the data
                int total = 0;
                int n;
                while (total < data.length) 
                    {
                        n = is.read(data, total, data.length-total);
                        if (n < 0) 
                            {
                                break;
                            }
                        total += n;
                    }

                //       long readTime = new Date().getTime();
                //       Debug.println("Elapsed time (ms) from open comm to end of read: "+(readTime - open));
                
                if (total > 0) 
                    {
                        result = new String(data, 0, total);
                        Debug.print("Protocol.recv: "+result);
                    } 
                else 
                    {
                        result = "";
                        // Debug.println("Empty input stream");
                    }
            }
        finally
            {
                //Close the input stream and the connection
                if(is != null)
                    {
                        is.close();
                    }
                if(c != null)
                    {
                        c.close();
                    }
            }
        //       long closeTime = new Date().getTime();
        //       Debug.println("Elapsed time (ms) from end of read to close comm: "+(closeTime - readTime));
        //paranoia
        if(result == null)
            {
                result = "";
            }
        return result.trim();
    }
    
    
}//end class
