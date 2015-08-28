package cs455.harvester.threadpool;

import java.util.ArrayList;
import java.util.LinkedList;

import cs455.harvester.Crawler;
import cs455.harvester.task.CrawlTask;
import cs455.harvester.task.Task;


public class ThreadPoolManager extends Thread {
	private final CrawlerThread[] allThreads; // a list of all the threads created
	private LinkedList<CrawlerThread> freeThreads; // a list of all the idle threads
	private LinkedList<Task> tasks; // a list of all the tasks that need to be completed
	private final Crawler owner; // a reference to who owns this pool, it is needed to pass to the Crawler threads
	private volatile boolean mustDie; // Tells the thread manager to kill all threads
	private volatile boolean doneDying; // Is true when the manager has sent the die message to all threads
	
	public ThreadPoolManager(Crawler owner, int size) {
		this.owner = owner;
		allThreads = new CrawlerThread[size];
		freeThreads = new LinkedList<CrawlerThread>();
		tasks = new LinkedList<Task>();
		mustDie = false;
		doneDying = false;
		for(int i = 0; i < size; i++) {
			allThreads[i] = new CrawlerThread(this);
			freeThreads.add(allThreads[i]);
		}
	}
	
	public int taskSize() {
		synchronized (tasks) {
			return tasks.size();
		}
	}
	
	public synchronized boolean allFree() {
		synchronized (freeThreads) {
			return freeThreads.size() == allThreads.length;
		}
	}
	
	public synchronized void run() {
		try {
			while (true) {
				//System.out.println("Task: " + tasks.size() + " Threads: " + freeThreads.size());
				if (freeThreads.size() > 0 && tasks.size() > 0) {
					freeThreads.pop().assignTask(tasks.pop());
				} else {
					if (mustDie) {
						freeAllThreads();
						break;
					}
					this.wait();
				}
			}
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void freeAllThreads() {
		System.out.println("Killing all threads");
		for (int i = 0; i < allThreads.length; i++) {
			System.out.println("\tKilling thread " + i);
			allThreads[i].interrupt();
		}
		
		doneDying = true;
	}
	
	// Helper methods for other classes
	
	// For Crawler
	public void initialize() {
		for (int i = 0; i < allThreads.length; i++) {
			allThreads[i].start();
		}
	}
	
	public synchronized void addTask (Task t) {
		synchronized (tasks) {
			tasks.add(t);
			//System.out.println("Task size: " + tasks.size());
			this.notify();
		}
	}
	
	public synchronized void die() {
		//System.out.println("Manger: Setting must die boolean");
		mustDie = true;
		this.notify(); 
	}
	
	public synchronized boolean getStatus() {
		return doneDying;
	}
	
	// For CrawlerThread
	public synchronized void addFree(CrawlerThread t) {
		synchronized (freeThreads) {
			freeThreads.add(t);
			this.notify();
		}
	}
	
	public void sendURLsToCrawler(ArrayList<String> URLs, String search_url, int recurrance, String orig_url) {
		owner.checkURLs(URLs, search_url, recurrance, orig_url);
	}
	
	// For testing
	public static void main(String [] args) {
		ThreadPoolManager tester = new ThreadPoolManager(null, 10);
		tester.initialize();
		tester.start();
		
		CrawlTask t = new CrawlTask("http://www.colostate.edu/Depts/Psychology", 0, false, null, null, null);
		tester.addTask(t);

	}

	public void completedHandOff(String url, String sender) {
		owner.completedHandOff(url, sender);
	}
}
