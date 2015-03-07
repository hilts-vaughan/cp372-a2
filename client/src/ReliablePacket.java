

/**
 * A reliable packet implementation that encapsulates data for reliable file
 * transfer over UDP. The sequence number, payload, and timestamp are recorded.
 * Generally, a reliable packet is immutable with the exception of the
 * timestamp.
 * 
 * Upon retransmission, the timestamp should be updated so the timer can ticked
 * down.
 * 
 * @author Vaughan Hilts
 *
 */
public class ReliablePacket {

	//constants we may want to change later
	private static final int HEADER_SIZE = 3;
	private static final int BYTES=255;
	//Initialize the variables for the class
	private byte m_sequenceNumber = 0;
	private byte[] m_payload;
	private long m_timestamp;

	private long m_id = 0;
	//a variable shared between the packets to easily differentiate between packets
	private static long uId = 0;

	public ReliablePacket(byte sequenceNumber, byte[] payload, long timestamp) {
		//builds the packet based on the given info
		this.m_payload = payload;
		this.m_sequenceNumber = sequenceNumber;
		this.m_timestamp = timestamp;
		this.m_id = uId++;
	}

	public byte getSequenceNumber() {
		//gets the sequence number for a packet
		return this.m_sequenceNumber;
	}

	public long getTimestamp() {
		//Retrieves the time stamp on a packet
		return this.m_timestamp;
	}

	public void setTimestamp(long time) {
		//sets the time stamp on a packet
		this.m_timestamp = time;
	}

	public long getUniqueId() {
		//gets the id of a packet
		return this.m_id;
	}

	public byte[] getPacketPayload() {
		byte[] packetData = new byte[this.m_payload.length + HEADER_SIZE];
		byte[] payload = this.m_payload;

		// Copy the payload out
		System.arraycopy(payload, 0, packetData, HEADER_SIZE, payload.length);

		// Shove in the sequence number
		packetData[0] = this.m_sequenceNumber;

		// TODO: Do something with this big of flag data; can use to signify
		// ACK, TEARDOWN etc
		// Of course, set it to something other than 255 as well
		packetData[1] = (byte) BYTES;

		packetData[2] = (byte) payload.length;

		return packetData;
	}

}
