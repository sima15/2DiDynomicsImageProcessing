package basicProcessing;


import java.awt.image.BufferedImage;

public class ImageErosion {
	//public static void main(String args[]) throws IOException{
	
    public static BufferedImage imageErode(BufferedImage src) throws Exception {
      // File file=new File("C:\\Users\\Honey\\Desktop\\New folder (2)\\e2.png");
       //BufferedImage src=ImageIO.read(file);
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        final int BLACK = 0xFF000000;
        final int WHITE = 0xFFFFFFFF;
        for (int x = 2; x < w - 2; x++) {
            for (int y = 2; y < h - 2; y++) {
                // System.out.println(Integer.toHexString(src.getRGB(x, y)));
                // a cross-like kernel (left, right, top, bottom)
                if (   src.getRGB(x - 1, y) != WHITE
                    || src.getRGB(x + 1, y) != WHITE 
                    || src.getRGB(x, y - 1) != WHITE 
                    || src.getRGB(x, y + 1) != WHITE
                    || src.getRGB(x - 2, y) != WHITE
                    || src.getRGB(x + 2, y) != WHITE 
                    || src.getRGB(x, y - 2) != WHITE 
                    || src.getRGB(x, y + 2) != WHITE
//                    || src.getRGB(x - 3, y) != WHITE
//                    || src.getRGB(x + 3, y) != WHITE 
//                    || src.getRGB(x, y - 3) != WHITE 
//                    || src.getRGB(x, y + 3) != WHITE
                ) {
                    // if one neighbor pixel is black, make this one black too
                    dest.setRGB(x, y, BLACK);
                } else {
                    dest.setRGB(x, y, WHITE);
                }
            }
        }
       
        
        
		//String formatName = "D:\\ImageDilation\\"+name+".png";
		//String formatName = "D:\\ImageDilation\\"+SysTime.time()+".png";
        //String formatName= "C:\\Users\\Honey\\Desktop\\New folder (2)\\e3.png";

		System.out.println("erode");


	 	  // System.out.println(formatName);


        //boolean suc=ImageIO.write(dest,"png",new File(formatName));
return dest;

        /*JFrame f = new JFrame("Erosion");
        f.getContentPane().add(new JLabel(new ImageIcon(src)), BorderLayout.CENTER);
        f.getContentPane().add(new JLabel(new ImageIcon(dest)), BorderLayout.EAST);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);*/
    }
}