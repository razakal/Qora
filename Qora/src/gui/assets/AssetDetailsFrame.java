package gui.assets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import qora.assets.Asset;

@SuppressWarnings("serial")
public class AssetDetailsFrame extends JFrame
{
	private Asset asset;
	
	public AssetDetailsFrame(Asset asset)
	{
		super("Qora - Asset Details");
		
		this.asset = asset;
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		//DETAIL GBC
		GridBagConstraints detailGBC = new GridBagConstraints();
		detailGBC.insets = new Insets(0, 5, 5, 0);
		detailGBC.fill = GridBagConstraints.HORIZONTAL;  
		detailGBC.anchor = GridBagConstraints.NORTHWEST;
		detailGBC.weightx = 1;	
		detailGBC.gridwidth = 2;
		detailGBC.gridx = 1;		
		
		//LABEL KEY
		labelGBC.gridy = 1;
		JLabel keyLabel = new JLabel("Key:");
		this.add(keyLabel, labelGBC);
				
		//KEY
		detailGBC.gridy = 1;
		JTextField txtKey = new JTextField(Long.toString(asset.getKey()));
		txtKey.setEditable(false);
		this.add(txtKey, detailGBC);	
		
		//LABEL NAME
		labelGBC.gridy = 2;
		JLabel nameLabel = new JLabel("Name:");
		this.add(nameLabel, labelGBC);
		
		//NAME
		detailGBC.gridy = 2;
		JTextField txtName = new JTextField(asset.getName());
		txtName.setEditable(false);
		this.add(txtName, detailGBC);		
		
		//LABEL DESCRIPTION
		labelGBC.gridy = 3;
		JLabel descriptionLabel = new JLabel("Description:");
		this.add(descriptionLabel, labelGBC);
		           
		//DESCRIPTION
		detailGBC.gridy = 3;
		JTextArea txtAreaDescription = new JTextArea(asset.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(txtName.getBorder());
		txtAreaDescription.setEditable(false);
		this.add(txtAreaDescription, detailGBC);	
		
		//LABEL OWNER
		labelGBC.gridy = 4;
		JLabel ownerLabel = new JLabel("Owner:");
		this.add(ownerLabel, labelGBC);
				
		//OWNER
		detailGBC.gridy = 4;
		JTextField owner = new JTextField(asset.getOwner().getAddress());
		owner.setEditable(false);
		this.add(owner, detailGBC);
		
		//LABEL QUANTITY
		labelGBC.gridy = 5;
		JLabel quantityLabel = new JLabel("Quantity:");
		this.add(quantityLabel, labelGBC);
		           
		//QUANTITY
		detailGBC.gridy = 5;
		JTextField txtQuantity = new JTextField(asset.getQuantity().toString());
		txtQuantity.setEditable(false);
		this.add(txtQuantity, detailGBC);		
		
		//LABEL DIVISIBLE
		labelGBC.gridy = 6;
		JLabel divisibleLabel = new JLabel("Quantity:");
		this.add(divisibleLabel, labelGBC);
		           
		//DIVISIBLE
		detailGBC.gridy = 6;
		JCheckBox chkDivisible = new JCheckBox();
		chkDivisible.setSelected(asset.isDivisible());
		chkDivisible.setEnabled(false);
		this.add(chkDivisible, detailGBC);	
		
        //PACK
		this.pack();
		this.setSize(500, this.getHeight());
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
