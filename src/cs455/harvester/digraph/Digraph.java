package cs455.harvester.digraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class Digraph {
	private ArrayList<Vertex> all_vertexes;
	private int num_edges;
	private int num_vertices;
	private ArrayList<Vertex> all_broken;
	private ArrayList<WeightedEdge> all_edges;
//	private final boolean runPageRank; // Used to determine whether or not to run page rank before printing
//	private final String domain; // Used to create the folder for this domain where all the graphs will be written
	private final String log_domain;
	private ArrayList<ArrayList<Vertex>> disjoint_graphs;
	private final boolean runDFS = true;
	
	public Digraph(boolean runPageRank, String domain) {
//		this.runPageRank = runPageRank;
		all_vertexes = new ArrayList<Vertex>();
		num_edges = 0;
		num_vertices = 0;
		all_broken = new ArrayList<Vertex>();
		all_edges = new ArrayList<WeightedEdge>();
		if (domain.contains("/")) {
			domain = domain.replaceAll("/", "-");
		}
		log_domain = "/tmp/cs455-acarbona/" + domain + "/";
		disjoint_graphs = new ArrayList<ArrayList<Vertex>>();
	}
	
	public void initialize() {
		File nodeDirectory = new File(log_domain + "nodes/");
		nodeDirectory.mkdirs();
		File graphsDirectory = new File(log_domain + "disjoint-subgraphs/");
		graphsDirectory.mkdirs();
	}
	
	public int getNumEdges() {
		return num_edges;
	}
	
	public int getNumVertices() {
		return num_vertices;
	}
	
	public void add(String start, String end, boolean isBroken) {
		if (start != null) {
			Vertex vertex = new Vertex(start, false);
			Vertex vertex_end = new Vertex(end, isBroken); 
			
			if (!isBroken) {
				synchronized (all_vertexes) {
					if (!all_vertexes.contains(vertex)) {
						all_vertexes.add(vertex);
					} else {
						vertex = all_vertexes.get(all_vertexes.indexOf(vertex));
					}
					
					if (!all_vertexes.contains(vertex_end)) {
						all_vertexes.add(vertex_end);
					} else {
						vertex_end = all_vertexes.get(all_vertexes.indexOf(vertex_end));
					}
					
					if (!vertex.getOuts().contains(vertex_end)) {
						vertex.addOut(vertex_end);				
					}
					
					if (!vertex_end.getIns().contains(vertex)) {
						vertex_end.addIn(vertex);
					}
				}
				
				WeightedEdge edge = new WeightedEdge(vertex, vertex_end, 100);
				
				synchronized (all_edges) {
					all_edges.add(edge);
				}
			} else {
				synchronized (all_broken) {
					if (!all_broken.contains(vertex_end)) {
						all_broken.add(vertex_end);
					}
				}
			}
		} else {
		}
		
		System.out.println("Graph size: " + all_vertexes.size());
	}
	
	private synchronized void runDFS() {
		for (Vertex v : all_vertexes) {
			v.setDiscovered(false);
		}
		
		for (Iterator<Vertex> iterator = all_vertexes.iterator(); iterator.hasNext();) {
			Vertex node = iterator.next();
			boolean merge = false;
			if (!node.getDiscovered()) {
				node.setDiscovered(true);
				ArrayList<Vertex> nodeList = dfs(node);
				if (nodeList.size() != 1) {
					for (ArrayList<Vertex> other_list : disjoint_graphs) {
						for (Vertex out : nodeList) {
							if (other_list.contains(out)) {
								merge(other_list, nodeList);
								merge = true;
								break;
							}
						}
					}
					
					if (!merge) {
						System.out.println("Adding to list");
						disjoint_graphs.add(nodeList);
					}
				}
			} 
		}
	}
	
//	private void clearDiscovered() {
//		for (Vertex v : all_vertexes) {
//			v.setDiscovered(false);
//		}
//	}

	private void merge(ArrayList<Vertex> other_list, ArrayList<Vertex> nodeList) {
		for (Vertex node : nodeList) {
			if (!other_list.contains(node)) {
				other_list.add(node);
			}
		}
		
	}

	private synchronized ArrayList<Vertex> dfs(Vertex v) {
		ArrayList<Vertex> list = new ArrayList<Vertex>();
		Stack<Vertex> stack = new Stack<Vertex>();
		stack.push(v);
		v.setDiscovered(true);
		if (!list.contains(v)){
			list.add(v);
		}
		
		while (!stack.isEmpty()) {
			Vertex vertex = stack.peek();
			Vertex child = vertex.getUnvisitedChild();
			if (child != null) {
                all_vertexes.get(all_vertexes.indexOf(child)).setDiscovered(true);
				child.setDiscovered(true);
				if (!list.contains(child)) {
					list.add(child);
				}
				stack.push(child);
			} else {
				stack.pop();
			}
		}
		
		return list;
	}
	
	
	public synchronized void printGraph() {
		try {
			printNodes();
			printBroken();
			if (runDFS) {
			System.out.println("Running DFS");
			runDFS();
			}
			printDisjoint();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			//System.out.println("Error: " + e.getMessage());
			//e.printStackTrace();
		}
		
		System.out.println("Done printing");
	}
	
	private void printNodes() throws FileNotFoundException, MalformedURLException {
		synchronized (all_vertexes) {
			System.out.println(all_vertexes.size());
			for(Vertex v : all_vertexes) {
				URL url = new URL(v.getURL());
				String name = url.getPath().replaceAll("/", "-");
				
				if (url.getPath().replaceAll("/", "").isEmpty()) {
					if (v.getURL().contains("http://")) {
						name = v.getURL().substring(7).replaceAll("/", "-");
					} else {
						name = v.getURL().substring(v.getURL().indexOf('w')).replaceAll("/", "-");
					}
				}
				
				if (name.indexOf('-') == 0) {
					name = name.replaceFirst("-", "");
				}
				
				if (name.endsWith("-")) {
					name = name.substring(0, name.length() - 1);
				}
				File directory = new File(log_domain + "nodes/" + name);
				directory.mkdirs();
				
				PrintWriter inprinter = new PrintWriter(new File(log_domain + "nodes/" + name + "/in"));
				if (!v.getIns().isEmpty()) {
					for(Vertex ins : v.getIns()) {
						inprinter.println(ins.getURL());
					}
					
				} else {
					inprinter.println();
				}
				inprinter.close();

				PrintWriter outprinter = new PrintWriter(new File(log_domain + "nodes/" + name + "/out"));
				if (!v.getOuts().isEmpty()) {
					for(Vertex outs : v.getOuts()) {
						outprinter.println(outs.getURL());
					}
					
				} else {
					outprinter.println();
				}
				outprinter.close();
			}
			
			System.out.println("Done printing nodes");
		}
	}
	
	private synchronized void printBroken() throws FileNotFoundException {
		synchronized (all_broken) {
			PrintWriter printer = new PrintWriter(new File(log_domain + "broken-links"));
			if (!all_broken.isEmpty()) {
				for(Vertex v : all_broken) {
					printer.println(v.getURL());
				}				
			} else {
				printer.println();
			}
			
			printer.close();
			System.out.println("Done printing broken links file");
		}
	}
	
	private synchronized void printDisjoint() throws FileNotFoundException {
		synchronized (disjoint_graphs) {
			for(int count = 0; count < disjoint_graphs.size(); count++) {
				ArrayList<Vertex> graph = disjoint_graphs.get(count);
				File graphsDirectory = new File(log_domain + "disjoint-subgraphs/");
				graphsDirectory.mkdirs();
				PrintWriter printer = new PrintWriter(new File(log_domain + "disjoint-subgraphs/graph" + (count+1)));
				
				for (Vertex v : graph) {
					printer.println(v.getURL());
				}
				printer.close();
			}
			System.out.println("Done printing disjoint graphs");
		}
	}
}
