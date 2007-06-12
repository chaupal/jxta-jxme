/************************************************************************
 *
 * $Id: TicTacToe.java,v 1.11 2003/05/02 23:27:22 kuldeep Exp $
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
 * @(#)TicTacToe.java	1.4 98/06/29
 *
 * Copyright (c) 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license
 * to use, modify and redistribute this software in source and binary
 * code form, provided that i) this copyright notice and license
 * appear on all copies of the software; and ii) Licensee does not
 * utilize the software in a manner which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line
 * control of aircraft, air traffic, aircraft navigation or aircraft
 * communications; or in the design, construction, operation or
 * maintenance of any nuclear facility. Licensee represents and
 * warrants that it will not use or redistribute the Software for such
 * purposes.
 */

package net.jxta.midp.demo.tictactoe;

import java.io.IOException;
import java.util.Random;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

/**
 * A TicTacToe MIDlet. A very simple, and mostly brain-dead
 * implementation of your favorite game! <p>
 *
 * In this game a position is represented by a bitmask. A bit is set
 * if a position is ocupied. There are 9 squares so there are 1<<9
 * possible positions for each side. An array of 1<<9 booleans is
 * created, it marks all the winning positions.
 *
 * @version 	1.2, 13 Oct 1995
 * @author Arthur van Hoff
 * @modified 04/23/96 Jim Hagen : winning sounds
 * @modified 02/10/98 Mike McCloskey : added destroy()
 * @modified 11/07/01 Akhil Arora : converted to a MIDlet
 */

