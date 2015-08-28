package cs455.harvester.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/***
 * The TCP Sender provides the send method needed to send information to the 
 * corresponding recevier thread. 
 * 
 * @author acarbona
 *
 */
public class TCPSender {
	private Socket socket;
	private DataOutputStream dout;
	
	public TCPSender(Socket socket) throws IOException {
		this.socket = socket;
		dout = new DataOutputStream(socket.getOutputStream());
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	/***
	 * sendData will write out the length of the data then the data to the output stream.
	 * It is synchronized so multiple threads won't mix their data together if they are
	 * context switched out
	 * @param dataToSend The data to send
	 * @throws IOException
	 */
	public synchronized void sendData(byte[] dataToSend) throws IOException {
		int dataLength = dataToSend.length;
		dout.writeInt(dataLength);
		dout.write(dataToSend, 0, dataLength);
		dout.flush();
	}
}
