/************************************************************************
 *
 * $Id: BitBoard.java,v 1.3 2003/03/27 10:38:30 pmaugeri Exp $
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
public class BitBoard {

	private char[] bitBoard;


	public BitBoard() {

		bitBoard = new char[8];
		for (int i=0; i<8; i++) 
			bitBoard[i] = 0;	
	}

	public BitBoard ( int square ) {

		bitBoard = new char[8];
		int one = 1;
		for (int i=0; i<8; i++) 
			bitBoard[i] = 0;	
                if ( square >= 0 && square <= 63 )
                	bitBoard[ square / 8 ] = (char)(one << (square%8));
        }

	/*
	 * Locate the fisrt positive bit of this bitboard and return the square number (0..63).
	 * eg. 10000000 ... 00000000 => 0
	 *     01000000 ... 00000000 => 1
	 *     00000001 ... 00000000 => 7
	 *     00000000 ... 00000001 => 63
	 *
	 * @return the converted coordinated in square number of the first "1" bit found, or -1 if the bit board is empty.
	 *   
	 */
	public int square () {
	
		for ( int r=0; r<8; r++ )
			if ( bitBoard[r] != 0 )
				for ( int c=0; c<8; c++ )
					if ( ((bitBoard[r] >> c) & 1) == 1 )
						return r*8+c;
		return -1;
	}

	public BitBoard and( BitBoard bit_board ) {
		
		BitBoard bb = new BitBoard();
		for (int i=0;i<8;i++)
			bb.bitBoard[i] = (char)(bitBoard[i] & bit_board.bitBoard[i]);
		return bb;
	}

	public BitBoard or( BitBoard bit_board ) {
		
		BitBoard bb = new BitBoard();
		for (int i=0;i<8;i++)
			bb.bitBoard[i] = (char)(bitBoard[i] | bit_board.bitBoard[i]);
		return bb;
	}

	public BitBoard xor( BitBoard bit_board ) {
		
		BitBoard bb = new BitBoard();
		for (int i=0;i<8;i++)
			bb.bitBoard[i] = (char)(bitBoard[i] ^ bit_board.bitBoard[i]);
		return bb;
	}


	public boolean empty() {

		for (int i=0; i<8; i++) 
			if ( bitBoard[i] != 0 )
				return false;
		return true;
        }

	public BitBoard copy() {
		
		BitBoard bb = new BitBoard();
		for (int i=0;i<8;i++)
			bb.bitBoard[i] = bitBoard[i];
		return bb;
	}

	public void print() {

		System.out.println( "--------" );
		for ( int i=0; i<8; i++) {
			for ( int j=0; j<8; j++ ){
				if ( ( ( bitBoard[i] >> j ) & 1 ) == 1 )
					System.out.print( "1" );
				else
					System.out.print( "." );
			}
			System.out.println();
		}
		System.out.println( "--------" );
	} 

}
