package basicProcessing;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
/**
* <The class Dilation using dilation algorithm to expand the binary image and then to complete lattices.>
* Copyright (C) <2008> <Chung Yuan Su>
* 
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
* 
* contact : x081173@yahoo.com.tw
*/
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageDilation {

	// public static void main(String args[]) throws IOException{

	public static BufferedImage imageDilation(BufferedImage image) throws IOException {

		// File file=new File("C:\\Users\\Honey\\Desktop\\New folder
		// (2)\\d2.png");
		// BufferedImage image=ImageIO.read(file);
		BufferedImage dilatedImage = dilate(image, image.getHeight(), image.getWidth());
		// DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		// get current date time with Date()
		// Date date = new Date(0);

		// String name=String.valueOf(dateFormat.format(date));

		// String formatName = "D:\\ImageDilation\\"+SysTime.time()+".png";
		// String formatName = "D:\\ImageErosion\\"+name+".png";

		// String formatName= "C:\\Users\\Honey\\Desktop\\New folder
		// (2)\\d3.png";
		// boolean suc=ImageIO.write(dilatedImage,"png",new File(formatName));
		System.out.println("Dilate");
		return dilatedImage;

	}

	public static BufferedImage dilate(BufferedImage bimage, int height, int width) {
		BufferedImage dil = new BufferedImage(bimage.getWidth(), bimage.getHeight(), bimage.getType());

		Kernel kernel = new Kernel(3, 3, new float[] { 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f });
		ConvolveOp op = new ConvolveOp(kernel);
		op.filter(bimage, dil);

		return dil;

	}
}
