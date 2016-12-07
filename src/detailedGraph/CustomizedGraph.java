package detailedGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cycleFinder.FindCycles;
import equationSolver.Solve_Linear_Equation;
import ij.ImagePlus;
import simulation.Controller;
import skeleton_analysis.Edge;
import skeleton_analysis.Vertex;

public class CustomizedGraph {

	private static final int _PRESSURE = 1000; // kPa
	private List<CustomizedEdge> edges;
	private Map<String, CustomizedNode> nodes;
	private ImagePlus localThicknessImage;
	private List<List<CustomizedEdge>> cycles;
	private List<CustomizedEdge> removedEdges;
	private List<CustomizedNode> removedNodes;
	private CustomizedNode startNode;
	private CustomizedNode endNode;
	private static final int SOURCEPOSX = 16;
	private static final int SINKPOSX = 990;
	private double[][] equationLeftSide;
	private double[][] equationRightSide;
	private List<CustomizedNode> connectedNodeSet;
	private boolean graphCreatedFlag;
	private double[][] flowRateMatrix;
	private double bloodViscosity = 0.00089;

	public CustomizedGraph() {
		this.removedEdges = new ArrayList<CustomizedEdge>();
		this.removedNodes = new ArrayList<CustomizedNode>();
		this.edges = new ArrayList<CustomizedEdge>();
		this.nodes = new HashMap<String, CustomizedNode>();
		graphCreatedFlag = false;
	}

	public CustomizedGraph convertGraph(ArrayList<Edge> skeletonEdges, ImagePlus localThicknessImage) throws Exception {
		this.localThicknessImage = localThicknessImage;
		createGraph(skeletonEdges);

		if (!connectedNodeSet.contains(endNode)) {
			System.out.println("Error");
			throw new Exception("Error");
		}

		FindCycles findCycles = new FindCycles(this);
		cycles = findCycles.getCycles();
		// findCycles.displayCycles();
		try {
			generateEquationMatrix();
			Solve_Linear_Equation solver = new Solve_Linear_Equation(edges.size() + 2, equationLeftSide,
					equationRightSide);
			flowRateMatrix = solver.solve();

			for (CustomizedEdge e : edges) {
				e.setFlowRate(flowRateMatrix[Integer.parseInt(e.getLabel())][0]);
			}
			for (CustomizedEdge e : getRemovedEdges()) {
				if ((e.getVertex1().equals(getStartNode()) && e.getVertex2().equals(getStartNode()))
						|| (e.getVertex1().equals(getEndNode()) && e.getVertex2().equals(getEndNode()))) {
					e.setFlowRate(flowRateMatrix[flowRateMatrix.length - 1][0]);
				} else {
					e.setFlowRate(0);
				}
			}
		} catch (Exception e) {e.printStackTrace();}
		// DisplayGraph.draw(edges, localThicknessImage);
		graphCreatedFlag = true;
		return this;
	}

	private void createGraph(List<Edge> skeletonEdges) {
		for (Edge skeletonEdge : skeletonEdges) {
			CustomizedNode node1;
			CustomizedNode node2;
			if (!Controller.nodeMergerOptimizer) {
				node1 = addOrGetNode(skeletonEdge.getV1());
				node2 = addOrGetNode(skeletonEdge.getV2());
			} else {
				node1 = addOrGetNodeOptimized(skeletonEdge.getV1());
				node2 = addOrGetNodeOptimized(skeletonEdge.getV2());
			}

			if (!node1.equals(node2)) {
				CustomizedEdge edge = new CustomizedEdge(node1, node2, skeletonEdge, localThicknessImage);
				edges.add(edge);
			}
		}

		addEdgestoNodes();
		mergeStartAndEndNodes();
		mergeEdgesBetweenSameNodes();
		filterDeadEnds();
		assignLabeltoNodes();
		assignLabeltoEdges();
		generateConnectedSet();

		System.out.println("Detailed graph created");

	}

	public CustomizedNode addOrGetNodeOptimized(Vertex skeletonVertex) {
		int x = (int) Math.floor(skeletonVertex.getPoints().get(0).x / Controller.NodeMergerTileSize) * Controller.NodeMergerTileSize;
		int y = (int) Math.floor(skeletonVertex.getPoints().get(0).y / Controller.NodeMergerTileSize) * Controller.NodeMergerTileSize;
		CustomizedNode node = new CustomizedNode(x, y);
		if ((nodes.get(node.getKey())) == null) {
			nodes.put(node.getKey(), node);
		} else {
			node = nodes.get(node.getKey());
		}
		return node;
	}

