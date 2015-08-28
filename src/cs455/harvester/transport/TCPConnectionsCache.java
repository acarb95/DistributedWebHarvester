package cs455.harvester.transport;

import java.util.HashMap;
import java.util.Set;

/***
 * The TCPConnectionsCache holds a data structure that contains all the connections
 * in the Registry or Messaging Node for the Routing table. It provides basic operations such as
 * 	- Get
 * 	- Contains
 * 	- Add
 * 	- Remove
 * 	- Size
 * @author acarbona
 *
 */

public class TCPConnectionsCache {

	HashMap<String, TCPConnection> dataset;
	
	public TCPConnectionsCache() {
		dataset = new HashMap<String, TCPConnection>();
	}
	
	public void add(String url, TCPConnection connection) {
		dataset.put(url, connection);
	}
	
	public void remove(String url){
		dataset.remove(url);
	}
	
	public TCPConnection getConnection(String url) {
		return dataset.get(url);
	}
	
	public byte[] getIPAddress(String url) {
		return dataset.get(url).getConnectedAddress();
	}
	
	public int getPort(String url) {
		return dataset.get(url).getPort();
	}
	
	public boolean containsValue(TCPConnection connection) {
		return dataset.containsValue(connection);
	}
	
	public boolean containsKey(String url) {
		return dataset.containsKey(url);
	}
	
	public int size() {
		return dataset.size();
	}
	
	public Set<String> getKeys() {
		return dataset.keySet();
	}
}