public final class TicTacToe extends MIDlet 
    implements CommandListener, Runnable {

    private static final int ALERT_TIMEOUT = 5;
    private static final String PIPE_NAME = "JxtaTicTacToe";
    private static final String TITLE_NAME = "JxtaTicTacToe";
    private static final String ELEMENT_NAME = "JxtaTicTacToe";

    private static final String CMD_EXIT = "Exit";
    private static final String CMD_RESET = "Reset Game";
    private static final String CMD_SETTINGS = "Settings";
    private static final String CMD_CONNECT = "Connect";

    private static final int STATE_NOT_CONNECTED = 0;
    private static final int STATE_WAITING_FOR_OPPONENT = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_OBSERVING = 3;
    private static final int STATE_GAME_OVER = 4;
    private static final int STATE_WIN = 5;
    private static final int STATE_LOSE = 6;
    private static final int STATE_STALEMATE = 7;
    private static final int STATE_INVALID = 8;

    /**
     * The Display object for the MIDlet.
     */
    private static Display display = null;

    private Config config = null;
    private byte[] state = new byte[0];
    private PeerNetwork peerNet = null;

    /**
     * The Board canvas.
     */
    private BoardCanvas canvas = null;

    /**
     * The Message displayed on the canvas.
     */
    String message = null;

    /**
     * Computer's current position. 
     */
    private int peer = 0;

    /**
     * Your current position. 
     */
    private int you = 0;

    /**
     * The squares in order of importance...
     */
    private static final int moves[] = {4, 0, 2, 6, 8, 1, 3, 5, 7};

    /**
     * The winning positions.
     */
    private static boolean won[] = new boolean[1 << 9];
    private static final int DONE = (1 << 9) - 1;
    private static final int OK = 0;
    private static final int WIN = 1;
    private static final int LOSE = 2;
    private static final int STALEMATE = 3;


    /** 
     * For debuging and quantifying
     */
    private static final boolean DEBUG = true;
    private static final boolean QUANTIFY = true;

    private boolean stopPolling = false;
    private int gameState = STATE_NOT_CONNECTED;
    private String opponent = null;

    /**
     * Mark all positions with these bits set as winning.
     */
    private static void isWon(int pos) {
	for (int i = 0 ; i < DONE ; i++) {
	    if ((i & pos) == pos) {
		won[i] = true;
	    }
	}
    }

    /**
     * Initialize all winning positions.
     */
    static {
	isWon((1 << 0) | (1 << 1) | (1 << 2));
	isWon((1 << 3) | (1 << 4) | (1 << 5));
	isWon((1 << 6) | (1 << 7) | (1 << 8));
	isWon((1 << 0) | (1 << 3) | (1 << 6));
	isWon((1 << 1) | (1 << 4) | (1 << 7));
	isWon((1 << 2) | (1 << 5) | (1 << 8));
	isWon((1 << 0) | (1 << 4) | (1 << 8));
	isWon((1 << 2) | (1 << 4) | (1 << 6));
    }
    private static final String PIPE_ID = "urn:jxta:uuid-" + 
        "59616261646162614E50472050325033" + 
        "AFAFAFAFAFAFAFAFAFAFAFAFAFAFAFA104";


    public TicTacToe() {
    }

    public void startApp() {
        display = Display.getDisplay(this);

	if (canvas == null) {
	    canvas = new BoardCanvas(this);
	    canvas.addCommand(new Command(CMD_EXIT, Command.STOP, 1));
	    canvas.addCommand(new Command(CMD_CONNECT, Command.SCREEN, 2));
	    canvas.addCommand(new Command(CMD_RESET, Command.SCREEN, 3));
	    canvas.addCommand(new Command(CMD_SETTINGS, Command.SCREEN, 4));
	    canvas.setCommandListener(this);
	}

	if (config == null) {
	    config = new Config(this, display, canvas);
	}
	
	// startup in the config screen if the identity is not configured
	if ("".equals(config.getIdentity())) {
	    display.setCurrent(config);
	} else {
	    display.setCurrent(canvas);
	}
        stopPolling = false;
    }
    
    public void pauseApp() {
        stopPolling = true;
    }

    public void destroyApp(boolean unconditional) {
        stopPolling = true;
    }

    public void commandAction(Command c, Displayable displayable) {
        if (c.getCommandType() == Command.STOP) {
            destroyApp(true);
            notifyDestroyed();
            return;
        } 

        Displayable next = canvas;
        String label = c.getLabel();
        if (label.equals(CMD_SETTINGS)) {
            next = config;
        } else if (label.equals(CMD_RESET)) {
            canvas.reset();
        } else if (label.equals(CMD_CONNECT)) {
            connect();
        } 

        display.setCurrent(next);
    }


    /**
     * User move.
     * @return true if legal
     */
    private boolean yourMove(int m) {
	if ((m < 0) || (m > 8)) {
	    return false;
	}
	if (((you | peer) & (1 << m)) != 0) {
	    return false;
	}
	you |= 1 << m;
	return true;
    }

    /**
     * Figure what the status of the game is.
     */
    private int status() {
	if (won[peer]) {
	    return WIN;
	}
	if (won[you]) {
	    return LOSE;
	}
	if ((you | peer) == DONE) {
	    return STALEMATE;
	}
	return OK;
    }

    /**
     * Who goes first in the next game?
     */
    private boolean first = true;

    /**
     * Whose move is it?
     */
    private boolean myMove = false;

    private static class BoardCanvas extends Canvas
    {
        /**
         * The image for peer.
         */
        Image notImage;
        
        /**
         * The image for you.
         */
        Image crossImage;

        TicTacToe midlet;
        boolean alerted = false;
        int width;
        int fullheight;
        int height;
        int lineHeight;

        BoardCanvas(TicTacToe midlet) {
            this.midlet = midlet;

            width = getWidth();
            fullheight = getHeight();

            lineHeight = Font.getDefaultFont().getHeight();
            // keep space for the status line
            height = fullheight - lineHeight - 2;

            try {
                notImage = Image.createImage("/not.png");
                crossImage = Image.createImage("/cross.png");
            } catch (IOException ex) {
                notImage = null;
                crossImage = null;
            }
        }

        /**
         * Paint it.
         */
        public void paint(Graphics g) {
            if (alerted) {
                midlet.destroyApp(true);
                midlet.notifyDestroyed();
                return;
            }

            if (notImage == null || crossImage == null) {
                showAlert("TicTacToe", 
                          "Fatal error: could not load images", 
                          AlertType.ERROR,
                          Alert.FOREVER,
                          this);
                alerted = true;
                return;
            }

            computeMessage();

            g.setColor(255, 255, 255);
            g.fillRect(0, 0, width, fullheight);
            g.setColor(0, 0, 0);

            g.drawString(midlet.message, 0, height,
                         Graphics.LEFT|Graphics.TOP);

            int xoff = width / 3;
            int yoff = height / 3;
            g.drawLine(xoff, 0, xoff, height);
            g.drawLine(2*xoff, 0, 2*xoff, height);
            g.drawLine(0, yoff, width, yoff);
            g.drawLine(0, 2*yoff, width, 2*yoff);

            int i = 0;
            for (int r = 0 ; r < 3 ; r++) {
                for (int c = 0 ; c < 3 ; c++, i++) {
                    if ((midlet.peer & (1 << i)) != 0) {
                        g.drawImage(notImage, 
                                    c*xoff + 1 + xoff/2,
                                    r*yoff + 1 + yoff/2,
                                    Graphics.VCENTER|Graphics.HCENTER);
                    } else if ((midlet.you & (1 << i)) != 0) {
                        g.drawImage(crossImage, 
                                    c*xoff + 1 + xoff/2,
                                    r*yoff + 1 + yoff/2,
                                    Graphics.VCENTER|Graphics.HCENTER);
                    }
                }
            }
        }

        /**
         * Compute the status message depending upon the game state.
         */
        private void computeMessage() {
            midlet.message = "JXTA TicTacToe";
            switch (midlet.gameState) {
            case STATE_NOT_CONNECTED:
                midlet.message = "Waiting to connect";
                break;

            case STATE_WAITING_FOR_OPPONENT:
                midlet.message = "Waiting for opponent";
                break;

            case STATE_PLAYING:
                if (midlet.myMove) {
                    midlet.message = "Playing " + midlet.opponent;
                } else {
                    midlet.message = "Awaiting " + midlet.opponent;
                }
                break;

            case STATE_OBSERVING:
                midlet.message = "Observing";
                break;

            case STATE_GAME_OVER:
                midlet.message = "Game over";
                break;

            case STATE_WIN:
                midlet.message = "You win!";
                break;

            case STATE_LOSE:
                midlet.message = "You lose!";
                break;

            case STATE_STALEMATE:
                midlet.message = "Stalemate!";
                break;

            case STATE_INVALID:
                midlet.message = "Invalid move";
                break;
            }
            repaint();
        }

        /**
         * Reset the game.
         */
        private void reset() {
            midlet.peer = midlet.you = 0;
            midlet.gameState = STATE_PLAYING;
            repaint();
        }

        /**
         * The user has pressed a key. Figure out which one
         * and see if a legal move is possible. If it is a legal
         * move, respond with a legal move (if possible).
         */
        public void keyPressed(int keyCode) {
            switch (midlet.status()) {
            case WIN:
            case LOSE:
            case STALEMATE:
                reset();
                return;
            }

            if (keyCode < KEY_NUM1 || keyCode > KEY_NUM9) {
                // not a game key, ignore
                return;
            }

            if (!midlet.myMove) {
                showAlert(TITLE_NAME, 
                          "Not your move. Waiting for " + midlet.opponent,
                          AlertType.ERROR, 
                          ALERT_TIMEOUT,
                          this);
                return;
            }
            midlet.myMove = !midlet.myMove;

            // Figure out the row/column
            keyCode -= KEY_NUM1;
            int c = keyCode % 3;
            int r = keyCode / 3;

            if (midlet.yourMove(c + r * 3)) {
                switch (midlet.status()) {
                case WIN: 
                    midlet.gameState = STATE_LOSE; 
                    // loser gets to play first in the next game
                    midlet.first = true;
                    break;

                case LOSE: 
                    midlet.gameState = STATE_WIN; 
                    // loser gets to play first in the next game
                    midlet.first = false;
                    break;

                case STALEMATE: 
                    midlet.gameState = STATE_STALEMATE; 
                    midlet.first = !midlet.first;
                    break;
                }
            } else {
                midlet.gameState = STATE_INVALID;
            }

            midlet.send();
            repaint();
        }
    }

    public void run() {
        while (!stopPolling) {
            poll();

            try {
                // poll interval is specified in seconds
                Thread.currentThread().sleep(config.getPollInterval() * 1000);
            } catch (InterruptedException ignore) {
            }
        }
    }
    

    private synchronized boolean connect() {
        String host = config.getRelayHost();
        int port = 0;
        try {
            port = Integer.parseInt(config.getRelayPort());
        } catch (NumberFormatException ex) {
            showAlert("Connect", 
                      "Error parsing relay port number: " + 
                      config.getRelayPort(),
                      AlertType.ERROR, 
                      Alert.FOREVER,
                      canvas);
            return false;
        }

        String url = "http://" + host + ":" + port;

        PeerNetwork pn = PeerNetwork.createInstance(config.getIdentity());
	if (DEBUG) {
	    System.out.println("Connecting to " + url + "...");
	}
        try {
            state = pn.connect(url, state);
            pn.create(PeerNetwork.PIPE, PIPE_NAME, PIPE_ID, "JxtaPropagate");
            pn.listen(PIPE_ID);
        } catch (IOException ex) {
            showAlert("Connect", 
                      "Error connecting to relay: " + ex.getMessage(),
                      AlertType.ERROR, 
                      Alert.FOREVER,
                      canvas);
            return false;
        }

	if (DEBUG) {
	    System.out.println("Connected");
	}

        peerNet = pn;
	gameState = STATE_WAITING_FOR_OPPONENT;
        send();

	Thread incomingThread = new Thread(this);
	incomingThread.start();

        display.setCurrent(canvas);
        return true;
    }

    private void send() {
        if (peerNet == null) {
            if (!connect()) {
                return;
            }
        }

        TicTacToeMessage t3msg = 
            new TicTacToeMessage(gameState, peer, you, config.getIdentity());

	if (DEBUG) {
	    System.out.println(">> " + t3msg);
	}

        try {
            Element[] elm = new Element[2];
            elm[0] = new Element(ELEMENT_NAME, 
                                 t3msg.toBytes(), 
                                 null, null);
	    elm[1] = new Element("GrpName", 
				 "NetPeerGroup".getBytes(), 
				 null, null);
            Message msg = new Message(elm);
            peerNet.send(PIPE_ID, msg);
        } catch (Exception ex) {
            showAlert("Send", 
                      "Error sending message: " + ex.getMessage(),
                      AlertType.ERROR, 
                      ALERT_TIMEOUT,
                      canvas);
            return;
        }

        display.setCurrent(canvas);
    }

    private void poll() {
        if (peerNet == null) {
            if (!connect()) {
                return;
            }
        }

        Message msg = null;
        try {
            // timeout must not be zero: zero means block forever
            msg = peerNet.poll(1);
        } catch (IOException ex) {
            showAlert("Poll", 
                      "Error polling relay: " + ex.getMessage(),
                      AlertType.ERROR, 
                      ALERT_TIMEOUT,
                      canvas);
            return;
        }

        if (msg == null) {
            return;
        }

	Element el = null;
        TicTacToeMessage t3msg = null;
        for (int i=0; i < msg.getElementCount(); i++) {
	    el = msg.getElement(i);
	    if (ELEMENT_NAME.equals(el.getName())) {
                try {
                    t3msg = new TicTacToeMessage(el.getData());
                } catch (IOException ex) {
                    if (DEBUG) {
                        System.err.println("Error reading message: " + ex);
                    }
                }
	    }
	}
	
        if (t3msg == null) {
            return;
        }

        if (t3msg.getSender().equals(config.getIdentity())) {
            return;
        }

        if (DEBUG) {
            System.out.println("<< " + t3msg);
        }

        int peerGameState = t3msg.getState();
        if (gameState == STATE_WAITING_FOR_OPPONENT &&
            peerGameState == STATE_WAITING_FOR_OPPONENT) {
            opponent = t3msg.getSender();
            gameState = STATE_PLAYING;
            send();
        } else if (gameState == STATE_WAITING_FOR_OPPONENT &&
            peerGameState == STATE_PLAYING) {
            opponent = t3msg.getSender();
            gameState = STATE_PLAYING;
            send();
        } else if (gameState == STATE_PLAYING) {
            you = t3msg.getPeerBoard();
            peer = t3msg.getSenderBoard();
            myMove = !myMove;
        }

        switch (status()) {
        case WIN: gameState = STATE_LOSE; break;
        case LOSE: gameState = STATE_WIN; break;
        case STALEMATE: gameState = STATE_STALEMATE; break;
        }
            
        canvas.repaint();
    }

    static void showAlert(String title, String message, AlertType type,
                          int timeout, Displayable back) {
        Alert alert = new Alert(title, message, null, type);
        alert.setTimeout(timeout);
        display.setCurrent(alert, back);
    }
}