	private void generateConnectedSet() {
		connectedNodeSet = new ArrayList<CustomizedNode>();
		generateConnectedSet(startNode);
		List<CustomizedEdge> removeList = new ArrayList<CustomizedEdge>();
		for (CustomizedEdge e : edges) {
			if (!connectedNodeSet.contains(e.getVertex1()) || !connectedNodeSet.contains(e.getVertex2())) {
				removeList.add(e);
			}
		}
		for (CustomizedEdge e : removeList) {
			removeEdge(e);
		}
	}

	private void generateConnectedSet(CustomizedNode n) {
		connectedNodeSet.add(n);
		for (CustomizedNode node : n.getAdjacentNodes()) {
			if (!connectedNodeSet.contains(node)) {
				generateConnectedSet(node);
			}
		}

	}

	private void mergeStartAndEndNodes() {
		startNode = new CustomizedNode(SOURCEPOSX - 400, 230);
		endNode = new CustomizedNode(SINKPOSX + 400, 230);
		List<CustomizedNode> nodeList = new ArrayList<CustomizedNode>(nodes.values());
		for (CustomizedNode n : nodeList) {
			if (n.getX() < SOURCEPOSX) {
				mergeNode(startNode, n);
			}
			if (n.getX() > SINKPOSX) {
				mergeNode(endNode, n);
			}
		}
		nodes.put(startNode.getKey(), startNode);
		nodes.put(endNode.getKey(), endNode);
		removeEdgesWithSameEnds();
	}

	private void mergeEdgesBetweenSameNodes() {
		List<CustomizedEdge> removeList = new ArrayList<CustomizedEdge>();
		for (int i = 0; i < edges.size() - 1; i++) {
			for (int j = i + 1; j < edges.size(); j++) {
				CustomizedEdge e1 = edges.get(i);
				CustomizedEdge e2 = edges.get(j);
				if ((e1.getVertex1().equals(e2.getVertex1()) && e1.getVertex2().equals(e2.getVertex2()))
						|| (e1.getVertex1().equals(e2.getVertex2()) && e1.getVertex2().equals(e2.getVertex1()))) {
					e2.getVertex1().getEdges().remove(e2);
					e2.getVertex2().getEdges().remove(e2);
					e1.getSkeletonEdges().addAll(e2.getSkeletonEdges());
					removeList.add(e2);
				}
			}
		}
		edges.removeAll(removeList);
	}

	private void mergeNode(CustomizedNode node1, CustomizedNode node2) {
		for (CustomizedEdge e : node2.getEdges()) {
			boolean addedFlag = false;
			if (e.getVertex1().equals(node2)) {
				e.setVertex1(node1);
				node1.addEdge(e);
				addedFlag = true;
			}
			if (e.getVertex2().equals(node2)) {
				e.setVertex2(node1);
				if (!addedFlag)
					node1.addEdge(e);
			}
			nodes.remove(node2.getKey());
		}
	}

	private void removeEdgesWithSameEnds() {
		List<CustomizedEdge> removeList = new ArrayList<CustomizedEdge>();
		for (CustomizedEdge e : edges) {
			if (e.getVertex1().equals(e.getVertex2())) {
				removeList.add(e);
				e.getVertex1().getEdges().remove(e);
			}
		}
		edges.removeAll(removeList);
		removedEdges.addAll(removeList);
	}

	private void assignLabeltoNodes() {
		CustomizedNode.resetNodeCounter();
		for (String key : nodes.keySet()) {
			nodes.get(key).setLabel(CustomizedNode.getNewNodeLabel());
		}
	}

	private void assignLabeltoEdges() {
		CustomizedEdge.resetEdgeCounter();
		for (CustomizedEdge e : edges) {
			e.setLabel(CustomizedEdge.getNewEdgeLabel());
		}
		for (CustomizedEdge e : removedEdges) {
			e.setLabel(CustomizedEdge.getNewEdgeLabel());
		}
	}

	private void addEdgestoNodes() {
		for (CustomizedEdge edge : edges) {
			edge.getVertex1().addEdge(edge);
			edge.getVertex2().addEdge(edge);
		}
	}

