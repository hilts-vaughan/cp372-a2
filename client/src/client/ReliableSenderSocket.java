package client;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Implements a reliable socket UDP datagram implementation. All of the neccessary implementation
 * details are hidden within the socket class.
 * @author Vaughan Hilts
 *
 */
public class ReliableSenderSocket extends DatagramSocket {

	public ReliableSenderSocket(int port) throws SocketException {
		
		super(port);

	}

}
