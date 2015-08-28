package cs455.harvester.task;

import java.io.IOException;

import cs455.harvester.Crawler;
import cs455.harvester.transport.TCPConnection;
import cs455.harvester.transport.TCPConnectionsCache;
import cs455.harvester.wireformats.CrawlerSendsTask;
import cs455.harvester.wireformats.Protocol;

public class HandOffTask implements Task, Runnable {

	private final String url;
	private final String domain_to_send;
	private TCPConnectionsCache cache;
	private final String domain_sent_from;
	private final Crawler owner;
	
	public HandOffTask(String url, String domain_to_send, String sent_from, TCPConnectionsCache connections, Crawler owner) {
		this.url = url;
		this.domain_to_send = domain_to_send;
		this.cache = connections;
		this.owner = owner;
		domain_sent_from = sent_from;
	}
	
	@Override
	public synchronized void run() {
		try {
			synchronized (cache) {
				TCPConnection connection = cache.getConnection(domain_to_send);
				//System.out.println("Attempting to send handoff: " + url);
				if (connection == null) {
					//System.out.println("\tDidn't work");
					wait(10000);
					cache = owner.getConnections();
				} else {
					//System.out.println("\tSending!");
					CrawlerSendsTask message = new CrawlerSendsTask(Protocol.CRAWLER_SENDS_TASK, url, domain_sent_from);
					connection.sendData(message.getBytes());
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getURL() {
		// TODO Auto-generated method stub
		return url;
	}
	
	public String getDomain() {
		return domain_to_send;
	}

	@Override
	public int getType() {
		return TaskType.HAND_OFF_TASK;
	}

}