	private void filterDeadEnds() {
		while (true) {
			List<CustomizedEdge> removeList = new ArrayList<CustomizedEdge>();
			for (CustomizedEdge e : edges) {
				boolean removeFlag = false;
				if (e.getVertex1().getEdges().size() < 2) {
					removeFlag = true;
					nodes.remove(e.getVertex1().getKey());
					e.getVertex2().getEdges().remove(e);
				}
				if (e.getVertex2().getEdges().size() < 2) {
					removeFlag = true;
					nodes.remove(e.getVertex2().getKey());
					e.getVertex1().getEdges().remove(e);
				}

				if (removeFlag) {
					removeList.add(e);
				}
			}

			if (removeList.isEmpty())
				break;

			edges.removeAll(removeList);
			removedEdges.addAll(removeList);
		}
	}

	private void generateEquationMatrix() {

		equationLeftSide = new double[nodes.size() + cycles.size()][edges.size() + 2];
		System.out.println("Nodes + cycles size: " + ((int) nodes.size() + cycles.size()));
		System.out.println("Edges size: " + ((int) edges.size() + 2));
		equationRightSide = new double[nodes.size() + cycles.size()][1];
		int equationCounter = 0;
		List<CustomizedEdge> longestCycle = getLongestCycle();
		cycles.remove(longestCycle);
		for (String key : nodes.keySet()) {
			populateVertexEquation(nodes.get(key), equationLeftSide, equationRightSide, equationCounter++);
		}

		for (List<CustomizedEdge> cycle : cycles) {
			populateCycleEquations(cycle, equationLeftSide, equationRightSide, equationCounter++);
		}

		populatePowerCycleEquation(longestCycle, equationLeftSide, equationRightSide, equationCounter++);
	}

	private void populatePowerCycleEquation(List<CustomizedEdge> longestCycle, double[][] adjMatrix,
			double[][] constants, int equationCounter) {
		System.out.println(longestCycle);
		List<CustomizedEdge> pathFromStartToEnd = new ArrayList<CustomizedEdge>();
		longestCycle.addAll(longestCycle);
		boolean startNodeFlag = false;
		boolean endNodeFlag = false;
		CustomizedNode currentNode = null;
		CustomizedEdge currentEdge = null;
		for (int i = 0; i < longestCycle.size() - 1; i++) {
			currentEdge = longestCycle.get(i);
			CustomizedEdge nextEdge = longestCycle.get(i + 1);
			if (currentEdge.getVertex1().equals(nextEdge.getVertex1())
					|| currentEdge.getVertex1().equals(nextEdge.getVertex2())) {
				currentNode = currentEdge.getVertex2();
			} else {
				currentNode = currentEdge.getVertex1();
			}
			if (currentNode.equals(startNode)) {
				startNodeFlag = true;
			}
			if (currentNode.equals(endNode)) {
				endNodeFlag = true;
			}
			if (startNodeFlag && endNodeFlag) {
				break;
			}
			if (startNodeFlag || endNodeFlag) {
				pathFromStartToEnd.add(currentEdge);
			}
		}
		populateCycleEquations(pathFromStartToEnd, adjMatrix, constants, equationCounter);
		System.out.println("pathFromStartToEnd : " + pathFromStartToEnd);
		if (pathFromStartToEnd.get(0).equals(endNode)) {
			constants[equationCounter][0] = 0;
		} else {
			constants[equationCounter][0] = _PRESSURE;
		}
	}

	private List<CustomizedEdge> getLongestCycle() {
		List<CustomizedEdge> longestCycle = new ArrayList<CustomizedEdge>();
		for (List<CustomizedEdge> cycle : cycles) {
			if (cycle.size() > longestCycle.size())
				longestCycle = cycle;
		}
		return longestCycle;
	}

	private void populateCycleEquations(List<CustomizedEdge> cycle, double[][] adjMatrix, double[][] constants,
			int equationCounter) {
		CustomizedNode currentNode = null;
		CustomizedNode nextNode = null;
		CustomizedEdge currentEdge = null;
		if (cycle.size() == 1) {
			int edgeLabel = Integer.parseInt(cycle.get(0).getLabel());
			adjMatrix[equationCounter][edgeLabel] = 1;
		} else {
			for (int i = 0; i < cycle.size(); i++) {
				currentEdge = cycle.get(i);
				int edgeLabel = Integer.parseInt(currentEdge.getLabel());
				if (i < cycle.size() - 1) {
					CustomizedEdge nextEdge = cycle.get(i + 1);
					if (currentEdge.getVertex1().equals(nextEdge.getVertex1())
							|| currentEdge.getVertex1().equals(nextEdge.getVertex2())) {
						currentNode = currentEdge.getVertex2();
						nextNode = currentEdge.getVertex1();
					} else {
						currentNode = currentEdge.getVertex1();
						nextNode = currentEdge.getVertex2();
					}
				} else {
					currentNode = nextNode;
					if (currentNode.equals(currentEdge.getVertex1())) {
						nextNode = currentEdge.getVertex2();
					} else {
						nextNode = currentEdge.getVertex1();
					}
				}
				int nodeLabel = Integer.parseInt(currentNode.getLabel());
				int v1label = Integer.parseInt(nextNode.getLabel());
				double length = getLength(currentEdge);
				double resistance = 8 * bloodViscosity * length / Math.pow(currentEdge.getEdgeThickness() / 2, 4)
						* Math.PI;
				if (v1label > nodeLabel) {
					adjMatrix[equationCounter][edgeLabel] = -resistance;
				} else {
					adjMatrix[equationCounter][edgeLabel] = resistance;
				}
				constants[equationCounter][0] = 0;
			}
		}
	}

