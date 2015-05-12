package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import utils.MenuPopupUtil;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import api.ApiClient;

@SuppressWarnings("serial")
public class ConsolePanel extends JPanel 
{
	private ApiClient client;
	private JTextArea areaConsole;
	private JTextField txtCommand;

	public ConsolePanel()
	{
		this.setLayout(new GridBagLayout());
		
		//CREATE SERVICE
		this.client = new ApiClient();//new RpcClient(new RpcServiceImpl());
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		//TEXTAREA GBC
		GridBagConstraints areaGBC = new GridBagConstraints();
		areaGBC.insets = new Insets(5,5,5,5);
		areaGBC.fill = GridBagConstraints.BOTH;  
		areaGBC.anchor = GridBagConstraints.NORTHWEST;
		areaGBC.weighty = 1;	
		areaGBC.weightx = 1;
		areaGBC.gridx = 0;	
		areaGBC.gridy = 0;
		
		//TEXTBOX GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		txtGBC.insets = new Insets(5,5,5,5);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;  
		txtGBC.anchor = GridBagConstraints.NORTHWEST;
		txtGBC.weightx = 1;
		txtGBC.gridx = 0;	
		txtGBC.gridy = 1;
		
		//TEXTAREA
		this.areaConsole = new JTextArea();
		this.areaConsole.setLineWrap(true);
		this.areaConsole.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(this.areaConsole);
		JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
		this.add(scrollPane, areaGBC);
		
		//TEXTFIELD
		this.txtCommand = new JTextField();
		this.txtCommand.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
            {
				//GET COMMAND
				String command = txtCommand.getText();
				areaConsole.append("[COMMAND] " + command + "\n");
				
				//EMPTY COMMAND FIELD
				txtCommand.setText("");
				
				//GET RESULT
				String result = client.executeCommand(command);
				
				//APPEND RESULT
				areaConsole.append("[RESULT] " + result + "\n");
            }
		});
		
		this.add(this.txtCommand, txtGBC);
		
		//CONTEXT MENU
		MenuPopupUtil.installContextMenu(this.areaConsole);
		MenuPopupUtil.installContextMenu(this.txtCommand);
	}
}
