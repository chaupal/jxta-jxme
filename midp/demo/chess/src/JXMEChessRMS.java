/************************************************************************
 *
 * $Id: JXMEChessRMS.java,v 1.1 2003/03/27 17:21:21 pmaugeri Exp $
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
import javax.microedition.rms.*;
import javax.microedition.io.*;

public class JXMEChessRMS {

	public static final String  PROXY_IP = "PROXY_IP";
	public static final String  PROXY_PORT = "PROXY_PORT";
	public static final String  SCREEN_NAME = "SCREEN_NAME";
	public static final String  PERSISTENT_STATE = "PERSISTENT_STATE";


	public boolean DEBUG = true;


	private RecordStore 	RMS;

	/**
	 * Default constructor. Open the Record Store or create it if it doesn't exist.
	 *
	 * @param rs_name - the name of the Record Store
	 */
	public JXMEChessRMS ( String rs_name ) { 
		
		// Open or create the Record Store
		try {
			if ( DEBUG ) System.out.println( "Opening/Creating the Record Store " + rs_name );
			RMS = RecordStore.openRecordStore( rs_name, true );
		}	
		catch ( Exception e ) {
			System.err.println( "An error occured while opening/creating the Record Store " + rs_name );
		}	
	}
	
	/**
	 * Return the JXTA proxy IP address setting stored into the Record Store.
	 *
	 * @return the proxy IP address stored into the Record Store or null it hasn't been found
	 */
	public String getProxyIP () {

		try {
			RecordEnumeration re = RMS.enumerateRecords( null, null, false );
			while ( re.hasNextElement() ) {	
				byte[] raw_data = re.nextRecord();
				String s = new String( raw_data );
				if ( s.indexOf( PROXY_IP )!=-1 ) {
					String s2 = new String( raw_data, PROXY_IP.length()+1, 
								s.length()-PROXY_IP.length()-1 );
					if ( DEBUG ) System.out.println( "Proxy IP found in RMS : " + s2 );
					return s2;
				}
			}
			return null;
		}
		catch ( Exception e ) {
			System.err.println( "An error occured while accessing the Record Store !" );
		}
		return null;
	} 



	/**
	 * Return the JXTA proxy port setting stored into the Record Store.
	 *
	 * @return the proxy port stored into the Record Store or null it hasn't been found
	 */
	public String getProxyPort () {

		try {
			RecordEnumeration re = RMS.enumerateRecords( null, null, false );
			while ( re.hasNextElement() ) {	
				byte[] raw_data = re.nextRecord();
				String s = new String( raw_data );
				if ( s.indexOf( PROXY_PORT )!=-1 ) {
					String s2 = new String( raw_data, PROXY_PORT.length()+1, s.length()-PROXY_PORT.length()-1 );
					if ( DEBUG ) System.out.println( "Proxy port found in RMS : " + s2 );
					return s2;
				}
			}
			return null;
		}
		catch ( Exception e ) {
			System.err.println( "An error occured while accessing the Record Store !" );
		}
		return null;
	} 



	/**
	 * Return the screen name setting stored into the Record Store.
	 *
	 * @return the screen name stored into the Record Store or null it hasn't been found
	 */
	public String getScreenName () {

		try {
			RecordEnumeration re = RMS.enumerateRecords( null, null, false );
			while ( re.hasNextElement() ) {	
				byte[] raw_data = re.nextRecord();
				String s = new String( raw_data );
				if ( s.indexOf( SCREEN_NAME )!=-1 ) {
					String s2 = new String( raw_data, SCREEN_NAME.length()+1, s.length()-SCREEN_NAME.length()-1 );
					if ( DEBUG ) System.out.println( "Screen name found in RMS : " + s2 );
					return s2;
				}
			}	
			return null;
		}
		catch ( Exception e ) {
			System.err.println( "An error occured while accessing the Record Store !" );
		}
		return null;
	} 



	/**
	 * Return the persistent state stored into the Record Store.
	 * The persistent state is generated by the PeerNetwork.connect().
	 *
	 * @return the persistent state stored into the Record Store or null it hasn't been found
	 */
	public byte[] getPersistentState () {

		try {
			RecordEnumeration re = RMS.enumerateRecords( null, null, false );
			while ( re.hasNextElement() ) {	
				byte[] raw_data = re.nextRecord();
				String s = new String( raw_data );
				if ( s.indexOf( PERSISTENT_STATE )!=-1 ) {
					String s2 = new String( raw_data, PERSISTENT_STATE.length()+1, s.length()-PERSISTENT_STATE.length()-1 );
					if ( DEBUG ) System.out.println( "Persistent state found in RMS : " + s2 );
					return s2.getBytes();
				}
			}
			return null;
		}
		catch ( Exception e ) {
			System.err.println( "An error occured while accessing the Record Store !" );
		}
		return null;
	} 


	/**
	 * Save a setting into the Record Store.
	 * 
	 * @param name - the name of the setting field ( JXMEChessRMS.PROXY_IP or .PROXY_PORT or .SCREEN_NAME)
	 * @param value - the value of the setting.
	 */
	public void saveSetting ( String name, String value ) {

		byte[] raw_data;
		int id;
		String s;

		try {
			// First delete the existing record
			RecordEnumeration re = RMS.enumerateRecords( null, null, false );
			while ( re.hasNextElement() ) {
				id = re.nextRecordId();
				raw_data = RMS.getRecord(id);
				s = new String( raw_data );
				if ( s.indexOf( name )!=-1 )
					RMS.deleteRecord(id);
			}

			// Add the record to the Record Store
			s = new String( name + "=" + value );
                	RMS.addRecord( s.getBytes(), 0, s.length() );
			if ( DEBUG ) System.out.println( "Saving the setting in RMS : " + name + "=" + value );
		}
		catch ( Exception e ) {
			System.err.println( "An error occured while accessing the Record Store !" );
		}
	}


	/** 
	 * Save a persistent state.
	 *
	 * @param persistent_state - the persistent state value
	 */
	public void savePersistentState ( byte[] persistent_state ) {

		byte[] raw_data;
		int id;
		String s;

		try {
			// First delete the existing field in the Record Store
			RecordEnumeration re = RMS.enumerateRecords( null, null, false );
			while ( re.hasNextElement() ) {
				id = re.nextRecordId();
				raw_data = RMS.getRecord(id);
				s = new String( raw_data );
				if ( s.indexOf( PERSISTENT_STATE )!=-1 )
					RMS.deleteRecord(id);
			}
			
			// Add the record to the Record Store
			s = new String( PERSISTENT_STATE + "=" + new String( persistent_state) );
                	RMS.addRecord( s.getBytes(), 0, s.length() );
			if ( DEBUG ) System.out.println( "Saving the persistent state in RMS : " + persistent_state );
		}
		catch ( Exception e ) {
			System.err.println( "An error occured while accessing the Record Store !" );
		}
	}




	// public void storeGame ( Game game ) 
	// public Game loadGame ()
}
		
