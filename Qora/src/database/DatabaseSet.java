package database;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.TxMaker;

import controller.Controller;
import settings.Settings;
import utils.ObserverMessage;

public class DatabaseSet implements Observer {

	private static final int ACTIONS_BEFORE_COMMIT = 5000;
	
	private static DatabaseSet instance;
	
	private BalanceDatabase balanceDatabase;
	private BlockDatabase blockDatabase;
	private ChildDatabase childDatabase;
	private HeightDatabase heightDatabase;
	private ReferenceDatabase referenceDatabase;
	private PeerDatabase peerDatabase;
	private TransactionDatabase transactionDatabase;
	private NameDatabase nameDatabase;
	private TransactionParentDatabase transactionParentDatabase;
	private NameExchangeDatabase nameExchangeDatabase;
	private UpdateNameDatabase updateNameDatabase;
	private CancelSellNameDatabase cancelSellNameDatabase;
	private BuyNameDatabase buyNameDatabase;
	private PollDatabase PollDatabase;
	private VoteOnPollDatabase voteOnPollDatabase;
	
	private DB database;
	//private TxMaker transactionMaker;
	private int actions;
	
	public static DatabaseSet getInstance()
	{
		if(instance == null)
		{
			//OPEN DB
			File dbFile = new File(Settings.getInstance().getDataDir(), "data.dat");
			dbFile.getParentFile().mkdirs();
			
			//DELETE TRANSACTIONS
			File transactionFile = new File(Settings.getInstance().getDataDir(), "data.dat.t");
			transactionFile.delete();	
			
			//CREATE DATABASE	
			DB database = DBMaker.newFileDB(dbFile)
					.closeOnJvmShutdown()
					.asyncWriteEnable()
					.make();
			
			TxMaker txMaker = DBMaker.newFileDB(dbFile)
					.closeOnJvmShutdown()
					.makeTxMaker();
			
			//CREATE INSTANCE
			instance = new DatabaseSet(database, txMaker);
		}
		
		return instance;
	}	
	
	public static DatabaseSet createEmptyDatabaseSet()
	{
		DB database = DBMaker.newMemoryDB()
				.make();
		
		return new DatabaseSet(database, null);
	}
	
	public DatabaseSet(DB database, TxMaker transactionMaker)
	{
		this.database = database;
		//this.transactionMaker = transactionMaker;
		this.actions = 0;
		
		this.balanceDatabase = new BalanceDatabase(this, database);
		this.blockDatabase = new BlockDatabase(this, database);
		this.childDatabase = new ChildDatabase(this, database);
		this.heightDatabase = new HeightDatabase(this, database);
		this.referenceDatabase = new ReferenceDatabase(this, database);
		this.peerDatabase = new PeerDatabase(this, database);
		this.transactionDatabase = new TransactionDatabase(this, database);
		this.nameDatabase = new NameDatabase(this, database);
		this.transactionParentDatabase = new TransactionParentDatabase(this, database);
		this.nameExchangeDatabase = new NameExchangeDatabase(this, database);
		this.updateNameDatabase = new UpdateNameDatabase(this, database);
		this.cancelSellNameDatabase = new CancelSellNameDatabase(this, database);
		this.buyNameDatabase = new BuyNameDatabase(this, database);
		this.PollDatabase = new PollDatabase(this, database);
		this.voteOnPollDatabase = new VoteOnPollDatabase(this, database);
	}
	
	protected DatabaseSet(DatabaseSet parent)
	{
		this.balanceDatabase = new BalanceDatabase(parent.balanceDatabase);
		this.blockDatabase = new BlockDatabase(parent.blockDatabase);
		this.childDatabase = new ChildDatabase(this.blockDatabase, parent.childDatabase);
		this.heightDatabase = new HeightDatabase(parent.heightDatabase);
		this.referenceDatabase = new ReferenceDatabase(parent.referenceDatabase);
		this.peerDatabase = new PeerDatabase(parent.peerDatabase);
		this.transactionDatabase = new TransactionDatabase(parent.transactionDatabase);		
		this.nameDatabase = new NameDatabase(parent.nameDatabase);
		this.transactionParentDatabase = new TransactionParentDatabase(this.blockDatabase, parent.transactionParentDatabase);
		this.nameExchangeDatabase = new NameExchangeDatabase(parent.nameExchangeDatabase);
		this.updateNameDatabase = new UpdateNameDatabase(parent.updateNameDatabase);
		this.cancelSellNameDatabase = new CancelSellNameDatabase(parent.cancelSellNameDatabase);
		this.buyNameDatabase = new BuyNameDatabase(parent.buyNameDatabase);
		this.PollDatabase = new PollDatabase(parent.PollDatabase);
		this.voteOnPollDatabase = new VoteOnPollDatabase(parent.voteOnPollDatabase);
	}
	
	public BalanceDatabase getBalanceDatabase() 
	{
		return this.balanceDatabase;
	}

	public BlockDatabase getBlockDatabase() 
	{
		return this.blockDatabase;
	}

	public ChildDatabase getChildDatabase() 
	{
		return this.childDatabase;
	}

	public HeightDatabase getHeightDatabase() 
	{
		return this.heightDatabase;
	}

	public ReferenceDatabase getReferenceDatabase() 
	{
		return this.referenceDatabase;
	}
	
	public PeerDatabase getPeerDatabase() 
	{
		return this.peerDatabase;
	}
	
	public TransactionDatabase getTransactionsDatabase() 
	{
		return this.transactionDatabase;
	}
	
	public NameDatabase getNameDatabase()
	{
		return this.nameDatabase;
	}
	
	public TransactionParentDatabase getTransactionParentDatabase()
	{
		return this.transactionParentDatabase;
	}
	
	public NameExchangeDatabase getNameExchangeDatabase()
	{
		return this.nameExchangeDatabase;
	}
	
	public UpdateNameDatabase getUpdateNameDatabase()
	{
		return this.updateNameDatabase;
	}
	
	public CancelSellNameDatabase getCancelSellNameDatabase()
	{
		return this.cancelSellNameDatabase;
	}
	
	public BuyNameDatabase getBuyNameDatabase()
	{
		return this.buyNameDatabase;
	}
	
	public PollDatabase getPollDatabase()
	{
		return this.PollDatabase;
	}
	
	public VoteOnPollDatabase getVoteOnPollDatabase()
	{
		return this.voteOnPollDatabase;
	}
	
	public DatabaseSet fork()
	{
		return new DatabaseSet(this);
	}
	
	public void close()
	{
		if(this.database != null)
		{
			if(!this.database.isClosed())
			{
				this.database.commit();
				this.database.close();
			}
		}
	}
	
	protected void commit()
	{
		this.actions++;
	}
	
	@Override
	public void update(Observable o, Object arg) 
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		//CHECK IF NEW BLOCK
		if(message.getType() == ObserverMessage.LIST_BLOCK_TYPE)
		{			
			
			//CHECK IF WE NEED TO COMMIT
			if(this.actions >= ACTIONS_BEFORE_COMMIT)
			{
				this.database.commit();
				this.actions = 0;
				
				//NOTIFY CONTROLLER SO HE CAN NOTIFY WALLET
				Controller.getInstance().onDatabaseCommit();
			}
		}
		
	}

}
