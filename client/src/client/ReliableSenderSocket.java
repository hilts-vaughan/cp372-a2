package client;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Implements a reliable socket UDP datagram implementation. All of the necessary implementation
 * details are hidden within the socket class.
 * @author Vaughan Hilts
 *
 */
public class ReliableSenderSocket extends DatagramSocket {

	public ReliableSenderSocket(int port) throws SocketException {		
		super(port);	
	}
	
	public void performConnection(String host, int port) throws SocketException {
		this.connect(new InetSocketAddress(host, port));
	}

}
