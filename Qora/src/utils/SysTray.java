package utils;

import gui.Gui;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

import controller.Controller;

public class SysTray {

	private static SysTray systray = null;
	private TrayIcon icon = null;
	private PopupMenu createPopupMenu;

	public static SysTray getInstance() {
		if (systray == null) {
			systray = new SysTray();
		}

		return systray;
	}

	public void createTrayIcon() throws HeadlessException,
			MalformedURLException, AWTException, FileNotFoundException {
		if (icon == null) {
			if (!SystemTray.isSupported()) {
				System.out.println("SystemTray is not supported");
			} else {
				createPopupMenu = createPopupMenu();
				TrayIcon icon = new TrayIcon(createImage(
						"images/icons/icon16.png", "tray icon"), "Qora "
						+ Controller.getInstance().getVersion(),
						createPopupMenu);
				

				SystemTray.getSystemTray().add(icon);
				this.icon = icon;
				
				icon.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							Gui.getInstance().bringtoFront();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				});
			}
		}
	}
	
	
	public void sendMessage(String caption, String text, TrayIcon.MessageType messagetype  )
	{
		if(icon != null)
		{
			icon.displayMessage(caption, text,
					messagetype);
		}
	}

	// Obtain the image URL
	private  Image createImage(String path, String description)
			throws MalformedURLException, FileNotFoundException {

		File file = new File(path);

		if (!file.exists()) {
			throw new FileNotFoundException("Iconfile not found: " + path);
		}

		URL imageURL = file.toURI().toURL();
		return (new ImageIcon(imageURL, description)).getImage();
	}

	private PopupMenu createPopupMenu() throws HeadlessException {
		PopupMenu menu = new PopupMenu();

		MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 Controller.getInstance().stopAll();
	              System.exit(0);
			}
		});
		menu.add(exit);

		return menu;
	}
}