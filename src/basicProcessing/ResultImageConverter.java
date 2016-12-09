package basicProcessing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import utils.LogFile;

public class ResultImageConverter {

	//private static final String RESULT_PATH = "C:\\Delin\\Updated Workspace\\iDynoMiCS\\resultss";

	public static String convertLastPOVtoPNG(String result_path) throws InterruptedException, IOException {
		
		/*File file = new File(result_path);
		String[] names = file.list();
		Arrays.sort(names);
		
		// reading the latest contact folder
		String name = names[names.length - 1];
		System.out.println(name);
*/
		
		LogFile.write("Replacing the last POV file to PNG. ");
		String zipPath = result_path + "\\povray.zip";
		LogFile.write("Zip file path: "+ zipPath);
		String extractedPath = result_path + "\\povray";
		LogFile.write("Result path: "+ extractedPath);
		System.out.println(zipPath);
		/*
		 *
		 * replace header file in the povray folder with new header with the
		 * light and reflection parameters deleted
		 */
		POVRayCleanHeaderSetter.copyHeaderTo(extractedPath);
		LogFile.write("New header file copied to "+ extractedPath);

		/* Get the newest file for a specific extension */
		ZipFile zipFile = new ZipFile(zipPath);

	    Enumeration<? extends ZipEntry> entries = zipFile.entries();
	    List<ZipEntry> povZipEntryList = new ArrayList<ZipEntry>();
	    while(entries.hasMoreElements()){
	        ZipEntry entry = entries.nextElement();
	        povZipEntryList.add(entry);
	    }
	    
		String finalPath = "";
		if (povZipEntryList.size() > 0) {
			/** The newest file comes first **/
			povZipEntryList.sort(new Comparator<ZipEntry>() {

				@Override
				public int compare(ZipEntry o1, ZipEntry o2) {
					if(o1.getTime() > o2.getTime())
						return 1;
					else
						return 0;
				}
		    	
			});
		    String lastFileName = povZipEntryList.get(0).getName();
		    LogFile.write("Last file name: "+ lastFileName+ " which will be used for image processing");
		    System.out.println(lastFileName);
		    
		    
		    InputStream initialStream = zipFile.getInputStream(povZipEntryList.get(0));
		    finalPath = extractedPath + "\\" + lastFileName;
			final Path destination = Paths.get(finalPath);
		    Files.copy(initialStream, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		    POVRayExecution.executer(finalPath);
		}
		zipFile.close();
		return finalPath.replaceAll("\\.pov", ".png");
	}
	
public static void main(String[] args) throws InterruptedException, IOException {
	System.out.println(ResultImageConverter.convertLastPOVtoPNG(""));
}
}
