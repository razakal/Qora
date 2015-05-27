package gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel 
{
    private BufferedImage image;

    public AboutPanel() {
       try {                
          image = ImageIO.read(new File("images/about.png"));
       } catch (IOException ex) {
    	   
       }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);          
    }
}
