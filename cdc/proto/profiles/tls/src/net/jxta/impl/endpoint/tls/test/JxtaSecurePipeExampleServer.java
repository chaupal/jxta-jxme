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
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.platform.ConfigurationFactory;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;

import net.jxta.impl.endpoint.tls.PipeService;


public class JxtaSecurePipeExampleServer implements DiscoveryListener {
	
	static PeerGroup netPeerGroup = null;
	
	private final static String SenderMessage = "JxtaTalkSenderMessage";
	
	private static final String SenderName = "JxtaTalkSenderName";
	
	/* Default PipeService */
	private PipeService pipeService;	
    
	/* Default DiscoveryService */
	private DiscoveryService discoveryService;
	
	/* The MimeMediaType constant */
	private final static MimeMediaType XMLMIMETYPE = new MimeMediaType("text/xml");	
	
	/* Default OutputPipe */
	private OutputPipe outputPipe = null;
	
	/**
     *  main
     *
     * @param  args  command line args
     */
    public static void main(String args[]) {
        JxtaSecurePipeExampleServer app = new JxtaSecurePipeExampleServer();
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
            ConfigurationFactory.setName("JXME TLS");
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
	 * Also assigns Discovery listener to get the PipeAdv   
	 */
	private void start() {		
		
		// Getting the Pipe service
		pipeService = (PipeService) netPeerGroup.getPipeService();
		
		// Getting the Discovery service
		discoveryService = netPeerGroup.getDiscoveryService();							
	
		// Assign the Default DiscoveryListener to grab all the advertisements			
        discoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, null, 
        		null, 100, this);	        
		   
    }
	
	/**
	 * Default DiscoveryListener
	 */
	public void discoveryEvent(DiscoveryEvent event) {
		
		DiscoveryResponseMsg response = event.getResponse();
		Enumeration localEnum = null;		
		
		try {			
			localEnum = response.getResponses();
			
			// Searches for any Pipe Advertisements
			while (localEnum.hasMoreElements()) {
				try {
					String str = (String)localEnum.nextElement();

					// Cast received message to PipeAdvertisement
					PipeAdvertisement pipeAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(
							XMLMIMETYPE, new ByteArrayInputStream(str.getBytes()));
					
					// Create a pipe
					createOutputPipe(pipeAdv);					
				}
				catch (Exception e) {
					// not a pipe adv - skip it
				}
			}				
		}
		catch (Exception e) {
			System.out.println("Error! Couldn't extract discovery event.");			
		}	
	}
	
	 /**
     * Creates an output pipe
     * @param pipeAdv the advertisement from whith to create a Pipe
     */
    private void createOutputPipe(PipeAdvertisement pipeAdv) throws Exception {
		
		try {			
			// Create the output pipe in response for the incoming input pipe
			outputPipe = pipeService.createOutputPipe(pipeAdv, 300);	
			
			sendMessages();
		}
		catch (Exception e) {			
			System.out.println("Error! Couldn't create the Unicast " +
						"Secure Output Pipe");
			e.printStackTrace();
			throw new Exception();
			
		}
		
	}
    
    private void sendMessages() {
        Message response = null;

        try {     	        	
            //Send the message
        	if (outputPipe != null) {
        		
        		for (int i = 0; i < 100; i++) {
        			response = new Message();

        			// The gram
        	        response.addMessageElement(new StringMessageElement(SenderMessage,
        	                                   "Message #" + (i+1),
        	                                   null));
        	        //Our name
        	        response.addMessageElement(new StringMessageElement(SenderName,
        	                                   netPeerGroup.getPeerName(),
        	                                   null));
        	        
        	        outputPipe.send(response);
        		}        		
        	}
        	
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
	
	
}
