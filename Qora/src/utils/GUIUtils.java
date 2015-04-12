package utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class GUIUtils {

	public static ImageIcon createIcon(Color colorOfIcon, Color backgroundColor)
	{
		//CREATE IMAGE
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        
        //AA
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        //SET COLOR
        g.setColor(colorOfIcon);
        
        //CREATE CIRCLE
        g.fillOval(0, 0, 16, 16);
        
        //SET BACKGROUND
        g.setBackground(backgroundColor);     
        
        //CONVERT TO ICON
        return new ImageIcon(image);
	}
}
