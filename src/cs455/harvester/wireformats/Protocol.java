package cs455.harvester.wireformats;

/***
 * Provides a mapping of message type to integer.
 * 
 * @author acarbona
 *
 */

public class Protocol {
	public static final int CRAWLER_SENDS_TASK = 2;
	public static final int CRAWLER_AWKNOWLEDGES_REGISTRATION = 3;
	public static final int CRAWLER_COMPLETES_HAND_OFF_TASK = 4;
	public static final int CRAWLER_SENDS_STATUS = 5;
	public static final int CRAWLER_SENDS_REGISTRATION = 6;

}
