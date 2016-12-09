package simulation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import LocalThickness.LocalThicknessWrapper;
import Skeletonize3D_.Skeletonize3D_;
import basicProcessing.ImageDilation;
import basicProcessing.ImageErosion;
import basicProcessing.ResultImageConverter;
import detailedGraph.CustomizedGraph;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.Thresholder;
import skeleton_analysis.SkeletonResult;
import skeletonize.AnalyzeSkeleton_;
import stacks.ThreePaneCrop;
import utils.LogFile;
import visualizations.EdgeIDImageCreator;

public class ImageProcessingUnit {
	private int[][] edgeIdMatrix;
	private ImagePlus binaryImage;
	private ImagePlus skeletonizedBinaryImage;
	private ImagePlus localThicknessImage;
	private ImagePlus binarizedImage;
	private CustomizedGraph custGraph;
	private ImagePlus flowVisualizationImage;
	private double[][] flowRateMatrix;
	private Map<String, Double> secretionMap;
	private ImagePlus maskImage;

	public boolean processImage(String result_path) throws Exception {
		LogFile.write("Starting image processing. ");
		String lastImage = ResultImageConverter.convertLastPOVtoPNG(result_path);
		String binaryPNGImagePath = generateBinaryImage(lastImage);
		String cropImage = cropImage(binaryPNGImagePath);
		ImagePlus binaryImage = IJ.openImage(cropImage);
		
		WindowManager.setTempCurrentImage(binaryImage);
		Thresholder thresholder = new Thresholder();
		thresholder.run("");
		binarizedImage = WindowManager.getCurrentImage().duplicate();
		if(!Controller.ratioLessThanFifty){
			binarizedImage.getChannelProcessor().invert();
			binarizedImage.getChannelProcessor().invertLut();
		}
		binarizedImage.show();
		LocalThicknessWrapper localThicknessWrapper = new LocalThicknessWrapper();
		localThicknessWrapper.setShowOptions(false);
		localThicknessWrapper.run("");
		localThicknessImage = WindowManager.getCurrentImage().duplicate();
		localThicknessImage.show();
		LogFile.write("Image converted to POV, cropped, binarized and local thickness produced!");
		
		LogFile.write("Attempting to skeletonize the image");
		custGraph = skeletonizedGraph(binarizedImage.duplicate(), localThicknessImage.duplicate());
		if (custGraph.isCreated()) {
			EdgeIDImageCreator edgeIDImageCreator = new EdgeIDImageCreator(custGraph,
					binarizedImage.duplicate());
			edgeIDImageCreator.generateImages();
			edgeIdMatrix = edgeIDImageCreator.getEdgeIdMatrix();
			secretionMap = edgeIDImageCreator.getSecretionMap();
			maskImage = edgeIDImageCreator.getMaskImage();
			flowVisualizationImage = edgeIDImageCreator.getFlowImage();
			flowRateMatrix = custGraph.getFlowRateMatrix();

			flowVisualizationImage.show();
			IJ.run("Fire");
			LogFile.write("Graph created");
			System.out.println("Processing ended");
			return true;
		}else{
			LogFile.write("Graph creation unsuccessful!");
			System.out.println("Image processing unsuccessful. :( ");
			return false;
		}
	}

	private static String generateBinaryImage(String imagePath) throws InterruptedException, IOException, Exception {
		BufferedImage bufferedImage = ImageIO.read(new File(imagePath));
		BufferedImage bufferedIm = ImageErosion.imageErode(
		ImageErosion.imageErode(ImageDilation.imageDilation(ImageErosion.imageErode(bufferedImage))));
		String binaryImagePath = imagePath.replaceAll("\\.png", "_binary.png");
		binaryImagePath = binaryImagePath.replaceAll("\\.PNG", "_binary.PNG");
		ImageIO.write(bufferedIm, "png", new File(binaryImagePath));
		return binaryImagePath;
	}

