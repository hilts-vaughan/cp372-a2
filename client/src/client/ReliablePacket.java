package client;

/**
 * A reliable packet 
 * 
 * @author Vaughan Hilts
 *
 */
public class ReliablePacket {

	private static final int HEADER_SIZE = 3;
	
	private byte m_sequenceNumber = 0;
	private byte[] m_payload;
	
	public ReliablePacket(byte sequenceNumber, byte[] payload) {
		this.m_payload = payload;
		this.m_sequenceNumber = sequenceNumber;
	}
	
	public byte getSequenceNumber() {
		return this.m_sequenceNumber;
	}
	
	public byte[] getPacketPayload() {
		byte[] packetData = new byte[this.m_payload.length + HEADER_SIZE];
		byte[] payload = this.m_payload;
		
		// Copy the payload out
		System.arraycopy(payload, 0, packetData, HEADER_SIZE, payload.length);
		
		// Shove in the sequence number
		packetData[0] = this.m_sequenceNumber;
		
		//TODO: Do something with this big of flag data; can use to signify ACK, TEARDOWN etc
		// Of course, set it to something other than 255 as well
		packetData[1] = (byte) 255;
		
		packetData[2] = (byte) payload.length;
		
		
		return packetData;
	}
	
}
