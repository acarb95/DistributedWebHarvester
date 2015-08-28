package cs455.harvester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cs455.harvester.digraph.Digraph;
import cs455.harvester.node.Node;
import cs455.harvester.task.CrawlTask;
import cs455.harvester.task.HandOffTask;
import cs455.harvester.threadpool.ThreadPoolManager;
import cs455.harvester.transport.TCPConnection;
import cs455.harvester.transport.TCPConnectionsCache;
import cs455.harvester.transport.TCPServerThread;
import cs455.harvester.wireformats.CrawlerCompletesHandOffTask;
import cs455.harvester.wireformats.CrawlerSendsRegistration;
import cs455.harvester.wireformats.CrawlerSendsStatus;
import cs455.harvester.wireformats.CrawlerSendsTask;
import cs455.harvester.wireformats.Event;
import cs455.harvester.wireformats.Protocol;

public class Crawler extends Thread implements Node {

	// Global Variables
	private final String config;
	private final String domain; // the assigned URL
	private final String start_url;
	private Digraph graph; // the graphical representation of the web structure
	private TCPConnectionsCache other_crawlers; // contains all the current connections to other crawlers
	private final ThreadPoolManager manager; // the object that maintains and operates the thread pool
	private TCPServerThread server; // the listener for this node
	private final HashMap <String, SimpleEntry<InetAddress, Integer>> crawler_ports; // Contains all information necessary to create a new connection
	private ArrayList<String> completed_crawlers; // A list of all the completed crawlers in the system
	private ArrayList<String> handoffqueue;
	private ArrayList<String> handedOff;
	private ArrayList<String> crawledURLs;
	private final String[] extensions = {"html", "htm", "cfm", "asp", "php", "jsp", "edu"};
	private final boolean local = false;
	
