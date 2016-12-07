package detailedGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomizedNode {

	private final int x, y;
	private String label;
	private Set<CustomizedEdge> edges;
	private static int nodeCounter = 0;
	
	public CustomizedNode(int x, int y){
		this.x = x;
		this.y = y;
		edges = new HashSet<CustomizedEdge>();
	}

	public static String getNewNodeLabel() {
		return Integer.toString(nodeCounter++);
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Set<CustomizedEdge> getEdges() {
		return edges;
	}

	public void setEdges(Set<CustomizedEdge> edges) {
		this.edges = edges;
	}

	public void addEdge(CustomizedEdge edge){
		edges.add(edge);
	}
	
	public CustomizedEdge getEdgeTo(CustomizedNode node){
		for(CustomizedEdge e: edges){
			if(e.getVertex1().equals(node) || e.getVertex2().equals(node)){
				return e;
			}
		}
		return null;
	}
	
	public boolean equals(CustomizedNode node){
		if(node.getKey().equals(getKey()))
			return true;
		return false;
	}
	
	public String toString(){
		return label;
	}
	public String getKey(){
		return x + "/" + y;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<CustomizedNode> getAdjacentNodes() {
		List<CustomizedNode> adjacentNodes = new ArrayList<CustomizedNode>();
		for(CustomizedEdge e: edges){
			if(e.getVertex1().equals(this)){
				adjacentNodes.add(e.getVertex2());
			}else{
				adjacentNodes.add(e.getVertex1());
			}
		}
		return adjacentNodes;
	}

	public static void resetNodeCounter() {
		nodeCounter = 0;
		
	}
}
