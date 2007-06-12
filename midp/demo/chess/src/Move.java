/************************************************************************
 *
 * $Id: Move.java,v 1.3 2003/03/27 10:38:31 pmaugeri Exp $
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
public class Move {

	private char initialColumn;
	private int initialRow;
	private char finalColumn;
	private int finalRow;
	private String shortNotation;

	private int toSquare;
	private int fromSquare;
	private int capturedPieceType;
	private int capturedPieceSquare;

	public Move ( char initial_column, int initial_row, char final_column, int final_row ) 
		throws Exception {

		if ( "abcdefgh".indexOf( initial_column ) == -1 ||
		     "abcdefgh".indexOf( final_column ) == -1 ||
		     (initial_row < 1 || initial_row > 8) ||
		     (final_row < 1 || final_row > 8) )
			throw new Exception ( "Malformed chess move !" );
		else {
			initialColumn = initial_column;
			initialRow = initial_row;
			finalColumn = final_column;
			finalRow = final_row;
			toSquare = (8-finalRow)*8 + finalColumn-'a';
                        fromSquare = (8-initialRow)*8+initialColumn-'a';
			capturedPieceType = 0;
			capturedPieceSquare = -1;
		}		
	}

	public Move ( int from, int to )
		throws Exception {
	
		if ( from < 0 || from > 63 || to < 0 || to > 63 ) 
			throw new Exception ( "Malformed chess move !");
		else {
			initialColumn = (char)('a' + from % 8);
			initialRow = from / 8;
			finalColumn = (char)('a' + to % 8);
			finalRow = to / 8;
			toSquare = to;
                        fromSquare = from;
			capturedPieceType = 0;
			capturedPieceSquare = -1;
		}
	}
	
	public void setInitialColumn( char column ) 
		throws Exception {

		if ( "abcdefgh".indexOf( column ) == -1 )
			throw new Exception ( "Malformed chess move !" );
		else {
			initialColumn = column;
                        fromSquare = (8-initialRow)*8+initialColumn-'a';
		}
	}

	public char getInitialColumn() {
		return initialColumn;
	}

	public void setInitialRow ( int row ) 
		throws Exception {

		if ( row < 1 || row > 8 )
			throw new Exception ( "Malformed chess move !" );
		else {
			initialRow = row;
                        fromSquare = (8-initialRow)*8+initialColumn-'a';
		}
	}
	
	public int getInitialRow() {
		return initialRow;
	}

	public void setFinalColumn( char column ) 
		throws Exception {

		if ( "abcdefgh".indexOf( column ) == -1 )
			throw new Exception ( "Malformed chess move !" );
		else {
			finalColumn = column;
			toSquare = (8-finalRow)*8 + finalColumn-'a';
		}
	}
	
	public char getFinalColumn() {
		return finalColumn;
	}

	public void setFinalRow ( int row ) 
		throws Exception {

		if ( row < 1 || row > 8 )
			throw new Exception ( "Malformed chess move !" );
		else {
			finalRow = row;
			toSquare = (8-finalRow)*8 + finalColumn-'a';
		}
	}
	
	public int getFinalRow() {
		return finalRow;
	}

	public String toString() {
		return new String( initialColumn + (new Integer( initialRow )).toString() + finalColumn + (new Integer( finalRow )).toString() );
	}

	public void setTo( int to ) 	
		throws Exception {
		
		if ( to < 0 || to > 63 ) 
			throw new Exception ( "Malformed chess move !");
		else {
			toSquare = to;
			finalRow = to / 8;
			finalColumn = (char)('a' + to % 8);
		}	
	}

	public int getTo () {
		return toSquare;
	}

	public void setFrom( int from ) 	
		throws Exception {
		
		if ( from < 0 || from > 63 ) 
			throw new Exception ( "Malformed chess move !");
		else {
			fromSquare = from;
			initialRow = from / 8;
			initialColumn = (char)('a' + from % 8);	
		}	
	}

	public int getFrom () {
		return fromSquare;
	}

	public void setCapturedPieceType ( int type ) {
		capturedPieceType = type;
	}

	public void setCapturedPieceSquare ( int square ) {
		capturedPieceSquare = square;
	}

	public int getCapturedPieceType () {
		return capturedPieceType;
	}

	public int getCapturedPieceSquare () {
		return capturedPieceSquare;
	}
}
