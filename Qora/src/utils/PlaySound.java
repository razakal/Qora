package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class PlaySound {
	private static PlaySound instance;
	private ArrayList<byte[]> transactionsAlreadyPlayed;
	
	public static PlaySound getInstance()
	{
		if(instance == null)
		{
			instance = new PlaySound();
		}
		
		return instance;
	}
	
	public PlaySound()
	{
		transactionsAlreadyPlayed = new ArrayList<byte[]>();
	}
	
	public void playSound(final String url, byte[] signature) {
		
		boolean is = false;
		
		for (byte[] transactionSign : transactionsAlreadyPlayed) {
			if(Arrays.equals(transactionSign, signature))
			{
				is = true;
				break;
			}
		}
		
		if(!is)
		{
			transactionsAlreadyPlayed.add(0, signature);	
		
			new Thread(new Runnable() {
				public void run() {
					File soundFile = new File( "sounds/" + url ); 
					try (AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);) {
			    	    
			    	    
			    	    
			    	    Clip clip = AudioSystem.getClip();
			    	    
			    	    clip.open(ais);
			    	    
			    	    clip.setFramePosition(0);
			    	    clip.start(); 
	
			    	    Thread.sleep(clip.getMicrosecondLength()/1000);
			    	    clip.stop(); 
			    	    clip.close(); 
			    	} catch(IOException | UnsupportedAudioFileException | LineUnavailableException exc) {
			    	    exc.printStackTrace();
			    	} catch (InterruptedException exc) {}
			    	
			    }
			  }).start();
		}
	}

}
