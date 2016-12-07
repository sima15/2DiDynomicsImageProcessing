package simulation;

import java.io.File;
import java.util.Map;

import basicProcessing.POVRayExecution;
import idyno.Idynomics;
import ij.IJ;
import ij.WindowManager;
import skeletonize.DisplayGraph;

public class Controller {

	private static final String CONSOLIDATE_SOLUTE_CONCENTRATION_PATH = "\\SoluteConcentration\\Consolidated.txt";
	private static final String SOLUTE_CONCENTRATION_PATH = "\\SoluteConcentration\\xy-1\\";
	private static final String POVRAY_PATH = "\\povray\\";
	private static String protocol_xml;
//	private static final String RESULT_PATH = "C:\\Sima\\Updated Workspace\\iDynoMiCS\\resultss\\experiments\\";
//	private static final String PROTOCOL_PATH = "C:\\Sima\\Updated Workspace\\iDynoMiCS\\protocols\\experiments\\";
	private static final String RESULT_PATH = "E:\\Courses\\cs6600\\Project\\program\\resultss\\experiments\\";
	private static final String PROTOCOL_PATH = "E:\\Courses\\cs6600\\Project\\program\\protocols\\experiments\\";
	
	public static final boolean speciesOptimizer = true;
	
	public static boolean nodeMergerOptimizer = false;
	public static int NodeMergerTileSize = 1;
	public static boolean ratioLessThanFifty = true;
	public static boolean fullRun = false;
	public static String name = "Vascularperc30 -quartSize(20161122_1742)"; // = "Vascularperc30-quartSize(20161122_1742)";
	
	public static String totalProduct = "0";
	static int[][] edgeIdMatrix = null ;
	static Map<String, Double> secretionMap = null ;
	
	public Controller(String n){
		name = n;
		System.out.println("Name of folder in Controller: " + name);
	}
	
	public static void main(String[] args) throws Exception {
		startFirstPhase(args);
	}
	public static void startFirstPhase(String[] args) throws Exception {
		System.out.println(Runtime.getRuntime().totalMemory());
		/*
		 * String[] protocolPath = { PROTOCOL_PATH + PROTOCOL_XML }; try {
		 * Idynomics.main(protocolPath); } catch (Exception e) {
		 * e.printStackTrace(); System.exit(0); }
		 */

		/*
		 * File file = new File(RESULT_PATH); String[] names = file.list();
		 * Arrays.sort(names);
		 */

		// reading the latest contact folder
		// String name = names[names.length - 1];
//		protocol_xml = "\\" + name.substring(0, 14) + ".xml";
		System.out.println("File name: "+ name);
		protocol_xml = "\\Vascularperc30 -quartSize.xml";
		ImageProcessingUnit imageProcessingUnit = new ImageProcessingUnit();
		boolean protocolModifiedFlag = true;
//		setName();
		
		try {
			
			if(!fullRun){
			imageProcessingUnit.processImage(RESULT_PATH + name);
			System.out.println("Image processed");
			edgeIdMatrix = imageProcessingUnit.getEdgeIdMatrix();
			secretionMap = imageProcessingUnit.getSecretionMap();

			System.out.println(edgeIdMatrix.length + " " + edgeIdMatrix[0].length);
			saveImages(imageProcessingUnit, RESULT_PATH + name + POVRAY_PATH);
			}
			else {
				ProtocolModifier protocolModifier = new OptimizedProtocolModifier(edgeIdMatrix, secretionMap,
						protocol_xml);
				if (speciesOptimizer) {
					protocolModifier = new OptimizedProtocolModifier(edgeIdMatrix, secretionMap, protocol_xml);
				} else {
					protocolModifier = new ProtocolModifier(edgeIdMatrix, secretionMap, protocol_xml);
				}
				protocolModifier.modifyXML(RESULT_PATH + name);
				saveImages(imageProcessingUnit, RESULT_PATH + name + POVRAY_PATH);
				System.out.println("Images saved");
				imageProcessingUnit = null;
				protocolModifier = null;
				runSecondPhase(name);
				IncFileSecondPhaseModifier incFileModifier = new IncFileSecondPhaseModifier(RESULT_PATH + name,
						secretionMap);
				incFileModifier.modify();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Bad Network");
			if(!fullRun){
				System.out.println("fullRun: "+ fullRun );
				System.out.println("NodeMergerOptimizer: "+ nodeMergerOptimizer);
				System.out.println("NodeMergerTileSize: "+ NodeMergerTileSize);
				nodeMergerOptimizer = true;
//				startFirstPhase(args);
				if(NodeMergerTileSize <24){
					NodeMergerTileSize++;
					System.out.println("Starting phase one with nodeMergerTileSize = "+ NodeMergerTileSize);
					startFirstPhase(args);
				}
			}else{
				System.out.println("Couldn't find a solution to this problem! :(");
				System.out.println("fullRun: "+ fullRun );
				System.out.println("NodeMergerOptimizer: "+ nodeMergerOptimizer);
				System.out.println("NodeMergerTileSize: "+ NodeMergerTileSize);
				fullRun = false;
				nodeMergerOptimizer = false;
				NodeMergerTileSize = 1;
			}
//			if (fullRun) {
//				SimpleProtocolModifier simpleProtocolModifier = new SimpleProtocolModifier(protocol_xml);
//				simpleProtocolModifier.modifyXML(RESULT_PATH + name);
//				runSecondPhase(name);
//				IncFileSecondPhaseModifier incFileModifier = new IncFileSecondPhaseModifier(RESULT_PATH + name, null);
//				incFileModifier.modifySimple();
//				System.out.println("Bad Network");
//			}
		}
		
		if(!fullRun){
			System.out.println("Setting fullRun to true...");
			fullRun = true;
			startFirstPhase(args);
		}
		if (fullRun) {

			File povray = new File(RESULT_PATH + name + POVRAY_PATH);
			String[] images = povray.list();
			String pov = null;
			for (int i = images.length - 1; i >= 0; i--) {
				String e = images[i];
				if (e.contains(".pov")) {
					pov = e;
					break;
				}
			}

			POVRayExecution.executer(RESULT_PATH + name + POVRAY_PATH + pov);
			totalProduct =  Test.consolidateSoluteConcentrations(RESULT_PATH, name);
		}
	}

	private static void runSecondPhase(String name) {
		System.out.println(Runtime.getRuntime().totalMemory());
		System.gc();
		System.out.println(Runtime.getRuntime().totalMemory());

		String[] restartProtocolPath = { RESULT_PATH + name + protocol_xml };
		try {
			Idynomics.main(restartProtocolPath);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static void saveImages(ImageProcessingUnit imageProcessingUnit, String path) {
		WindowManager.setTempCurrentImage(imageProcessingUnit.getSkeletonizedBinaryImage());
		IJ.save(path + "skeletonized.png");
		WindowManager.setTempCurrentImage(imageProcessingUnit.getLocalThicknessImage());
		IJ.save(path + "localThickness.png");
		IJ.run("Fire");
		WindowManager.setTempCurrentImage(imageProcessingUnit.getFlowVisualizationImage());
		IJ.save(path + "flowVisualization.png");
		IJ.run("Fire");
		DisplayGraph.draw(imageProcessingUnit.getCustGraph().getEdges(), imageProcessingUnit.getLocalThicknessImage(),
				path + "graph.png");
	}
	
	public static void setName(String name){
		Controller.name = name;
	}
}
