import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;

/**
 * Implements a reliable socket UDP datagram implementation. All of the
 * necessary implementation details are hidden within the socket class.
 * 
 * The only major details here are loss simulation.
 * 
 * @author Vaughan Hilts
 *
 */
public class ReliableSenderSocket extends DatagramSocket {

	private Random m_random = new Random();
	private int m_reliabilityNumber = 0;
	//Constructor for our socket
	public ReliableSenderSocket(int port, int reliabilityNumber)
			throws SocketException {
		super(port);
		//Pseudo reliability number set to this socket 
		this.m_reliabilityNumber = reliabilityNumber;
	}

	/**
	 * A custom implementation of send which has the possibility of sometimes
	 * "losing" a packet rather than actually sending it.
	 */
	@Override
	public void send(java.net.DatagramPacket packet) throws IOException {

		// If the number is zero we dont simulate loss
		if (this.m_reliabilityNumber != 0) {
			//this generates a random integer between 1 and a value given at start up
			int die = getRandomInt(1, this.m_reliabilityNumber);
			
			// if the given value is a 1 then the packet is not actually sent
			if (die == 1)
				return;

		}
		
		super.send(packet);
	}

	/**
	 * A simple helper method to generate random integers between [min, max]
	 * inclusive.
	 */
	private int getRandomInt(int min, int max) {
		//this generates our random number
		return this.m_random.nextInt(max - min + 1) + min;
	}

}
