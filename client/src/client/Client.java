package client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class Client {

	// We need a way of storing packets that are yet to recieve acknowledge;
	// we'll do so here
	private static Map<Byte, ReliablePacket> unackedPackets = new ConcurrentHashMap<Byte, ReliablePacket>();

	// A 2s timeout is plenty
	//TODO: Make this variable depending on network conditions
	private final static int TIMEOUT = 300;
	
	public static long oldestPacketTime = 0;
	
	public static void main(String[] args) throws InterruptedException, IOException {

		System.out.println("Param Set: ");
		for (String arg : args) {
			System.out.println(arg);
		}

		String hostAddress = "";
		int portHost, portClient;
		String fileName = "";
		int reliabilitySeed = 0;
		int windowSize = 0;

		try {

			hostAddress = args[0];
			portHost = Integer.parseInt(args[1]);
			portClient = Integer.parseInt(args[2]);
			fileName = args[3];
			reliabilitySeed = Integer.parseInt(args[4]);
			windowSize = Integer.parseInt(args[5]);

		} catch (Exception e) {
			System.out
					.println("The given command line arguments were not valid. "
							+ "Check your parameters and then try again.");
			return;
		}

		portClient = 7000;
		
		// OK, we've extracted what we need. Moving on...
		reliabilitySeed = 0;

		// TODO: Remove this... for now we set this to 1 for 'stop and wait'
		windowSize = 10;
		
		Thread ack_listener = new Thread(new AckListener(portHost, unackedPackets));
		ack_listener.start();
		
		long startTime = System.currentTimeMillis();
		

		ChunkedFile chunkedFile;
		try {
			chunkedFile = new ChunkedFile(fileName);
		} catch (FileNotFoundException exception) {
			System.out
					.println("The specified file provied could not be found. Exiting.");
			return;
		}

		// OK, now we setup our socket and prepare to do some actual work
		ReliableSenderSocket socket;

		try {
			socket = new ReliableSenderSocket(3333);
		} catch (SocketException exception) {
			System.out.println("The host could not be contacted. Aborting.");
			return;
		}

		// Get an IP address
		InetAddress IPAddress;
		try {
			IPAddress = InetAddress.getByName(hostAddress);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		System.out.println("Sending on port " + portClient);

		Boolean transmitComplete = false;

		int chunksSent = 0;

		byte seqNumber = 0;

		
		while (transmitComplete == false) {

			// Ensure the ack check is proper
			if (unackedPackets.values().size() < windowSize) {

				if (chunkedFile.isDataLeft()) {
					byte[] payload;

					try {
						payload = chunkedFile.getByteChunk();
					} catch (IOException exception) {
						System.out.println("Oops... ran out of file!");
						break;
					}

					// Create our packet with the timestamp of the current time
					ReliablePacket packet = new ReliablePacket(seqNumber,
							payload, System.currentTimeMillis());

					// If this is the only packet in queue, it must be the oldest
					if(unackedPackets.size() == 0)
						oldestPacketTime = packet.getTimestamp();
						
					
					unackedPackets.put(seqNumber, packet);

					// Increment our sequence counter
					seqNumber = (byte) ((seqNumber + 1) % 128);

					payload = packet.getPacketPayload();

	

					// Send our data
					try {
						socket.send(new DatagramPacket(payload, payload.length,
								IPAddress, portClient));
						chunksSent++;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					transmitComplete = true;
				}

			} // end ack check
			
			// Retransmit when we require it
			else {
				// Check to see if we need to retransmit
				long delta = System.currentTimeMillis() - oldestPacketTime;
				
				if(delta > TIMEOUT) {
					
					// Sort by the unique creation ID so we guarentee the oldest packet will be sent 
					// first without fail
					List<ReliablePacket> packets = new ArrayList<ReliablePacket>(unackedPackets.values());
										
					Collections.sort(packets, new Comparator<ReliablePacket>(){
					    public int compare(ReliablePacket s1, ReliablePacket s2) {
					        return Long.compare(s1.getUniqueId(), s2.getUniqueId());	
					    }
					});
					
					
					// Resend our payload
					for(ReliablePacket packet : packets) {						
						packet.setTimestamp(System.currentTimeMillis());
						System.out.println(System.currentTimeMillis() / 10000 +  "|| Retransmit: " + packet.getSequenceNumber());
						byte[] payload = packet.getPacketPayload();
						socket.send(new DatagramPacket(payload, payload.length,
								IPAddress, portClient));				
					}
					
					// Reset timer
					System.out.println(System.currentTimeMillis());					
					oldestPacketTime = System.currentTimeMillis();
				}
				
		
				
			}
			
			
			

		}

		System.out
				.println("I blasted everything. Goodbye. Sent: " + chunksSent);

		byte[] temp=new byte[1];
		
		ReliablePacket packet = new ReliablePacket((byte)-1, temp, System.currentTimeMillis());

		unackedPackets.put((byte)-1,packet);
		DatagramPacket terminate = new DatagramPacket(packet.getPacketPayload(), packet.getPacketPayload().length,
				IPAddress, portClient);

		long previous = System.currentTimeMillis();
		socket.send(terminate);
		while(unackedPackets.isEmpty()==false){
			if(System.currentTimeMillis()-previous>TIMEOUT){
				socket.send(terminate);
				previous=System.currentTimeMillis();
			}
		}
		// Kill ack listener
		
		
		ack_listener.stop();
		
		System.out.println( (System.currentTimeMillis() - startTime)/1000 + "s");
		
		// Goodbye
		socket.close();
	}

	public static class AckListener implements Runnable {

		private int m_port = 0;
		private Map<Byte, ReliablePacket> packetMap;
		
		
		public AckListener(int port, Map<Byte, ReliablePacket> packetMap) {
			m_port = 5001;
			this.packetMap = packetMap;
		}

		@Override
		public void run() {

			DatagramSocket socket;
			// Setup our listener here and wait for acks
			try {
				socket = new DatagramSocket(m_port);
			} catch (SocketException e) {
				System.out.println("Failed to bind ack to " + m_port);
				return;
			}
			
			for (;;) {


				DatagramPacket ackPacket = new DatagramPacket(new byte[1], 1);

				// Recieve the ack packet
				try {
					socket.receive(ackPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// Remove the element from the hash table; sequence number expected
				this.packetMap.remove(ackPacket.getData()[0]);
				
				System.out.println("Ack recieved: " + ackPacket.getData()[0]);
				
				// We need to update the oldest packet
				
				long newOldest = Long.MAX_VALUE;
				
				for(ReliablePacket packet : packetMap.values()) {
					if(packet.getTimestamp() < newOldest) {
						newOldest = packet.getTimestamp();
					}
				}
				
				Client.oldestPacketTime = newOldest;
				
				
			}

		}

	}

}
