package basicProcessing;

import java.io.IOException;
import java.io.PrintWriter;

public class POVRayExecution {
	
	private static final String POVRAY_ENGINE_PATH = "C:\\Program Files\\POV-Ray\\v3.7\\bin\\pvengine.exe";
	private static final String DOUBLE_QUOTE = "\"";

	public static void executer(String a) throws IOException, InterruptedException {
		String filename = DOUBLE_QUOTE + a + DOUBLE_QUOTE;

		String[] command1 = {

				POVRAY_ENGINE_PATH, "/EXIT", "/RENDER", filename, };
		Process process = Runtime.getRuntime().exec(command1);
		while(process.isAlive());
	}
}
