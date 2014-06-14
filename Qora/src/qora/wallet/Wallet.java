package qora.wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import controller.Controller;
import database.DatabaseSet;
import database.wallet.SecureWalletDatabase;
import database.wallet.WalletDatabase;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.block.Block;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.CreatePollTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.UpdateNameTransaction;
import qora.voting.Poll;
import utils.ObserverMessage;
import utils.Pair;

public class Wallet extends Observable implements Observer
{
	public static final int STATUS_UNLOCKED = 1;
	public static final int STATUS_LOCKED = 0;
	
	private WalletDatabase database;
	private SecureWalletDatabase secureDatabase;
	
	public static boolean isCorrupted()
	{
		return WalletDatabase.isCorrupted();
	}
	
	//CONSTRUCTORS
	
	public Wallet()
	{
		//CHECK IF EXISTS
		if(this.exists())
		{
			//OPEN WALLET
			this.database = new WalletDatabase();
			
			//ADD OBSERVER
		    Controller.getInstance().addObserver(this);
		    
		    //ADD OBSERVER FOR UNCONFIRMED TRANSACTION REMOVAL
			DatabaseSet.getInstance().getTransactionsDatabase().addObserver(this);
		}
	}
	
	//GETTERS/SETTERS
	
	public int getVersion()
	{
		return this.database.getVersion();
	}
	
	public boolean isUnlocked()
	{
		return this.secureDatabase != null;
	}
	
	public List<Account> getAccounts()
	{
		return this.database.getAccountsDatabase().getAccounts();
	}
	
	public boolean accountExists(String address)
	{
		return this.database.getAccountsDatabase().exists(address);
	}
	
	public Account getAccount(String address)
	{
		return this.database.getAccountsDatabase().getAccount(address);
	}
	
	public BigDecimal getUnconfirmedBalance(String address)
	{
		return this.database.getAccountsDatabase().getUnconfirmedBalance(address);
	}
	
	public List<PrivateKeyAccount> getprivateKeyAccounts()
	{
		if(this.secureDatabase == null)
		{
			return new ArrayList<PrivateKeyAccount>();
		}
		
		return this.secureDatabase.getAccountSeedsDatabase().getPrivateKeyAccounts();
	}
	
	public PrivateKeyAccount getPrivateKeyAccount(String address)
	{
		if(this.secureDatabase == null)
		{
			return null;
		}
		
		return this.secureDatabase.getAccountSeedsDatabase().getPrivateKeyAccount(address);
	}
	
	public boolean exists()
	{
		return WalletDatabase.exists();
	}
	
	public List<Pair<Account, Transaction>> getLastTransactions()
	{
		if(!this.exists())
		{
			new ArrayList<Pair<Account, Transaction>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getTransactionsDatabase().getLastTransactions(accounts);
	}
	
	public List<Transaction> getLastTransactions(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Transaction>();
		}

		return this.database.getTransactionsDatabase().getLastTransactions(account);
	}
	
