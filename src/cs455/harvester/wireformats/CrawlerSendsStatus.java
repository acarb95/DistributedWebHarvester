package cs455.harvester.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CrawlerSendsStatus implements Event {

	private int type;
	private String domain;
	private boolean isComplete;
	
	public CrawlerSendsStatus(int type, String url, boolean complete) {
		this.type = type;
		this.domain = url;
		isComplete = complete;
	}
	
	public CrawlerSendsStatus(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream bInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(bInputStream));

		type = din.read();
		isComplete = din.readBoolean();
		
		int length = din.read();
		byte[] urlBytes = new byte[length];
		din.readFully(urlBytes);
		
		domain = new String(urlBytes);
		
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
	
	public boolean getCompleted() {
		return isComplete;
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes;
		
		ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bOutputStream));
		
		dout.write(type);
		dout.writeBoolean(isComplete);
		
		byte[] urlBytes = domain.getBytes();
		int length = urlBytes.length;
		
		dout.write(length);
		dout.write(urlBytes);
		
		dout.flush();
		
		marshalledBytes = bOutputStream.toByteArray();
		
		dout.close();
		bOutputStream.close();
		
		return marshalledBytes;
	}

}
