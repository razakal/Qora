package gui.create;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Charsets;

import controller.Controller;
import lang.Lang;
import lang.LangFile;
import settings.Settings;
import utils.SaveStrToFile;

@SuppressWarnings("serial")
public class SettingLangFrame extends JDialog {
	
	private  JList<LangFile> listLang;
	
	public SettingLangFrame()
	{
		super();
		this.setTitle("Qora" + " - " + "Language select");
		this.setModal(true);

		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
        GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {10, 300, 10};
		gridBagLayout.rowHeights = new int[]{20, 200, 20, 10};
	     
		//LAYOUT
		this.setLayout(gridBagLayout);
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.fill = GridBagConstraints.BOTH;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.gridy = 0;	
		labelGBC.gridx = 1;
		
		//LANGS GBC
		GridBagConstraints listLangGBC = new GridBagConstraints();
		listLangGBC.insets = new Insets(0,0,5,0);
		listLangGBC.fill = GridBagConstraints.BOTH;  
		listLangGBC.anchor = GridBagConstraints.NORTHWEST;
		listLangGBC.gridy = 1;	
		listLangGBC.gridx = 1;	
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(0,0,0,0);
		buttonGBC.fill = GridBagConstraints.BOTH;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridy = 2;	
		buttonGBC.gridx = 1;		
		
        JLabel labelSelect = new JLabel("Language:");
		this.add(labelSelect, labelGBC);	

      	// read internet 
        String stringFromInternet = "";
		try {
			String url = Lang.translationsUrl + Controller.getInstance().getVersion().replace(" ", "%20") + "/available.json";

			URL u = new URL(url);
			InputStream in = u.openStream();
			stringFromInternet = IOUtils.toString(in, Charsets.UTF_8);
		} catch (Exception e1) {
			e1.printStackTrace();
			stringFromInternet = "";
		}
		
		JSONObject inernetLangsJSON = (JSONObject) JSONValue.parse(stringFromInternet);

		DefaultListModel<LangFile> listModel = new DefaultListModel<LangFile>();
		listModel.addElement( new LangFile() );
		if(inernetLangsJSON != null && !inernetLangsJSON.isEmpty()) 
		{
			for (Object internetKey : inernetLangsJSON.keySet()) {
				JSONObject internetValue = (JSONObject) inernetLangsJSON.get(internetKey);
				listModel.addElement( new LangFile((String)internetValue.get("_lang_name_"), (String)internetValue.get("_file_"), (Long)internetValue.get("_timestamp_of_translation_")) ); 
			}
		}
		
		listLang = new JList<LangFile>(listModel);
		listLang.setSelectedIndex(0);
		listLang.setFocusable(false);
		
	    JScrollPane scrollPaneLang = new JScrollPane(listLang);
	    
		this.add(scrollPaneLang, listLangGBC);	
		
		if(inernetLangsJSON == null || inernetLangsJSON.isEmpty()) 
		{
			onOKClick();
			return;
		}
		
        //BUTTON OK
	    JButton nextButton = new JButton("OK");
	    nextButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onOKClick();
				}
			});	
	  	this.add(nextButton, buttonGBC);
	  		      
	  	//CLOSE NICELY
	    this.addWindowListener(new WindowAdapter()
	    {
	    	public void windowClosing(WindowEvent e)
	        {
	    		Controller.getInstance().stopAll();
	    		System.exit(0);
	        }
	    });
	    
      	this.pack();
	    this.setResizable(false);
	    this.setLocationRelativeTo(null);
	    this.setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
	    this.setVisible(true);
	}
	
	@SuppressWarnings("unchecked")
	public void onOKClick()
	{
		try {
			String langFileName = listLang.getSelectedValue().getFileName();
			
			if(listLang.getSelectedIndex() > 0) 
			{
				String url = Lang.translationsUrl + Controller.getInstance().getVersion().replace(" ", "%20") + "/languages/" + langFileName;
				FileUtils.copyURLToFile(new URL(url), new File(Settings.getInstance().getLangDir(), langFileName));
			}
			JSONObject settingsLangJSON = Settings.getInstance().Dump();
			settingsLangJSON.put("lang", langFileName);
			SaveStrToFile.saveJsonFine(Settings.getInstance().getSettingsPath(), settingsLangJSON);
			Settings.FreeInstance();
			Lang.getInstance().loadLang();
		}catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(
				new JFrame(), "Error writing to the file: "
						+ "\nProbably there is no access.",
						"Error!",
						JOptionPane.ERROR_MESSAGE);
		}
		
		this.dispose();
	}
}