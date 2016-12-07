package visualizations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import LocalThickness.MaskThicknessMapWithOriginal;
import detailedGraph.CustomizedEdge;
import detailedGraph.CustomizedGraph;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import skeleton_analysis.Edge;
import skeleton_analysis.Point;

public class EdgeIDImageCreator {

	CustomizedGraph customizedGraph;
	ImagePlus thicknessImage;
	ImagePlus impBinary;
	ImagePlus maskImage;
	Map<String, Double> flowRateMap;
	Map<String, Double> secretionMap;
	int[][] edgeIdMatrix;
	double maxFlowRate;
	double muMax = 1.1;
	double kS = .015;

	public EdgeIDImageCreator(CustomizedGraph customizedGraph, ImagePlus impBinary) {
		this.customizedGraph = customizedGraph;
		this.thicknessImage = customizedGraph.getLocalThicknessImage();
		this.impBinary = impBinary;
		edgeIdMatrix = new int[thicknessImage.getWidth()][thicknessImage.getHeight()];
		flowRateMap = new HashMap<String, Double>();
		secretionMap = new LinkedHashMap<String, Double>();
		maxFlowRate = 0;
		this.maskImage = impBinary.duplicate();

		filledgeIdMatrix(edgeIdMatrix);
	}

	private void filledgeIdMatrix(int[][] edgeIdMatrix) {
		for (int i = 0; i < edgeIdMatrix.length; i++) {
			for (int j = 0; j < edgeIdMatrix[0].length; j++) {
				edgeIdMatrix[i][j] = Integer.MAX_VALUE;
			}
		}
	}

	public void generateImages() {
		modifyImpId();
		convertToImageMatrix(customizedGraph.getEdges());
		convertToImageMatrix(customizedGraph.getRemovedEdges());
		mapFlowRates();
		dilateMatrix();
	}

	private void modifyImpId() {
		List<List<Integer>> changedList = new ArrayList<List<Integer>>();
		List<Integer> coord = new ArrayList<Integer>();
		coord.add(10);
		coord.add(maskImage.getHeight() / 2);
		changedList.add(coord);
		//System.out.println(maskImage.getChannelProcessor().getf(changedList.get(0).get(0), changedList.get(0).get(1)));
		while (!changedList.isEmpty()) {
			int i = changedList.get(0).get(0);
			int j = changedList.get(0).get(1);
			//System.out.println("Outside" + i + ","+ j);
			changedList.remove(0);
			if (i > 0) {
				if (j > 0 && maskImage.getChannelProcessor().getf(i - 1, j - 1) == 255f) {
					coord = new ArrayList<Integer>();
					coord.add(i - 1);
					coord.add(j - 1);
					changedList.add(coord);
					maskImage.getChannelProcessor().setf(i-1, j-1, 128f);	
				}
				if (j < maskImage.getHeight() - 1 && maskImage.getChannelProcessor().getf(i - 1, j + 1) == 255f) {
					coord = new ArrayList<Integer>();
					coord.add(i - 1);
					coord.add(j + 1);
					changedList.add(coord);
					maskImage.getChannelProcessor().setf(i-1, j+1, 128f);	
				}
				if (maskImage.getChannelProcessor().getf(i - 1, j) == 255f) {
					coord = new ArrayList<Integer>();
					coord.add(i - 1);
					coord.add(j);
					changedList.add(coord);
					maskImage.getChannelProcessor().setf(i-1, j, 128f);	
				}
			}
			if (i < maskImage.getWidth() - 1) {
				if (j > 0 && maskImage.getChannelProcessor().getf(i + 1, j - 1) == 255f) {
					coord = new ArrayList<Integer>();
					coord.add(i + 1);
					coord.add(j - 1);
					changedList.add(coord);
					maskImage.getChannelProcessor().setf(i+1, j-1, 128f);	
				}
				if (j < maskImage.getHeight() - 1 && maskImage.getChannelProcessor().getf(i + 1, j + 1) == 255f) {
					coord = new ArrayList<Integer>();
					coord.add(i + 1);
					coord.add(j + 1);
					changedList.add(coord);
					maskImage.getChannelProcessor().setf(i+1, j+1, 128f);	
				}
				if (maskImage.getChannelProcessor().getf(i + 1, j) == 255f) {
					coord = new ArrayList<Integer>();
					coord.add(i + 1);
					coord.add(j);
					changedList.add(coord);
					maskImage.getChannelProcessor().setf(i+1, j, 128f);	
				}
			}
			if (j > 0 && maskImage.getChannelProcessor().getf(i, j - 1) == 255f) {
				coord = new ArrayList<Integer>();
				coord.add(i);
				coord.add(j - 1);
				changedList.add(coord);
				maskImage.getChannelProcessor().setf(i, j-1, 128f);	
			}
			if (j < maskImage.getHeight() - 1 && maskImage.getChannelProcessor().getf(i, j + 1) == 255f) {
				coord = new ArrayList<Integer>();
				coord.add(i);
				coord.add(j + 1);
				changedList.add(coord);
				maskImage.getChannelProcessor().setf(i, j+1, 128f);	
			}
			maskImage.getChannelProcessor().setf(i, j, 128f);	
		}
		maskImage.getChannelProcessor().setMinAndMax(0.0, 255f);
		for (int i = 0; i < maskImage.getWidth(); i++) {
			for (int j = 0; j < maskImage.getHeight(); j++) {
				if (maskImage.getChannelProcessor().getf(i, j) == 255f) {
					maskImage.getChannelProcessor().setf(i, j, 0f);
				}
				if (maskImage.getChannelProcessor().getf(i, j) == 128f) {
					maskImage.getChannelProcessor().setf(i, j, 255f);
				}
			}
		}
		System.out.println("Showing maskimage");
		maskImage.setTitle("Mask Image");
		maskImage.show();
		IJ.run("Fire");
	}

