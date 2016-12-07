package skeletonize;

import java.awt.Color;

/*
 * Output:
 *  
 */

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import detailedGraph.CustomizedEdge;
import detailedGraph.CustomizedNode;
import ij.ImagePlus;
import skeleton_analysis.Edge;

public class DisplayGraph extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int scale = 1;
	private static int width,height;
	public List<CustomizedEdge> edges;
	public static List<String> wantedList = new ArrayList<String>();
	private static String savePath;
	
	public void paint(Graphics g) {
		//System.out.println("Edge size: " + edges.size());
		int i = 0;
		 try {
		      // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed
		      // into integer pixels
		      BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

		      Graphics2D ig2 = bi.createGraphics();
		      
		      ig2.setPaint(Color.black);
		      for (CustomizedEdge edge : edges){
					/*if(i++ > 1)
						break;*/
		    	  	//System.out.println(edge.getV1().pointsToString());
					g.drawLine((int) edge.getVertex1().getX()*scale, (int) edge.getVertex1().getY()*scale,
							(int) edge.getVertex2().getX()*scale, (int) edge.getVertex2().getY()*scale);
					//if(wantedList.contains(edge.getLabel())){
						//System.out.println("Id: display: " + edge.getLabel());
						g.drawChars(edge.getLabel().toCharArray(), 0, edge.getLabel().toCharArray().length, midPointX(edge.getVertex1(), edge.getVertex2())*scale, midPointY(edge.getVertex1(), edge.getVertex2())*scale);
				//	}
					ig2.drawLine((int) edge.getVertex1().getX()*scale, (int) edge.getVertex1().getY()*scale,
							(int) edge.getVertex2().getX()*scale, (int) edge.getVertex2().getY()*scale);
				}
		      

		      ImageIO.write(bi, "PNG", new File(savePath));
		    } catch (IOException ie) {
		      ie.printStackTrace();
		    }
		 wantedList = new ArrayList<String>();
	}
	
	public static int midPointX(CustomizedNode node1, CustomizedNode node2) {
	     return (node1.getX() + node2.getX())/2;
	}
	
	public static int midPointY(CustomizedNode node1, CustomizedNode node2) {
	     return (node1.getY() + node2.getY())/2;
	}

	public static void draw(List<CustomizedEdge> edges, ImagePlus imp, String savePath) {
		DisplayGraph.savePath = savePath;
		DisplayGraph displayGraph = new DisplayGraph();
		displayGraph.edges = edges;
		displayGraph.setOpaque(false);
		JFrame frame = new JFrame();
		frame.getContentPane().add(displayGraph);
		width = imp.getWidth()*scale;
		height = imp.getHeight()*scale;
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(imp.getWidth()*scale, imp.getHeight()*scale);
		frame.getContentPane().setBackground(Color.WHITE);
		frame.setVisible(true);
	}
}