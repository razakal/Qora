package database;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import controller.Controller;
import settings.Settings;
import utils.ObserverMessage;

public class DBSet implements Observer, IDB {

	private static final int ACTIONS_BEFORE_COMMIT = 10000;
	
	private static DBSet instance;
	
	private BalanceMap balanceMap;
	private BlockMap blockMap;
	private ChildMap childMap;
	private HeightMap heightMap;
	private ReferenceMap referenceMap;
	private PeerMap peerMap;
	private TransactionMap transactionMap;
	private NameMap nameMap;
	private TransactionParentMap transactionParentMap;
	private NameExchangeMap nameExchangeMap;
	private UpdateNameMap updateNameMap;
	private CancelSellNameMap cancelSellNameMap;
	private PollMap pollMap;
	private VoteOnPollMap voteOnPollMap;
	private AssetMap assetMap;
	private IssueAssetMap issueAssetMap;
	private OrderMap orderMap;
	private CompletedOrderMap completedOrderMap;
	private TradeMap tradeMap;
	
	private DB database;
	private int actions;
	
	public static DBSet getInstance()
	{
		if(instance == null)
		{
			//OPEN DB
			File dbFile = new File(Settings.getInstance().getDataDir(), "data.dat");
			dbFile.getParentFile().mkdirs();
			
			//CREATE DATABASE	
			DB database = DBMaker.newFileDB(dbFile)
					.closeOnJvmShutdown()
					.cacheSize(2048)
					.checksumEnable()
					.mmapFileEnableIfSupported()
					.make();
			
			//CREATE INSTANCE
			instance = new DBSet(database);
		}
		
		return instance;
	}	
	
	public static DBSet createEmptyDatabaseSet()
	{
		DB database = DBMaker.newMemoryDB()
				.make();
		
		return new DBSet(database);
	}
	
	public DBSet(DB database)
	{
		this.database = database;
		this.actions = 0;
		
		this.balanceMap = new BalanceMap(this, database);
		this.blockMap = new BlockMap(this, database);
		this.childMap = new ChildMap(this, database);
		this.heightMap = new HeightMap(this, database);
		this.referenceMap = new ReferenceMap(this, database);
		this.peerMap = new PeerMap(this, database);
		this.transactionMap = new TransactionMap(this, database);
		this.nameMap = new NameMap(this, database);
		this.transactionParentMap = new TransactionParentMap(this, database);
		this.nameExchangeMap = new NameExchangeMap(this, database);
		this.updateNameMap = new UpdateNameMap(this, database);
		this.cancelSellNameMap = new CancelSellNameMap(this, database);
		this.pollMap = new PollMap(this, database);
		this.voteOnPollMap = new VoteOnPollMap(this, database);
		this.assetMap = new AssetMap(this, database);
		this.issueAssetMap = new IssueAssetMap(this, database);
		this.orderMap = new OrderMap(this, database);
		this.completedOrderMap = new CompletedOrderMap(this, database);
		this.tradeMap = new TradeMap(this, database);
	}
	
	protected DBSet(DBSet parent)
	{
		this.balanceMap = new BalanceMap(parent.balanceMap);
		this.blockMap = new BlockMap(parent.blockMap);
		this.childMap = new ChildMap(this.blockMap, parent.childMap);
		this.heightMap = new HeightMap(parent.heightMap);
		this.referenceMap = new ReferenceMap(parent.referenceMap);
		this.peerMap = new PeerMap(parent.peerMap);
		this.transactionMap = new TransactionMap(parent.transactionMap);		
		this.nameMap = new NameMap(parent.nameMap);
		this.transactionParentMap = new TransactionParentMap(this.blockMap, parent.transactionParentMap);
		this.nameExchangeMap = new NameExchangeMap(parent.nameExchangeMap);
		this.updateNameMap = new UpdateNameMap(parent.updateNameMap);
		this.cancelSellNameMap = new CancelSellNameMap(parent.cancelSellNameMap);
		this.pollMap = new PollMap(parent.pollMap);
		this.voteOnPollMap = new VoteOnPollMap(parent.voteOnPollMap);
		this.assetMap = new AssetMap(parent.assetMap);
		this.issueAssetMap = new IssueAssetMap(parent.issueAssetMap);
		this.orderMap = new OrderMap(parent.orderMap);
		this.completedOrderMap = new CompletedOrderMap(parent.completedOrderMap);
		this.tradeMap = new TradeMap(parent.tradeMap);
	}
	
	public void reset() {
		
		this.balanceMap.reset();
		this.heightMap.reset();
		this.referenceMap.reset();
		this.peerMap.reset();
		this.transactionMap.reset();
		this.nameMap.reset();
		this.transactionParentMap.reset();
		this.nameExchangeMap.reset();
		this.updateNameMap.reset();
		this.cancelSellNameMap.reset();
		this.pollMap.reset();
		this.voteOnPollMap.reset();
		this.assetMap.reset();
		this.issueAssetMap.reset();
		this.orderMap.reset();
		this.completedOrderMap.reset();
		this.tradeMap.reset();
	}
	
	public BalanceMap getBalanceMap() 
	{
		return this.balanceMap;
	}

	public BlockMap getBlockMap() 
	{
		return this.blockMap;
	}

	public ChildMap getChildMap() 
	{
		return this.childMap;
	}

	public HeightMap getHeightMap() 
	{
		return this.heightMap;
	}

	public ReferenceMap getReferenceMap() 
	{
		return this.referenceMap;
	}
	
	public PeerMap getPeerMap() 
	{
		return this.peerMap;
	}
	
	public TransactionMap getTransactionMap() 
	{
		return this.transactionMap;
	}
	
	public NameMap getNameMap()
	{
		return this.nameMap;
	}
	
	public TransactionParentMap getTransactionParentMap()
	{
		return this.transactionParentMap;
	}
	
	public NameExchangeMap getNameExchangeMap()
	{
		return this.nameExchangeMap;
	}
	
	public UpdateNameMap getUpdateNameMap()
	{
		return this.updateNameMap;
	}
	
	public CancelSellNameMap getCancelSellNameMap()
	{
		return this.cancelSellNameMap;
	}
	
	public PollMap getPollMap()
	{
		return this.pollMap;
	}
	
	public VoteOnPollMap getVoteOnPollDatabase()
	{
		return this.voteOnPollMap;
	}
	
	public AssetMap getAssetMap()
	{
		return this.assetMap;
	}
	
	public IssueAssetMap getIssueAssetMap()
	{
		return this.issueAssetMap;
	}
	
	public OrderMap getOrderMap()
	{
		return this.orderMap;
	}
	
	public CompletedOrderMap getCompletedOrderMap()
	{
		return this.completedOrderMap;
	}
	
	public TradeMap getTradeMap()
	{
		return this.tradeMap;
	}
	
	public DBSet fork()
	{
		return new DBSet(this);
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
	
	public void commit()
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