	private void mapFlowRates() {
		for (String edgeId : flowRateMap.keySet()) {
			double flowRate = flowRateMap.get(edgeId) / maxFlowRate;
			double secretionRate = muMax * flowRate / (flowRate + kS);
			/*
			 * System.out.println(flowRateMap.get(edgeId)); System.out.println(
			 * "flowrate: " + flowRate); System.out.println("secretionRate: " +
			 * secretionRate);
			 */
			secretionMap.put(edgeId, secretionRate);
		}
		// System.out.println(maxFlowRate);
	}

	public ImagePlus getFlowImage() {
		ImagePlus impEdgeId = thicknessImage.duplicate();
		System.out.println("maxFlowRate: " + maxFlowRate);
		impEdgeId.getChannelProcessor().setMinAndMax(0, 255);
		for (int i = 0; i < impEdgeId.getWidth(); i++) {
			for (int j = 0; j < impEdgeId.getHeight(); j++) {
				if (edgeIdMatrix[i][j] != Integer.MAX_VALUE) {
					double temp = flowRateMap.get(Integer.toString(edgeIdMatrix[i][j]))/maxFlowRate;
					temp = temp/(0.15+temp);
					impEdgeId.getChannelProcessor().setf(i, j,
							(float) (140*temp));
				} else {
					impEdgeId.getChannelProcessor().setf(i, j, 0);
				}
			}
		}
		MaskThicknessMapWithOriginal thicknessMask = new MaskThicknessMapWithOriginal();
		thicknessMask.inverse = false;
		thicknessMask.threshold = 1;
		// impEdgeId.show();
		WindowManager.setTempCurrentImage(impEdgeId);
		impEdgeId = thicknessMask.trimOverhang(maskImage, impEdgeId);

		ImagePlus temp = impEdgeId.duplicate();
		WindowManager.setTempCurrentImage(temp);
		/*
		 * temp.show(); IJ.run("Fire");
		 */
		for (int i = 0; i < impEdgeId.getWidth(); i++) {
			for (int j = 0; j < impEdgeId.getHeight(); j++) {
				if (impEdgeId.getChannelProcessor().getf(i, j) == 0) {
					impEdgeId.getChannelProcessor().setf(i, j, 255);
				}
			}
		}
		return impEdgeId;
	}

