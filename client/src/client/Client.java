package client;

import java.io.FileNotFoundException;
import java.net.SocketException;

public class Client {

	public static void main(String[] args) {

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
	
		
		// Goodbye
		socket.close();
	}

}