	private CustomizedGraph skeletonizedGraph(ImagePlus binaryImage, ImagePlus localThicknessImage) throws Exception {
		ImagePlus imp = binaryImage.duplicate();
		WindowManager.setTempCurrentImage(imp);
		Thresholder thresholder = new Thresholder();
		thresholder.run("");
		if(!Controller.ratioLessThanFifty){
			WindowManager.getTempCurrentImage().getChannelProcessor().invert();
			//WindowManager.getTempCurrentImage().getChannelProcessor().invertLut();
		}
		Skeletonize3D_ skeletonize3d_ = new Skeletonize3D_();
		skeletonize3d_.setup("", imp);
		skeletonize3d_.run(null);
		skeletonizedBinaryImage = WindowManager.getCurrentImage().duplicate();
		skeletonizedBinaryImage.show();
		AnalyzeSkeleton_ analyzeSkeleton = new AnalyzeSkeleton_();
		analyzeSkeleton.setup("", imp);
		analyzeSkeleton.run(0, false, false, imp, true, false);
		SkeletonResult skeletonResult = analyzeSkeleton.getSkeletonResult();
		CustomizedGraph custGraph = new CustomizedGraph();
		custGraph.convertGraph(skeletonResult.getGraph()[0].getEdges(), localThicknessImage.duplicate());
		return custGraph;
	}

	public int[][] getEdgeIdMatrix() {
		return edgeIdMatrix;
	}

	public double[][] getFlowRateMatrix() {
		return flowRateMatrix;
	}

	public void setFlowRateMatrix(double[][] flowRateMatrix) {
		this.flowRateMatrix = flowRateMatrix;
	}

	private static String cropImage(String imagePath) {
		String croppedImagePath = imagePath.replaceAll("\\.png", "_cropped.png");
		ImagePlus imp = IJ.openImage(imagePath);
		WindowManager.setTempCurrentImage(imp);
		System.out.println(imp.getWidth());
		System.out.println(imp.getHeight());
		//ImagePlus croppedImage = ThreePaneCrop.performCrop(imp, 180, 780, 64, 574, 0, 0, false);
		ImagePlus croppedImage = ThreePaneCrop.performCrop(imp, 0, 1024, 128, 640, 0, 0, false);
//		ImagePlus croppedImage = imp;
		WindowManager.setTempCurrentImage(croppedImage);
		IJ.save(croppedImagePath);
		return croppedImagePath;
	}

	public ImagePlus getBinaryImage() {
		return binaryImage;
	}

	public void setBinaryImage(ImagePlus binaryImage) {
		this.binaryImage = binaryImage;
	}

	public ImagePlus getSkeletonizedBinaryImage() {
		return skeletonizedBinaryImage;
	}

	public void setSkeletonizedBinaryImage(ImagePlus skeletonizedBinaryImage) {
		this.skeletonizedBinaryImage = skeletonizedBinaryImage;
	}

	public ImagePlus getLocalThicknessImage() {
		return localThicknessImage;
	}

	public void setLocalThicknessImage(ImagePlus localThicknessImage) {
		this.localThicknessImage = localThicknessImage;
	}

	public CustomizedGraph getCustGraph() {
		return custGraph;
	}

	public void setCustGraph(CustomizedGraph custGraph) {
		this.custGraph = custGraph;
	}

	public ImagePlus getFlowVisualizationImage() {
		return flowVisualizationImage;
	}

	public void setFlowVisualizationImage(ImagePlus flowVisualizationImage) {
		this.flowVisualizationImage = flowVisualizationImage;
	}

	public void setEdgeIdMatrix(int[][] edgeIdMatrix) {
		this.edgeIdMatrix = edgeIdMatrix;
	}

	public ImagePlus getBinarizedImage() {
		return binarizedImage;
	}

	public void setBinarizedImage(ImagePlus binarizedImage) {
		this.binarizedImage = binarizedImage;
	}

	/**
	 * @return the secretionMap
	 */
	public final Map<String, Double> getSecretionMap() {
		return secretionMap;
	}

