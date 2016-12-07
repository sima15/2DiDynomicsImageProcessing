package cycleFinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import detailedGraph.CustomizedEdge;
import detailedGraph.CustomizedGraph;
import detailedGraph.CustomizedNode;
import ij.ImagePlus;
import skeletonize.DisplayGraph;

public class FindCycles {
	List<CustomizedEdge> edges;
	Map<String, CustomizedNode> labelledKeyNodes;
	boolean[][] adjMatrix;
	private ArrayList<List<CustomizedEdge>> cycles;
	private ImagePlus localThicknessImage;
	CustomizedGraph graph;
	List<CustomizedEdge> traversedList;

	public FindCycles(CustomizedGraph graph) {
		this.graph = graph;
		this.localThicknessImage = graph.getLocalThicknessImage().duplicate();
	}

	private void generateAdjacencyMatrix() {
		this.edges = graph.getEdges();
		this.labelledKeyNodes = new HashMap<String, CustomizedNode>();
		for (String key : graph.getNodes().keySet()) {
			labelledKeyNodes.put(graph.getNodes().get(key).getLabel(), graph.getNodes().get(key));
		}
		adjMatrix = new boolean[labelledKeyNodes.size()][labelledKeyNodes.size()];
		for (CustomizedEdge edge : edges) {
			if (connectedEdge(edge)) {
				adjMatrix[(Integer.parseInt(edge.getVertex1().getLabel()))][(Integer
						.parseInt(edge.getVertex2().getLabel()))] = true;
				adjMatrix[Integer.parseInt(edge.getVertex2().getLabel())][Integer
						.parseInt(edge.getVertex1().getLabel())] = true;

			}
		}
	}

	public List<List<CustomizedEdge>> getRemainingCycles() {
		for (int i = 0; i < labelledKeyNodes.size(); i++) {

			for (int j = i + 1; j < labelledKeyNodes.size(); j++) {
				if (adjMatrix[i][j]) {
					List<CustomizedEdge> cycle = findCycle(i, j);
					if (cycle != null) {
						cycles.add(cycle);
						traversedList.addAll(cycle);

					}
				}
				if (adjMatrix[j][i]) {
					List<CustomizedEdge> cycle = findCycle(j, i);
					if (cycle != null) {
						cycles.add(cycle);
						traversedList.addAll(cycle);
					}
				}
			}
		}
		return cycles;
	}

	private boolean connectedEdge(CustomizedEdge edge) {
		return graph.getConnectedSet().contains(edge.getVertex1()); 
	}

	public ArrayList<List<CustomizedEdge>> getCycles() {

		int cycleSize = 0;
		cycles = new ArrayList<List<CustomizedEdge>>();
		List<CustomizedEdge> missingEdges1 = new ArrayList<CustomizedEdge>();
		List<CustomizedEdge> missingEdges2 = new ArrayList<CustomizedEdge>();
		while (true) {
			generateAdjacencyMatrix();
			generateCycles();
			getRemainingCycles();
			eliminateDuplicateCycles();
			if (cycleSize == cycles.size()) {
				break;
			}
			missingEdges1 = new ArrayList<CustomizedEdge>();
			missingEdges2 = new ArrayList<CustomizedEdge>();
			for (CustomizedEdge e : edges) {
				CustomizedNode n1 = e.getVertex1();
				CustomizedNode n2 = e.getVertex2();
				int n1Label = Integer.parseInt(n1.getLabel());
				int n2Label = Integer.parseInt(n2.getLabel());
				if (adjMatrix[n1Label][n2Label] || adjMatrix[n2Label][n1Label]) {
					missingEdges1.add(e);
					//DisplayGraph.wantedList.add(e.getLabel());
				}
				if (adjMatrix[n1Label][n2Label] && adjMatrix[n2Label][n1Label]) {
					/*
					 * dataCopy[n1Label][n2Label] = false;
					 * dataCopy[n2Label][n1Label] = false;
					 */// getRemainingCycles();
					missingEdges2.add(e);
					//DisplayGraph.wantedList.add(e.getLabel());
				}
			}
			for (CustomizedEdge e : missingEdges2) {
				graph.removeEdge(e);
			}
			// adjMatrix = dataCopy;
			//DisplayGraph.draw(missingEdges1, localThicknessImage);
			//DisplayGraph.draw(missingEdges2, localThicknessImage);
			//DisplayGraph.draw(traversedList, localThicknessImage);
			cycleSize = cycles.size();
		}

		generateAdjacencyMatrix();
		generateCycles();
		getRemainingCycles();
		eliminateDuplicateCycles();
		return cycles;
	}

	private void generateCycles() {
		cycles = new ArrayList<List<CustomizedEdge>>();
		traversedList = new ArrayList<CustomizedEdge>();
		for (CustomizedEdge e : edges) {
			if (!traversedList.contains(e)) {
				CustomizedNode n1 = e.getVertex1();
				CustomizedNode n2 = e.getVertex2();
				int n1Label = Integer.parseInt(n1.getLabel());
				int n2Label = Integer.parseInt(n2.getLabel());
				List<CustomizedEdge> cycle1 = null;
				List<CustomizedEdge> cycle2 = null;
				if (adjMatrix[n1Label][n2Label]) {
					cycle1 = findCycle(n1Label, n2Label);
					if (cycle1 != null) {
						cycles.add(cycle1);
						traversedList.addAll(cycle1);
					}
				}
				if (adjMatrix[n2Label][n1Label]) {
					cycle2 = findCycle(n2Label, n1Label);
					if (cycle2 != null)
						if (cycle1 == null || !(cycle1.get(0).equals(cycle2.get(cycle2.size() - 2))
								&& cycle2.get(0).equals(cycle1.get(cycle1.size() - 2)))) {
							cycles.add(cycle2);
							traversedList.addAll(cycle2);
						}
				}
			}
		}
	}

