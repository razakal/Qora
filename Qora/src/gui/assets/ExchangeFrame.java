package gui.assets;

import gui.models.BuyOrdersTableModel;
import gui.models.OrdersTableModel;
import gui.models.TradesTableModel;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import qora.assets.Asset;

public class ExchangeFrame extends JFrame
{
	private static final long serialVersionUID = -7052380905136603354L;
	
	private Asset have;
	private Asset want;
	
	private OrdersTableModel sellOrdersTableModel;
	private BuyOrdersTableModel buyOrdersTableModel;
	private TradesTableModel tradesTableModel;
	
	public ExchangeFrame(Asset have, Asset want) 
	{
		super("Qora - Asset Exchange");
		
		this.have = have;
		this.want = want;
		
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
		
		//ORDER GBC
		GridBagConstraints orderGBC = new GridBagConstraints();
		orderGBC.insets = new Insets(0, 5, 5, 0);
		orderGBC.fill = GridBagConstraints.BOTH;  
		orderGBC.anchor = GridBagConstraints.NORTHWEST;
		orderGBC.weightx = 1;
		orderGBC.gridy = 2;	
		
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.insets = new Insets(0, 5, 5, 0);
		tableGBC.fill = GridBagConstraints.BOTH;  
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;
		tableGBC.weighty = 1;
		tableGBC.gridy = 4;	
		
		//CREATE TITLE LABEL
		JLabel lblTitle = new JLabel("(" + this.have.getKey() + ")" + this.have.getName() + "/(" + this.want.getKey() + ")" + this.want.getName());
		lblTitle.setFont(new Font("Serif", Font.PLAIN, 24));
		this.add(lblTitle, labelGBC);
		
		//CREATE BUY LABEL
		labelGBC.gridy = 1;
		JLabel lblBuy = new JLabel("Sell (" + this.want.getKey() + ")" + this.want.getName());
		lblBuy.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblBuy, labelGBC);
		
		//CREATE SELL LABEL
		labelGBC.gridx = 1;
		JLabel lblSell = new JLabel("Sell (" + this.have.getKey() + ")" + this.have.getName());
		lblSell.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblSell, labelGBC);
		
		//CREATE BUY PANEL
		OrderPanel buyOrderPanel = new OrderPanel(this.want, this.have, true);
		this.add(buyOrderPanel, orderGBC);
		
		//CREATE SELL PANEL
		orderGBC.gridx = 1;
		OrderPanel sellOrderPanel = new OrderPanel(this.have, this.want, false);
		this.add(sellOrderPanel, orderGBC);
	
		//CREATE SELL ORDERS LABEL
		labelGBC.gridx = 0;
		labelGBC.gridy = 3;
		JLabel lblSellOrders = new JLabel("Sell orders");
		lblSellOrders.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblSellOrders, labelGBC);
		
		//CREATE BUY ORDERS LABEL
		labelGBC.gridx = 1;
		JLabel lblBuyOrders = new JLabel("Buy orders");
		lblBuyOrders.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblBuyOrders, labelGBC);
						
		//CREATE SELL ORDERS TABLE
		this.sellOrdersTableModel = new OrdersTableModel(this.have, this.want);
		final JTable sellOrdersTable = new JTable(this.sellOrdersTableModel);
		this.add(new JScrollPane(sellOrdersTable), tableGBC);
		
		//CREATE BUY ORDERS TABLE
		tableGBC.gridx = 1;
		this.buyOrdersTableModel = new BuyOrdersTableModel(this.want, this.have);
		final JTable buyOrdersTable = new JTable(this.buyOrdersTableModel);
		this.add(new JScrollPane(buyOrdersTable), tableGBC);
		
		//CREATE TRADE HISTORY LABEL
		labelGBC.gridx = 0;
		labelGBC.gridy = 5;
		JLabel lblTradeHistory = new JLabel("Trade History");
		lblTradeHistory.setFont(new Font("Serif", Font.PLAIN, 18));
		this.add(lblTradeHistory, labelGBC);
		
		//CREATE TRADE HISTORY TABLE
		tableGBC.gridx = 0;
		tableGBC.gridy = 6;
		tableGBC.gridwidth = 2;
		//tableGBC.weighty = 1;
		this.tradesTableModel = new TradesTableModel(this.have, this.want);
		final JTable TradesTable = new JTable(this.tradesTableModel);
		this.add(new JScrollPane(TradesTable), tableGBC);
		
		//PACK
		this.pack();
		this.setResizable(true);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

}
