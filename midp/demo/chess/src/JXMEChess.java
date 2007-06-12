/************************************************************************
 *
 * $Id: JXMEChess.java,v 1.5 2003/03/31 13:41:41 pmaugeri Exp $
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
import java.lang.String;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Random;
*/
import java.io.IOException;

import net.jxta.j2me.PeerNetwork;
import net.jxta.j2me.Message;
import net.jxta.j2me.Element;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;



public class JXMEChess extends MIDlet
	implements CommandListener, Runnable {	

	private static final String RMS_NAME = "JCRMS";

	private static final int MESSAGE_POLLING_INTERVAL = 1000; // interval in milliseconds
	private static int POLL_RETRIES = 10; 
	private static final int POLL_TIMEOUT = 1000; // timeout in milliseconds
	private static final String PEER_NAME_PREFIX = "JXTAChess-";
	

	// Definition for JXTA framework
	private PeerNetwork peerNetwork = null;
	private byte[] persistentState = null;
	private static final String PIPE_NAME_PREFIX = "JXTAChess-";
	private int listenQueryId = 0;
	private int pipeSearchQueryId = 0;
	private int sendMoveRequestId = 0;
	private int sendInvitationRequestId = 0;
	private int invitationAnswerId = -1;
	public static String screenName;
	private static String preferredColor;

	//BufferedReader in = null;

	// Current list of players found
	public Players players = null;

	// Flag to know the status of the connection to the JXTA proxy
	private boolean connected = false;

	// Flag to control message polling
	private boolean stopPolling = false;

	// Flags used to block methods till receiving an ACK from the remote peer
	private boolean moveSendACK = false;
	private boolean invitationSendACK = false;

	// Current opponent (the one who we play the game with)
	private Player opponent = null;

	// The current game played
	private Game game = null;
	private boolean inGame = false;

	Thread polling = null;


	// variables needed for the management of this midlet
	private Display display;
	private Form initPage = new Form( "" );
	public Command okCommand = new Command( "OK", Command.SCREEN, 1);
	public Command quitCommand = new Command( "Quit", Command.SCREEN, 1);
	public Command cancelCommand = new Command( "Cancel", Command.CANCEL, 1 );
	public Command exitCommand = new Command( "Exit", Command.SCREEN, 1);
	public Command settingsCommand = new Command( "Settings", Command.SCREEN, 2);
	public Command searchPlayersCommand = new Command( "Search players" , Command.SCREEN, 2);
	public Command showPlayersCommand = new Command( "Show players", Command.SCREEN, 2);
	public Command showGameCommand = new Command( "Show Game", Command.SCREEN, 2);
	public Command newGameCommand = new Command( "New game", Command.SCREEN, 2);
	public Command connectCommand = new Command( "Connect", Command.SCREEN, 2); 
	public Command disconnectCommand = new Command( "Disconnect", Command.SCREEN, 2); 
	public Command yesCommand = new Command( "Yes", Command.SCREEN, 1); 
	public Command noCommand = new Command( "No", Command.SCREEN, 1); 
	private JXMEChessRMS RMS;
	
	// Specific variables for settings management
	private boolean	inSettings = false;
	private Form settingsForm = new Form( "Settings" );
	private TextField proxyIPTF = new TextField( "Proxy host", "", 50, 0 );
	private TextField proxyPortTF = new TextField( "Proxy port", "", 50, 0 );
	private TextField screenNameTF = new TextField( "Screen name", "", 50, 0 );

	// Specific variables for choosing player
	private boolean inChooseOpponent = false;
	public ChoiceGroup opponentChoiceGroup;
	public ChoiceGroup colorChoiceGroup;


	/**
	 * The constructor.
	 *
	 */
	public JXMEChess () {
	}

	public void run() {

		Message msg = null;

		// Polling message untill the stopPolling variable is set to true
		while ( !stopPolling ) {

			if ( peerNetwork != null )
				try {
					//System.out.println( screenName + "> polling ..." );
					msg = peerNetwork.poll( 1000 );
				}
				catch ( IOException e ) {
					System.err.println( "Error occured while polling message." );
				}
			if ( msg != null ) {
				String response = null;
				int request_id = 0;
				String from = null;
				String name = null;
				String pipe_id = null;
				String move_string = null;
				boolean game_invitation = false;
				boolean invitation_accepted = false;
				boolean invitation_denied = false;
				String color_invitation = null;

				System.out.println( screenName + "> 1 message received." );
				for (int i=0; i<msg.getElementCount(); i++) {
					Element e = msg.getElement(i);
					String element_name = e.getName();
					String element_value = new String( e.getData() );
					System.out.println(  "   " + element_name + " = " + element_value );
					if ( Message.RESPONSE_TAG.equals( element_name ) )
						response = element_value;
					if ( Message.REQUESTID_TAG.equals( element_name ) )
						request_id = Integer.parseInt( element_value );
					if ( Message.NAME_TAG.equals( element_name ) )
						name = element_value;
					if ( Message.ID_TAG.equals( element_name ) )
						pipe_id = element_value;
					if ( "Move".equals( element_name ) )
						move_string = element_value;
					if ( "Invitation".equals( element_name ) )
						game_invitation = true;
					if ( "Color".equals( element_name ) )
						color_invitation = element_value;
					if ( "From".equals( element_name ) )
						from = element_value;
					if ( "InvitationAccepted".equals( element_name ) )
						invitation_accepted = true;
					if ( "InvitationDenied".equals( element_name ) )
						invitation_denied = true;
				}
				
				// A game invitation has been accepted by the remote player
				if ( invitation_accepted ) {
					game.setP2PStatus( Game.READY_TO_PLAY );
					game.repaint();
					invitation_accepted = false;
					System.out.println( screenName + "> game invitation accepted." );
				}
				else
			
				// A game invitation has been denied by the remote player
				if ( invitation_denied ) {
					game = null;
					display.setCurrent( initPage );
					invitation_denied = false;
					System.out.println( screenName + "> game invitation denied." );
				}
				else

				// Check if the message received is an invitation anwer ACK
				if ( Message.RESPONSE_SUCCESS.equals( response ) && request_id == invitationAnswerId ) {
					invitationAnswerId = -1;
					System.out.println( screenName + "> answer invitation ACK received." );
					game.setP2PStatus( Game.READY_TO_PLAY );
					display.setCurrent( game );
				}
				else

				// Check if the message received is part of the response for the last
				// searchPlayers action
				if ( Message.RESPONSE_RESULT.equals( response) && request_id == pipeSearchQueryId ) {
					try {
						String player_name = name.substring( PIPE_NAME_PREFIX.length() );
						players.addPlayer( new Player( player_name, name, pipe_id ) );
						System.out.println( screenName + "> 1 player found : " + player_name );
					}
					catch ( Exception e ) {
						System.err.println( "Error occured while adding a new playing to the list." ); 
					}
				} 
				else

				// Case of an acknolegment of move sent
				if ( Message.RESPONSE_SUCCESS.equals( response ) && 
				     request_id == sendMoveRequestId &&
				     moveSendACK == false ) {
					moveSendACK = true;  // this will unlock the method sendMove()
				}	
				else

				// Case of an acknoledgement of a an invitation sent
				if ( Message.RESPONSE_SUCCESS.equals( response ) &&
				     request_id == sendInvitationRequestId &&
				     invitationSendACK == false ) {
					invitationSendACK = true;
					game.setP2PStatus( Game.WAITING_INVITATION_ANSWER );
					game.repaint();
					System.out.println( screenName + "> received a game invitation ACK." ); 
				}
				else

				// Case of a move received from a choosen opponent
				if ( Message.RESPONSE_MESSAGE.equals( response ) && move_string != null ) {
					try {
						int i;
						System.out.println( screenName + "> 1 move received : " + move_string );
						Move move = new Move( move_string.charAt(0), 
								      move_string.charAt(1) - '1' + 1,
								      move_string.charAt(2), 
								      move_string.charAt(3) - '1' + 1);
						if ( game.getOpponent().getScreenName().equals( from ) ) {
							game.addMove( move );
							game.repaint();
							if ( game.isChecked( true ) )
								System.out.println( "White King checked !" );
							if ( game.isChecked( false ) )
								System.out.println( "Black King checked !" );
							if ( game.isCheckedMate( true ) )
								System.out.println( "White King checked mate !" );
							if ( game.isCheckedMate( false ) )
								System.out.println( "Black King checked mate!" );
						}
						else
							System.out.println( "Received move discarded : opponent unknown !" );
					}
					catch ( Exception e ) {
						System.err.println( "Move received malformed !" );
					}
				}
				else

				// An invitation for playing a new game is received
				if ( game_invitation ) {
					System.out.println( screenName + "> game invitation from " + 
							    from + " with the color " + color_invitation );
					
					// Show a form for answering to the invitation
					Form f = new Form( "Game invitation" );
					StringItem si = new StringItem( "The player " + 
					   players.getPlayer( from ).getScreenName() + 
					   " invited you for playing a game. If you accept you will play with the color " +
					   color_invitation + ".", "" );
					f.append( si );
					f.addCommand( yesCommand );
					f.addCommand( noCommand );
					f.setCommandListener( this );
					display.setCurrent( f );

					// Create the new game and select it as the current one
					game = new Game( players.getPlayer( from ), 
						   	 color_invitation.equals( "white" )?true:false,
							 this  );
					game.setP2PStatus( Game.WAITING_INVITATION_ANSWER_ACK );
					game.repaint();
				}
					
			}
			try {
				Thread.currentThread().sleep( MESSAGE_POLLING_INTERVAL );
			} catch (InterruptedException ignore) {
				System.err.println( "Thread interrupted ! " + ignore );
			}
        	}

	}

	public void connectProxy() {

		// The peer name will contain a tag for JXTAChess, the screen name of the player
		// and the preferred color (WHITE/BLACK)
		peerNetwork = PeerNetwork.createInstance( screenName );
		System.out.println( "Creating peer named " + screenName );

		String url = "http://" + proxyIPTF.getString() + ":" + proxyPortTF.getString();
		try {	
			System.out.println( "Connecting to proxy ..." + url );
			long start_time = System.currentTimeMillis();
			persistentState = peerNetwork.connect( url, persistentState );
			long end_time = System.currentTimeMillis();
			System.out.println( "Connect took : " + Long.toString( end_time - start_time ) + " ms.");
			connected = true;
			
			// Save persistentState value to Record Store for next connection
			RMS.savePersistentState( persistentState );

			// Create an unicast pipe
			listenQueryId = peerNetwork.listen( PIPE_NAME_PREFIX + screenNameTF.getString(), 
							    null, PeerNetwork.UNICAST_PIPE );
			System.out.println("listen query id: " + listenQueryId);
		}
		catch ( IOException ioe ) {
			// [...]
			System.err.println( "Connection error." );
		}
	}



	/*
	 *
	 *
	 * @return an instance of Players representing the list of Players found
	 */
	public void searchPlayers () {
		
		players = new Players();
		// Searching all the pipes that begins with PEER_NAME_PREFIX (eg. JXTACHess*)
		try {
			pipeSearchQueryId = peerNetwork.search( PeerNetwork.PIPE, new String( PIPE_NAME_PREFIX + "*" ) );
			System.out.println( "Searching opponents. Request ID = " + pipeSearchQueryId );
		}
		catch ( IOException e ) {
			System.err.println( "Error while searching opponents." );
		}
	}



	/*
	 * Send an invitation for a new gama to a JXTAChess player.
	 *
	 * @param opponent - the player to invite
	 * @param opponent_color - the proposed color to the invited player
	 */
	public void invitePlayer ( Player opponent, boolean opponent_color ) {

		Element[] elm = new Element[3];
		Message msg;
		
		try {
			String color;
			if ( opponent_color )
				color = "white";
			else
				color = "black";
			elm[0] = new Element( PEER_NAME_PREFIX + ":From", screenName.getBytes(), null, null );
			elm[1] = new Element( PEER_NAME_PREFIX + ":Invitation", "".getBytes(), null, null );
			elm[2] = new Element( PEER_NAME_PREFIX + ":Color", color.getBytes(), null, null );
			msg = new Message( elm );
			sendInvitationRequestId = peerNetwork.send( opponent.getPipeName(), 	
									opponent.getPipeID(), 
									PeerNetwork.UNICAST_PIPE, 
									msg);
			System.out.println( screenName + "> sending a game invitation to " + opponent.getScreenName() );
			// Block untill receiving invitation request ACK
			invitationSendACK = false;
			while ( !invitationSendACK ) {}
			sendInvitationRequestId = 0;
		}
		catch ( IOException e ) {
			System.err.println( "Error while sending invitation !" );
			return;	
		}
	}


	/*
	 * Send a move coordinate to an opponent. As we want to be able to play 
	 * simultaneously several games, the opponent pipe ID is given in parameter.
	 *
	 * The method will block untill it receives an acknowledgement that the move
	 * has been correctly received by the oppoenent or if a timeout expires.
	 * 
	 * @param opponent - the opponent to send the move
	 * @param move - the standard reprensation of a chess game move in its complete format (eg. e2-e4)
	 * @return true if the move has been received by the opponent, false otherwise
	 */
	public boolean sendMove ( Player opponent, Move move ) {

		Message msg;
		Element[] elm = new Element[2];

		try {
			// Make the message to send
			elm[0] = new Element( PEER_NAME_PREFIX + ":From", screenName.getBytes(), null, null );
			elm[1] = new Element( PEER_NAME_PREFIX + ":Move", move.toString().getBytes(), null, null );
			msg = new Message( elm );
			sendMoveRequestId = peerNetwork.send( opponent.getPipeName(), 
								opponent.getPipeID(), 
								PeerNetwork.UNICAST_PIPE, 
								msg );
			// Wait untill receiving the send move ACK
			moveSendACK = false;
			while ( !moveSendACK ) { }
			sendMoveRequestId = 0;
			return true;
		}
		catch ( IOException e ) {
			System.err.println( "Error while sending the move to the opponent !" );
			return false;
		}
	}

/*
	public static int readInt ( BufferedReader bf ) {

		int value;

		while ( true ) {
			value = -1;	
                	try {
				value = new Integer(bf.readLine()).intValue();
				return value;
			}
			catch ( Exception e ) {
                	}
		}
	}
	


	public static String readString ( BufferedReader bf ) {

		String s = null;

		while ( true ) {
			try {
				s = null;
				s = bf.readLine();
				return s;
			} catch ( Exception e ) {
			}
		}
	}
	


	public static void main ( String args[] ) {

		JXTAChess test = new JXTAChess();
		int choice = -1;
		
		// Get the screen name of the user
		System.out.println( "Please enter your screen name : " );
		test.in = new BufferedReader(new InputStreamReader(System.in));
		screenName = readString( test.in );

		while ( choice != 10 ) {
			System.out.println( "" );
			System.out.println( "  |-------------------------------------|" );
			System.out.println( "  | JXTAChess menu                      |" );
			System.out.println( "  |-------------------------------------|" );
			System.out.println( "  | current screen name : " + screenName );
			if ( test.game != null ) {
			System.out.println( "  | Playing with : " + test.game.getOpponent().getScreenName() );
			}
			if ( test.connected )
			System.out.println( "  | Connected.                          |" );
			else
			System.out.println( "  | Not connected.                      |" );
			System.out.println( "  |-------------------------------------|" );
			System.out.println( "  | 0: show this menu again             |" );
			System.out.println( "  | 1: connect to proxy                 |" );
			System.out.println( "  | 2: search players                   |" );
			System.out.println( "  | 3: new game                         |" );
			System.out.println( "  | 5: show list of players found       |" );
			if ( test.game != null && test.game.canPlay() )
			System.out.println( "  | 6: play a move and send it          |" );
			System.out.println( "  | 7: undo a move                      |" );
			System.out.println( "  | 10: exit                            |" );
			System.out.println( "  |-------------------------------------|" );

			if ( test.game != null ) 
				test.game.printChessBoard();

			test.in = new BufferedReader(new InputStreamReader(System.in));
			choice = readInt( test.in );

			// Connection to the proxy
			if ( choice == 1 ) {
				if ( test.connected ) {
					System.out.println( "Already connected !" );
				}
				else {
					test.connectProxy();		
					Thread polling = new Thread( test );
					polling.start();
				}
			}

			// search players
			if ( choice == 2 ) {
				if ( !test.connected ) 
					System.out.println( "Connect first !" );
				else {
					test.searchPlayers();
				}
			}
			
			// New game
			if ( choice == 3 ) {
				// Select an opponent from the players list found
				Player opponent = null;
				while ( opponent == null ) {
					System.out.println( "Enter the name of the player to invite : ");
					String s = readString( test.in );	
					opponent = test.players.getPlayer( s );
					if ( opponent == null )
						System.out.println( "Opponent not found in list of players !" );
				}

				// Choose color to play with
				String s = ""; 
				while ( !s.equals( "white" ) && !s.equals( "black" ) ) {
					System.out.println( "Enter the color you want to play with (white/black): " );
					s = readString( test.in );
				}
				boolean c = s.equals( "white" )?false:true;

				// Send an invitation request to the opponent
				test.invitePlayer( opponent, !c );

				// Create the new game and select it as the current one
				System.out.println( "Invitation accepted. Creating the new game ..." );
				test.game = new Game( opponent, c ); 
				test.game.printChessBoard();
			}

			// Show list of players found
			if ( choice == 5 ) {
				if ( test.players == null ) 
					System.err.println( "Search request not sent yet !" );
				else {
					System.out.println( "Players found so far :" );
					for ( int i=0; i<test.players.size(); i++ ) {
						try {
							System.out.println( "  " + test.players.getPlayer(i).getScreenName() );
						}
						catch ( Exception e ) {	
							System.out.println( "An error occured while accessing the players list !" );
						}
					}
				}
			}

			// Play and send the move to the opponent
			if ( choice == 6 ) {
				if ( !test.connected || test.game == null ) 
					System.out.println( "Connect first and open a game !" );
				else {
					Move move = null;
					while ( move == null ) {
						System.out.println( "Enter your move :" );
						String s = readString( test.in );
						try {
							move = new Move( s.charAt(0), s.charAt(1) - '1' + 1,
									 s.charAt(2), s.charAt(3) - '1' + 1 );
							if ( !test.game.isLegalMove( move ) ) {
								System.out.println( "Illegal move !" );
								move = null;
							}
						}
						catch ( Exception e ) {
							move = null;
							System.out.println( "Malformed move !" );
						}
					}
					System.out.println( "Sending the move " + move.toString() + 
							    " to the player " + 
							    test.game.getOpponent().getScreenName() + " ..." );
					test.sendMove( test.game.getOpponent(), move );

					// Play the move locally
					test.game.addMove( move );
					if ( test.game.isChecked( true ) )
						System.out.println( "White King checked !" );
					if ( test.game.isChecked( false ) )
						System.out.println( "Black King checked !" );
				}
			}

			// Undo a move and send it to the opponent
			if ( choice == 7 ) {
				Move m = test.game.undoMove();	
				if ( test.game.isChecked( true ) )
					System.out.println( "White King checked !" );
				if ( test.game.isChecked( false ) )
					System.out.println( "Black King checked !" );
				test.game.printChessBoard();
			}
		}
		test.stopPolling = true;
	}
*/

	public void startApp() {

		display = Display.getDisplay( this );

		// Open (or create if it doesn't exist already) the Record Store	
		RMS = new JXMEChessRMS( RMS_NAME );

		// Show the home page
		try {
			initPage.append( Image.createImage( "/img/welcome-image.png" ) );
		}
		catch ( IOException e ) {
			System.out.println( "An error occured while trying to load welcome-image.png !" );
		}
		initPage.addCommand( exitCommand );
		initPage.addCommand( settingsCommand );
		initPage.setCommandListener( this );
		display.setCurrent( initPage );

		// Making the settings form
		settingsForm.append( proxyIPTF );
		settingsForm.append( proxyPortTF );
		settingsForm.append( screenNameTF );
		settingsForm.addCommand( okCommand );
		settingsForm.addCommand( cancelCommand );
		settingsForm.setCommandListener( this );
	}

	public void pauseApp() {
	}

	public void destroyApp( boolean unconditional) {
	}


	/**
	 * Remove all the commands of a Displayable object.
	 *
	 * @param d - the Displayable instance
	 */
	public void removeAllCommands( Displayable d ) {
		d.removeCommand( okCommand );
		d.removeCommand( quitCommand );
		d.removeCommand( cancelCommand );
		d.removeCommand( exitCommand );
		d.removeCommand( settingsCommand );
		d.removeCommand( searchPlayersCommand );
		d.removeCommand( showPlayersCommand );
		d.removeCommand( showGameCommand );
		d.removeCommand( newGameCommand );
		d.removeCommand( connectCommand );
		d.removeCommand( disconnectCommand );
		d.removeCommand( yesCommand );
		d.removeCommand( noCommand );
	}




	public void commandAction( Command c, Displayable s ) {

		System.out.println( "A command has been received." );

		// OK command in the Settings form
		if ( c==okCommand && inSettings ) {

			screenName = screenNameTF.getString();

			// Load the persistent state from previous connection if the screen name is the same
			// as the one used for previous connection
			if ( RMS.getScreenName() != null && RMS.getScreenName().equals( screenNameTF.getString() ) )
				persistentState = RMS.getPersistentState();
			else 
				persistentState = null;

			// Save the settings to the Record Store
			RMS.saveSetting( JXMEChessRMS.PROXY_IP, proxyIPTF.getString() );
			RMS.saveSetting( JXMEChessRMS.PROXY_PORT, proxyPortTF.getString() );
			RMS.saveSetting( JXMEChessRMS.SCREEN_NAME, screenNameTF.getString() );
		
			// Return to home page
			removeAllCommands( initPage );
			initPage.addCommand( connectCommand );
			initPage.addCommand( settingsCommand );
			initPage.addCommand( exitCommand );
			initPage.setCommandListener( this );
			display.setCurrent( initPage );
			inSettings = false;
		}
		else

		// Settings form requested
		if ( c == settingsCommand ) {

			// Load the settings from the Record Store
			proxyIPTF.setString( RMS.getProxyIP() );
			proxyPortTF.setString( RMS.getProxyPort() );
			screenNameTF.setString( RMS.getScreenName() );

			// Show the settings form
			display.setCurrent( settingsForm );
			inSettings = true;
		}
		else

		// Show game requested
		if ( c == showGameCommand ) {
			if ( game == null ) {
				Alert no_game_alert = new Alert( "There is no game to show !" );
				no_game_alert.setTimeout( 1500 );
				display.setCurrent( no_game_alert );	
			}
			else {
				game.addCommand( exitCommand );
				display.setCurrent( game );
				game.repaint();
			}
		}

		// Exit command requested in SettingsForm
		if ( c==cancelCommand && inSettings ) {
			removeAllCommands( initPage );
			initPage.addCommand( settingsCommand );
			initPage.addCommand( exitCommand );
			initPage.setCommandListener( this );
			display.setCurrent( initPage );
			inSettings = false;
		}
		else

		// Connect to proxy requested
		if ( c == connectCommand ) {
			connectProxy();		
			polling = new Thread( this );
			polling.start();
			removeAllCommands( initPage );
			initPage.addCommand( disconnectCommand );
			initPage.addCommand( searchPlayersCommand );
			initPage.addCommand( exitCommand );
			newGameCommand = new Command( "New game", Command.SCREEN, 2);
			initPage.addCommand( newGameCommand );
		}
		else

		// Disconnect requested
		if ( c == disconnectCommand ) {
			// [...]
			removeAllCommands( initPage );
			initPage.addCommand( connectCommand );
			initPage.addCommand( settingsCommand );
			initPage.addCommand( exitCommand );
			display.setCurrent( initPage );
		}
		else

		// Search players command requested
		if ( c == searchPlayersCommand ) {
			searchPlayers();
			removeAllCommands( initPage );
			initPage.addCommand( newGameCommand );
			initPage.addCommand( showGameCommand );
			initPage.addCommand( searchPlayersCommand );
			initPage.addCommand( disconnectCommand );
			initPage.addCommand( exitCommand );
			display.setCurrent( initPage );
		}
		else

		// New game command requested
		if ( c == newGameCommand ) {

			// Test if some players have been found
			if ( players == null || players.size()==0 ) {
				Alert no_players_alert = new Alert( "There is no player found !" );
				no_players_alert.setTimeout( 1500 );
				display.setCurrent( no_players_alert );	
			}
			else {
				Form f = new Form( "Opponent choice" );
				try {
					opponentChoiceGroup = new ChoiceGroup( "Your opponent :", ChoiceGroup.EXCLUSIVE );
					for ( int i=0; i<players.size(); i++) 
                        			opponentChoiceGroup.append( players.getPlayer(i).getScreenName(), null );
				}
				catch ( Exception e ) {
					System.out.println( "An error occured while making the list of players found !" );
				}
				f.append( opponentChoiceGroup );
				colorChoiceGroup = new ChoiceGroup( "Your color :", ChoiceGroup.EXCLUSIVE );
				colorChoiceGroup.append( "white", null );
				colorChoiceGroup.append( "black", null );
				f.append( colorChoiceGroup );
				f.addCommand( okCommand );
				f.addCommand( cancelCommand );
				f.setCommandListener( this );
				display.setCurrent( f );
				inChooseOpponent = true;
			}
		}
		else

		// The player has created a new game and choosen an opponent
		if ( inChooseOpponent && c==okCommand ) {

			inChooseOpponent = false;
			boolean color;
			if ( colorChoiceGroup.getString( colorChoiceGroup.getSelectedIndex() ).equals( "white" ) )
				color = true;
			else
				color = false;
			try {
				opponent = players.getPlayer( opponentChoiceGroup.getSelectedIndex());
			}
			catch ( Exception e ) {
				System.err.println( "An error occured while creating the game." ); 
				System.err.println( e );
			}
			game = new Game( opponent, color, this );
			invitePlayer( opponent, !color );
			game.setP2PStatus( Game.WAITING_INVITATION_ACK );
			game.repaint();
			game.addCommand( exitCommand );
			display.setCurrent( game );	
			inGame = true;
		}
		else  
	
		// The player has cancelled the creation of a game
		if ( inChooseOpponent && c==cancelCommand ) {
			inChooseOpponent = false;
			removeAllCommands( initPage );
			initPage.addCommand( newGameCommand );
			initPage.addCommand( showGameCommand );
			initPage.addCommand( searchPlayersCommand );
			initPage.addCommand( disconnectCommand );
			initPage.addCommand( exitCommand );
			display.setCurrent( initPage );
		}
		else

		// The player has accepted a game invitation
		if ( c == yesCommand && game.getP2PStatus() == Game.WAITING_INVITATION_ANSWER_ACK ) {

			// Send the answer to the remote player that sent the invitation
			Element[] elm = new Element[2];
			Message msg;
                	try {
				System.out.println( screenName + "> answering to a game invitation" );
                        	elm[0] = new Element( PEER_NAME_PREFIX + ":From", screenName.getBytes(), null, null );
                        	elm[1] = new Element( PEER_NAME_PREFIX + ":InvitationAccepted", "".getBytes(), null, null );
                        	msg = new Message( elm );
                        	invitationAnswerId = peerNetwork.send( game.getOpponent().getPipeName(),
									game.getOpponent().getPipeID(),
                        						PeerNetwork.UNICAST_PIPE,
                        						msg);
				game.setP2PStatus( Game.WAITING_INVITATION_ANSWER_ACK );
				game.repaint();
	                }
			catch ( IOException e ) {
				System.err.println( "Error while answering to an invitation !" );
			}

			// display the homepage till the invitation answer ACK is received
			display.setCurrent( initPage );
		}
		else

		// Exit command
		if ( c == exitCommand ) {
			polling = null;
			destroyApp( false );
			notifyDestroyed();
		}
		
	}
}