	private void convertToImageMatrix(List<CustomizedEdge> edges) {
		for (CustomizedEdge edge : edges) {
			double flowRate = (double) Math.abs(edge.getFlowRate());
			boolean addFlag = false;
			for (Edge skeletonEdge : edge.getSkeletonEdges()) {
				for (Point p : skeletonEdge.getSlabs()) {
					if(maskImage.getChannelProcessor().getf(p.x, p.y)==255f){
						edgeIdMatrix[p.x][p.y] = Integer.parseInt(edge.getLabel());
						addFlag = true;
					}
				}
			}
			if(addFlag){
				flowRateMap.put(edge.getLabel(), flowRate);
				if (flowRate > maxFlowRate) {
					maxFlowRate = flowRate;
				}
			}
		}
	}

	private void dilateMatrix() {
		int dilateCount = Integer.max(thicknessImage.getHeight(), thicknessImage.getWidth());
		int edgeIdMatrixWidth = edgeIdMatrix.length - 1;
		int edgeIdMatrixHeight = edgeIdMatrix[0].length - 1;
		for (int i = 0; i < dilateCount; i++) {
			int[][] newEdgeIdMatrix = new int[edgeIdMatrixWidth + 1][edgeIdMatrixHeight + 1];
			filledgeIdMatrix(newEdgeIdMatrix);
			for (int j = 0; j < thicknessImage.getWidth(); j++) {
				for (int k = 0; k < thicknessImage.getHeight(); k++) {
					if (edgeIdMatrix[j][k] != Integer.MAX_VALUE && maskImage.getChannelProcessor().getf(j, k) == 255f) {
						newEdgeIdMatrix[j][k] = edgeIdMatrix[j][k];
						if (j > 0 && edgeIdMatrix[j - 1][k] == Integer.MAX_VALUE) {
							newEdgeIdMatrix[j - 1][k] = edgeIdMatrix[j][k];
						}
						if (j < edgeIdMatrixWidth && edgeIdMatrix[j + 1][k] == Integer.MAX_VALUE) {
							newEdgeIdMatrix[j + 1][k] = edgeIdMatrix[j][k];
						}
						if (k > 0 && edgeIdMatrix[j][k - 1] == Integer.MAX_VALUE) {
							newEdgeIdMatrix[j][k - 1] = edgeIdMatrix[j][k];
						}
						if (k < edgeIdMatrixHeight && edgeIdMatrix[j][k + 1] == Integer.MAX_VALUE) {
							newEdgeIdMatrix[j][k + 1] = edgeIdMatrix[j][k];
						}

						if (j > 0 && k > 0 && edgeIdMatrix[j - 1][k - 1] == Integer.MAX_VALUE) {
							newEdgeIdMatrix[j - 1][k - 1] = edgeIdMatrix[j][k];
						}
						if (j < edgeIdMatrixWidth && k < edgeIdMatrixHeight
								&& edgeIdMatrix[j + 1][k + 1] == Integer.MAX_VALUE) {
							newEdgeIdMatrix[j + 1][k + 1] = edgeIdMatrix[j][k];
						}

						if (j < edgeIdMatrixWidth && k > 0 && edgeIdMatrix[j + 1][k - 1] == Integer.MAX_VALUE) {
							newEdgeIdMatrix[j + 1][k - 1] = edgeIdMatrix[j][k];
						}
						if (j > 0 && k < edgeIdMatrixHeight && edgeIdMatrix[j - 1][k + 1] == Integer.MAX_VALUE) {
							newEdgeIdMatrix[j - 1][k + 1] = edgeIdMatrix[j][k];
						}
					}else{
						secretionMap.remove(edgeIdMatrix[j][k]);
						flowRateMap.remove(edgeIdMatrix[j][k]);
					}
				}
			}
			edgeIdMatrix = newEdgeIdMatrix;
		}
	}

	public int[][] getEdgeIdMatrix() {
		return edgeIdMatrix;
	}

	public void setEdgeIdMatrix(int[][] edgeIdMatrix) {
		this.edgeIdMatrix = edgeIdMatrix;
	}

	/**
	 * @return the secretionMap
	 */
	public final Map<String, Double> getSecretionMap() {
		return secretionMap;
	}

	/**
	 * @param secretionMap
	 *            the secretionMap to set
	 */
	public final void setSecretionMap(Map<String, Double> secretionMap) {
		this.secretionMap = secretionMap;
	}

	public ImagePlus getMaskImage() {
		return maskImage;
	}

	public void setMaskImage(ImagePlus maskImage) {
		this.maskImage = maskImage;
	}
}
