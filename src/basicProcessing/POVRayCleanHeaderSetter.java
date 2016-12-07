package basicProcessing;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;

public class POVRayCleanHeaderSetter {

	private static final String REQUIRED_FILE_DIRECTORY = "src\\lib";
	static String INC_EXT = ".inc";

	public static void copyHeaderTo(String povRayExtractedPath){
		new POVRayCleanHeaderSetter().copy(povRayExtractedPath);
	}
	
	public void copy(String povRayExtractedPath) {
		String dir = povRayExtractedPath;
		File source = new File(REQUIRED_FILE_DIRECTORY);
		File dest = new File(dir);
		try {
			FileFilter filter = (FileFilter) new GenericExtFilter(INC_EXT);
			FileUtils.copyDirectory(source, dest, filter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// inner class, generic extension filter
	public class GenericExtFilter implements FileFilter {

		private String ext;

		public GenericExtFilter(String ext) {
			this.ext = ext;
		}

		@Override
		public boolean accept(File pathname) {
			// TODO Auto-generated method stub
			return (pathname.getName().endsWith(ext));
		}
	}
}