	private void eliminateDuplicateCycles() {
		List<List<Integer>> cyclesSorted = new ArrayList<List<Integer>>();
		List<Integer> removeList = new ArrayList<Integer>();
		for (List<CustomizedEdge> cycle : cycles) {
			List<Integer> cycleLabels = new ArrayList<Integer>();
			for (CustomizedEdge e : cycle) {
				cycleLabels.add(Integer.parseInt(e.getLabel()));
			}
			Collections.sort(cycleLabels);
			cyclesSorted.add(cycleLabels);
		}

		for (int i = 0; i < cyclesSorted.size() - 1; i++) {
			List<Integer> cycle1 = cyclesSorted.get(i);
			for (int j = i + 1; j < cyclesSorted.size(); j++) {
				List<Integer> cycle2 = cyclesSorted.get(j);
				if (cycle1.size() == cycle2.size()) {
					boolean removeFlag = true;
					for (int k = 0; k < cycle1.size(); k++) {
						if (cycle1.get(k).intValue() != cycle2.get(k).intValue()) {
							removeFlag = false;
							break;
						}
					}
					if (removeFlag) {
						removeList.add(j);
					}
				}
			}
		}
	}

	static int maxCount = 0;

	private List<CustomizedEdge> findCycle(int source, int current) {
		List<CustomizedEdge> cycle = new ArrayList<CustomizedEdge>();
		if (findCycle(source, current, cycle, source)) {
			cycle.add(labelledKeyNodes.get(Integer.toString(source))
					.getEdgeTo(labelledKeyNodes.get(Integer.toString(current))));
			if (maxCount < cycle.size())
				maxCount = cycle.size();
			return cycle;
		}
		return null;
	}

	private boolean findCycle(int previous, int current, List<CustomizedEdge> cycle, int source) {
		boolean history1 = adjMatrix[previous][current];
		boolean history2 = adjMatrix[current][previous];
		adjMatrix[previous][current] = false;
		adjMatrix[current][previous] = false;

		if (current == source) {
			adjMatrix[current][previous] = history2;
			return true;
		}
		CustomizedNode previousNode = labelledKeyNodes.get(Integer.toString(previous));
		CustomizedNode currentNode = labelledKeyNodes.get(Integer.toString(current));
		CustomizedNode nextNode = null;
		Map<String, CustomizedNode> candidateNodes = new HashMap<String, CustomizedNode>();
		for (CustomizedEdge e : currentNode.getEdges()) {
			CustomizedNode candidateNode = null;
			if (e.getVertex1().equals(currentNode)) {
				candidateNode = e.getVertex2();
			} else {
				candidateNode = e.getVertex1();
			}
			if (!candidateNode.equals(previousNode)) {
				int angle = angleBetweenTwoPointsWithFixedPoint(previousNode.getX(), previousNode.getY(),
						candidateNode.getX(), candidateNode.getY(), currentNode.getX(), currentNode.getY());
				candidateNodes.put(Integer.toString(angle), candidateNode);
			}
		}
		int minAngle = Integer.MAX_VALUE;
		for (String key : candidateNodes.keySet()) {
			int temp = Integer.parseInt(key);
			if (temp < minAngle) {
				minAngle = temp;
			}
		}

		if (minAngle < Integer.MAX_VALUE)
			nextNode = candidateNodes.get(Integer.toString(minAngle));

		if (nextNode != null) {
			int next = Integer.parseInt(nextNode.getLabel());
			if (adjMatrix[current][next] && findCycle(current, next, cycle, source)) {
				CustomizedEdge edge = currentNode.getEdgeTo(nextNode);
				if (edge != null)
					cycle.add(edge);
				else
					System.out.println("Error: " + currentNode + "  " + nextNode);
				adjMatrix[current][previous] = history2;
				return true;
			}
		}
		adjMatrix[previous][current] = history1;
		adjMatrix[current][previous] = history2;
		return false;
	}

	public static int angleBetweenTwoPointsWithFixedPoint(double point1X, double point1Y, double point2X,
			double point2Y, double fixedX, double fixedY) {

		double angle1 = Math.atan2(point1Y - fixedY, point1X - fixedX);
		double angle2 = Math.atan2(point2Y - fixedY, point2X - fixedX);
		int degreeAngle = (int) Math.toDegrees(angle1 - angle2);
		if (degreeAngle < 0)
			return 360 + degreeAngle;
		else
			return degreeAngle;
	}

	public void displayCycles() {
		System.out.println("Number of Cycles: " + cycles.size());
		for (List<CustomizedEdge> cycle : cycles) {
			System.out.println("Number of edges in cycle: " + cycle.size());
			System.out.println(cycle);
		}
	}
}
