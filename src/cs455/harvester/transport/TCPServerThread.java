package cs455.harvester.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import cs455.harvester.node.Node;

/***
 * Represents the server thread that will be listening to nodes attempting to connect.
 * It also holds a map of all the current connections that have been created. This allows
 * the owning node to pull connections from the server thread and store into their own
 * data structure. 
 * 
 * @author acarbona
 *
 */
public class TCPServerThread extends Thread {
	private ServerSocket sSocket;
	private Node owner;
	private HashMap<InetAddress, ArrayList<TCPConnection>> currentConnections;
	
	public TCPServerThread(int portNum, Node owner) throws IOException {
		sSocket = new ServerSocket(portNum);
		this.owner = owner;
		currentConnections = new HashMap<InetAddress, ArrayList<TCPConnection>>();
	}
	
	// Getters for the information of the server socket
	public String getHostName() {
		return sSocket.getInetAddress().getHostName();
	}
	
	public int getPort() {
		return sSocket.getLocalPort();
	}
	
	public InetAddress getIP() {
		return sSocket.getInetAddress();
	}
	
	// Returns the current connections list
	public HashMap<InetAddress, ArrayList<TCPConnection>> getUnNamedConnections() {
		return currentConnections;
	}
	
	// Allows the using node to add a connection back to the list if it is "deregistered"
	public void addConnection(TCPConnection connection, InetAddress addr) {
		if (currentConnections.containsKey(addr)) {
			ArrayList<TCPConnection> connections = currentConnections.get(addr);
			if (!connections.contains(connection)) {
				currentConnections.get(addr).add(connection);
			}
		} else {
			ArrayList<TCPConnection> connections = new ArrayList<TCPConnection>();
			connections.add(connection);
			currentConnections.put(addr, connections);
		}
	}
	
	// If the using node pulls a connection from current connections it will need to be removed
	// from the data structure
	public void removeConnection(InetAddress addr, TCPConnection connection) {
		if (currentConnections.containsKey(addr)) {
			ArrayList<TCPConnection> connections = currentConnections.get(addr);
			if (connections.contains(connection)) {
				currentConnections.get(addr).remove(connection);
			} else {
				System.out.println("Error: Connection does not exist");
			}
			
			if (currentConnections.get(addr).size() == 0) {
				currentConnections.remove(addr);
			}
		} else {
			System.out.println("Error: Address not valid");
		}
	}

	// Waits to get an incoming connection. If it gets one it will save it and start it's receiver thread
	@Override
	public void run() {
		while(true) {
			try {
				Socket newClient = sSocket.accept();
				TCPConnection newConnectionToClient = new TCPConnection(newClient, owner);
				
				if (currentConnections.containsKey(newClient.getInetAddress())) {
					currentConnections.get(newClient.getInetAddress()).add(newConnectionToClient);
				} else {
					ArrayList<TCPConnection> newlist = new ArrayList<TCPConnection>();
					newlist.add(newConnectionToClient);
					currentConnections.put(newClient.getInetAddress(), newlist);
				}
				
				newConnectionToClient.readData();
			} catch (IOException e) {
				System.out.println("Error in server thread. ");
				e.printStackTrace();
			}
			
		}
		
	}

}