	/**
	 * @param secretionMap the secretionMap to set
	 */
	public final void setSecretionMap(Map<String, Double> secretionMap) {
		this.secretionMap = secretionMap;
	}

	public ImagePlus getmaskImage() {
		return maskImage;
	}
//	private int[][] edgeIdMatrix;
//	private ImagePlus binaryImage;
//	private ImagePlus skeletonizedBinaryImage;
//	private ImagePlus localThicknessImage;
//	private ImagePlus binarizedImage;
//	private CustomizedGraph custGraph;
//	private ImagePlus flowVisualizationImage;
//	private double[][] flowRateMatrix;
//	private Map<String, Double> secretionMap;
//	private ImagePlus maskImage;
//
//	public void processImage(String result_path) throws Exception {
//		String lastImage = ResultImageConverter.convertLastPOVtoPNG(result_path);
//		String binaryPNGImagePath = generateBinaryImage(lastImage);
//		String cropImage = cropImage(binaryPNGImagePath);
//		ImagePlus binaryImage = IJ.openImage(cropImage);
//		
//		WindowManager.setTempCurrentImage(binaryImage);
//		Thresholder thresholder = new Thresholder();
//		thresholder.run("");
//		binarizedImage = WindowManager.getCurrentImage().duplicate();
//		if(!Controller.ratioLessThanFifty){
//			binarizedImage.getChannelProcessor().invert();
//			binarizedImage.getChannelProcessor().invertLut();
//		}
//		binarizedImage.show();
//		LocalThicknessWrapper localThicknessWrapper = new LocalThicknessWrapper();
//		localThicknessWrapper.setShowOptions(false);
//		localThicknessWrapper.run("");
//		localThicknessImage = WindowManager.getCurrentImage().duplicate();
//		localThicknessImage.show();
//
//		custGraph = skeletonizedGraph(binarizedImage.duplicate(), localThicknessImage.duplicate());
//		if (custGraph.isCreated()) {
//			EdgeIDImageCreator edgeIDImageCreator = new EdgeIDImageCreator(custGraph,
//					binarizedImage.duplicate());
//			edgeIDImageCreator.generateImages();
//			edgeIdMatrix = edgeIDImageCreator.getEdgeIdMatrix();
//			secretionMap = edgeIDImageCreator.getSecretionMap();
//			maskImage = edgeIDImageCreator.getMaskImage();
//			flowVisualizationImage = edgeIDImageCreator.getFlowImage();
//			flowRateMatrix = custGraph.getFlowRateMatrix();
//
//			flowVisualizationImage.show();
//			IJ.run("Fire");
//			System.out.println("Processing ended");
//		}else{
//			System.out.println("Image processing unsuccessful");
//		}
//		
//	}
//
//	private static String generateBinaryImage(String imagePath) throws InterruptedException, IOException, Exception {
//		BufferedImage bufferedImage = ImageIO.read(new File(imagePath));
//		BufferedImage bufferedIm = ImageErosion.imageErode(
//				ImageErosion.imageErode(ImageDilation.imageDilation(ImageErosion.imageErode(bufferedImage))));
//		String binaryImagePath = imagePath.replaceAll("\\.png", "_binary.png");
//		binaryImagePath = binaryImagePath.replaceAll("\\.PNG", "_binary.PNG");
//		ImageIO.write(bufferedIm, "png", new File(binaryImagePath));
//		return binaryImagePath;
//	}
//
//	private CustomizedGraph skeletonizedGraph(ImagePlus binaryImage, ImagePlus localThicknessImage) throws Exception {
//		ImagePlus imp = binaryImage.duplicate();
//		WindowManager.setTempCurrentImage(imp);
//		Thresholder thresholder = new Thresholder();
//		thresholder.run("");
//		if(!Controller.ratioLessThanFifty){
//			WindowManager.getTempCurrentImage().getChannelProcessor().invert();
//			//WindowManager.getTempCurrentImage().getChannelProcessor().invertLut();
//		}
//		Skeletonize3D_ skeletonize3d_ = new Skeletonize3D_();
//		skeletonize3d_.setup("", imp);
//		skeletonize3d_.run(null);
//		skeletonizedBinaryImage = WindowManager.getCurrentImage().duplicate();
//		skeletonizedBinaryImage.show();
//		AnalyzeSkeleton_ analyzeSkeleton = new AnalyzeSkeleton_();
//		analyzeSkeleton.setup("", imp);
//		analyzeSkeleton.run(0, false, false, imp, true, false);
//		SkeletonResult skeletonResult = analyzeSkeleton.getSkeletonResult();
//		CustomizedGraph custGraph = new CustomizedGraph();
//		custGraph.convertGraph(skeletonResult.getGraph()[0].getEdges(), localThicknessImage.duplicate());
//		return custGraph;
//	}
//
//	public int[][] getEdgeIdMatrix() {
//		return edgeIdMatrix;
//	}
//
//	public double[][] getFlowRateMatrix() {
//		return flowRateMatrix;
//	}
//
//	public void setFlowRateMatrix(double[][] flowRateMatrix) {
//		this.flowRateMatrix = flowRateMatrix;
//	}
//
//	private static String cropImage(String imagePath) {
//		String croppedImagePath = imagePath.replaceAll("\\.png", "_cropped.png");
//		ImagePlus imp = IJ.openImage(imagePath);
//		WindowManager.setTempCurrentImage(imp);
//		System.out.println(imp.getWidth());
//		System.out.println(imp.getHeight());
//		//ImagePlus croppedImage = ThreePaneCrop.performCrop(imp, 180, 780, 64, 574, 0, 0, false);
//		ImagePlus croppedImage = ThreePaneCrop.performCrop(imp, 0, 1024, 128, 640, 0, 0, false);
//		WindowManager.setTempCurrentImage(croppedImage);
//		IJ.save(croppedImagePath);
//		return croppedImagePath;
//	}
//
//	public ImagePlus getBinaryImage() {
//		return binaryImage;
//	}
//
//	public void setBinaryImage(ImagePlus binaryImage) {
//		this.binaryImage = binaryImage;
//	}
//
//	public ImagePlus getSkeletonizedBinaryImage() {
//		return skeletonizedBinaryImage;
//	}
//
//	public void setSkeletonizedBinaryImage(ImagePlus skeletonizedBinaryImage) {
//		this.skeletonizedBinaryImage = skeletonizedBinaryImage;
//	}
//
//	public ImagePlus getLocalThicknessImage() {
//		return localThicknessImage;
//	}
//
//	public void setLocalThicknessImage(ImagePlus localThicknessImage) {
//		this.localThicknessImage = localThicknessImage;
//	}
//
//	public CustomizedGraph getCustGraph() {
//		return custGraph;
//	}
//
//	public void setCustGraph(CustomizedGraph custGraph) {
//		this.custGraph = custGraph;
//	}
//
//	public ImagePlus getFlowVisualizationImage() {
//		return flowVisualizationImage;
//	}
//
//	public void setFlowVisualizationImage(ImagePlus flowVisualizationImage) {
//		this.flowVisualizationImage = flowVisualizationImage;
//	}
//
//	public void setEdgeIdMatrix(int[][] edgeIdMatrix) {
//		this.edgeIdMatrix = edgeIdMatrix;
//	}
//
//	public ImagePlus getBinarizedImage() {
//		return binarizedImage;
//	}
//
//	public void setBinarizedImage(ImagePlus binarizedImage) {
//		this.binarizedImage = binarizedImage;
//	}
//
//	/**
//	 * @return the secretionMap
//	 */
//	public final Map<String, Double> getSecretionMap() {
//		return secretionMap;
//	}
//
//	/**
//	 * @param secretionMap the secretionMap to set
//	 */
//	public final void setSecretionMap(Map<String, Double> secretionMap) {
//		this.secretionMap = secretionMap;
//	}
//
//	public ImagePlus getmaskImage() {
//		return maskImage;
//	}
}
