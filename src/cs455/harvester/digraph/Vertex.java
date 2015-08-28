package cs455.harvester.digraph;

import java.util.ArrayList;
import java.util.Stack;

public class Vertex implements Comparable<Vertex> {
	private final String url;
	private ArrayList<Vertex> in;
	private ArrayList<Vertex> out;
	private final boolean broken;
	private volatile boolean discovered;
	private Stack<Vertex> unvisited;
	
	public Vertex(String link, boolean isBroken) {
		broken = isBroken;
		url = link;
		in = new ArrayList<Vertex>();
		out = new ArrayList<Vertex>();
		unvisited = new Stack<Vertex>();
	}
	
	public String getURL() {
		return url;
	}
	
	public boolean getBroken() {
		return broken;
	}
	
	public synchronized void setDiscovered(boolean value) {
		discovered = value;
	}
	
	public synchronized boolean getDiscovered() {
		return discovered;
	}
	
	public int compareTo(Vertex other) {
		return this.getURL().compareTo(other.getURL());
	}

	public boolean equals(Object other) {
		if (other instanceof Vertex) {
			return equals((Vertex) other);
		} else {
			return false;
		}
	}
	
	private boolean equals(Vertex other) {
        if (this.getURL().endsWith("/") && !other.getURL().endsWith("/")) {
            return this.getURL().substring(0, this.getURL().lastIndexOf("/")).equals(other.getURL());
        } else if (!this.getURL().endsWith("/") && other.getURL().endsWith("/")) {
            return this.getURL().equals(other.getURL().substring(0, other.getURL().lastIndexOf("/")));
        } else {
		    return this.getURL().equals(other.getURL());
        }
	}
	
	public Vertex shallow_copy() {
		Vertex copy = new Vertex(this.getURL(), this.getBroken());
		
		copy.setDiscovered(this.getDiscovered());
		
		return copy;
	}
	
	public void addIn(Vertex url) {
		synchronized (in) {
			in.add(url);
		}
	}
	
	public void addOut(Vertex url) {
		synchronized (out) {
			out.add(url);			
		}
	}
	
	public ArrayList<Vertex> getIns() {
		ArrayList<Vertex> ins = new ArrayList<Vertex>();
		synchronized (in) {
			for (Vertex i : in) {
				ins.add(i);
			}	
		}
		
		return ins;
	}
	
	public ArrayList<Vertex> getOuts() {
		ArrayList<Vertex> outs = new ArrayList<Vertex>();
		
		synchronized (out) {
			for (Vertex i : out) {
				outs.add(i);
			}
		}
		
		return outs;
	}

	public synchronized Vertex getUnvisitedChild() {
		if (unvisited.isEmpty()) {
			for (Vertex i: out) {
				if (!i.getDiscovered()) {
					unvisited.add(i);
				}
			}
		}
		
		if (unvisited.isEmpty()) {
			return null;
		} else {
            unvisited.peek().setDiscovered(true);
			return unvisited.pop();
		}
	}
}
