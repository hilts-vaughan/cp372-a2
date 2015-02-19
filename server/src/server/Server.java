package server;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * A sample implementation of our working server.
 * 
 * A simple file transfer server that will load and store bytes from clients.
 * 
 * The server is a fairly simple implementation which will simply discard packets if it
 * can't get what it wants
 * @author Vaughan Hilts
 *
 */
public class Server {

	
	public static void main(String[] args) throws IOException {
		
		String hostAddress = "";
		int portAck = 0;
		int portData = 0;
		String fileNameSaveTo = "";
		
		try {
			hostAddress = args[0];
			portAck = Integer.parseInt(args[1]);
			portData = Integer.parseInt(args[2]);
			fileNameSaveTo = args[3];
		}
		catch(Exception exception) {
			System.out.println("The arguments provided were not valid");
			return;
		}
		
		portAck = 5001;
		portData = 7000;
		
		// Try binding
		DatagramSocket socket;
		try {
			socket = new DatagramSocket(portData);
		} catch (SocketException e) {
			System.out.println("Failed to bind server to port: " + portData);
			return;
		}
		
		System.out.println("The file server is ready. Bound to port " + portData);
		
		FileOutputStream out;
		try {
			out = new FileOutputStream(fileNameSaveTo);
		} catch (FileNotFoundException e1) {
			System.out.println("Output name was not valid");
			return;
		}

		int expectedSeqNum = 0;
		
		// Loop forever
		for( ;; ) {
			
			// Allocate enough space for 128 bytes
            DatagramPacket packet = new DatagramPacket( new byte[512], 512 ) ;

            try {
				socket.receive(packet);
				//System.out.println("Got data");
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
            byte[] data = packet.getData();
 
            System.out.println("Packet recieved with sequence number: " + data[0]);
            System.out.println("Expected... " + expectedSeqNum);
            // Just discard the packet if it's not what we expected
            if(data[0] != expectedSeqNum)
            	continue;
          
            
            for(int i = 3; i < 3 + data[2]; i++) {
            	try {
					out.write(data[i]);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
            

			// Increment our sequence counter
            expectedSeqNum = (byte) ((expectedSeqNum + 1) % 128);
			
            // Send our acknowledgement to the client listener
            sendPacketAck(packet, socket, portAck);
            
            // out.flush();
        
            
                                 
		}
		
		
	}
	
	
	/**
	 * Given a specific packet number, sends the acknowledgment for it
	 * @param ackNumber
	 * @throws IOException 
	 */
	private static void sendPacketAck(DatagramPacket packet, DatagramSocket socket, int ackPort) throws IOException {
		// Send to the port specified by the client getting acknowledgements
		int port = ackPort;
		InetAddress address = packet.getAddress();
		
		// A single byte with the acknowledgment number
		byte[] buffer = new byte[1];
		buffer[0] = packet.getData()[0];
		
		DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length, address, port);
		socket.send(ackPacket);
		
	}
	
	
	
}
