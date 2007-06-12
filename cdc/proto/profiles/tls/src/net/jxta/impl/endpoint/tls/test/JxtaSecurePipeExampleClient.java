package test;

/*
 * Class name: JxtaSecurePipeExample.java 
 * Created on: 7.9.2005
 * Created by: Tair
 * 
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.impl.endpoint.tls.PipeService;
import net.jxta.platform.ConfigurationFactory;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;


public class JxtaSecurePipeExampleClient implements PipeMsgListener {
	
	static PeerGroup netPeerGroup = null;
	
	private final static String SenderMessage = "JxtaTalkSenderMessage";
	
	private static final String SenderName = "JxtaTalkSenderName";	
	
	/* Default PipeService */
	private PipeService pipeService;	
    
	/* Default DiscoveryService */
	private DiscoveryService discoveryService;
	
	/* Default InputPipe */
	private InputPipe inputPipe = null;		
	
	/**
     *  main
     *
     * @param  args  command line args
     */
    public static void main(String args[]) {
        JxtaSecurePipeExampleClient app = new JxtaSecurePipeExampleClient();
        try {
            app.startJxta();
            app.start();
        } catch (IOException io) {
            io.printStackTrace();
        }
        synchronized(app) {
            //The intention with the wait is to ensure the app continues to run
            //run.
            try {
                app.wait();
            } catch (InterruptedException ie) {
                Thread.interrupted();
            }
        }
    }
    
    /**
     * Starts jxta
     *
     */
    private void startJxta() throws IOException {
        try {
            // Set the peer name
            ConfigurationFactory.setName("JxtaSecurePipeExampleClient");
            // Configure the platform
            Advertisement config = ConfigurationFactory.newPlatformConfig();
            // save it in the default directory $cwd/.jxta
            ConfigurationFactory.save(config, false);
            // create, and Start the default jxta NetPeerGroup
            netPeerGroup = PeerGroupFactory.newNetPeerGroup();     
            
            System.out.println("Connected to JXTA netPeerGroup.");
            
        }
        catch (PeerGroupException e) {
            // could not instantiate the group, print the stack and exit
            System.out.println("fatal error : group creation failure");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
	 * Gets the default services of the NetPeerGroup	  
	 */
	private void start() {		
		
		// Getting the Pipe service
		pipeService = netPeerGroup.getPipeService();
		
		// Getting the Discovery service
		discoveryService = netPeerGroup.getDiscoveryService();		
		
		// Create the Input pipe
		createInputPipe();	
		
		System.out.println("Secure Unicast Input Pipe created. Waiting for connections...");
    }	
	
	/**
     * Display messages as they arrive
     */
    public void pipeMsgEvent(PipeMsgEvent event) {

        Message msg = null;
        try {
            // grab the message from the event
            msg = event.getMessage();
            if (msg == null) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String senderName = "unknown";

        // Get originator's name
        MessageElement nameEl = msg.getMessageElement(SenderName);
        if (nameEl != null) {
            senderName = nameEl.toString();
        }

        // now the message
        String senderMessage = null;
        MessageElement msgEl = msg.getMessageElement(SenderMessage);
        if (msgEl != null) {
            senderMessage = msgEl.toString();
        } else {
            System.out.println("received an unknown message");
            return;
        }

        // Get message
        if (senderMessage == null) {
            senderMessage = "[empty message]";
        }
        
        System.out.println(senderName+"> " + senderMessage);        
    }
	
	 /**
     * Creates an output pipe for private communication
     * @param pipeAdv the advertisement from whith to create a Pipe
     */
    private void createInputPipe() {
		
		try {			
			
			PipeAdvertisement localPipeAdv = createPipeAdvertisement();
			
			// Create the output pipe in response for the incoming input pipe
			inputPipe = pipeService.createInputPipe(localPipeAdv, this);	
			
			// Publish the pipe adv so that Server finds it
			discoveryService.remotePublish(localPipeAdv);
		}
		catch (Exception e) {			
			System.out.println("Error! Couldn't create the Unicast " +
						"Secure Input Pipe");
			e.printStackTrace();			
		}
		
	}
    
    /**
     * Creates a UnicastSecure PipeAdv
     * @return pipeAdv the generated Pipe Advertisement
     */
    private PipeAdvertisement createPipeAdvertisement() {
		
    	PipeAdvertisement pipeAdv = null;
		// Generate new Pipe Advertisement
		pipeAdv = (PipeAdvertisement)AdvertisementFactory.newAdvertisement(
				PipeAdvertisement.getAdvertisementType());
		
		// Set the pipe name
		pipeAdv.setName("JxtaSecurePipeExample");
		
		// Set the pipe desc
		pipeAdv.setDescription("SecureUnicast Pipe to test TLS");

		// Set Pipe type to Unicast
		pipeAdv.setType(PipeService.UnicastSecureType);
		pipeAdv.setPipeID((ID) net.jxta.id.IDFactory.newPipeID(netPeerGroup.getPeerGroupID()));	
		System.out.println(pipeAdv);
		return pipeAdv;
	}    
	
}
