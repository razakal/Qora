package gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import gui.models.WalletBlocksTableModel;
import gui.models.WalletTransactionsTableModel;
import gui.naming.NamingServicePanel;
import gui.transaction.TransactionDetailsFactory;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import qora.transaction.Transaction;
import utils.BigDecimalStringComparator;
import utils.DateStringComparator;
import utils.IntegerComparator;
import utils.LongComparator;

public class GeneralTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2717571093561259483L;

	private WalletTransactionsTableModel transactionsModel;
	private JTable transactionsTable;
	
	@SuppressWarnings("unchecked")
	public GeneralTabPane()
	{
		super();
		
		//ADD TABS
		this.addTab("Accounts", new AccountsPanel());
        
		this.addTab("Send money", new SendMoneyPanel());
        
		this.transactionsModel = new WalletTransactionsTableModel();
		this.transactionsTable = Gui.createSortableTable(this.transactionsModel, WalletTransactionsTableModel.COLUMN_TIMESTAMP);
		
		TableRowSorter<WalletTransactionsTableModel> transactionSorter =  (TableRowSorter<WalletTransactionsTableModel>) this.transactionsTable.getRowSorter();
		transactionSorter.setComparator(WalletTransactionsTableModel.COLUMN_TIMESTAMP, new DateStringComparator());
		transactionSorter.setComparator(WalletTransactionsTableModel.COLUMN_CONFIRMATIONS, new IntegerComparator());
		transactionSorter.setComparator(WalletTransactionsTableModel.COLUMN_AMOUNT, new BigDecimalStringComparator());
		
		this.transactionsTable.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent e) 
			{
				if(e.getClickCount() == 2) 
				{
					//GET ROW
			        int row = transactionsTable.getSelectedRow();
			        row = transactionsTable.convertRowIndexToModel(row);
			        
			        //GET TRANSACTION
			        Transaction transaction = transactionsModel.getTransaction(row);
			         
			        //SHOW DETAIL SCREEN OF TRANSACTION
			        TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
			    }
			}
		});			
		this.addTab("Transactions", new JScrollPane(this.transactionsTable));       
		
		JTable blocksTable = Gui.createSortableTable(new WalletBlocksTableModel(), WalletBlocksTableModel.COLUMN_HEIGHT);
		
		TableRowSorter<WalletBlocksTableModel> blockSorter =  (TableRowSorter<WalletBlocksTableModel>) blocksTable.getRowSorter();
		blockSorter.setComparator(WalletBlocksTableModel.COLUMN_HEIGHT, new IntegerComparator());
		blockSorter.setComparator(WalletBlocksTableModel.COLUMN_TIMESTAMP, new DateStringComparator());
		blockSorter.setComparator(WalletBlocksTableModel.COLUMN_TRANSACTIONS, new IntegerComparator());
		blockSorter.setComparator(WalletBlocksTableModel.COLUMN_BASETARGET, new LongComparator());
		blockSorter.setComparator(WalletBlocksTableModel.COLUMN_FEE, new BigDecimalStringComparator());
		
        this.addTab("Generated Blocks", new JScrollPane(blocksTable));
        
        this.addTab("Naming service", new NamingServicePanel());       
	}
	
}
