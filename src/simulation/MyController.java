package simulation;


import java.io.File;
import java.util.Map;

import basicProcessing.POVRayExecution;
import idyno.Idynomics;
import ij.IJ;
import ij.WindowManager;
import skeletonize.DisplayGraph;

public class MyController {


		private final String CONSOLIDATE_SOLUTE_CONCENTRATION_PATH = "\\SoluteConcentration\\Consolidated.txt";
		private final String SOLUTE_CONCENTRATION_PATH = "\\SoluteConcentration\\xy-1\\";
		private final String POVRAY_PATH = "\\povray\\";
		private static String protocol_xml;
//		private static final String RESULT_PATH = "C:\\Sima\\Updated Workspace\\iDynoMiCS\\resultss\\experiments\\";
//		private static final String PROTOCOL_PATH = "C:\\Sima\\Updated Workspace\\iDynoMiCS\\protocols\\experiments\\";
//		private final String RESULT_PATH = "E:\\Courses\\cs6600\\Project\\program\\resultss\\experiments\\12062016\\";
		private final String RESULT_PATH = "E:\\Courses\\cs6600\\Project\\program\\resultss\\experiments\\12062016\\";
		private final String PROTOCOL_PATH = "E:\\Courses\\cs6600\\Project\\program\\protocols\\experiments\\";
		
		public final boolean speciesOptimizer = true;
		
		public boolean nodeMergerOptimizer = false;
		public int NodeMergerTileSize = 1;
		public boolean ratioLessThanFifty = true;
		public boolean fullRun = false;
		public static String name = "Vascularperc30-quartSize(20161201_1759)";
		
		public String totalProduct = "0";
		static int[][] edgeIdMatrix = null ;
		static Map<String, Double> secretionMap = null ;
		boolean finished;
		
		public MyController(String n){
			name = n;
			System.out.println("Name of folder in Controller: " + name);
		}
		
		public static void main(String[] args) throws Exception {
			MyController controller = new MyController(name);
			controller.startFirstPhase();
		}
		
		public void startFirstPhase() throws Exception {
			System.out.println(Runtime.getRuntime().totalMemory());

			// reading the latest folder in the resultss directory
			System.out.println("File name: "+ name);
			protocol_xml = "\\Vascularperc30-quartSize.xml";
			ImageProcessingUnit imageProcessingUnit = new ImageProcessingUnit();
			boolean protocolModifiedFlag = true;
			
			try {
//				if(!fullRun){
					imageProcessingUnit.processImage(RESULT_PATH + name);
					System.out.println("Image processed");
					edgeIdMatrix = imageProcessingUnit.getEdgeIdMatrix();
					secretionMap = imageProcessingUnit.getSecretionMap();
	
					System.out.println(edgeIdMatrix.length + " " + edgeIdMatrix[0].length);
					saveImages(imageProcessingUnit, RESULT_PATH + name + POVRAY_PATH);
//				}
				if(fullRun) {
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
					if(NodeMergerTileSize <24){
						NodeMergerTileSize++;
						System.out.println("Starting phase one with nodeMergerTileSize = "+ NodeMergerTileSize);
						startFirstPhase();
					}else finished = true;
				}else{
					System.out.println("Couldn't find a solution to this problem! :(");
					System.out.println("fullRun: "+ fullRun );
					System.out.println("NodeMergerOptimizer: "+ nodeMergerOptimizer);
					System.out.println("NodeMergerTileSize: "+ NodeMergerTileSize);
					fullRun = false;
					nodeMergerOptimizer = false;
					NodeMergerTileSize = 1;
					finished = true;
					return;
				}
			}
			if(finished) return;
			if(!fullRun){
				System.out.println("Setting fullRun to true...");
				fullRun = true;
				startFirstPhase();
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

		private void runSecondPhase(String name) {
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

		private void saveImages(ImageProcessingUnit imageProcessingUnit, String path) {
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
