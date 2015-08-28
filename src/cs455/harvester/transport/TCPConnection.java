package cs455.harvester.transport;

import java.io.IOException;
import java.net.Socket;

import cs455.harvester.node.Node;

/***
 * The data structure that represents the TCPConnection. It holds the receiver thread and 
 * sender. The nodes must invoke it's methods to send or receive data.
 * 
 * 
 * @author acarbona
 *
 */
public class TCPConnection {
	private TCPReceiverThread receive;
	private TCPSender sender;
	private Socket socket;
	
	/***
	 * The constructor sets up the connection with the other node by creating the
	 * sender and the receiver thread.
	 * 
	 * @param socket the socket of the connecting node
	 * @param owner the node that owns it (required for the receiver to call onEvent)
	 * @throws IOException
	 */
	public TCPConnection(Socket socket, Node owner) throws IOException {
		this.socket = socket;
		receive = new TCPReceiverThread(socket, owner);
		sender = new TCPSender(socket);
	}
	
	/***
	 * The gets the connected address of the socket in a byte array
	 * It is primarily used in construction of events that require and IP address
	 * @return a byte[] representation of the address it is connected to
	 */
	public byte[] getConnectedAddress() {
		return socket.getInetAddress().getAddress();
	}
	
	/***
	 * Starts the receiver thread
	 */
	public void readData() {
		receive.start();
	}
	
	/***
	 * Calls the sender sendData method with the data parameter. This is synchronized to ensure
	 * that only one node can attempt to send data at once.
	 * @param data the byte array to send
	 * @throws IOException
	 */
	public synchronized void sendData(byte[] data) throws IOException {
		sender.sendData(data);
	}
	
	/***
	 * Gets the port the socket is attached to.
	 * @return an int representing the port
	 */
	public int getPort() {
		return socket.getPort();
	}
}
