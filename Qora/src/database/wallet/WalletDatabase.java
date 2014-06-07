package database.wallet;

import java.io.File;
import java.io.IOError;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import qora.account.Account;
import settings.Settings;

public class WalletDatabase 
{
	private static final File WALLET_FILE = new File(Settings.getInstance().getWalletDir(), "wallet.dat");
	
	private static final String VERSION = "version";
	
	private DB database;	
	private AccountsDatabase accountsDatabase;
	private TransactionsDatabase transactionsDatabase;
	private BlocksDatabase blocksDatabase;
	private NamesDatabase namesDatabase;
	private NameSalesDatabase nameSalesDatabase;
	
	public static boolean isCorrupted()
	{
		try
		{
			if(WALLET_FILE.exists())
			{			
				//CREATE DATABASE	
				DB database = DBMaker.newFileDB(WALLET_FILE)
						.closeOnJvmShutdown()
						.asyncWriteEnable()
						.make();
				
				//CHECK IF WE COULD OPEN DATABASE
				if(database == null)
				{
					return true;
				}
				
				//CLOSE
				database.close();
			}
			
			//RETURN
			return false;
		}
		catch(IOError e)
		{
			return true;
		}
	}
	
	public static boolean exists()
	{
		return WALLET_FILE.exists();
	}
	
	public WalletDatabase()
	{
		//OPEN WALLET
		WALLET_FILE.getParentFile().mkdirs();
		
	    this.database = DBMaker.newFileDB(WALLET_FILE)
	    		.closeOnJvmShutdown()
	            .make();
	    
	    this.accountsDatabase = new AccountsDatabase(this, this.database);
	    this.transactionsDatabase = new TransactionsDatabase(this, this.database);
	    this.blocksDatabase = new BlocksDatabase(this, this.database);
	    this.namesDatabase = new NamesDatabase(this, this.database);
	    this.nameSalesDatabase = new NameSalesDatabase(this, this.database);
	}
	
	public void setVersion(int version)
	{
		this.database.getAtomicInteger(VERSION).set(version);
	}
	
	public int getVersion()
	{
		return this.database.getAtomicInteger(VERSION).intValue();
	}
	
	public AccountsDatabase getAccountsDatabase()
	{
		return this.accountsDatabase;
	}
	
	public TransactionsDatabase getTransactionsDatabase()
	{
		return this.transactionsDatabase;
	}
	
	public BlocksDatabase getBlocksDatabase()
	{
		return this.blocksDatabase;
	}
	
	public NamesDatabase getNamesDatabase()
	{
		return this.namesDatabase;
	}
	
	public NameSalesDatabase getNameSalesDatabase()
	{
		return this.nameSalesDatabase;
	}
	
	public void delete(Account account)
	{
		this.accountsDatabase.delete(account);
		this.blocksDatabase.delete(account);
		this.transactionsDatabase.delete(account);
		this.namesDatabase.delete(account);
		this.nameSalesDatabase.delete(account);
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

