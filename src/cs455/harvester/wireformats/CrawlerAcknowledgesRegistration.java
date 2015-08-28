package cs455.harvester.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CrawlerAcknowledgesRegistration implements Event {

	private int type;
	private String url;
	private String domain;
	
	public CrawlerAcknowledgesRegistration(int type, String sending_url, String receiving_domain) {
		this.type = type;
		this.url = sending_url;
		this.domain = receiving_domain;
	}
	
	public CrawlerAcknowledgesRegistration(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream bInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(bInputStream));
		
		type = din.read();
		
		int length = din.read();
		byte[] urlBytes = new byte[length];
		din.readFully(urlBytes);
		
		url = new String(urlBytes);
		
		length = din.read();
		byte[] domainBytes = new byte[length];
		din.readFully(domainBytes);
		
		domain = new String(domainBytes);
		
		bInputStream.close();
		din.close();
	}
	
	@Override
	public byte getType() {
		return (byte) type;
	}
	
	public String getURL() {
		return url;
	}
	
	public String getDomain() {
		return domain;
	}

	@Override
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes;
		
		ByteArrayOutputStream bOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(bOutputStream));
		
		dout.write(type);
		
		byte[] urlBytes = url.getBytes();
		int length = urlBytes.length;
		
		dout.write(length);
		dout.write(urlBytes);
		
		byte[] domainBytes = domain.getBytes();
		length = domainBytes.length;
		
		dout.write(length);
		dout.write(domainBytes);
		
		dout.flush();
		
		marshalledBytes = bOutputStream.toByteArray();
		
		dout.close();
		bOutputStream.close();
		
		return marshalledBytes;
	}

}
