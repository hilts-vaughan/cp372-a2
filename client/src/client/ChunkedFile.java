package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A chunked file returns a portion at a time when requested.
 * The chunked file itself for simplicity will contain this amount
 * @author Vaughan Hilts
 *
 */
public class ChunkedFile {
	
	private FileInputStream m_internalStream;
	private final int CHUNK_SIZE = 100;
	
	public ChunkedFile(String fileName) throws FileNotFoundException {
		// Get out stream ready for the incoming file provided to us
		m_internalStream = new FileInputStream( fileName);		
	}
	
	/**
	 * Returns the next few bytes in chunked increments availble from the
	 * filesystem to the caller.
	 * @return	The chunked bytes
	 * @throws IOException Throws if an I/O error occurs for some reason
	 */
	public byte[] getByteChunk() throws IOException {
		byte[] b = new byte[CHUNK_SIZE];
		int count = m_internalStream.read(b);
		return b;
	}
	
}
