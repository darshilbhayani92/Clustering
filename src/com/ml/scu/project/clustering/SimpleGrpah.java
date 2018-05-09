package com.ml.scu.project.clustering;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class SimpleGrpah extends JPanel {

	private static final long serialVersionUID = 1L;
	static int[] data ;
	static int[] limit;

	static String lable;
	final int PAD = 20;

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int w = getWidth();
		int h = getHeight();
		
		g2.drawString(lable, 25, 25);

		// Draw ordinate.
		g2.draw(new Line2D.Double(PAD, PAD, PAD, h - PAD));
		// Draw abcissa.
		g2.draw(new Line2D.Double(PAD, h - PAD, w - PAD, h - PAD));
		double xInc = (double) (w - 2 * PAD) / (data.length - 1);
		double scale = (double) (h - 2 * PAD) / getMax();
		// Mark data points.

		for (int i = 0; i < data.length; i++) {

			if(limit.length<=3) {
			if (i <= limit[0])
				g2.setPaint(Color.CYAN);
			else if(i > limit[0] && i<=limit[1])
				g2.setPaint(Color.BLUE);
			else if(i > limit[1] && i<=limit[2])
				g2.setPaint(Color.RED);
			}else {
				g2.setPaint(Color.black);
			}

			double x = PAD + i * xInc;
			double y = h - PAD - scale * data[i];
			
			g2.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
		}
	}

	private int getMax() {
		int max = -Integer.MAX_VALUE;
		for (int i = 0; i < data.length; i++) {
			if (data[i] > max)
				max = data[i];
		}
		return max;
	}

	public void drawGraph() {
		
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new SimpleGrpah());
		f.setSize(400, 400);
		f.setLocation(200, 200);
		f.setVisible(true);
	}
}