	public List<Pair<Account, Block>> getLastBlocks()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Block>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getBlocksDatabase().getLastBlocks(accounts);
	}

	public List<Block> getLastBlocks(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Block>();
		}

		return this.database.getBlocksDatabase().getLastBlocks(account);
	}
		
	public List<Pair<Account, Name>> getNames()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Name>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getNamesDatabase().getNames(accounts);
	}
	
	public List<Name> getNames(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Name>();
		}

		return this.database.getNamesDatabase().getNames(account);
	}
	
	public List<Pair<Account, NameSale>> getNameSales()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, NameSale>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getNameSalesDatabase().getNameSales(accounts);
	}
	
	public List<NameSale> getNameSales(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<NameSale>();
		}

		return this.database.getNameSalesDatabase().getNameSales(account);
	}
	
	public List<Pair<Account, Poll>> getPolls()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Poll>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getPollDatabase().getPolls(accounts);
	}
	
	public List<Poll> getPolls(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Poll>();
		}

		return this.database.getPollDatabase().getPolls(account);
	}
	
	//CREATE
	
	public boolean create(byte[] seed, String password, int depth, boolean synchronize)
	{
		//OPEN WALLET
		WalletDatabase database = new WalletDatabase();
		
	    //OPEN SECURE WALLET
		SecureWalletDatabase secureDatabase = new SecureWalletDatabase(password);
	    
	    //CREATE
	    return this.create(database, secureDatabase, seed, depth, synchronize);
	}
	
	public boolean create(WalletDatabase database, SecureWalletDatabase secureDatabase, byte[] seed, int depth, boolean synchronize)
	{
		//CREATE WALLET
		this.database = database;
	    
	    //CREATE SECURE WALLET
	    this.secureDatabase = secureDatabase;
	    
	    //ADD VERSION
	    this.database.setVersion(1);
	    
	    //ADD SEED
	    this.secureDatabase.setSeed(seed);
	    
	    //ADD NONCE
	    this.secureDatabase.setNonce(0);
	    
	    //CREATE ACCOUNTS
	    for(int i=1; i<=depth; i++)
	    {
	    	this.generateNewAccount();
	    }
	    
	    //SCAN TRANSACTIONS
	    if(synchronize)
	    {
	    	this.synchronize();
	    }
	    
	    //COMMIT
	    this.commit();
	    
	    //ADD OBSERVER
	    Controller.getInstance().addObserver(this);
	      
	    
	    return true;
	}
	
	public String generateNewAccount() 
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return "";
		}
	    
	    //READ SEED
	    byte[] seed = this.secureDatabase.getSeed();
	    
	    //READ NONCE
	    int nonce = this.secureDatabase.getAndIncrementNonce();
	    
	    //GENERATE ACCOUNT SEED
	    byte[] accountSeed = this.generateAccountSeed(seed, nonce);
	    PrivateKeyAccount account = new PrivateKeyAccount(accountSeed);
	    
	    //CHECK IF ACCOUNT ALREADY EXISTS
	    if(!this.accountExists(account.getAddress()))
	    {	    
	    	//ADD TO DATABASE
		    this.secureDatabase.getAccountSeedsDatabase().add(account);
		    this.database.getAccountsDatabase().add(account);
		    
		    //NOTIFY
		    this.setChanged();
		    this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_ACCOUNT_TYPE, account));
	    }
	    
	    return account.getAddress();
	}
	
	private byte[] generateAccountSeed(byte[] seed, int nonce) 
	{		
		byte[] nonceBytes = Ints.toByteArray(nonce);
		byte[] accountSeed = Bytes.concat(nonceBytes, seed, nonceBytes);
		return Crypto.getInstance().doubleDigest(accountSeed);		
	}
	
	//DELETE
	
	public boolean deleteAccount(PrivateKeyAccount account)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return false;
		}
		
		//DELETE FROM DATABASE
		this.database.delete(account);
		this.secureDatabase.delete(account);
		
		//NOTIFY
	    this.setChanged();
	    this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_ACCOUNT_TYPE, account));
	    
	    this.setChanged();
	    this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_BLOCK_TYPE, this.getLastBlocks()));
	    
	    this.setChanged();
	    this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_TRANSACTION_TYPE, this.getLastTransactions()));
	    
	    //RETURN
	    return true;
	}
	
	//SYNCRHONIZE
	
	public void synchronize()
	{
		List<Account> accounts = this.getAccounts();
		
		//RESET UNCONFIRMED BALANCE
		synchronized(accounts)
		{
			for(Account account: accounts)
			{
				this.database.getAccountsDatabase().update(account, account.getConfirmedBalance());
			}
		}
		
		//SCAN TRANSACTIONS
		Map<Account, List<Transaction>> transactions;
		synchronized(accounts)
		{
			transactions = Controller.getInstance().scanTransactions(accounts);
		}
		
		//DELETE TRANSACTIONS
		this.database.getTransactionsDatabase().deleteAll(accounts);
		
		//ADD TRANSACTIONS
		this.database.getTransactionsDatabase().addAll(transactions);
	    	
		//TODO SCAN UNCONFIRMED TRANSACTIONS    
	    	    
	    //SCAN BLOCKS
	    Map<Account, List<Block>> blocks;
	    synchronized(accounts)
		{
	    	blocks = Controller.getInstance().scanBlocks(accounts);
		}
	    
	    //DELETE BLOCKS
	  	this.database.getBlocksDatabase().deleteAll(accounts);
	  	
	  	//ADD BLOCKS
	  	this.database.getBlocksDatabase().addAll(blocks);
	    
	    //SCAN NAMES
	    Map<Account, List<Name>> names;
	    synchronized(accounts)
		{
	    	names = Controller.getInstance().scanNames(accounts);
		}
	    
	    //DELETE NAMES
	  	this.database.getNamesDatabase().deleteAll(accounts);
	  	
	  	//ADD NAMES
	  	this.database.getNamesDatabase().addAll(names);
	  	
	  	//TODO SCAN UNCONFIRMED NAMES
	    
	  	//SCAN NAMESALES
	    Map<Account, List<NameSale>> nameSales;
	    synchronized(accounts)
		{
	    	nameSales = Controller.getInstance().scanNameSales(accounts);
		}
	    
	    //DELETE NAMESALES
	  	this.database.getNameSalesDatabase().deleteAll(accounts);
	  	
	  	//ADD NAMES
	  	this.database.getNameSalesDatabase().addAll(nameSales);
	  	
	  	//SCAN POLLS
	  	Map<Account, List<Poll>> polls;
	  	synchronized(accounts)
	  	{
	  		polls = Controller.getInstance().scanPolls(accounts);
	  	}
	  	
	  	//DELETE POLLS
	  	this.database.getPollDatabase().deleteAll(accounts);
	  	
	  	//ADD POLLS
	  	this.database.getPollDatabase().addAll(polls);
	  	
	  	//NOTIFY OBSERVERS
	    this.setChanged();
	    this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_TRANSACTION_TYPE, this.getLastTransactions()));
	  		
	    this.setChanged();
	    this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_BLOCK_TYPE, this.getLastBlocks()));   
	    
	    this.setChanged();
	    this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));   
	    
	    this.setChanged();
	    this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
	    
	    this.setChanged();
	    this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_POLL_TYPE, this.getPolls()));     	
	}
	
	//UNLOCK
	
	public boolean unlock(String password)
	{
		if(this.isUnlocked())
		{
			return false;
		}
		
		//TRY TO UNLOCK
		try
		{
			SecureWalletDatabase secureDatabase = new SecureWalletDatabase(password);
			return this.unlock(secureDatabase);
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public boolean unlock(SecureWalletDatabase secureDatabase)
	{
		this.secureDatabase = secureDatabase;
		
		//NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_STATUS, STATUS_UNLOCKED));
		
		return true;
	}
	
	public boolean lock()
	{
		if(!this.isUnlocked())
		{
			return false;
		}
		
		//CLOSE
		this.secureDatabase.close();
		this.secureDatabase = null;
		
		//NOTIFY
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.WALLET_STATUS, STATUS_LOCKED));
		
		//LOCK SUCCESSFULL
		return true;
	}

	//IMPORT/EXPORT
	
	public String importAccountSeed(byte[] accountSeed)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return "";
		}
		
		//CHECK LENGTH
		if(accountSeed.length != 32)
		{
			return "";
		}
		
		//CREATE ACCOUNT
		PrivateKeyAccount account = new PrivateKeyAccount(accountSeed);
		
		//CHECK IF ACCOUNT ALREADY EXISTS
	    if(!this.accountExists(account.getAddress()))
	    {	
	    	//ADD TO DATABASE
		    this.secureDatabase.getAccountSeedsDatabase().add(account);
		    this.database.getAccountsDatabase().add(account);
		    
		    //SYNCHRONIZE
		    this.synchronize();
		    
		    //NOTIFY
		    this.setChanged();
		    this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_TRANSACTION_TYPE, this.getLastTransactions()));
		    
		    this.setChanged();
		    this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_BLOCK_TYPE, this.getLastBlocks()));
		    
		    //RETURN
		    return account.getAddress();
	    }
	    
	    return "";
	}

	public byte[] exportAccountSeed(String address)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return null;
		}
		
		PrivateKeyAccount account = this.getPrivateKeyAccount(address);
		
		if(account == null)
		{
			return null;
		}
		
		return account.getSeed();	
	}
	
	public byte[] exportSeed()
	{
		//CHECK IF WALLET IS OPEN
		if(!this.isUnlocked())
		{
			return null;
		}
		
		return this.secureDatabase.getSeed();
	}
	
	//OBSERVER
	
	@Override
	public void addObserver(Observer o)
	{
		super.addObserver(o);
		
		//SEND LAST TRANSACTIONS ON REGISTER
		o.update(this, new ObserverMessage(ObserverMessage.LIST_TRANSACTION_TYPE, this.getLastTransactions()));
		
		//SEND LAST BLOCKS ON REGISTER
		o.update(this, new ObserverMessage(ObserverMessage.LIST_BLOCK_TYPE, this.getLastBlocks()));
		
		//SEND LAST NAMES ON REGISTER
		o.update(this, new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));
		
		//SEND LAST NAME SALES ON REGISTER
		o.update(this, new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
		
		//SEND LAST POLLS ON REGISTER
		o.update(this, new ObserverMessage(ObserverMessage.LIST_POLL_TYPE, this.getPolls()));
		
		//SEND STATUS
		int status = STATUS_LOCKED;
		if(this.isUnlocked())
		{
			status = STATUS_UNLOCKED;
		}
		
		o.update(this, new ObserverMessage(ObserverMessage.WALLET_STATUS, status));
	}

	private void processTransaction(Transaction transaction)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//FOR ALL ACCOUNTS
		boolean involved = false;
		List<Account> accounts = this.getAccounts();	
		synchronized(accounts)
		{		
			for(Account account: accounts)
			{
				//CHECK IF INVOLVED
				if(transaction.isInvolved(account))
				{
					involved = true;
					
					//ADD TO ACCOUNT TRANSACTIONS
					if(this.database.getTransactionsDatabase().add(account, transaction))
					{					
						//UPDATE UNCONFIRMED BALANCE
						BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(account.getAddress()).add(transaction.getAmount(account));
						this.database.getAccountsDatabase().update(account, unconfirmedBalance);
					}
				}
			}
		}
		
		if(involved)
		{
			//UPDATE OBSERVERS
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_TRANSACTION_TYPE, this.getLastTransactions()));
		}
	}
	
	private void orphanTransaction(Transaction transaction)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		///FOR ALL ACCOUNTS
		List<Account> accounts = this.getAccounts();
		
		synchronized(accounts)
		{		
			for(Account account: accounts)
			{
				//CHECK IF INVOLVED
				if(transaction.isInvolved(account))
				{
					//DELETE FROM ACCOUNT TRANSACTIONS
					this.database.getTransactionsDatabase().delete(account, transaction);
					
					//UPDATE UNCONFIRMED BALANCE
					BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(account.getAddress()).subtract(transaction.getAmount(account));
					this.database.getAccountsDatabase().update(account, unconfirmedBalance);
				}
			}
		}
		
		//UPDATE OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_TRANSACTION_TYPE, this.getLastTransactions()));
	}

	private void processBlock(Block block)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE GENERATOR
		if(this.accountExists(block.getGenerator().getAddress()))
		{
			//ADD BLOCK
			this.database.getBlocksDatabase().add(block);
			
			//KEEP TRACK OF UNCONFIRMED BALANCE
			BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(block.getGenerator().getAddress()).add(block.getTotalFee());
			this.database.getAccountsDatabase().update(block.getGenerator(), unconfirmedBalance);
			
			//UPDATE OBSERVERS
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_BLOCK_TYPE, this.getLastBlocks()));
		}
	}

	private void orphanBlock(Block block)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//CHECK IF WE ARE GENERATOR
		if(this.accountExists(block.getGenerator().getAddress()))
		{
			//DELETE BLOCK
			this.database.getBlocksDatabase().delete(block);
			
			//KEEP TRACK OF UNCONFIRMED BALANCE
			BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(block.getGenerator().getAddress()).subtract(block.getTotalFee());
			this.database.getAccountsDatabase().update(block.getGenerator(), unconfirmedBalance);
			
			//UPDATE OBSERVERS
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_BLOCK_TYPE, this.getLastBlocks()));
		}
	}
	
	private void processNameRegistration(RegisterNameTransaction nameRegistration)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(nameRegistration.getName().getOwner().getAddress()))
		{
			//ADD NAME
			this.database.getNamesDatabase().add(nameRegistration.getName());
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_NAME_TYPE, nameRegistration.getName()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));
		}
	}
	
	private void orphanNameRegistration(RegisterNameTransaction nameRegistration)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(nameRegistration.getName().getOwner().getAddress()))
		{
			//DELETE NAME
			this.database.getNamesDatabase().delete(nameRegistration.getName());
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_NAME_TYPE, nameRegistration.getName()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));
		}
	}
	
	private void processPollCreation(CreatePollTransaction pollCreation)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(pollCreation.getPoll().getCreator().getAddress()))
		{
			//ADD POLL
			this.database.getPollDatabase().add(pollCreation.getPoll());
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_POLL_TYPE, pollCreation.getPoll()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_POLL_TYPE, this.getPolls()));
		}
	}
	
	private void orphanPollCreation(CreatePollTransaction pollCreation)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(pollCreation.getPoll().getCreator().getAddress()))
		{
			//DELETE POLL
			this.database.getPollDatabase().delete(pollCreation.getPoll());
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_POLL_TYPE, pollCreation.getPoll()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_POLL_TYPE, this.getPolls()));
		}
	}
	
	private void processNameUpdate(UpdateNameTransaction nameUpdate)
	{
		//CHECK IF WE ARE OWNER
		if(this.accountExists(nameUpdate.getOwner().getAddress()))
		{
			//CHECK IF OWNER CHANGED
			if(!nameUpdate.getOwner().getAddress().equals(nameUpdate.getName().getOwner().getAddress()))
			{
				//DELETE PREVIOUS NAME
				Name name = DatabaseSet.getInstance().getUpdateNameDatabase().getOrphanData(nameUpdate);
				this.database.getNamesDatabase().delete(nameUpdate.getOwner(), name);
				
				//NOTIFY
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_NAME_TYPE, nameUpdate.getName()));
				
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));
			}
		}
		
		//CHECK IF WE ARE NEW OWNER
		if(this.accountExists(nameUpdate.getName().getOwner().getAddress()))
		{
			//ADD NAME
			this.database.getNamesDatabase().add(nameUpdate.getName());
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_NAME_TYPE, nameUpdate.getName()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));
		}
	}
	
	private void orphanNameUpdate(UpdateNameTransaction nameUpdate)
	{
		//CHECK IF WE WERE OWNER
		if(this.accountExists(nameUpdate.getOwner().getAddress()))
		{
			//CHECK IF OWNER WAS CHANGED
			if(!nameUpdate.getOwner().getAddress().equals(nameUpdate.getName().getOwner().getAddress()))
			{
				//ADD PREVIOUS  NAME
				Name name = DatabaseSet.getInstance().getNameDatabase().getName(nameUpdate.getName().getName());
				this.database.getNamesDatabase().add(name);
				
				//NOTIFY
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_NAME_TYPE, nameUpdate.getName()));
				
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));
			}
		}
		
		//CHECK IF WE WERE NEW OWNER
		if(this.accountExists(nameUpdate.getName().getOwner().getAddress()))
		{
			//ADD NAME
			this.database.getNamesDatabase().delete(nameUpdate.getName());
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_NAME_TYPE, nameUpdate.getName()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));
		}
	}
	
	private void processNameSale(SellNameTransaction nameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(nameSaleTransaction.getNameSale().getName().getOwner().getAddress()))
		{
			//ADD TO DATABASE
			this.database.getNameSalesDatabase().add(nameSaleTransaction.getNameSale());
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_NAME_SALE_TYPE, nameSaleTransaction.getNameSale()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
		}
	}
	
	private void orphanNameSale(SellNameTransaction nameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(nameSaleTransaction.getNameSale().getName().getOwner().getAddress()))
		{
			//REMOVE FROM DATABASE
			this.database.getNameSalesDatabase().delete(nameSaleTransaction.getNameSale());
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_NAME_SALE_TYPE, nameSaleTransaction.getNameSale()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
		}
	}

	private void processCancelNameSale(CancelSellNameTransaction cancelNameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(cancelNameSaleTransaction.getOwner().getAddress()))
		{
			//REMOVE FROM DATABASE
			BigDecimal amount = DatabaseSet.getInstance().getCancelSellNameDatabase().getOrphanData(cancelNameSaleTransaction);
			NameSale nameSale = new NameSale(cancelNameSaleTransaction.getName(), amount);
			this.database.getNameSalesDatabase().delete(nameSale);
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_NAME_SALE_TYPE, nameSale));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
		}
	}
	
	private void orphanCancelNameSale(CancelSellNameTransaction cancelNameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(cancelNameSaleTransaction.getOwner().getAddress()))
		{
			//ADD TO DATABASE
			NameSale nameSale = DatabaseSet.getInstance().getNameExchangeDatabase().getNameSale(cancelNameSaleTransaction.getName());
			this.database.getNameSalesDatabase().add(nameSale);
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_NAME_SALE_TYPE, nameSale));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
		}
	}
	
	private void processNamePurchase(BuyNameTransaction namePurchase)
	{
		//CHECK IF WE ARE BUYER
		if(this.accountExists(namePurchase.getBuyer().getAddress()))
		{
			//ADD NAME
			Name name = DatabaseSet.getInstance().getNameDatabase().getName(namePurchase.getNameSale().getKey());
			this.database.getNamesDatabase().add(name);
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_NAME_TYPE, name.getName()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));
		}
		
		//CHECK IF WE ARE SELLER
		Account seller = namePurchase.getSeller();
		if(this.accountExists(seller.getAddress()))
		{
			//DELETE NAMESALE
			this.database.getNameSalesDatabase().delete(seller, namePurchase.getNameSale());
			
			//DELETE NAME
			Name name = DatabaseSet.getInstance().getNameDatabase().getName(namePurchase.getNameSale().getKey());
			name.setOwner(seller);
			this.database.getNamesDatabase().delete(seller, name);
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_NAME_SALE_TYPE, namePurchase.getNameSale()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_NAME_TYPE, name));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));
		}
	}
	
	private void orphanNamePurchase(BuyNameTransaction namePurchase)
	{
		//CHECK IF WE WERE BUYER
		if(this.accountExists(namePurchase.getBuyer().getAddress()))
		{
			//DELETE NAME
			Name name = namePurchase.getNameSale().getName();
			name.setOwner(namePurchase.getBuyer());
			this.database.getNamesDatabase().delete(namePurchase.getBuyer(), name);
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_NAME_TYPE, name.getName()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));
		}
		
		//CHECK IF WE WERE SELLER
		Account seller = namePurchase.getSeller();
		if(this.accountExists(seller.getAddress()))
		{
			//ADD NAMESALE
			this.database.getNameSalesDatabase().add(namePurchase.getNameSale());
			
			//ADD NAME
			Name name = namePurchase.getNameSale().getName();
			this.database.getNamesDatabase().add(name);
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_NAME_SALE_TYPE, namePurchase.getNameSale()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_NAME_TYPE, name.getName()));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_TYPE, this.getNames()));
		}

	}
	
	@Override
	public void update(Observable o, Object arg) 
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE)
		{
			Block block = (Block) message.getValue();
				
			//CHECK BLOCK
			this.processBlock(block);
				
			//CHECK TRANSACTIONS
			for(Transaction transaction: block.getTransactions())
			{
				this.processTransaction(transaction);
				
				//CHECK IF NAME REGISTRATION
				if(transaction instanceof RegisterNameTransaction)
				{
					this.processNameRegistration((RegisterNameTransaction) transaction);
				}
				
				//CHECK IF NAME UPDATE
				if(transaction instanceof UpdateNameTransaction)
				{
					this.processNameUpdate((UpdateNameTransaction) transaction);
				}
				
				//CHECK IF NAME SALE
				if(transaction instanceof SellNameTransaction)
				{
					this.processNameSale((SellNameTransaction) transaction);
				}
				
				//CHECK IF NAME SALE
				if(transaction instanceof CancelSellNameTransaction)
				{
					this.processCancelNameSale((CancelSellNameTransaction) transaction);
				}
				
				//CHECK IF NAME PURCHASE
				if(transaction instanceof BuyNameTransaction)
				{
					this.processNamePurchase((BuyNameTransaction) transaction);
				}
				
				//CHECK IF POLL CREATION
				if(transaction instanceof CreatePollTransaction)
				{
					this.processPollCreation((CreatePollTransaction) transaction);
				}
			}
		}
		
		if(message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE)
		{	
			Transaction transaction = (Transaction) message.getValue();
				
			this.processTransaction(transaction);
			
			//CHECK IF NAME REGISTRATION
			if(transaction instanceof RegisterNameTransaction)
			{
				this.processNameRegistration((RegisterNameTransaction) transaction);
			}
			
			//CHECK IF POLL CREATION
			if(transaction instanceof CreatePollTransaction)
			{
				this.processPollCreation((CreatePollTransaction) transaction);
			}
		}
		
		if(message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE)
		{
			Block block = (Block) message.getValue();
				
			//CHECK BLOCK
			this.orphanBlock(block);
				
			//CHECK TRANSACTIONS
			for(Transaction transaction: block.getTransactions())
			{
				this.orphanTransaction(transaction);
				
				//CHECK IF NAME REGISTRATION
				if(transaction instanceof RegisterNameTransaction)
				{
					this.orphanNameRegistration((RegisterNameTransaction) transaction);
				}
				
				//CHECK IF NAME UPDATE
				if(transaction instanceof UpdateNameTransaction)
				{
					this.orphanNameUpdate((UpdateNameTransaction) transaction);
				}
				
				//CHECK IF NAME SALE
				if(transaction instanceof SellNameTransaction)
				{
					this.orphanNameSale((SellNameTransaction) transaction);
				}
				
				//CHECK IF CANCEL NAME SALE
				if(transaction instanceof CancelSellNameTransaction)
				{
					this.orphanCancelNameSale((CancelSellNameTransaction) transaction);
				}
				
				//CHECK IF CANCEL NAME SALE
				if(transaction instanceof BuyNameTransaction)
				{
					this.orphanNamePurchase((BuyNameTransaction) transaction);
				}
				
				//CHECK IF POLL CREATION
				if(transaction instanceof CreatePollTransaction)
				{
					this.orphanPollCreation((CreatePollTransaction) transaction);
				}
			}
		}
		
		if(message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{	
			Transaction transaction = (Transaction) message.getValue();
				
			this.orphanTransaction(transaction);
					
			//CHECK IF NAME REGISTRATION
			if(transaction instanceof RegisterNameTransaction)
			{
				this.orphanNameRegistration((RegisterNameTransaction) transaction);
			}
			
			//CHECK IF POLL CREATION
			if(transaction instanceof CreatePollTransaction)
			{
				this.orphanPollCreation((CreatePollTransaction) transaction);
			}
		}
	}

	//CLOSE
	
	public void close()
	{
		if(this.database != null)
		{
			this.database.close();
		}
		
		if(this.secureDatabase != null)
		{
			this.secureDatabase.close();
		}
	}

	public void commit() 
	{
		if(this.database != null)
		{
			this.database.commit();
		}
		
		if(this.secureDatabase != null)
		{
			this.secureDatabase.commit();
		}
		
	}	
}