	public Crawler(int port_num, int thread_pool_size, String root_url, String config_file_path) {
		try {
			server = new TCPServerThread(port_num, this);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		config = config_file_path;
		manager = new ThreadPoolManager(this, thread_pool_size);
		manager.initialize();
		
		if (!root_url.endsWith("/") && !root_url.endsWith("html")) {
			root_url += "/";
		}
		
		if (root_url.contains("Psychology")) {
			Pattern p = Pattern.compile("http://(.+)");
			Matcher m = p.matcher(root_url);
			m.find();
			String temp = m.group(1);
			if (temp.endsWith("/")) {
				temp = temp.substring(0, temp.lastIndexOf("/"));
			}
			domain = temp;
			graph = new Digraph(false, domain);
			start_url = root_url;
		} else if (root_url.contains("chm")) {
			start_url = "http://www.chem.colostate.edu/";
			domain = "www.chem.colostate.edu";
			graph = new Digraph(false, domain);
		} else {
			Pattern p = Pattern.compile("http://www.(.+).edu");
			Matcher m = p.matcher(root_url);
			m.find();
			String temp = m.group(1);
			temp += ".edu";
			temp = "www." + temp;
			domain = temp;
			graph = new Digraph(false, temp);
			start_url = root_url;
		}
		other_crawlers = new TCPConnectionsCache();
		crawler_ports = new HashMap<String, SimpleEntry<InetAddress,Integer>>();
		completed_crawlers = new ArrayList<String>();
		crawledURLs = new ArrayList<String>();
		handoffqueue = new ArrayList<String>();
		handedOff = new ArrayList<String>();
		System.out.println("Domain: " + domain);
	}
	
	public void initialize() {
		try {
			server.start();
			Thread.sleep(9000);
			if (!local) {
				populateConnections(config);
			}
			Thread.sleep(1000);
			graph.initialize();
			manager.start();
			generateTask(start_url, 0, false, null, null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onEvent(Event e) {
		switch(e.getType()) {
			case Protocol.CRAWLER_SENDS_REGISTRATION:
				CrawlerSendsRegistration registration = (CrawlerSendsRegistration) e;
				String id = registration.getURL();
				String ip = registration.getIP();

				System.out.println("Received registration from " + id + " with ip " + ip);
				
				try {
					synchronized (this) {
						if (!other_crawlers.containsKey(id)) {
							if (server.getUnNamedConnections().containsKey(InetAddress.getByName(ip))) {
								TCPConnection connection = server.getUnNamedConnections().get(InetAddress.getByName(ip)).get(0);
								server.removeConnection(InetAddress.getByName(ip), connection);
								other_crawlers.add(id, connection);
							}
						}
					}
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
				
				break;
			case Protocol.CRAWLER_SENDS_TASK:
				CrawlerSendsTask info = (CrawlerSendsTask) e;
				String domain = info.getDomain();
				String url = info.getURL();
				
				if (url.contains("%7E")) {
					url = url.replaceAll("%7E", "~");
				} else if (url.contains("%7e")) {
					url = url.replaceAll("%7e", "~");
				}

				System.out.println("Received " + url + " from " + domain);
				
				TCPConnection connection = getConnection(domain);
				
				boolean broken = false;
				int value = checkURL(url);
				if ((0b001 & value) != 1) {
					broken = true;
				}				
				
				if ((0b100 & value) == 4 && !broken) {
					System.out.println("Generating handoff task for " + url);
					generateTask(url, 0, true, domain, null);
					synchronized (completed_crawlers) {
						if (completed_crawlers.contains(domain)) {
							System.out.println("Not completed anymore, sending message");
							completed_crawlers.remove(domain);
							CrawlerSendsStatus message = new CrawlerSendsStatus(Protocol.CRAWLER_SENDS_STATUS, domain, false);
							for (String key : other_crawlers.getKeys()) {
								TCPConnection newconnection = other_crawlers.getConnection(key);
								try {
									newconnection.sendData(message.getBytes());
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				} else {
					System.out.println("Task " + url + " has already been crawled.");
					CrawlerCompletesHandOffTask complete = new CrawlerCompletesHandOffTask(Protocol.CRAWLER_COMPLETES_HAND_OFF_TASK, url, this.domain);
					try {
						connection.sendData(complete.getBytes());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
				break;
			case Protocol.CRAWLER_COMPLETES_HAND_OFF_TASK:
				CrawlerCompletesHandOffTask completed_info = (CrawlerCompletesHandOffTask) e;
				String complete_url = completed_info.getURL();
				
				System.out.println("Received completed handoff task from " + completed_info.getDomain() + " for URL " + complete_url);
				
				synchronized (handoffqueue) {
					handoffqueue.remove(complete_url);
				}
				break;
			case Protocol.CRAWLER_SENDS_STATUS:
				CrawlerSendsStatus status_info = (CrawlerSendsStatus) e;
				String status_domain = status_info.getURL();
				boolean completed = status_info.getCompleted();

				System.out.println("Received status message from " + status_domain + " with completion being " + completed);
				
				synchronized (completed_crawlers) {
					if (completed && !completed_crawlers.contains(status_domain)) {
						completed_crawlers.add(status_domain);
					} else if (completed_crawlers.contains(status_domain) && !completed) {
						completed_crawlers.remove(status_domain);
					}
				}
				
				break;
		}		
	}

	public void checkURLs(ArrayList<String> list, String url, int recurrance, String orig_url) {
		addToGraph(url, orig_url, false);
		for (String link : list) {
			if (containsExtension(link)) {
				int value = checkURL(link);
				//System.out.println("Url: " + url + ", Value: " + value);
				if ((0b010 & value) != 2) {
					// hand off
					handoffTask(link);
					continue;
				}
				if ((0b100 & value) == 4 && recurrance < 5) {
					generateTask(link, recurrance, false, null, url);
				}
			}
		}
	}
	
	public void run() {
		try {
			while(true) {
				Thread.sleep(10000);
				synchronized (manager) {
					if (manager.taskSize() == 0 && manager.allFree()) {
						if (!local) {
							synchronized (completed_crawlers) {
								if (!completed_crawlers.contains(domain)) {
									completed_crawlers.add(domain);
									System.out.println("Sending status message to...");
									CrawlerSendsStatus message = new CrawlerSendsStatus(Protocol.CRAWLER_SENDS_STATUS, domain, true);
									for (String key : other_crawlers.getKeys()) {
										System.out.println("\t" + key);
										TCPConnection connection = other_crawlers.getConnection(key);
										connection.sendData(message.getBytes());
									}
								}
							}
						}
						
						synchronized (this) {
							System.out.println("Checking values...");
							System.out.println("\tCompleted Crawlers: " + completed_crawlers.size());
							System.out.println("\tNumber of Crawlers: " + crawler_ports.size());
							System.out.println("\tHandoff Queue: " + handoffqueue.size());
							if ((handoffqueue.size() == 0 && completed_crawlers.size() == crawler_ports.size()) || local) {
								//System.out.println("Sending die");
								sendDieMessage();
								System.exit(0);
							}
						}
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Helpers
	private synchronized TCPConnection getConnection(String domain) {
		TCPConnection connection = null;
		if (other_crawlers.containsKey(domain)) {
			connection = other_crawlers.getConnection(domain);
		}
		
		return connection;
	}
	
	private void populateConnections(String config_file) {
		try {
			//System.out.println("Populating connects!");
			Pattern p = Pattern.compile("(\\p{Alpha}+):(\\p{Digit}+),(.+)");
			//System.out.println("Opening config file");
			Scanner reader = new Scanner(new File(config_file));
			String host = "";
			int port = 0;
			boolean createConnections = false;
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				Matcher m = p.matcher(line);
				m.find();
				String ip_name = m.group(1);
				int port_num = Integer.parseInt(m.group(2));
				String url = m.group(3);
				synchronized (crawler_ports) {
					crawler_ports.put(url, new SimpleEntry<InetAddress, Integer>(InetAddress.getByName(ip_name), port_num));
				}
				
				if (createConnections) {
					TCPConnection newConnect = new TCPConnection(new Socket(ip_name, port_num), this);
					newConnect.readData();
					String domain;
					if (url.contains("Psychology")) {
						domain = url.substring(url.indexOf("www"));
					} else if (url.contains("chm")) {
						domain = "www.chem.colostate.edu";
					} else {
						Pattern p2 = Pattern.compile("http://www.(.+).edu");
						Matcher m2 = p2.matcher(url);
						m2.find();
						String temp = m2.group(1);
						temp += ".edu";
						temp = "www." + temp;
						domain = temp;
					}
					
					synchronized (other_crawlers) {
						other_crawlers.add(domain, newConnect);
					}
					
					System.out.println("Sent connection request to " + domain + " on ip " + ip_name);
					// Create registration message
					CrawlerSendsRegistration message = new CrawlerSendsRegistration(Protocol.CRAWLER_SENDS_REGISTRATION, this.domain, host, port);
					// Send registration message
					newConnect.sendData(message.getBytes());
				}
				
				if (url.contains(domain)) {
					createConnections = true;
					host = ip_name;
					port = port_num;
				} else if (domain.contains("chem")) {
					if (url.contains("chm.colostate.edu")) {
						createConnections = true;
						host = ip_name;
						port = port_num;
					}
				} else if (domain.contains("Psychology") && url.contains("Psychology")) {
					createConnections = true;
					host = ip_name;
					port = port_num;
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void addToGraph(String link, String orig, boolean isBroken) {
		synchronized(graph) {
			//System.out.println("Crawler: Adding to graph");
			graph.add(orig, link, isBroken);
		}
	}
	
	private boolean containsExtension(String url) {
		if (url.lastIndexOf(".") > 0) {
			String test = url.substring(url.lastIndexOf("."));
			for (String extension : extensions) {
				if (test.contains(extension)) {
					return true;
				}
			}
		} else {
			// Stupid web developers dun fucked up and can't create an actual url
		}
		return false;
	}
	
	private void generateTask(String url, int recurrance, boolean handoff, String sender, String original) {
		// creates a link and adds to thread pool
		//System.out.println("Crawler: adding " + url + ", " + recurrance);
		boolean isContained = false;
		synchronized (crawledURLs) {
			if (!crawledURLs.contains(url)) {
				crawledURLs.add(url);		
			} else {
				isContained = true;
			}
		}
		
		if (!isContained) {
			CrawlTask task = new CrawlTask(url, recurrance, handoff, sender, original, this);
			synchronized (manager) {
				manager.addTask(task);
			}
		}
	}
	
/*	private boolean isLive(String link) {
		// determines if the link is live (not broken)
		HttpURLConnection urlconn = null;
		try {
			if (link.toLowerCase().contains("notfound")) {
				return false;
			}
			URL url = new URL(link);
			urlconn = (HttpURLConnection) url.openConnection();
			urlconn.setInstanceFollowRedirects(false);
			urlconn.setConnectTimeout(10000);
			urlconn.setReadTimeout(10000);
			urlconn.setRequestMethod("GET");
			urlconn.connect();
			if (urlconn.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
				return true;
			} else if (urlconn.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
				return true;
			} else {
				return urlconn.getResponseCode() == HttpURLConnection.HTTP_OK;
			}

		} catch (Exception e) {
			//System.out.println("Exception in isLive: " + e.getMessage() + " for link " + link);
			return false;
		} finally {
			if (urlconn != null) {
				urlconn.disconnect();
			}
		}
	}
*/	
	private int checkURL(String url) {
		// checks to see what to do with the url
		int result = 0b0;
		boolean isNotContained = false;
		
		synchronized (crawledURLs) {
			if (!crawledURLs.contains(url)) {
				if (url.endsWith("/")) {
					if (!crawledURLs.contains(url.substring(0, url.indexOf("/")))) {
						result += 0b1;
						isNotContained = true;
					}
				} else {
					if (!crawledURLs.contains(url + "/")) {
						result += 0b1;	
						isNotContained = true;
					}
				}				
			}
		}
		result = result << 1;
		
		if (url.contains(domain) && isNotContained) {
			result += 0b1;
		} 
		result = result << 1;
		
		return result;
	}
	
	private synchronized void handoffTask(String url) {
		if (!local){
			// create a hand off task and add to the queue
			String send_to = null;
			for (String domains : other_crawlers.getKeys()) {
				if (url.contains(domains)) {
					send_to = domains;
					break;
				}
			}
			if (send_to != null) {
				if (!handedOff.contains(url) && !handoffqueue.contains(url)) {
					HandOffTask task = new HandOffTask(url, send_to, domain, other_crawlers, this);
					handoffqueue.add(url);
					handedOff.add(url);
					manager.addTask(task);
				}
			}
		}
	}
	
	public TCPConnectionsCache getConnections() {
		TCPConnectionsCache copy = new TCPConnectionsCache();
		for(String key : other_crawlers.getKeys()) {
			copy.add(key, other_crawlers.getConnection(key));
		}
		
		return copy;
	}

	private void sendDieMessage() {
		manager.die();
		
		synchronized (graph) {
			graph.printGraph();
		}
	}
	
	private static void printUsage() {
		System.out.println("cs455.harvester.Crawler <port_num> <num_threads> <url> <config>");
		System.out.println("\tport_num: the port number this crawler's server socket will bind to");
		System.out.println("\tnum_threads: the number of threads that the thread pool manager will create");
		System.out.println("\turl: the starting url that the crawler will crawl");
		System.out.println("\tconfig: the path to the config file");
	}
	
	public static void main(String[] args) {
		if (args.length != 4) {
			printUsage();
		} else {
			int port_num = Integer.parseInt(args[0]);
			int num_threads = Integer.parseInt(args[1]);
			String url = args[2];
			String config = args[3];
			Crawler test = new Crawler(port_num, num_threads, url, config);
			test.initialize();
			test.start();
		}
	}

	public void completedHandOff(String url, String sender) {
		CrawlerCompletesHandOffTask message = new CrawlerCompletesHandOffTask(Protocol.CRAWLER_COMPLETES_HAND_OFF_TASK, url, domain);
		TCPConnection connect = getConnection(sender);
		
		if (connect != null) {
			try {
				connect.sendData(message.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addBrokenLink(String url, String orig_url) {
		addToGraph(url, orig_url, true);
	}
}