	private double getLength(CustomizedEdge e) {
		double maxLength = 0;
		for (Edge se : e.getSkeletonEdges()) {
			if (maxLength < se.getLength())
				maxLength = se.getLength();
		}
		return maxLength;
	}

	private void populateVertexEquation(CustomizedNode node, double[][] adjMatrix, double[][] constants,
			int equationCounter) {
		int nodeLabel = Integer.parseInt(node.getLabel());
		for (CustomizedEdge edge : node.getEdges()) {
			int v1label = Integer.parseInt(edge.getVertex1().getLabel());
			int v2label = Integer.parseInt(edge.getVertex2().getLabel());
			int edgeLabel = Integer.parseInt(edge.getLabel());

			if (v1label > nodeLabel || v2label > nodeLabel) {
				adjMatrix[equationCounter][edgeLabel] = 1;
			} else {
				adjMatrix[equationCounter][edgeLabel] = -1;
			}

			if (node.equals(startNode)) {
				adjMatrix[equationCounter][edges.size()] = -1;

			}

			if (node.equals(endNode)) {
				adjMatrix[equationCounter][edges.size() + 1] = 1;
			}
			constants[equationCounter][0] = 0;
		}
	}

	public CustomizedNode addOrGetNode(Vertex skeletonVertex) {
		int x = (int) Math.floor(skeletonVertex.getPoints().get(0).x / 5) * 5;
		int y = (int) Math.floor(skeletonVertex.getPoints().get(0).y / 5) * 5;
		CustomizedNode node = new CustomizedNode(x, y);
		if ((nodes.get(node.getKey())) == null) {
			nodes.put(node.getKey(), node);
		} else {
			node = nodes.get(node.getKey());
		}
		return node;
	}

	public List<CustomizedEdge> getEdges() {
		return edges;
	}

	public Map<String, CustomizedNode> getNodes() {
		return nodes;
	}

	public List<List<CustomizedEdge>> getCycles() {
		return cycles;
	}

	public ImagePlus getLocalThicknessImage() {
		return localThicknessImage;
	}

	public void setLocalThicknessImage(ImagePlus localThicknessImage) {
		this.localThicknessImage = localThicknessImage;
	}

	public List<CustomizedEdge> getRemovedEdges() {
		return removedEdges;
	}

	public void setRemovedEdges(List<CustomizedEdge> removedEdges) {
		this.removedEdges = removedEdges;
	}

	public CustomizedNode getStartNode() {
		return startNode;
	}

	public void setStartNode(CustomizedNode startNode) {
		this.startNode = startNode;
	}

	public CustomizedNode getEndNode() {
		return endNode;
	}

	public void setEndNode(CustomizedNode endNode) {
		this.endNode = endNode;
	}

	public void removeEdge(CustomizedEdge e) {
		if (edges.contains(e)) {
			removedEdges.add(e);
			e.getVertex1().getEdges().remove(e);
			e.getVertex2().getEdges().remove(e);
			edges.remove(e);
			filterDeadEnds();
			assignLabeltoEdges();
			assignLabeltoNodes();
			generateConnectedSet();
		}
	}

	public List<CustomizedNode> getConnectedSet() {
		return connectedNodeSet;
	}

	public void setConnectedSet(List<CustomizedNode> connectedSet) {
		this.connectedNodeSet = connectedSet;
	}

	public boolean isCreated() {
		return graphCreatedFlag;
	}

	public double[][] getFlowRateMatrix() {
		return flowRateMatrix;
	}

	public void setFlowRateMatrix(double[][] flowRateMatrix) {
		this.flowRateMatrix = flowRateMatrix;
	}

}
