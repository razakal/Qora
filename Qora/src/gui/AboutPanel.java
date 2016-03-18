package gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class AboutPanel extends JPanel 
{
    private BufferedImage image;
    
	private static final Logger LOGGER = Logger.getLogger(AboutPanel.class);
    public AboutPanel() {
       try {                
          image = ImageIO.read(new File("images/about.png"));
       } catch (IOException ex) {
    	   LOGGER.error(ex);
       }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);          
    }
}
