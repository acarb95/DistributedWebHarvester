package cs455.harvester.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import cs455.harvester.node.Node;
import cs455.harvester.wireformats.EventFactory;

/***
 * The receiver thread waits for data to be send to the particular connection.
 * It will block until data is received and read fully. Then it will send the data
 * in an event to the owning node using the onEvent method
 * 
 * @author acarbona
 *
 */
public class TCPReceiverThread extends Thread {
	private Socket socket;
	private DataInputStream din;
	private Node owner;
	
	/***
	 * The constructor of the receiver thread requires the owner to be of type Node.
	 * This is because the thread shouldn't care about whether or not the node who
	 * created it is a Registry or Messaging node. 
	 * @param socket the socket to connect to
	 * @param owner the node that created the connection that holds this thread
	 * @throws IOException
	 */
	public TCPReceiverThread(Socket socket, Node owner) throws IOException {
		this.socket = socket;
		din = new DataInputStream(socket.getInputStream());
		this.owner = owner;
	}
	
	/***
	 * The run method just has the thread continuous attempt to read data. It will
	 * block if there is no data to read. Once it reads data then it will create an Event with
	 * that data to send to it's owner's onEvent method
	 */
	public void run() {
		int dataLength;
		while (socket != null) {
			try {
//				System.out.println("Blocked attempting to read data");
				dataLength = din.readInt();
//				System.out.println("Able to read data");
				byte[] data = new byte[dataLength];
				din.readFully(data, 0, dataLength);
				owner.onEvent(EventFactory.getInstance().createEvent(data));
			} catch (SocketException se) {
				//System.out.println("Socket error in reciever thread: " + se.getMessage());
				//se.printStackTrace();
				break;
			} catch (IOException ioe) {
				//System.out.println("IO error in reciever thread: " + ioe.getMessage());
				//ioe.printStackTrace();
				break;
			}
		}
	}
}
