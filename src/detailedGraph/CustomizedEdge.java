package detailedGraph;

import java.util.ArrayList;
import java.util.List;

import ij.ImagePlus;
import skeleton_analysis.Edge;
import skeleton_analysis.Point;

public class CustomizedEdge {
	private String label;
	private CustomizedNode vertex1;
	private CustomizedNode vertex2;
	private static int edgeCounter = 0;
	private List<Edge> skeletonEdges;
	private ImagePlus thicknessImage;
	private double flowRate;
	
	public CustomizedEdge(CustomizedNode node1, CustomizedNode node2, Edge e, ImagePlus thicknessImage){
		this.vertex1 = node1;
		this.vertex2 = node2;
		skeletonEdges = new ArrayList<Edge>();
		skeletonEdges.add(e);
		this.thicknessImage = thicknessImage;
	}

	public static String getNewEdgeLabel() {
		return Integer.toString(edgeCounter++);
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public CustomizedNode getVertex1() {
		return vertex1;
	}

	public CustomizedNode getVertex2() {
		return vertex2;
	}

	public static int getEdgeCounter() {
		return edgeCounter;
	}

	public void setVertex1(CustomizedNode vertex1) {
		this.vertex1 = vertex1;
	}

	public void setVertex2(CustomizedNode vertex2) {
		this.vertex2 = vertex2;
	}

	public List<Edge> getSkeletonEdges() {
		return skeletonEdges;
	}

	public void setSkeletonEdges(List<Edge> edges) {
		this.skeletonEdges = edges;
	}
	
	public String toString(){
		return label;
	}
	
	public boolean equals(CustomizedEdge e){
		if(e.getLabel().equals(this.getLabel()))
			return true;
		return false;
	}
	
	public int getEdgeThickness(){
		int thickness = 0;
		for(Edge e:skeletonEdges){
			thickness = Math.max(getEdgeThickness(e),thickness);
		}
		if(thickness == 0){
			System.out.println("0 thicknss for " + label);
		}
		return Math.max(thickness, 8);
	}
	
	private int getEdgeThickness(Edge skeletonEdge) {
		int avgThickness = 0;
		int count = 0;
		for (Point point : skeletonEdge.getSlabs()) {
			avgThickness += (int) thicknessImage.getChannelProcessor().getf(point.x, point.y);
			count++;
		}
		return avgThickness/count;
	}

	public double getFlowRate() {
		return flowRate;
	}

	public void setFlowRate(double flowRate) {
		this.flowRate = flowRate;
	}

	public static void resetEdgeCounter() {
		edgeCounter = 0;
		
	}
}
