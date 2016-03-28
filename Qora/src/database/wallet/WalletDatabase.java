package database.wallet;

import java.io.File;

import org.mapdb.Atomic.Var;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import database.IDB;
import qora.account.Account;
import settings.Settings;

public class WalletDatabase implements IDB
{
	private static final File WALLET_FILE = new File(Settings.getInstance().getWalletDir(), "wallet.dat");
	
	private static final String VERSION = "version";
	private static final String LAST_BLOCK = "lastBlock";
	
	private DB database;	
	private AccountMap accountMap;
	private TransactionMap transactionMap;
	private BlockMap blockMap;
	private NameMap nameMap;
	private NameSaleMap nameSaleMap;
	private PollMap pollMap;
	private AssetMap assetMap;
	private OrderMap orderMap;
	private AssetFavoritesSet assetFavoritesSet;
	
	public static boolean exists()
	{
		return WALLET_FILE.exists();
	}
	
	public WalletDatabase()
	{
		//OPEN WALLET
		WALLET_FILE.getParentFile().mkdirs();
		
		//DELETE TRANSACTIONS
		//File transactionFile = new File(Settings.getInstance().getWalletDir(), "wallet.dat.t");
		//transactionFile.delete();	
		
	    this.database = DBMaker.newFileDB(WALLET_FILE)
	    		.closeOnJvmShutdown()
	    		.cacheSize(2048)
	    		.checksumEnable()
	    		.mmapFileEnableIfSupported()
	            .make();
	    
	    this.accountMap = new AccountMap(this, this.database);
	    this.transactionMap = new TransactionMap(this, this.database);
	    this.blockMap = new BlockMap(this, this.database);
	    this.nameMap = new NameMap(this, this.database);
	    this.nameSaleMap = new NameSaleMap(this, this.database);
	    this.pollMap = new PollMap(this, this.database);
	    this.assetMap = new AssetMap(this, this.database);
	    this.orderMap = new OrderMap(this, this.database);
	    this.assetFavoritesSet = new AssetFavoritesSet(this, this.database);
	}
	
	public void setVersion(int version)
	{
		this.database.getAtomicInteger(VERSION).set(version);
	}
	
	public int getVersion()
	{
		return this.database.getAtomicInteger(VERSION).intValue();
	}
	
	public void setLastBlockSignature(byte[] signature)
	{
		Var<byte[]> atomic = this.database.getAtomicVar(LAST_BLOCK);
		atomic.set(signature);
	}
	
	public byte[] getLastBlockSignature()
	{
		Var<byte[]> atomic = this.database.getAtomicVar(LAST_BLOCK);
		return atomic.get();
	}
	
	public AccountMap getAccountMap()
	{
		return this.accountMap;
	}
	
	public TransactionMap getTransactionMap()
	{
		return this.transactionMap;
	}
	
	public BlockMap getBlockMap()
	{
		return this.blockMap;
	}
	
	public NameMap getNameMap()
	{
		return this.nameMap;
	}
	
	public NameSaleMap getNameSaleMap()
	{
		return this.nameSaleMap;
	}
	
	public PollMap getPollMap()
	{
		return this.pollMap;
	}
	
	public AssetMap getAssetMap()
	{
		return this.assetMap;
	}
	
	public OrderMap getOrderMap()
	{
		return this.orderMap;
	}
	
	public AssetFavoritesSet getAssetFavoritesSet()
	{
		return this.assetFavoritesSet;
	}
	
	public void delete(Account account)
	{
		this.accountMap.delete(account);
		this.blockMap.delete(account);
		this.transactionMap.delete(account);
		this.nameMap.delete(account);
		this.nameSaleMap.delete(account);
		this.pollMap.delete(account);
		this.assetMap.delete(account);
		this.orderMap.delete(account);
	}
	
	public void commit()
	{
		this.database.commit();
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
}

