package cs455.harvester.wireformats;

import java.io.IOException;

/***
 * A singleton class that generates events. This must be single because it would 
 * cause too much memory overhead to have one Event factor for each thread that needs
 * to create an event.
 * 
 * @author acarbona
 *
 */
public class EventFactory {
	// START: This portion makes it singleton (there will only be one instance per machine)
	private static EventFactory instance = new EventFactory();
	
	private EventFactory() {
	}
	
	public static EventFactory getInstance() {
		return instance;
	}
	// END
	
	/***
	 * Creates the event object based on the first byte of data.
	 * It exits if the event type is unknown because that means
	 * the messages got corrupted or mixed up and the program can no
	 * longer continue
	 * @param data - the byte array received
	 * @return The event object that represents the data
	 * @throws IOException
	 */
	public Event createEvent(byte[] data) throws IOException {
		Event e = null;
		switch(data[0]) {
			case Protocol.CRAWLER_AWKNOWLEDGES_REGISTRATION:
				e = new CrawlerAcknowledgesRegistration(data);
				break;
			case Protocol.CRAWLER_COMPLETES_HAND_OFF_TASK:
				e = new CrawlerCompletesHandOffTask(data);
				break;
			case Protocol.CRAWLER_SENDS_STATUS:
				e = new CrawlerSendsStatus(data);
				break;
			case Protocol.CRAWLER_SENDS_TASK:
				e = new CrawlerSendsTask(data);
				break;
			case Protocol.CRAWLER_SENDS_REGISTRATION:
				e = new CrawlerSendsRegistration(data);
				break;
			default:
				System.out.println("Error in Event Factory. Message type unknown: " + data[0]);
				Exception e1 = new Exception();
				e1.printStackTrace();
				System.exit(-1);
		}
		
		return e;
	}
}
