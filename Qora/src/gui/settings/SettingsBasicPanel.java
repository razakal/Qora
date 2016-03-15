package gui.settings;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Charsets;

import controller.Controller;
import lang.Lang;
import settings.Settings;
import utils.DateTimeFormat;
import lang.LangFile;

@SuppressWarnings("serial")
public class SettingsBasicPanel extends JPanel 
{
	public JTextField txtRpcPort;
	public JTextField txtWebport;
	public JTextField textDataFolder;
	public JTextField textWallet;
	public JCheckBox chckbxGuiEnabled;
	public JCheckBox chckbxKeyCaching;
	public JCheckBox chckbxRpcEnabled;
	public JCheckBox chckbxWebEnabled;
	public JCheckBox chckbxSoundNewTransaction; 
	public JCheckBox chckbxSoundReceiveMessage;
	public JCheckBox chckbxSoundReceivePayment;
	public JTextField textMinConnections;
	public JTextField textMaxConnections;
	public JDialog waitDialog;
	public JComboBox<LangFile> cbxListOfAvailableLangs;
	public JButton btnLoadNewLang;
	
	public SettingsBasicPanel()
	{
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 5, 5, 10));
        
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowHeights = new int[] {30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 0, 30, 40};
        //gridBagLayout.columnWidths = new int[] {40, 70, 92, 88, 92, 30, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        setLayout(gridBagLayout);
        
        chckbxGuiEnabled = new JCheckBox(Lang.getInstance().translate("GUI enabled"));
        chckbxGuiEnabled.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxGuiEnabled.setSelected(Settings.getInstance().isGuiEnabled());
        GridBagConstraints gbc_chckbxGuiEnabled = new GridBagConstraints();
        gbc_chckbxGuiEnabled.fill = GridBagConstraints.BOTH;
        gbc_chckbxGuiEnabled.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxGuiEnabled.gridwidth = 4;
        gbc_chckbxGuiEnabled.gridx = 1;
        gbc_chckbxGuiEnabled.gridy = 0;
        add(chckbxGuiEnabled, gbc_chckbxGuiEnabled);
        
        JLabel lblGUIExplanatoryText = new JLabel(Lang.getInstance().translate("Enable/Disable the Graphical User Interface."));
        lblGUIExplanatoryText.setVerticalAlignment(SwingConstants.TOP);
        lblGUIExplanatoryText.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_lblExplanatoryText = new GridBagConstraints();
        gbc_lblExplanatoryText.fill = GridBagConstraints.BOTH;
        gbc_lblExplanatoryText.insets = new Insets(0, 0, 5, 5);
        gbc_lblExplanatoryText.gridwidth = 4;
        gbc_lblExplanatoryText.gridx = 1;
        gbc_lblExplanatoryText.gridy = 1;
        add(lblGUIExplanatoryText, gbc_lblExplanatoryText);
        
        chckbxRpcEnabled = new JCheckBox(Lang.getInstance().translate("RPC enabled"));
        chckbxRpcEnabled.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxRpcEnabled.setSelected(Settings.getInstance().isRpcEnabled());
        GridBagConstraints gbc_chckbxRpcEnabled = new GridBagConstraints();
        gbc_chckbxRpcEnabled.gridwidth = 2;
        gbc_chckbxRpcEnabled.fill = GridBagConstraints.BOTH;
        gbc_chckbxRpcEnabled.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxRpcEnabled.gridx = 1;
        gbc_chckbxRpcEnabled.gridy = 2;
        add(chckbxRpcEnabled, gbc_chckbxRpcEnabled);
        
        JLabel lblRpcPort = new JLabel(Lang.getInstance().translate("RPC port:"));
        GridBagConstraints gbc_lblRpcPort = new GridBagConstraints();
        gbc_lblRpcPort.anchor = GridBagConstraints.EAST;
        gbc_lblRpcPort.insets = new Insets(0, 0, 5, 5);
        gbc_lblRpcPort.gridx = 3;
        gbc_lblRpcPort.gridy = 2;
        add(lblRpcPort, gbc_lblRpcPort);
        
        txtRpcPort = new JTextField();
        txtRpcPort.setText(Integer.toString(Settings.getInstance().getRpcPort()));
        txtRpcPort.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_txtRpcPort = new GridBagConstraints();
        gbc_txtRpcPort.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtRpcPort.anchor = GridBagConstraints.WEST;
        gbc_txtRpcPort.insets = new Insets(0, 0, 5, 5);
        gbc_txtRpcPort.gridx = 4;
        gbc_txtRpcPort.gridy = 2;
        add(txtRpcPort, gbc_txtRpcPort);
        txtRpcPort.setColumns(10);
        
        JLabel lblRPCExplanatoryText = new JLabel(Lang.getInstance().translate("Enable/Disable API calls using the given port."));
        lblRPCExplanatoryText.setVerticalAlignment(SwingConstants.TOP);
        lblRPCExplanatoryText.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_lblAnExplanatoryText_1 = new GridBagConstraints();
        gbc_lblAnExplanatoryText_1.fill = GridBagConstraints.BOTH;
        gbc_lblAnExplanatoryText_1.insets = new Insets(0, 0, 5, 0);
        gbc_lblAnExplanatoryText_1.gridwidth = 5;
        gbc_lblAnExplanatoryText_1.gridx = 1;
        gbc_lblAnExplanatoryText_1.gridy = 3;
        add(lblRPCExplanatoryText, gbc_lblAnExplanatoryText_1);
        
        chckbxWebEnabled = new JCheckBox(Lang.getInstance().translate("WEB enabled"));
        chckbxWebEnabled.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxWebEnabled.setSelected(Settings.getInstance().isWebEnabled());
        GridBagConstraints gbc_chckbxWebEnabled = new GridBagConstraints();
        gbc_chckbxWebEnabled.gridwidth = 2;
        gbc_chckbxWebEnabled.fill = GridBagConstraints.BOTH;
        gbc_chckbxWebEnabled.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxWebEnabled.gridx = 1;
        gbc_chckbxWebEnabled.gridy = 4;
        add(chckbxWebEnabled, gbc_chckbxWebEnabled);
        
        JLabel lblWebPort = new JLabel(Lang.getInstance().translate("WEB port:"));
        GridBagConstraints gbc_lblWebPort = new GridBagConstraints();
        gbc_lblWebPort.anchor = GridBagConstraints.EAST;
        gbc_lblWebPort.insets = new Insets(0, 0, 5, 5);
        gbc_lblWebPort.gridx = 3;
        gbc_lblWebPort.gridy = 4;
        add(lblWebPort, gbc_lblWebPort);
        
        txtWebport = new JTextField();
        txtWebport.setText(Integer.toString(Settings.getInstance().getWebPort()));
        txtWebport.setHorizontalAlignment(SwingConstants.LEFT);
        txtWebport.setColumns(10);
        GridBagConstraints gbc_txtWebport = new GridBagConstraints();
        gbc_txtWebport.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtWebport.anchor = GridBagConstraints.WEST;
        gbc_txtWebport.insets = new Insets(0, 0, 5, 5);
        gbc_txtWebport.gridx = 4;
        gbc_txtWebport.gridy = 4;
        add(txtWebport, gbc_txtWebport);
        
        JLabel lblWEBExplanatoryText = new JLabel(Lang.getInstance().translate("Enable/Disable decentralized web server. Use \"Access permission\" tab for additional options."));
        lblWEBExplanatoryText.setVerticalAlignment(SwingConstants.TOP);
        lblWEBExplanatoryText.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_lblAnExplanatoryText_2 = new GridBagConstraints();
        gbc_lblAnExplanatoryText_2.fill = GridBagConstraints.BOTH;
        gbc_lblAnExplanatoryText_2.insets = new Insets(0, 0, 5, 0);
        gbc_lblAnExplanatoryText_2.gridwidth = 5;
        gbc_lblAnExplanatoryText_2.gridx = 1;
        gbc_lblAnExplanatoryText_2.gridy = 5;
        add(lblWEBExplanatoryText, gbc_lblAnExplanatoryText_2);
        
        chckbxKeyCaching = new JCheckBox(Lang.getInstance().translate("Generator Key Caching"));
        chckbxKeyCaching.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxKeyCaching.setSelected(Settings.getInstance().isGeneratorKeyCachingEnabled());
        GridBagConstraints gbc_chckbxKeyCaching = new GridBagConstraints();
        gbc_chckbxKeyCaching.fill = GridBagConstraints.BOTH;
        gbc_chckbxKeyCaching.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxKeyCaching.gridwidth = 4;
        gbc_chckbxKeyCaching.gridx = 1;
        gbc_chckbxKeyCaching.gridy = 6;
        add(chckbxKeyCaching, gbc_chckbxKeyCaching);
        
        JLabel lblKeyCachingExplanatoryText = new JLabel(Lang.getInstance().translate("Allows forging even when your wallet is locked. You need to unlock it once."));
        lblKeyCachingExplanatoryText.setVerticalAlignment(SwingConstants.TOP);
        lblKeyCachingExplanatoryText.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_lblAnExplanatoryText_3 = new GridBagConstraints();
        gbc_lblAnExplanatoryText_3.fill = GridBagConstraints.BOTH;
        gbc_lblAnExplanatoryText_3.insets = new Insets(0, 0, 5, 5);
        gbc_lblAnExplanatoryText_3.gridwidth = 5;
        gbc_lblAnExplanatoryText_3.gridx = 1;
        gbc_lblAnExplanatoryText_3.gridy = 7;
        add(lblKeyCachingExplanatoryText, gbc_lblAnExplanatoryText_3);
        
        JLabel lblDataDir = new JLabel(Lang.getInstance().translate("Data dir:"));
        GridBagConstraints gbc_lblDataDir = new GridBagConstraints();
        gbc_lblDataDir.anchor = GridBagConstraints.WEST;
        gbc_lblDataDir.insets = new Insets(0, 0, 5, 5);
        gbc_lblDataDir.gridx = 1;
        gbc_lblDataDir.gridy = 8;
        add(lblDataDir, gbc_lblDataDir);
        
        textDataFolder = new JTextField();
        textDataFolder.setText(Settings.getInstance().getDataDir());
        textDataFolder.setHorizontalAlignment(SwingConstants.LEFT);
        textDataFolder.setColumns(10);
        GridBagConstraints gbc_textDataFolder = new GridBagConstraints();
        gbc_textDataFolder.gridwidth = 2;
        gbc_textDataFolder.insets = new Insets(0, 0, 5, 5);
        gbc_textDataFolder.fill = GridBagConstraints.HORIZONTAL;
        gbc_textDataFolder.gridx = 2;
        gbc_textDataFolder.gridy = 8;
        add(textDataFolder, gbc_textDataFolder);
        
        JButton btnBrowseDataFolder = new JButton(Lang.getInstance().translate("Browse..."));
        GridBagConstraints gbc_btnBrowseDataFolder = new GridBagConstraints();
        gbc_btnBrowseDataFolder.anchor = GridBagConstraints.WEST;
        gbc_btnBrowseDataFolder.insets = new Insets(0, 0, 5, 5);
        gbc_btnBrowseDataFolder.gridx = 4;
        gbc_btnBrowseDataFolder.gridy = 8;
        btnBrowseDataFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();  
                fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileopen.setCurrentDirectory(new File(textDataFolder.getText()));
                int ret = fileopen.showDialog(null, Lang.getInstance().translate("Set data dir"));                
                if (ret == JFileChooser.APPROVE_OPTION) {
                	textDataFolder.setText(fileopen.getSelectedFile().toString());
                }
            }
        });
        add(btnBrowseDataFolder, gbc_btnBrowseDataFolder);
        
        JLabel lblWelletDir = new JLabel(Lang.getInstance().translate("Wallet dir:"));
        GridBagConstraints gbc_lblWelletDir = new GridBagConstraints();
        gbc_lblWelletDir.anchor = GridBagConstraints.WEST;
        gbc_lblWelletDir.insets = new Insets(0, 0, 5, 5);
        gbc_lblWelletDir.gridx = 1;
        gbc_lblWelletDir.gridy = 9;
        add(lblWelletDir, gbc_lblWelletDir);
        
        textWallet = new JTextField();
        textWallet.setText(Settings.getInstance().getWalletDir());
        textWallet.setHorizontalAlignment(SwingConstants.LEFT);
        textWallet.setColumns(10);
        GridBagConstraints gbc_textWallet = new GridBagConstraints();
        gbc_textWallet.gridwidth = 2;
        gbc_textWallet.insets = new Insets(0, 0, 5, 5);
        gbc_textWallet.fill = GridBagConstraints.HORIZONTAL;
        gbc_textWallet.gridx = 2;
        gbc_textWallet.gridy = 9;
        add(textWallet, gbc_textWallet);
        
        JButton btnBrowseWallet = new JButton(Lang.getInstance().translate("Browse..."));
        GridBagConstraints gbc_BrowseWalletbutton = new GridBagConstraints();
        gbc_BrowseWalletbutton.anchor = GridBagConstraints.WEST;
        gbc_BrowseWalletbutton.insets = new Insets(0, 0, 5, 5);
        gbc_BrowseWalletbutton.gridx = 4;
        gbc_BrowseWalletbutton.gridy = 9;
        
        btnBrowseWallet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser();             
                fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileopen.setCurrentDirectory(new File(textWallet.getText()));
                int ret = fileopen.showDialog(null, Lang.getInstance().translate("Set wallet dir"));                
                if (ret == JFileChooser.APPROVE_OPTION) {   
                    textWallet.setText(fileopen.getSelectedFile().toString());
                }
            }
        });
        
        add(btnBrowseWallet, gbc_BrowseWalletbutton);
        
        JLabel lblAnExplanatoryText_4 = new JLabel(Lang.getInstance().translate("The data folder contains blockchain data. The wallet dir contains user specific data."));
        lblAnExplanatoryText_4.setVerticalAlignment(SwingConstants.TOP);
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_1.gridwidth = 4;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 1;
        gbc_lblNewLabel_1.gridy = 10;
        add(lblAnExplanatoryText_4, gbc_lblNewLabel_1);
        
        JLabel lblMinConnections = new JLabel(Lang.getInstance().translate("Min connections:"));
        GridBagConstraints gbc_lblMinConnections = new GridBagConstraints();
        gbc_lblMinConnections.anchor = GridBagConstraints.EAST;
        gbc_lblMinConnections.insets = new Insets(0, 0, 5, 5);
        gbc_lblMinConnections.gridx = 1;
        gbc_lblMinConnections.gridy = 11;
        add(lblMinConnections, gbc_lblMinConnections);
        
        textMinConnections = new JTextField();
        textMinConnections.setText(Integer.toString(Settings.getInstance().getMinConnections()));
        textMinConnections.setHorizontalAlignment(SwingConstants.LEFT);
        textMinConnections.setColumns(10);
        GridBagConstraints gbc_textMinConnections = new GridBagConstraints();
        gbc_textMinConnections.anchor = GridBagConstraints.WEST;
        gbc_textMinConnections.insets = new Insets(0, 0, 5, 5);
        gbc_textMinConnections.fill = GridBagConstraints.HORIZONTAL;
        gbc_textMinConnections.gridx = 2;
        gbc_textMinConnections.gridy = 11;
        add(textMinConnections, gbc_textMinConnections);
        
        JLabel lblMaxConnections = new JLabel(Lang.getInstance().translate("Max connections:"));
        GridBagConstraints gbc_lblMaxConnections = new GridBagConstraints();
        gbc_lblMaxConnections.anchor = GridBagConstraints.EAST;
        gbc_lblMaxConnections.insets = new Insets(0, 0, 5, 5);
        gbc_lblMaxConnections.gridx = 3;
        gbc_lblMaxConnections.gridy = 11;
        add(lblMaxConnections, gbc_lblMaxConnections);
        
        textMaxConnections = new JTextField();
        textMaxConnections.setText(Integer.toString(Settings.getInstance().getMaxConnections()));
        textMaxConnections.setHorizontalAlignment(SwingConstants.LEFT);
        textMaxConnections.setColumns(10);
        GridBagConstraints gbc_textMaxConnections = new GridBagConstraints();
        gbc_textMaxConnections.anchor = GridBagConstraints.WEST;
        gbc_textMaxConnections.insets = new Insets(0, 0, 5, 5);
        gbc_textMaxConnections.fill = GridBagConstraints.HORIZONTAL;
        gbc_textMaxConnections.gridx = 4;
        gbc_textMaxConnections.gridy = 11;
        add(textMaxConnections, gbc_textMaxConnections);
        
        JLabel lbllimitConnections = new JLabel(Lang.getInstance().translate("Allows you to change the amount of simultaneous connections to the server."));
        lbllimitConnections.setVerticalAlignment(SwingConstants.TOP);
        GridBagConstraints gbc_lbllimitConnections = new GridBagConstraints();
        gbc_lbllimitConnections.fill = GridBagConstraints.BOTH;
        gbc_lbllimitConnections.gridwidth = 4;
        gbc_lbllimitConnections.insets = new Insets(0, 0, 0, 5);
        gbc_lbllimitConnections.gridx = 1;
        gbc_lbllimitConnections.gridy = 12;
        add(lbllimitConnections, gbc_lbllimitConnections);
     
        
        JLabel lblSounds = new JLabel(Lang.getInstance().translate("Sounds:"));
        lblSounds.setVerticalAlignment(SwingConstants.TOP);
        GridBagConstraints gbc_lblSounds = new GridBagConstraints();
        gbc_lblSounds.fill = GridBagConstraints.BOTH;
        gbc_lblSounds.gridwidth = 4;
        gbc_lblSounds.insets = new Insets(0, 0, 0, 5);
        gbc_lblSounds.gridx = 1;
        gbc_lblSounds.gridy = 13;
        add(lblSounds, gbc_lblSounds);
         
        chckbxSoundReceivePayment = new JCheckBox(Lang.getInstance().translate("Receive payment"));
        chckbxSoundReceivePayment.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxSoundReceivePayment.setSelected(Settings.getInstance().isSoundReceivePaymentEnabled());
        GridBagConstraints gbc_chckbxSoundReceivePayment = new GridBagConstraints();
        gbc_chckbxSoundReceivePayment.fill = GridBagConstraints.BOTH;
        gbc_chckbxSoundReceivePayment.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxSoundReceivePayment.gridwidth = 1;
        gbc_chckbxSoundReceivePayment.gridx = 1;
        gbc_chckbxSoundReceivePayment.gridy = 14;
        add(chckbxSoundReceivePayment, gbc_chckbxSoundReceivePayment);
        
        chckbxSoundReceiveMessage = new JCheckBox(Lang.getInstance().translate("Receive message"));
        chckbxSoundReceiveMessage.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxSoundReceiveMessage.setSelected(Settings.getInstance().isSoundReceiveMessageEnabled());
        GridBagConstraints gbc_chckbxSoundReceiveMessage = new GridBagConstraints();
        gbc_chckbxSoundReceiveMessage.fill = GridBagConstraints.BOTH;
        gbc_chckbxSoundReceiveMessage.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxSoundReceiveMessage.gridwidth = 1;
        gbc_chckbxSoundReceiveMessage.gridx = 2;
        gbc_chckbxSoundReceiveMessage.gridy = 14;
        add(chckbxSoundReceiveMessage, gbc_chckbxSoundReceiveMessage);

        chckbxSoundNewTransaction = new JCheckBox(Lang.getInstance().translate("Other transactions"));
        chckbxSoundNewTransaction.setHorizontalAlignment(SwingConstants.LEFT);
        chckbxSoundNewTransaction.setSelected(Settings.getInstance().isSoundNewTransactionEnabled());
        GridBagConstraints gbc_chckbxSoundNewTransaction = new GridBagConstraints();
        gbc_chckbxSoundNewTransaction.fill = GridBagConstraints.BOTH;
        gbc_chckbxSoundNewTransaction.insets = new Insets(0, 0, 5, 5);
        gbc_chckbxSoundNewTransaction.gridwidth = 2;
        gbc_chckbxSoundNewTransaction.gridx = 3;
        gbc_chckbxSoundNewTransaction.gridy = 14;
        add(chckbxSoundNewTransaction, gbc_chckbxSoundNewTransaction);
  
        JLabel lblLang = new JLabel(Lang.getInstance().translate("Interface language")+":");
        GridBagConstraints gbc_lblLang = new GridBagConstraints();
        gbc_lblLang.anchor = GridBagConstraints.WEST;
        gbc_lblLang.insets = new Insets(0, 0, 5, 5);
        gbc_lblLang.gridx = 1;
        gbc_lblLang.gridy = 15;
        add(lblLang, gbc_lblLang);
        
        cbxListOfAvailableLangs = new JComboBox<LangFile>();
        
        for (LangFile langFile : Lang.getInstance().getListOfAvailable()) {
        	cbxListOfAvailableLangs.addItem(langFile);
        	
        	if(langFile.getFileName().equals(Settings.getInstance().getLang())) {
        		cbxListOfAvailableLangs.setSelectedItem(langFile);
        	}
		}
        
        btnLoadNewLang = new JButton(Lang.getInstance().translate("Download"));
		GridBagConstraints btn_LoadNewLang = new GridBagConstraints();
		btn_LoadNewLang.anchor = GridBagConstraints.WEST;
		btn_LoadNewLang.insets = new Insets(0, 0, 5, 5);
		btn_LoadNewLang.gridx = 4;
		btn_LoadNewLang.gridy = 15;
		btnLoadNewLang.setPreferredSize(new Dimension(80, 25));
		btnLoadNewLang.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		btnLoadNewLang.setText( "..." );
        		btnLoadNewLang.repaint(0);
        		
        		EventQueue.invokeLater(new Runnable() {
        		    @Override
        		    public void run() {
        		    	try {
	        		    	final JPopupMenu menu = new JPopupMenu();
	        				
	        				String stringFromInternet = "";
	        				try {
	        					String url = Lang.translationsUrl + Controller.getInstance().getVersion().replace(" ", "%20") + "/available.json";

	        					URL u = new URL(url);
	        					InputStream in = u.openStream();
	        					stringFromInternet = IOUtils.toString(in, Charsets.UTF_8);
	        				} catch (Exception e1) {
	        					e1.printStackTrace();
	        				}
	        				JSONObject inernetLangsJSON = (JSONObject) JSONValue.parse(stringFromInternet);
	        				
	        				
	        				for (Object internetKey : inernetLangsJSON.keySet()) {

	        					JSONObject internetValue = (JSONObject) inernetLangsJSON.get(internetKey);

	        					String itemText = null;
	        					final String langFileName = (String)(internetValue).get("_file_");

	        					long time_of_translation = ((Long)(internetValue).get("_timestamp_of_translation_")).longValue();
	        					
	        					try {
		        					JSONObject oldLangFile = Lang.openLangFile(langFileName);
		        					
		        					if(oldLangFile == null) {
		        						itemText = (String)(internetValue).get("download lang_name translation");
		        						
		        					} else if (time_of_translation > (Long)oldLangFile.get("_timestamp_of_translation_")) {
		        						itemText = ((String)(internetValue).get("download update of lang_name translation from %date%")).replace("%date%", DateTimeFormat.timestamptoString(time_of_translation, "yyyy-MM-dd", ""));
		        					}
	        					} catch( Exception e2 ) {
	        						itemText = (String)(internetValue).get("download lang_name translation");
	        					}
	        					
	        					if(itemText != null) {
		        					
	        						JMenuItem item = new JMenuItem();
	        						item.setText("[" + (String) internetKey + "] " + itemText);
	        						
	        						item.addActionListener(new ActionListener()
	        						{
	        							public void actionPerformed(ActionEvent e) 
	        							{
	        								try {
	        		        					String url = Lang.translationsUrl + Controller.getInstance().getVersion().replace(" ", "%20") + "/languages/" + langFileName;

	        		        					FileUtils.copyURLToFile(new URL(url), new File(Settings.getInstance().getLangDir(), langFileName));
	
	        								} catch (Exception e1) {
	        		        					e1.printStackTrace();
	        		        				}
	        								
	        						        cbxListOfAvailableLangs.removeAllItems();
	        						        
	        						        for (LangFile langFile : Lang.getInstance().getListOfAvailable()) {
	        						        	cbxListOfAvailableLangs.addItem(langFile);
	        						        	
	        						        	if(langFile.getFileName().equals(langFileName)) {
	        						        		cbxListOfAvailableLangs.setSelectedItem(langFile);
	        						        	}
	        								}
	        							}
	        						});
	        						
	        						menu.add(item);	
	        					}
	        					
	        				}
	        				if(menu.getComponentCount() == 0) {
	        					JMenuItem item = new JMenuItem();
        						item.setText(Lang.getInstance().translate("No new translations"));
        						item.setEnabled(false);
        						menu.add(item);	
	        				}
	        				
	        				menu.show(btnLoadNewLang, 0, btnLoadNewLang.getHeight());
        		    	} finally {
        		    		btnLoadNewLang.setText(Lang.getInstance().translate("Download"));	
        		    	}
        		    }
        		});
        	}
        });	   
		add(btnLoadNewLang, btn_LoadNewLang);
		
        
        GridBagConstraints gbc_cbxListOfAvailableLangs = new GridBagConstraints();
        gbc_cbxListOfAvailableLangs.gridwidth = 2;
        gbc_cbxListOfAvailableLangs.insets = new Insets(0, 0, 5, 5);
        gbc_cbxListOfAvailableLangs.fill = GridBagConstraints.HORIZONTAL;
        gbc_cbxListOfAvailableLangs.gridx = 2;
        gbc_cbxListOfAvailableLangs.gridy = 15;
        add(cbxListOfAvailableLangs, gbc_cbxListOfAvailableLangs);
        
        
        GridBagConstraints gbc_bottom = new GridBagConstraints();
        gbc_bottom.gridy = 18;
        gbc_bottom.weighty = 1;
      	this.add(new JPanel(), gbc_bottom);
        
 	}
}