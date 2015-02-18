package client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;

public class Client {

	public static void main(String[] args) throws InterruptedException {

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

		// OK, we've extracted what we need. Moving on...
		reliabilitySeed = 0;

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
			socket = new ReliableSenderSocket(portClient);
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
		
		System.out.println("Sending on port " + portHost);

		Boolean transmitComplete = false;

		int chunksSent = 0;
		
		while (transmitComplete == false) {

			if (chunkedFile.isDataLeft()) {
				byte[] payload;

				try {
					payload = chunkedFile.getByteChunk();
				} catch (IOException exception) {
					System.out.println("Oops... ran out of file!");
					break;
				}

				// Create our packet
				ReliablePacket packet = new ReliablePacket((byte) 0, payload);
				payload = packet.getPacketPayload();

				// If you want to overflow your buffer, simply comment this
				Thread.sleep(2);
				
				// Send our data
				try {
					socket.send(new DatagramPacket(payload, payload.length,
							IPAddress, portHost));
					chunksSent++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				transmitComplete = true;
			}

		}
		
		System.out.println("I blasted everything. Goodbye. Sent: " + chunksSent);

		// Goodbye
		socket.close();
	}
}
