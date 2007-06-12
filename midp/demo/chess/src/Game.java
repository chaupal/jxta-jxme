/************************************************************************
 *
 * $Id: Game.java,v 1.4 2003/03/27 17:18:55 pmaugeri Exp $
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
import java.io.IOException;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;


/*
   
   b0 b1 b2 b3 b4 b5 b6 b7
 8 0  1  2  3  4  5  6  7  : +0
 7 8  9  10 11 12 13 14 15 : +1
 6 16 17 18 19 20 21 22 23 : +2
 5 24 25 26 27 28 29 30 31 : +3
 4 32 33 34 35 36 37 38 39 : +4
 3 40 41 42 43 44 45 46 47 : +5
 2 48 49 50 51 52 53 54 55 : +6
 1 56 57 58 59 60 61 62 63 : +7
 a  b  c  d  e  f  g  h

*/
public class Game extends Canvas {

	// Constants definition
	private static final int MAX_GAME_MOVES = 100;  // maximum number of moves in a game (usually around 50)

	// History of the moves
	private Move[] moves;
	private int lastMove;

	// The chess board.
	// The chess board is represented by an array of 64 integer. The first entry of this array ( chessBoard[0]
	// corresponds to the a8 case of the chess board, chessBoard[1] = b8, ..., chessBoard[8] = a7, ...,
	// chessBoard[63] = h7.
	public int chessBoard[];

	private char pieceRepresentation[] = {  'k', 'q', 'b', 'b', 'n', 'n', 'r', 'r', 
						'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p',
						'.',
						'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P',
						'R', 'R', 'N', 'N', 'B', 'B', 'Q', 'K' };


	// Bits boards representation for all the pieces.
	private BitBoard[]  whitePiece;
	private BitBoard[]  whitePiecePM;	// white pieces possible moves
	private BitBoard[]  blackPiece;
	private BitBoard[]  blackPiecePM;	// white pieces possible moves
	public static final int EMPTY = 0;
	public static final int PAWN1 = 1;
	public static final int PAWN2 = 2;
	public static final int PAWN3 = 3;
	public static final int PAWN4 = 4;
	public static final int PAWN5 = 5;
	public static final int PAWN6 = 6;
	public static final int PAWN7 = 7;
	public static final int PAWN8 = 8;
	public static final int ROOK1 = 9;
	public static final int ROOK2 = 10;
	public static final int KNIGHT1 = 11;
	public static final int KNIGHT2 = 12;
	public static final int BISHOP1 = 13;
	public static final int BISHOP2 = 14;
	public static final int QUEEN = 15;
	public static final int KING = 16;
	public static final String[] PIECE_LABEL = { "EMPTY", "PAWN1", "PAWN2", "PAWN3", "PAWN4", "PAWN5", "PAWN6", "PAWN7", "PAWN8", "ROOK1", "ROOK2", "KNIGHT1", "KNIGHT2", "BISHOP1", "BISHOP2", "QUEEN", "KING" };

	private BitBoard whitePieces;
	private BitBoard blackPieces;

	// Flags for castlings
	// when false it cancel the possible move for the rest of the game.
	private boolean white00;
	private boolean white000;
	private boolean black00;
	private boolean black000;

	private JXMEChess jxmeChess;

	// The player we're playing the game with 
	private Player opponent;

	// Flag representing our color (true if we plays with white color, false if we plays with black color)
	private boolean color;

	// Variables for the graphical management
	private int squareWidth;
	private Image[] pieceImage;

	// Variable for managing the graphical cursor
	private boolean showCursor1;
	private int columnCursor1;
	private int rowCursor1;
	private boolean showCursor2;
	private int columnCursor2;
	private int rowCursor2;
	
	// Status of the game in the P2P environment
	public int p2pStatus;
	public static final int READY_TO_PLAY = 0;			// The player has to play now
	public static final int WAITING_INVITATION_ACK = 1;		// The player has sent a game invitation and wait
									// for the message ACK
	public static final int WAITING_INVITATION_ANSWER = 2;		// The player has sent a game invitation and wait
									// the remote player to accept it or not
	public static final int WAITING_INVITATION_ANSWER_ACK = 3;	// The player has answered an invitation and wait
									// for the message ACK
	public static final int WAITING_MOVE_ACK = 4;			// The player has sent a move and wait for the 
									// message ACK
	public static final int WAITING_MOVE = 5;			// The player is expected the remote player to 
									// send a move

