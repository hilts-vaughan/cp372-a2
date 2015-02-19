package client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Random;

/**
 * Implements a reliable socket UDP datagram implementation. All of the
 * necessary implementation details are hidden within the socket class.
 * 
 * @author Vaughan Hilts
 *
 */
public class ReliableSenderSocket extends DatagramSocket {

	private Random m_random = new Random();

	public ReliableSenderSocket(int port) throws SocketException {
		super(port);
	}

	@Override
	public void send(java.net.DatagramPacket packet) throws IOException {
		int die = getRandomInt(1, 500);
		
		// Pseudo-lossy
		if(die == 1)
			return;
		
		super.send(packet);
	}

	private int getRandomInt(int min, int max){
		  return this.m_random.nextInt(max - min + 1) + min;
	}

}
