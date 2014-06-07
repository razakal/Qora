package gui;

import gui.models.BlocksTableModel;
import gui.models.PeersTableModel;
import gui.models.TransactionsTableModel;
import gui.transaction.TransactionDetailsFactory;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import qora.transaction.Transaction;
import utils.BigDecimalStringComparator;
import utils.DateStringComparator;
import utils.IntegerComparator;
import utils.LongComparator;

public class DebugTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2717571093561259483L;

	private PeersTableModel peersTableModel;
	private TransactionsTableModel transactionsTableModel;
	private BlocksTableModel blocksTableModel;
	private LoggerTextArea loggerTextArea;
	private JTable transactionsTable;
	
	@SuppressWarnings("unchecked")
	public DebugTabPane()
	{
		super();
		
		//ADD TABS
        this.addTab("Console", new ConsolePanel());
		
        this.peersTableModel = new PeersTableModel();
		this.addTab("Peers", new JScrollPane(Gui.createSortableTable(this.peersTableModel, 0)));
        
		this.transactionsTableModel = new TransactionsTableModel();
		this.transactionsTable = Gui.createSortableTable(this.transactionsTableModel, TransactionsTableModel.COLUMN_TIMESTAMP);
		
		TableRowSorter<TransactionsTableModel> transactionSorter =  (TableRowSorter<TransactionsTableModel>) this.transactionsTable.getRowSorter();
		transactionSorter.setComparator(TransactionsTableModel.COLUMN_TIMESTAMP, new DateStringComparator());
		transactionSorter.setComparator(TransactionsTableModel.COLUMN_FEE, new BigDecimalStringComparator());
		
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
			        Transaction transaction = transactionsTableModel.getTransaction(row);
			         
			        //SHOW DETAIL SCREEN OF TRANSACTION
			        TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
			    }
			}
		});			
		this.addTab("Transactions", new JScrollPane(this.transactionsTable)); 
	           
		
		this.blocksTableModel = new BlocksTableModel();
		JTable blocksTable = Gui.createSortableTable(this.blocksTableModel, BlocksTableModel.COLUMN_HEIGHT);
		
		TableRowSorter<BlocksTableModel> blockSorter =  (TableRowSorter<BlocksTableModel>) blocksTable.getRowSorter();
		blockSorter.setComparator(BlocksTableModel.COLUMN_HEIGHT, new IntegerComparator());
		blockSorter.setComparator(BlocksTableModel.COLUMN_TIMESTAMP, new DateStringComparator());
		blockSorter.setComparator(BlocksTableModel.COLUMN_TRANSACTIONS, new IntegerComparator());
		blockSorter.setComparator(BlocksTableModel.COLUMN_BASETARGET, new LongComparator());
		blockSorter.setComparator(BlocksTableModel.COLUMN_FEE, new BigDecimalStringComparator());
		
        this.addTab("Blocks", new JScrollPane(blocksTable));
		
        this.loggerTextArea = new LoggerTextArea(Logger.getGlobal());
        JScrollPane scrollPane = new JScrollPane(this.loggerTextArea);
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
        this.addTab("Logger", scrollPane);
	}

	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
		this.peersTableModel.removeObservers();
		
		this.transactionsTableModel.removeObservers();
		
		this.blocksTableModel.removeObservers();
		
		this.loggerTextArea.removeNotify();	
	}
	
}