	private BitBoard possibleMoves( int s, int p, BitBoard own_pieces, BitBoard opposite_pieces ) {

		int i;
		BitBoard bb = new BitBoard();
			
		// ROOK or QUEEN towards east
		if ( p==ROOK1 || p==ROOK2 || p==(-ROOK1) || p==(-ROOK2) || p==QUEEN || p==(-QUEEN) )
		for ( i=s+1; i%8!=0; i++ ) {
			if ( !(opposite_pieces.and(new BitBoard(i))).empty() ) {
				bb = bb.or(new BitBoard(i));
				break;
			}
			if ( !(own_pieces.and(new BitBoard(i))).empty() )
				break; 
			bb = bb.or(new BitBoard(i));
		}

		//ROOK or QUEEN towards west
		if ( p==ROOK1 || p==ROOK2 || p==(-ROOK1) || p==(-ROOK2) || p==QUEEN || p==(-QUEEN) )
		for ( i=s-1; i>=0 && i%8!=7; i-- ) {	
			if ( !(opposite_pieces.and(new BitBoard(i))).empty() ) {
				bb = bb.or(new BitBoard(i));
				break;
			}
			if ( !(own_pieces.and(new BitBoard(i))).empty() )
				break; 
			bb = bb.or(new BitBoard(i));
		}

		// ROOK or QUEEN towards south
		if ( p==ROOK1 || p==ROOK2 || p==(-ROOK1) || p==(-ROOK2) || p==QUEEN || p==(-QUEEN) )
		for ( i=s+8; i<64; i=i+8 ) {
			if ( !(opposite_pieces.and(new BitBoard(i))).empty() ) {
				bb = bb.or(new BitBoard(i));
				break;
			}
			if ( !(own_pieces.and(new BitBoard(i))).empty() )
				break; 
			bb = bb.or(new BitBoard(i));
		}

		// ROOK or QUEEN towards north
		if ( p==ROOK1 || p==ROOK2 || p==(-ROOK1) || p==(-ROOK2) || p==QUEEN || p==(-QUEEN) )
		for ( i=s-8; i>=0; i=i-8 ) {
			if ( !(opposite_pieces.and(new BitBoard(i))).empty() ) {
				bb = bb.or(new BitBoard(i));
				break;
			}
			if ( !(own_pieces.and(new BitBoard(i))).empty() )
				break; 
			bb = bb.or(new BitBoard(i));
		}
		
		// BISHOP or QUEEN towards north-east
		if ( p==BISHOP1 || p==BISHOP2 || p==(-BISHOP1) || p==(-BISHOP2) || p==QUEEN || p==(-QUEEN) )
		for ( i=s-7; i>0 && i%8!=0; i=i-7 ) {
			if ( !(opposite_pieces.and(new BitBoard(i))).empty() ) {
				bb = bb.or(new BitBoard(i));
				break;
			}
			if ( !(own_pieces.and(new BitBoard(i))).empty() )
				break; 
			bb = bb.or(new BitBoard(i));
		}

		// BISHOP or QUEEN towards north-west
		if ( p==BISHOP1 || p==BISHOP2 || p==(-BISHOP1) || p==(-BISHOP2) || p==QUEEN || p==(-QUEEN) )
		for ( i=s-9; i>=0 && i%8!=7; i=i-9 ) {
			if ( !(opposite_pieces.and(new BitBoard(i))).empty() ) {
				bb = bb.or(new BitBoard(i));
				break;
			}
			if ( !(own_pieces.and(new BitBoard(i))).empty() )
				break; 
			bb = bb.or(new BitBoard(i));
		}

		// BISHOP or QUEEN towards south-west
		if ( p==BISHOP1 || p==BISHOP2 || p==(-BISHOP1) || p==(-BISHOP2) || p==QUEEN || p==(-QUEEN) )
		for ( i=s+7; i<64 && i%8!=7; i=i+7 ) {
			if ( !(opposite_pieces.and(new BitBoard(i))).empty() ) {
				bb = bb.or(new BitBoard(i));
				break;
			}
			if ( !(own_pieces.and(new BitBoard(i))).empty() )
				break; 
			bb = bb.or(new BitBoard(i));
		}

		// BISHOP or QUEEN towards south-east
		if ( p==BISHOP1 || p==BISHOP2 || p==(-BISHOP1) || p==(-BISHOP2) || p==QUEEN || p==(-QUEEN) )
		for ( i=s+9; i<64 && i%8!=0; i=i+9 ) {
			if ( !(opposite_pieces.and(new BitBoard(i))).empty() ) {
				bb = bb.or(new BitBoard(i));
				break;
			}
			if ( !(own_pieces.and(new BitBoard(i))).empty() )
				break; 
			bb = bb.or(new BitBoard(i));
		}

		// KNIGHT
		if ( p==KNIGHT1 || p==KNIGHT2 || p==(-KNIGHT1) || p==(-KNIGHT2) ) {
			BitBoard target;
			target = new BitBoard(s-17);
			if ( s>15 && s%8>0 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s-15);
			if ( s>15 && s%8<7 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s-10);
			if ( s>7 && s%8>1 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s-6);
			if ( s>7 && s%8<6 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s+6);
			if ( s<56 && s%8>1 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s+10);
			if ( s<56 && s%8<6 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s+15);
			if ( s<48 && s%8>0 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s+17);
			if ( s<48 && s%8<7 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
		}

		// KING
		if ( p==KING || p==(-KING) ) {
			BitBoard target;
			target = new BitBoard(s-9);
			if ( s>7 && s%8>0 && (target.and(own_pieces)).empty() ) 
				bb = bb.or( target );	
			target = new BitBoard(s-8);
			if ( s>7 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s-7);
			if ( s>7 && s%8<7 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s-1);
			if ( s%8>0 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s+1);
			if ( s%8<7 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s+7);
			if ( s<56 && s%8>0 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s+8);
			if ( s<56 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			target = new BitBoard(s+9);
			if ( s<56 && s%8<7 && (target.and(own_pieces)).empty() )
				bb = bb.or( target );	
			// 0-0 white king
			if ( p==KING && white00 && chessBoard[61]==EMPTY && chessBoard[62]==EMPTY ) {
				boolean ok = true;
				for ( int k=1; k<=16 && ok; k++ )
					for ( int j=60;j<=62;j++ )
						if ( !(blackPiecePM[k].and(new BitBoard(j))).empty() )	{
							ok = false;
							break;
						}
				if ( ok )
					bb = bb.or(new BitBoard(62));
			}
			// 0-0-0 white king
			if ( p==KING && white000 && chessBoard[59]==EMPTY && chessBoard[58]==EMPTY  && chessBoard[57]==EMPTY) {
				boolean ok = true;
				for ( int k=1; k<=16 && ok; k++ )
					for ( int j=58;j<=60;j++ )
						if ( !(blackPiecePM[k].and(new BitBoard(j))).empty() )	{
							ok = false;
							break;
						}
				if ( ok )
					bb = bb.or(new BitBoard(58));
			}
			// 0-0 black king
			if ( p==(-KING) && black00 && chessBoard[5]==EMPTY && chessBoard[6]==EMPTY ) {
				boolean ok = true;
				for ( int k=1; k<=16 && ok; k++ )
					for ( int j=4;j<=6;j++ )
						if ( !(whitePiecePM[k].and(new BitBoard(j))).empty() )	{
							ok = false;
							break;
						}
				if ( ok )
					bb = bb.or(new BitBoard(6));
			}
			// 0-0-0 black king
			if ( p==(-KING) && black000 && chessBoard[1]==EMPTY && chessBoard[2]==EMPTY && chessBoard[3]==EMPTY) {
				boolean ok = true;
				for ( int k=1; k<=16 && ok; k++ )
					for ( int j=2;j<=4;j++ )
						if ( !(whitePiecePM[k].and(new BitBoard(j))).empty() )	{
							ok = false;
							break;
						}
				if ( ok )
					bb = bb.or(new BitBoard(2));
			}
		}

		// PAWN
		if ( p==PAWN1 || p==PAWN2 || p==PAWN3 || p==PAWN4 || p==PAWN5 || p==PAWN6 || p==PAWN7 || p==PAWN8 ) {
			BitBoard target;
			target = new BitBoard( s-8 );	// P north
			if ( target.and(whitePieces).empty() && target.and(blackPieces).empty() )	
				bb = bb.or(target);
			if ( s>=48 && s<=55 ) {		// P north 2 squares
				target = new BitBoard( s-16 );
				if ( target.and(whitePieces).empty() && target.and(blackPieces).empty() &&
				     (new BitBoard(s-8)).and(whitePieces).empty() &&
				     (new BitBoard(s-8)).and(blackPieces).empty() )
					bb = bb.or(target);
			}
			if ( s%8>0 ) {			// P north-west
				target = new BitBoard(s-9);
				if (!(target.and(opposite_pieces)).empty())
					bb = bb.or(target);
			}
			if ( s%8<7 ) {			// P north-east
				target = new BitBoard(s-7);
				if (!(target.and(opposite_pieces)).empty())
					bb = bb.or(target);
			}

			// "En passant" special move
			if ( lastMove>0 && lastMove%2==0 ) {
				int prev_p = chessBoard[moves[lastMove-1].getTo()];
				if ( ( prev_p==(-PAWN1) || prev_p==(-PAWN2) || prev_p==(-PAWN3) || prev_p==(-PAWN4) ||
				       prev_p==(-PAWN5) || prev_p==(-PAWN6) || prev_p==(-PAWN7) || prev_p==(-PAWN8) )
				       && moves[lastMove-1].getInitialRow()==7 && moves[lastMove-1].getFinalRow()==5 ) {
					if ( moves[lastMove-1].getTo() == s-1 )
						bb = bb.or(new BitBoard(s-9));
					if ( moves[lastMove-1].getTo() == s+1 )
						bb = bb.or(new BitBoard(s-7));
				}  
			}
			
		}
		if ( p==(-PAWN1) || p==(-PAWN2) || p==(-PAWN3) || p==(-PAWN4) || 
		     p==(-PAWN5) || p==(-PAWN6) || p==(-PAWN7) || p==(-PAWN8) ) {
			BitBoard target;
			target = new BitBoard( s+8 );	// P south
			if ( target.and(blackPieces).empty() && target.and(whitePieces).empty() )	
				bb = bb.or(target);
			if ( s>=8 && s<=15 ) {		// P south 2 squares
				target = new BitBoard( s+16 );
				if ( target.and(whitePieces).empty() && target.and(blackPieces).empty() &&
				     (new BitBoard(s+8)).and(whitePieces).empty() &&
				     (new BitBoard(s+8)).and(blackPieces).empty() )
					bb = bb.or(target);
			}
			if ( s%8>0 ) {			// P south-west
				target = new BitBoard(s+7);
				if (!(target.and(opposite_pieces)).empty())
					bb = bb.or(target);
			}
			if ( s%8<7 ) {			// P south-east
				target = new BitBoard(s+9);
				if (!(target.and(opposite_pieces)).empty())
					bb = bb.or(target);
			}

			// "En passant" special move
			if ( lastMove%2==1 ) {
				int prev_p = chessBoard[moves[lastMove-1].getTo()];
				if ( ( prev_p==PAWN1 || prev_p==PAWN2 || prev_p==PAWN3 || prev_p==PAWN4 ||
				       prev_p==PAWN5 || prev_p==PAWN6 || prev_p==PAWN7 || prev_p==PAWN8 )
				       && moves[lastMove-1].getInitialRow()==2 && moves[lastMove-1].getFinalRow()==4 ) {
					if ( moves[lastMove-1].getTo() == s-1 )
						bb = bb.or(new BitBoard(s+7));
					if ( moves[lastMove-1].getTo() == s+1 )
						bb = bb.or(new BitBoard(s+9));
				}  
			}
		}

				

		return bb;
	}



	/*
	 * Constructor.
	 *
	 * @param opponent - the Player instance that represents our opponent
	 * @param white - true if the player plays with white color, false if the opponents plays with black color 
	 */
	public Game ( Player opponent, boolean white, JXMEChess jxme_chess ) {
		
		this.jxmeChess = jxme_chess;

		moves = new Move[ MAX_GAME_MOVES ];
		lastMove = 0;
		this.opponent = opponent;
		color = white;	
		white00 = true;
		white000 = true;
		black00 = true;
		black000 = true;
		
		chessBoard = new int[64];
		for (int i=0; i<64; i++) 
			chessBoard[i] = EMPTY;
		chessBoard[48] = PAWN1;
		chessBoard[49] = PAWN2;
		chessBoard[50] = PAWN3;
		chessBoard[51] = PAWN4;
		chessBoard[52] = PAWN5;
		chessBoard[53] = PAWN6;
		chessBoard[54] = PAWN7;
		chessBoard[55] = PAWN8;
		chessBoard[56] = ROOK1;
		chessBoard[57] = KNIGHT1;
		chessBoard[58] = BISHOP1;
		chessBoard[59] = QUEEN;
		chessBoard[60] = KING;
		chessBoard[61] = BISHOP2;
		chessBoard[62] = KNIGHT2;
		chessBoard[63] = ROOK2;
		chessBoard[0] = -ROOK1;
		chessBoard[1] = -KNIGHT1;
		chessBoard[2] = -BISHOP1;
		chessBoard[3] = -QUEEN;
		chessBoard[4] = -KING;
		chessBoard[5] = -BISHOP2;
		chessBoard[6] = -KNIGHT2;
		chessBoard[7] = -ROOK2;
		chessBoard[8] = -PAWN1;
		chessBoard[9] = -PAWN2;
		chessBoard[10] = -PAWN3;
		chessBoard[11] = -PAWN4;
		chessBoard[12] = -PAWN5;
		chessBoard[13] = -PAWN6;
		chessBoard[14] = -PAWN7;
		chessBoard[15] = -PAWN8;
		//printChessBoard();
		//
		System.out.println( "Creating the bit boards ..." );

		whitePiece = new BitBoard[17];
		blackPiece = new BitBoard[17];
		whitePiece[PAWN1] = new BitBoard(48);
		whitePiece[PAWN2] = new BitBoard(49);
		whitePiece[PAWN3] = new BitBoard(50);
		whitePiece[PAWN4] = new BitBoard(51);
		whitePiece[PAWN5] = new BitBoard(52);
		whitePiece[PAWN6] = new BitBoard(53);
		whitePiece[PAWN7] = new BitBoard(54);
		whitePiece[PAWN8] = new BitBoard(55);
		whitePiece[ROOK1] = new BitBoard(56);
		whitePiece[KNIGHT1] = new BitBoard(57);
		whitePiece[BISHOP1] = new BitBoard(58);
		whitePiece[QUEEN] = new BitBoard(59);
		whitePiece[KING] = new BitBoard(60);
		whitePiece[BISHOP2] = new BitBoard(61);
		whitePiece[KNIGHT2] = new BitBoard(62);
		whitePiece[ROOK2] = new BitBoard(63);
		blackPiece[PAWN1] = new BitBoard(8);
		blackPiece[PAWN2] = new BitBoard(9);
		blackPiece[PAWN3] = new BitBoard(10);
		blackPiece[PAWN4] = new BitBoard(11);
		blackPiece[PAWN5] = new BitBoard(12);
		blackPiece[PAWN6] = new BitBoard(13);
		blackPiece[PAWN7] = new BitBoard(14);
		blackPiece[PAWN8] = new BitBoard(15);
		blackPiece[ROOK1] = new BitBoard(0);
		blackPiece[KNIGHT1] = new BitBoard(1);
		blackPiece[BISHOP1] = new BitBoard(2);
		blackPiece[QUEEN] = new BitBoard(3);
		blackPiece[KING] = new BitBoard(4);
		blackPiece[BISHOP2] = new BitBoard(5);
		blackPiece[KNIGHT2] = new BitBoard(6);
		blackPiece[ROOK2] = new BitBoard(7);

		// Sum the bit boards of all white pieces
		whitePieces = new BitBoard();
		for (int i=1; i<=16; i++)
			whitePieces = whitePieces.or(whitePiece[i]);
		//System.out.println( "whitePieces:" );
		//whitePieces.print();

		// Sum the bit board of all black pieces
		blackPieces = new BitBoard();
		for (int i=1; i<=16; i++)
			blackPieces = blackPieces.or(blackPiece[i]);
		//System.out.println( "blackPieces:" );
		//blackPieces.print();

		// Possible moves (PM) initialisation
		whitePiecePM = new BitBoard[17];
		blackPiecePM = new BitBoard[17];
		for ( int i=1; i<=16; i++ ) {
			whitePiecePM[i] = possibleMoves( whitePiece[i].square(), i, whitePieces, blackPieces );
			//System.out.println( "whitePiecePM=" + i );
			//whitePiecePM[i].print();
		}
		for ( int i=1; i<=16; i++ ) {
			blackPiecePM[i] = possibleMoves( blackPiece[i].square(), -i, blackPieces, whitePieces );
			//System.out.println( "blackPiecePM=" + i );
			//blackPiecePM[i].print();
		}

		if ( getWidth() > getHeight() )
			squareWidth = getHeight()/8;
		else
			squareWidth = getWidth()/8;
		squareWidth = 10;

                // Loading images for the pieces
		pieceImage = new Image[33];
		try {
			pieceImage[8]=pieceImage[9]=pieceImage[10]=pieceImage[11]=pieceImage[12]=pieceImage[13]=pieceImage[14]=pieceImage[15] = Image.createImage( "/img/black_pawn.png" );
			pieceImage[7] = pieceImage[6] = Image.createImage( "/img/black_rook.png" );
			pieceImage[5] = pieceImage[4] = Image.createImage( "/img/black_knight.png" );
			pieceImage[3] = pieceImage[2] = Image.createImage( "/img/black_bishop.png" );
			pieceImage[1] = Image.createImage( "/img/black_queen.png" );
			pieceImage[0] = Image.createImage( "/img/black_king.png" );
			pieceImage[18]=pieceImage[19]=pieceImage[20]=pieceImage[21]=pieceImage[22]=pieceImage[23]=pieceImage[24]=pieceImage[17] = Image.createImage( "/img/white_pawn.png" );
			pieceImage[25] = pieceImage[26] = Image.createImage( "/img/white_rook.png" );
			pieceImage[27] = pieceImage[28] = Image.createImage( "/img/white_knight.png" );
			pieceImage[29] = pieceImage[30] = Image.createImage( "/img/white_bishop.png" );
			pieceImage[31] = Image.createImage( "/img/white_queen.png" );
			pieceImage[32] = Image.createImage( "/img/white_king.png" );
		}
		catch ( IOException e ) {
			System.err.println( "An error occured while reading the pieces images." + e );
		}

		// Init graphical cursor
		columnCursor2 = 5;
		rowCursor2 = 1;
		showCursor2 = false;
		columnCursor1 = 5;
		rowCursor1 = 1;
		showCursor1 = true;
	}



	/*
	 * Add a move to the game moves history
	 *
	 * @param the move to add
	 * @return true if the move has been added to the history, false otherwise (illegal move)
	 */
	public boolean addMove ( Move m ) {

		if ( lastMove == MAX_GAME_MOVES )
			return false;
		else {
			int p = chessBoard[ m.getFrom() ];
			moves[ lastMove++ ] = m;

			// "en passant" special move case
			if ( p>0 && p>=PAWN1 && p<=PAWN8 && (m.getTo()==m.getFrom()-7 || m.getTo()==m.getFrom()-9) && 
			     chessBoard[m.getTo()]==EMPTY &&
			     chessBoard[m.getTo()+8]<=(-PAWN1) && chessBoard[m.getTo()+8]>=(-PAWN8) ) {
				m.setCapturedPieceType(chessBoard[m.getTo()+8]);	
				m.setCapturedPieceSquare(m.getTo()+8);	
				chessBoard[m.getTo()+8] = EMPTY;
				blackPiece[chessBoard[m.getTo()+8]] = new BitBoard();
			}
			if ( p<0 && p>=(-PAWN8) && p<=(-PAWN1) && (m.getTo()==m.getFrom()+7 || m.getTo()==m.getFrom()+9) &&
			     chessBoard[m.getTo()]==EMPTY &&
			     chessBoard[m.getTo()-8]>=PAWN1 && chessBoard[m.getTo()-8]<=PAWN8 ) {
				m.setCapturedPieceType(chessBoard[m.getTo()-8]);	
				m.setCapturedPieceSquare(m.getTo()-8);	
				chessBoard[m.getTo()-8] = EMPTY;
				whitePiece[chessBoard[m.getTo()+8]] = new BitBoard();
			}

			// Update the chess board (move the piece + empty original cell)
			if ( chessBoard[m.getTo()] != EMPTY ) {			// a piece is captured
				m.setCapturedPieceType(chessBoard[m.getTo()]);	
				m.setCapturedPieceSquare(m.getTo());	
			}
			chessBoard[ m.getTo() ] = chessBoard[ m.getFrom() ];
			chessBoard[ m.getFrom() ] = EMPTY;
			
			// Update bit boards
			if ( p > 0 ) { 	// white piece
				whitePieces = whitePieces.xor( whitePiece[p] );	// Remove old position from whitePieces
				whitePiece[p] = new BitBoard( m.getTo() );
				whitePieces = whitePieces.or( whitePiece[p] );	// Add new position to whitePieces
				if ( m.getCapturedPieceType() < 0 )
					blackPiece[-(m.getCapturedPieceType())] = new BitBoard();

				// 0-0 of white KING
				if ( p==KING && m.getFrom()==60 && m.getTo()==62 ) {
					chessBoard[61] = chessBoard[63];
					chessBoard[63] = EMPTY;
					whitePieces = whitePieces.xor(whitePiece[ROOK2]);
					whitePiece[ROOK2] = new BitBoard(61);
					whitePieces = whitePieces.or(whitePiece[ROOK2]);
				}
				// 0-0-0 of white KING
				if ( p==KING && m.getFrom()==60 && m.getTo()==58 ) {
					chessBoard[59] = chessBoard[56];
					chessBoard[56] = EMPTY;
					whitePieces = whitePieces.xor(whitePiece[ROOK1]);
					whitePiece[ROOK1] = new BitBoard(59);
					whitePieces = whitePieces.or(whitePiece[ROOK1]);
				}
	
				// If the KING/ROOK moves this disables all/some castling cases
				if ( p==KING ) {
					white00 = false;
					white000 = false;
				}
				if ( p==ROOK1 )
					white000 = false;
				if ( p==ROOK2 )
					white00 = false;
		
			
			}
			else {		// black piece
				blackPieces = blackPieces.xor( blackPiece[-p] ); // Remove old position from blackPieces
				blackPiece[-p] = new BitBoard( m.getTo() );
				blackPieces = blackPieces.or( blackPiece[-p] );  // Add new position to blackPoeces
				if ( m.getCapturedPieceType() > 0 )
					whitePiece[m.getCapturedPieceType()] = new BitBoard();

				// 0-0 of black KING
				if ( p==(-KING) && m.getFrom()==4 && m.getTo()==6 ) {
					chessBoard[5] = chessBoard[7];
					chessBoard[7] = EMPTY;
					blackPieces = blackPieces.xor(blackPiece[ROOK2]);
					blackPiece[ROOK2] = new BitBoard(5);
					blackPieces = blackPieces.or(blackPiece[ROOK2]);
				}
				// 0-0-0 of black KING
				if ( p==(-KING) && m.getFrom()==4 && m.getTo()==2 ) {
					chessBoard[3] = chessBoard[0];
					chessBoard[0] = EMPTY;
					whitePieces = whitePieces.xor(whitePiece[ROOK1]);
					whitePiece[ROOK1] = new BitBoard(3);
					whitePieces = whitePieces.or(whitePiece[ROOK1]);
				}

				// If the KING/ROOK moves this disables all/some castling cases
				if ( p==(-KING) ) {
					black00 = false;
					black000 = false;
				}
				if ( p==(-ROOK1) )
					black000 = false;
				if ( p==(-ROOK2) )
					black00 = false;
		
				// [...]
				// Insert ENPASSANT here
				// [...]
			}

			// Update possible moves bit boards
			for ( int i=1; i<=16; i++ ) {
				whitePiecePM[i] = possibleMoves( whitePiece[i].square(), i, whitePieces, blackPieces );
				//System.out.println( "Possible moves for white " + PIECE_LABEL[i] );
				//whitePiecePM[i].print();
				blackPiecePM[i] = possibleMoves( blackPiece[i].square(), -i, blackPieces, whitePieces );
				//System.out.println( "Possible moves for black" + PIECE_LABEL[i] );
				//blackPiecePM[i].print();
			}

			return true;
		}
	}


	/*
	 * Undo a move
	 *
	 * @return the move cancelled by this Undo - null in case of error (eg. no previous move anymore)
	 */
	public Move undoMove () {

		if ( lastMove == 0 )
			return null;
		else {
			Move m = moves[--lastMove];
			int p = chessBoard[ m.getTo() ];

			// Update chess board
			chessBoard[m.getFrom()] = chessBoard[m.getTo()];
			chessBoard[m.getTo()] = EMPTY;
			if ( m.getCapturedPieceType() != 0 )
				chessBoard[m.getCapturedPieceSquare()] = m.getCapturedPieceType();
		
			// White king 0-0 case
			if ( p==KING && m.getFrom()==60 && m.getTo()==62 ) {
				chessBoard[63] = chessBoard[61];
				chessBoard[61] = EMPTY;
				whitePiece[ROOK2] = new BitBoard(63);
			}
			// Black king 0-0 case
			if ( p==(-KING) && m.getFrom()==4 && m.getTo()==6 ) {
				chessBoard[7] = chessBoard[5];
				chessBoard[5] = EMPTY;
				whitePiece[ROOK2] = new BitBoard(7);
			}
			// White king 0-0-0 case
			if ( p==KING && m.getFrom()==60 && m.getTo()==58 ) {
				chessBoard[56] = chessBoard[59];
				chessBoard[59] = EMPTY;
				whitePiece[ROOK1] = new BitBoard(56);
			}
			// Black king 0-0-0 case
			if ( p==(-KING) && m.getFrom()==4 && m.getTo()==2 ) {
				chessBoard[0] = chessBoard[3];
				chessBoard[3] = EMPTY;
				whitePiece[ROOK1] = new BitBoard(0);
			}

			// Update bit boards
			if ( p>0 ) {		// white piece move
				whitePieces = whitePieces.xor(whitePiece[p]);
				whitePiece[p] = new BitBoard(m.getFrom());
				whitePieces = whitePieces.or(whitePiece[p]);
			}
			else {			// black piece move
				blackPieces = blackPieces.xor(blackPiece[-p]);
				blackPiece[-p] = new BitBoard(m.getFrom());
				blackPieces = blackPieces.or(blackPiece[-p]);
			}
			
			// Update possible moves bit boards
			for ( int i=1; i<=16; i++ ) {
				whitePiecePM[i] = possibleMoves( whitePiece[i].square(), i, whitePieces, blackPieces );
				//System.out.println( "Possible moves for white " + PIECE_LABEL[i] );
				//whitePiecePM[i].print();
				blackPiecePM[i] = possibleMoves( blackPiece[i].square(), -i, blackPieces, whitePieces );
				//System.out.println( "Possible moves for black" + PIECE_LABEL[i] );
				//blackPiecePM[i].print();
			}

			return m;
		}
	}


	public int[] getChessBoard() {
		return chessBoard;
	}

	public void printChessBoard() {

		//for (int i=0;i<64;i++)
		//	System.out.println( i + ":" + chessBoard[i] );
		System.out.println();
                for ( int r=0; r<8; r++ ) {
                	System.out.print( 8-r + "| " );
                	for ( int c=0; c<8; c++) 
				System.out.print( pieceRepresentation[16+chessBoard[(r*8)+c]] + " " );
                	System.out.println();
                }
                System.out.println( "------------------");
                System.out.println( "   a b c d e f g h");
                System.out.println( "------------------");
                System.out.println( "Playing with : " + opponent.getScreenName() );
	}


	/*
	 * Return the Player instance of our opponent
	 *
	 * @return a Player instance.
	 */
	public Player getOpponent() {
		return opponent;
	}
	
	/*
	 * Return the color we play with.
	 *
	 * @return true if we play with white color, false otherwise.
	 */
	public boolean getColor() {
		return color;
	}


	/*
	 * Return the current number of moves of the game.
	 *
	 * @return the number of moves
	 */
	public int countMoves() {
		return lastMove;
	}


	/*
	 * Return true if the user owner of this game can play or not
	 *
	 * @return true or false if the user can play or not
	 */
	public boolean canPlay() {
		
		if ( p2pStatus != READY_TO_PLAY )
			return false;	
		if ( ( ( ( lastMove % 2 ) == 0 ) && color ) || ( ( ( lastMove % 2 ) == 1) && !color ) )
			return true;
		else
			return false;
	}

	

	/* 
	 * Run a several tests to verify that a move is a legal chess move depending on
	 * the current configuration of the game.
	 * 
	 * @param a Move instance - the move the test 
	 * @return true if the move is legal, false if the move is illegal
	 */
	public boolean isLegalMove( Move m ) {

		int p = chessBoard[m.getFrom()];
		BitBoard[] piece_PM;

		// Check if the player is moving one his/her pieces
		if ( (lastMove%2==0 && p<0) || (lastMove%2==1 && p>0) )
			return false;

		if ( p>0 )		// White piece		
			piece_PM = whitePiecePM;
		else { 			// Black piece
			piece_PM = blackPiecePM;
			p = (-p);
		}
		if ( (piece_PM[p].and(new BitBoard(m.getTo()))).empty() )
			return false;
		else 
			return true;
	}


	/*	
	 * Return a flag saying if the color side specified in parameter is checked.
	 *
	 * @param true to test if the white king is checked, false to test if the black king is checked 
	 * @return true if a Check has been detected, false otherwise
	 */
	public boolean isChecked( boolean color ) {

		int i;

		if ( color )	// test if the white king is checked
			for ( i=1; i<=16; i++ ) {
				if ( !((blackPiecePM[i].and(whitePiece[KING])).empty()) )
					return true;
			}
		else		// test if the black king is checked
			for ( i=1; i<=16; i++ ) {
				if ( !((whitePiecePM[i].and(blackPiece[KING])).empty()) )
					return true;
			}
		return false;
	}

	
	/*
	 * Return true if the color side specified is checked mate.
	 * 
	 * @param true to test if the white king is checked mate, false to test if the black king is checked mate.
	 * @return true if a Checked mate has been detected, false otherwise.
	 */
	public boolean isCheckedMate( boolean color ) {

		int i = 0;
		int to = 0;
		BitBoard sim = null;
		Move m  = null;

		if ( color ) {	// test if the white side is checked mate
			if ( !isChecked(color) )
				return false;
			for ( i=1; i<=16; i++ ) {
				sim = whitePiecePM[i].copy();
				while ( !sim.empty() ) {
					to = sim.square();
					try {
						m = new Move( whitePiece[i].square(), to );			
					}
					catch ( Exception e ) {
						System.out.println( "An error occured while creating a move instance !" );
					}
					addMove( m );
					if ( !isChecked( color ) ) {
						undoMove();
						return false;
					} 
					undoMove();
					sim = sim.xor(new BitBoard(to));
				}
			}
		}
		else {		// test if the black side is checked mate
			if ( !isChecked(color) )
				return false;
			for ( i=1; i<=16; i++ ) {
				sim = blackPiecePM[i].copy();
				while ( !sim.empty() ) {
					to = sim.square();
					try {
						m = new Move( blackPiece[i].square(), to );			
					}
					catch ( Exception e ) {
						System.out.println( "An error occured while creating a move instance !" );
					}
					addMove( m );
					if ( !isChecked( color ) ) {
						undoMove();
						return false;
					} 
					undoMove();
					sim = sim.xor(new BitBoard(to));
				}
			}
		}
		return true;
	}

	public void paint( Graphics g ) {

		int r,c,color;

		g.setColor( 255, 255, 255 );
		g.fillRect( 1, 1, getWidth(), getHeight() );
		g.setColor( 0, 0, 0 );
		g.drawRect( 1, 1, squareWidth*8+1, squareWidth*8+1 );

		color = 255;
		for ( r=1; r<=8; r++ ) {
			color = (color==128)?255:128;
			for ( c=1; c<=8; c++ ) {

				// Draw the square
				color = (color==128)?255:128;
				g.setColor( color, color, color );
				g.fillRect( 2+(c-1)*squareWidth, 2+(r-1)*squareWidth, squareWidth, squareWidth );

				// Draw the piece
				int i = c - 1 + (r-1)*8;
				if ( chessBoard[i] != EMPTY ) {
					g.drawImage( pieceImage[16+chessBoard[i]], 2+(c-1)*squareWidth+2, 2+(r-1)*squareWidth+2, Graphics.TOP|Graphics.LEFT );
				}
			}
		}

		// Draw the cursor
		if ( canPlay() )
			showCursor1 = true;
		else
			showCursor1 = showCursor2 = false;
		g.setColor( 0, 0, 0 );
		if ( showCursor1 )
			g.drawRect( 2+(columnCursor1-1)*squareWidth, 2+(8-rowCursor1)*squareWidth, 
				    squareWidth, squareWidth );
		if ( showCursor2 )
			g.drawRect( 2+(columnCursor2-1)*squareWidth, 2+(8-rowCursor2)*squareWidth, 
				    squareWidth, squareWidth );

		// Write the names of the players
		g.setColor( 0, 0, 0 );
		Font f = Font.getFont( Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL );
		Font f_bold = Font.getFont( Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL );
		if ( canPlay() && p2pStatus == READY_TO_PLAY )
			g.setFont( f_bold );
		else
			g.setFont( f );
		g.drawString( jxmeChess.screenName, 2, squareWidth*8+4, Graphics.TOP|Graphics.LEFT );
		int advance = g.getFont().stringWidth( jxmeChess.screenName );
		if ( !canPlay() && p2pStatus == READY_TO_PLAY )
			g.setFont( f_bold );
		else
			g.setFont( f );
		g.drawString( opponent.getScreenName(), 4 + advance, squareWidth*8+4, Graphics.TOP|Graphics.LEFT );
	}
	


	protected void keyPressed( int keyCode ) {

		int action = getGameAction(keyCode);
			if (action == Canvas.LEFT){
				columnCursor1--;
				if ( columnCursor1 < 1 )
					columnCursor1 = 8;
			}
			else 
			if (action == Canvas.RIGHT){
				columnCursor1++;
				if ( columnCursor1 > 8 )
					columnCursor1 = 1;
			}
			else 
			if (action == Canvas.UP){
				rowCursor1++;
				if ( rowCursor1 > 8 )
					rowCursor1 = 1;
			}
			else 
			if (action == Canvas.DOWN){
				rowCursor1--;
				if ( rowCursor1 < 1 )
					rowCursor1 = 8;
			}
			if ( action == Canvas.FIRE ) {

				// The player cancels the move
				if ( showCursor1 && showCursor2 && rowCursor1 == rowCursor2 && 
				     columnCursor1 == columnCursor2 ) 
					showCursor2 = false;
				else

				// A move has been made
				if ( showCursor1 && showCursor2 ) {
					String s = " abcdefgh";
					try {
						Move m = new Move( s.charAt( columnCursor2 ), rowCursor2,
								   s.charAt( columnCursor1 ), rowCursor1 ); 
						if ( !isLegalMove( m ) ) {
							System.out.println( jxmeChess.screenName + "> Illegal move." );
							showCursor2 = false;
						}
						else {
							// Play the move locally
							System.out.println( jxmeChess.screenName + "> playing move " + 
									    m.toString() );
							addMove( m );
							// send the move
							System.out.println( jxmeChess.screenName + "> sending move " + 
									    m.toString() );
							jxmeChess.sendMove( opponent, m );
							repaint();
							showCursor2 = false;
							// Test if there is king checked
							if ( isChecked( true ) )
								System.out.println( "White king checked !" );
							if ( isChecked( false ) )
								System.out.println( "Black king checked !" );
						}
					}
					catch ( Exception e ) {	
						System.out.println( "Malformed move !" );
						showCursor2 = false;
					}
				}

				// The "origin" of the move has been done
				else {
					rowCursor2 = rowCursor1;
					columnCursor2 = columnCursor1;
					showCursor2 = true;
				}
			}
			repaint();
	}


	public void setP2PStatus( int status ) {
		p2pStatus = status;
	}

	public int getP2PStatus() {
		return p2pStatus;
	}

}




