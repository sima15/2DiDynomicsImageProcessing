package basicProcessing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * This code try to join two BufferedImage
 * 
 * @author wangdq 2013-12-29
 * 
 */

public class SetComplexityFinder {
	
	public static Map<Integer, Long> cValue = new HashMap<Integer, Long>();
	public static Map<String, Long> ccValue = new HashMap<String, Long>();
	public static Map<String, Double> NCDValue = new HashMap<String, Double>();

	static List<File> FileNames = new ArrayList<File>();
	static List<File> CombinedFileNames = new ArrayList<File>();
	static int count = 0;

	public static double calcuate() throws Exception

	{	String filepath=ResultImageConverter.convertLastPOVtoPNG("");
/*	for(int i=0;i<4;i++)
	{
		 

		filepath[i]=filepath[i].replace(".pov", ".png");
		
		// 
		WriteToFile.write(filepath[i]);

		fileSet1[i]= new File(filepath[i]);
		fileSet2[i]= new File(filepath[i]);

		
	}*/


		// double[] C = new double[1000];//TO STORE C(i)
		//final File dir = new File(JgapConstants.FILE_PATH);
		// File dir2 = new File(JgapConstants.FILE_PATH);
	//	final String[] EXTENSIONS = new String[] { "png" // and other formats
															// you need
		//};
		//??@Override
			/*public boolean accept(final File dir, final String name) {
				for (final String ext : EXTENSIONS) {
					if (name.endsWith("." + ext)) {
						return (true);
					}
				}
				return (false);
			}
		};
fileSet1
		if (dir.isDirectory()) { // make sure it's a directory
			for (final File f1 : dir.listFiles(IMAGE_FILTER)) {
				// 
				FileNames.add(f1);
			}
		}*/
		/*for (int i = 0; i < 4; i++) {
			BufferedImage img1 = null;
			img1 = ImageIO.read(fileSet1[i]);
			ImageIO.write(img1
					, "png", new File("D:\\ImageSeries\\"+SysTime.time()+".png"));
			
			BufferedImage bufferedImage = ImageIO.read(fileSet1[i]);
			//BufferedImage bufferedIm = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(),
					//BufferedImage.TYPE_BYTE_BINARY);
			//Erode and dilate Image to get smooth curves
			BufferedImage bufferedIm =ImageErosion.imageErode(
			ImageErosion.imageErode(
            ImageDilation.imageDilation(
	        ImageErosion.imageErode(bufferedImage))));
			
			Graphics2D graphics = bufferedIm.createGraphics();
			graphics.drawImage(bufferedIm, 0, 0, null);
			String Filenam = /JgapConstants.COMPRESSED_IMAGE_FILE_PATH + "//" + i + ".png";
			
			ImageIO.write(bufferedIm
					, "png", new File(Filenam));
			ImageIO.write(bufferedIm
					, "png", new File("D:\\ImageSeries\\"+SysTime.time()+".png"));
			File file = new File(Filenam);
			cValue.put(i, file.length());
			//  
			// ImageIO.write(bufferedIm, "png", new File(Filenam));
			//if (i < FileNames.size() - 3) {
				for (int j = 0; j < 4; j++)

				{
					if(i!=j)
					{
					BufferedImage img2 = null;
					img2 = ImageIO.read(fileSet1[j]);

					BufferedImage joinedImg1 = joinBufferedImage(img1, img2);
					BufferedImage joinedImg =ImageErosion.imageErode(
							ImageErosion.imageErode(
				            ImageDilation.imageDilation(
					        ImageErosion.imageErode(joinedImg1))));
					// change to i_j if needed
					// String
					// joinedFilename=JgapConstants.COMBINED_IMAGE_FILE_PATH+"//"+FileNames[i].getName()+"_"+FileNames[j].getName();
					String joinedFilename = JgapConstants.COMBINED_COMPRESSED_IMAGE_FILE_PATH + "//" + i + "+" + j
							+ ".png";

					boolean suc = ImageIO.write(joinedImg, "png", new File(joinedFilename));
					//ImageIO.write(joinedImg
							//, "png", new File("D:\\ImageSeries\\"+SysTime.time()+".png"));
					// ImageIO.write(bufferedIm, "png", new File(Filenam));

					File file1 = new File(joinedFilename);
					ccValue.put(i + "+" + j, file1.length());
					//  

					// System.out.println("saved success? "+i+"+"+j+"
					// "+NCDValue.get(i+"+"+j));

				}
			}
		}

		// 
		ncdCalculator();
*/		return setcomplexityCalculator();
		//return count;
	}

	private static void ncdCalculator() {
		double ncd = 0;
		for (int i = 0; i < 4 ; i++) {

			for (int j = 0; j < 4; j++) {
				// 
				// 

			//	 
                if(i!=j)
                {

				double combineValue = (ccValue.get(i + "+" + j));
				double minValue = Math.min(cValue.get(i), cValue.get(j));
				double maxValue = Math.max(cValue.get(i), cValue.get(j));
				ncd = (combineValue - minValue) / maxValue;
				// 
				NCDValue.put(i + "+" + j, ncd);

			}}
		}

	}

	private static double setcomplexityCalculator() {
		double setComplexity;
		double sum,sc;
		//for (int t = 0; t < 4; t++) {
			
			sc=0;
			for (int i=0; i<4; i++)
			{
				sum=0;
			
				for (int j=0; j<4; j++)
				{
                if(i!=j)
              {
                	int x,y,u,v;
                	x=i;
                	y=j;
                	// 
                	// 
                	// 

                	u=Math.min(x,y);
                	v=Math.max(x,y);


                	// 
                	// 

                	
                	sum+=(NCDValue.get(u+"+"+v)*(1-(NCDValue.get(u+"+"+v))));
              }}
				// 
                sc+=cValue.get(i)*sum;
	
            }
				setComplexity=sc/4*(4-1);
				 
				return setComplexity;
			}
			

		
		
	
	

	/**
	 * join two BufferedImage you can add a orientation parameter to control
	 * direction you can use a array to join more BufferedImage
	 */

	public static BufferedImage joinBufferedImage(BufferedImage img1, BufferedImage img2) {

		// do some calculate first
		int offset = 5;
		int wid = img1.getWidth() + img2.getWidth() + offset;
		int height = Math.max(img1.getHeight(), img2.getHeight()) + offset;
		// create a new buffer and draw two image into the new image
		// compression............
		BufferedImage newImage = new BufferedImage(wid, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = newImage.createGraphics();
		Color oldColor = g2.getColor();
		// fill background
		g2.setPaint(Color.WHITE);
		g2.fillRect(0, 0, wid, height);
		// draw image
		g2.setColor(oldColor);
		g2.drawImage(img1, null, 0, 0);
		g2.drawImage(img2, null, img1.getWidth() + offset, 0);
		g2.dispose();
		// compress.................
		// BufferedImage bufferedIm = new BufferedImage(newImage.getWidth(),
		// newImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		return newImage;
	}
}