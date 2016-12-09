package simulation;


import java.io.File;
import java.io.IOException;
import java.util.Map;

import basicProcessing.POVRayExecution;
import idyno.Idynomics;
import ij.IJ;
import ij.WindowManager;
import skeletonize.DisplayGraph;

public class MyController {

//	private static final String RESULT_PATH = "C:\\Sima\\Updated Workspace\\iDynoMiCS\\resultss\\experiments\\";
//	private static final String PROTOCOL_PATH = "C:\\Sima\\Updated Workspace\\iDynoMiCS\\protocols\\experiments\\";

		private final String CONSOLIDATE_SOLUTE_CONCENTRATION_PATH = "\\SoluteConcentration\\Consolidated.txt";
		private final String SOLUTE_CONCENTRATION_PATH = "\\SoluteConcentration\\xy-1\\";
		private final String POVRAY_PATH = "\\povray\\";
		private static String protocol_xml;
		private final String RESULT_PATH = "E:\\eclipse workspace\\iDynoMiCS\\resultss\\experiments\\";
		private final String PROTOCOL_PATH = "E:\\eclipse workspace\\iDynoMiCS\\protocols\\experiments\\";
		
		
		private final boolean speciesOptimizer = true;
		private boolean nodeMergerOptimizer = false;
		private int NodeMergerTileSize = 1;
		private boolean ratioLessThanFifty = true;
		private boolean fullRun = false;
		private boolean finished;
//		public static String name = "Vascularperc30-quartSize(20161203_1647)";
		public static String name;
		
		private String totalProduct = "-100";
		static int[][] edgeIdMatrix = null ;
		static Map<String, Double> secretionMap = null ;
		private ImageProcessingUnit imageProcessingUnit;
		
		public MyController(String n){
			name = n;
			System.out.println("Name of folder in Controller: " + name);
		}
		
		public static void main(String[] args) throws Exception {
			MyController controller = new MyController(name);
			controller.verifyConditions();
			System.out.println("I'm back");
			controller.resetParams();
		}
		
		/**
		 * Verifies if this file can be fully processed to output product amount
		 * @throws Exception
		 */
		public void verifyConditions() throws Exception {
			System.out.println("Current thread: " + Thread.currentThread().getName());
//			System.out.println(Runtime.getRuntime().totalMemory());

			// reading the latest folder in the resultss directory
			System.out.println("File name: "+ name);
			protocol_xml = "\\Vascularperc30-quartSize.xml";
			imageProcessingUnit = new ImageProcessingUnit();
			boolean protocolModifiedFlag = true;
			boolean ProcessSuccess; 
			try {
				ProcessSuccess = imageProcessingUnit.processImage(RESULT_PATH + name);
				if(ProcessSuccess){
					System.out.println("Image processed");
					edgeIdMatrix = imageProcessingUnit.getEdgeIdMatrix();
					secretionMap = imageProcessingUnit.getSecretionMap();
	
					System.out.println(edgeIdMatrix.length + " " + edgeIdMatrix[0].length);
					saveImages(imageProcessingUnit, RESULT_PATH + name + POVRAY_PATH);
					if(fullRun) {
						runFirst1Phase();
						runSec1Phase();
					}else{
						startFullRun();
					}
			   }else if(!fullRun){
				    System.out.println("Inside try block of myController");
					System.out.println("Current thread: " + Thread.currentThread().getName());
					System.out.println("fullRun: "+ fullRun );
					System.out.println("NodeMergerOptimizer: "+ nodeMergerOptimizer);
					System.out.println("NodeMergerTileSize: "+ NodeMergerTileSize);
					nodeMergerOptimizer = true;
					finished = true;
					return;
			   }
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Bad Network");
				if(!fullRun){
					System.out.println("Inside exception handler of myController");
					System.out.println("Current thread: " + Thread.currentThread().getName());
					System.out.println("fullRun: "+ fullRun );
					System.out.println("NodeMergerOptimizer: "+ nodeMergerOptimizer);
					System.out.println("NodeMergerTileSize: "+ NodeMergerTileSize);
					nodeMergerOptimizer = true;
					if(NodeMergerTileSize <3){
						NodeMergerTileSize++;
						System.out.println("Starting verification with nodeMergerTileSize = "+ NodeMergerTileSize);
						verifyConditions();
					}else {
						finished = true;
						return;
					}
				}else{
					System.out.println("Couldn't find a solution to this problem! :(");
					System.out.println("Current thread: " + Thread.currentThread().getName());
					System.out.println("fullRun: "+ fullRun );
					System.out.println("NodeMergerOptimizer: "+ nodeMergerOptimizer);
					System.out.println("NodeMergerTileSize: "+ NodeMergerTileSize);
//					resetParams();
					finished = true;
					return;
				}
			}
			
			if(finished){
				System.out.println("Inside if block! finished = true");
				System.out.println("Current thread: " + Thread.currentThread().getName());
//				resetParams();
				return;
			}
		}
		
		/**
		 * Does the first image processing actions needed
		 * @throws IOException
		 * @throws InterruptedException
		 */
		public void runFirst1Phase() throws IOException, InterruptedException{
			System.out.println("Current thread: " + Thread.currentThread().getName());
			ProtocolModifier protocolModifier = new OptimizedProtocolModifier(edgeIdMatrix, 
					secretionMap, protocol_xml);
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
		
		/**
		 * Runs the first image processing phase completely
		 * @throws Exception
		 */
		public void startFullRun() throws Exception{
				System.out.println("Setting fullRun to true...");
				System.out.println("Current thread: " + Thread.currentThread().getName());
				fullRun = true;
				verifyConditions();
		}
		
		public void runSec1Phase() throws IOException, InterruptedException{
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
			System.out.println("Current thread: " + Thread.currentThread().getName());
			POVRayExecution.executer(RESULT_PATH + name + POVRAY_PATH + pov);
			totalProduct =  Test.consolidateSoluteConcentrations(RESULT_PATH, name);
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
		
		public void resetParams(){
			fullRun = false;
			nodeMergerOptimizer = false;
			NodeMergerTileSize = 1;
			finished = false;
		}
		
		public double getProduct() {
			if(totalProduct == "-100")
				return 0;
			return Double.parseDouble(totalProduct);
		}
		
//		public void setFullRun(){
//			fullRun = true;
//		}
//		
//		public void resetFullRun(){
//			fullRun = false;
//		}

}
