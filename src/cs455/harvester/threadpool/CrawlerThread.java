package cs455.harvester.threadpool;

import cs455.harvester.task.CrawlTask;
import cs455.harvester.task.HandOffTask;
import cs455.harvester.task.Task;
import cs455.harvester.task.TaskType;


public class CrawlerThread extends Thread {

	private volatile boolean isFree; // true if the thread does not currently have a task to run, false if the thread is currenlty running a task
	private final ThreadPoolManager owner; // The owner of the thread pool, needed to send back gathered URLs
	private volatile Task task; // The task this thread needs to complete
	
	public CrawlerThread(ThreadPoolManager owner) {
		this.owner = owner;
		isFree = true;
		task = null;
	}
	
	public synchronized void assignTask(Task t) {
		if (isFree) {
			task = t;
			isFree = false;
			this.notify();
		}
	}

	public synchronized void die() {
		System.out.println("Interrupting this thread");
		this.interrupt();
	}
	
	@Override
	public synchronized void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				if (!isFree && task != null) {
					switch(task.getType()) {
						case TaskType.CRAWL_TASK:
							CrawlTask crawltask = (CrawlTask) task;
							System.out.println("ID: " + this.getId() + ", CRAWLING: " + crawltask.getURL() + "\t" + owner.taskSize()); 
							crawltask.run();
							if (crawltask.getHandOff()) {
								owner.completedHandOff(crawltask.getURL(), crawltask.getSender());
							}
							owner.sendURLsToCrawler(crawltask.getURLs(), crawltask.getURL(), crawltask.getRecurrance(), crawltask.getOrigUrl());
							break;
							
						case TaskType.HAND_OFF_TASK:
							HandOffTask handofftask = (HandOffTask) task;
							System.out.println("ID: " + this.getId() + ", HANDING OFF: " + handofftask.getURL() + "\t" + owner.taskSize()); 
							handofftask.run();
							break;
					}
					
					Thread.sleep(1000);
					
					//System.out.println("Adding myself back: " + this.getId());
					isFree = true;
					owner.addFree(this);
					task = null;
				} else {
					this.wait();
				}
			}
			
		} catch (InterruptedException e) {
			System.out.println("Thread exiting");
			return;
		}		
	}
}
