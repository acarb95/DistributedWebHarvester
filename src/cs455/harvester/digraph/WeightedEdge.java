package cs455.harvester.digraph;

public class WeightedEdge {
	private final Vertex start;
	private final Vertex end;
	private int weight;
	
	public WeightedEdge(Vertex start, Vertex end, int start_weight) {
		this.start = start;
		this.end = end;
		weight = start_weight;
	}
	
	public synchronized void changeWeight(int new_weight) {
		weight = new_weight;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public Vertex getStart() {
		return start;
	}
	
	public Vertex getEnd() {
		return end;
	}
}
