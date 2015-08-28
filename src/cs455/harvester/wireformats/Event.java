package cs455.harvester.wireformats;

import java.io.IOException;

/***
 * Provides and interface for all events. It requires the event to implement
 * getType() and getBytes() to be used by the onEvent method in the nodes
 *  
 * @author acarbona
 *
 */
public interface Event {
	public byte getType();
	/***
	 * Takes the information stored in the class variables and then turns it into a byte array
	 * that can be sent over a TCP connections. This must be called AFTER one of the constructors.
	 * 
	 * @return - a byte array
	 */
	public byte[] getBytes() throws IOException;
}
