package cs455.harvester.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CrawlerSendsRegistration implements Event {

	private int type;
	private String domain;
	private String ip_addr;
	private int port_num;
	
	public CrawlerSendsRegistration(int type, String url, String ip, int port) {
		this.type = type;
		this.domain = url;
		ip_addr = ip;
		port_num = port;
	}
	
	public CrawlerSendsRegistration(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream bInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(bInputStream));

		type = din.read();
		
		int length = din.read();
		byte[] urlBytes = new byte[length];
		din.readFully(urlBytes);
		
		domain = new String(urlBytes);
		
		length = din.read();
		byte[] ipBytes = new byte[length];
		din.readFully(ipBytes);
		
		ip_addr = new String(ipBytes);
		
		port_num = din.readInt();
		
		bInputStream.close();
		din.close();
	}
	
	@Override
	public byte getType() {
		return (byte) type;
	}
	
	public String getURL() {
		return domain;
	}
	
	public String getIP() {
		return ip_addr;
	}
	
	public int getPort() {
		return port_num;
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes;
		
		ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bOutputStream));
		
		dout.write(type);
		
		byte[] urlBytes = domain.getBytes();
		int length = urlBytes.length;
		
		dout.write(length);
		dout.write(urlBytes);
		
		byte[] ipBytes = ip_addr.getBytes();
		length = ipBytes.length;
		
		dout.write(length);
		dout.write(ipBytes);
		
		dout.writeInt(port_num);
		
		dout.flush();
		
		marshalledBytes = bOutputStream.toByteArray();
		
		dout.close();
		bOutputStream.close();
		
		return marshalledBytes;
	}

}
