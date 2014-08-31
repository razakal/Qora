package qora.wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import controller.Controller;
import database.DBSet;
import database.wallet.SecureWalletDatabase;
import database.wallet.WalletDatabase;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.block.Block;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.CreatePollTransaction;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.UpdateNameTransaction;
import qora.transaction.VoteOnPollTransaction;
import qora.voting.Poll;
import utils.ObserverMessage;
import utils.Pair;

public class Wallet extends Observable implements Observer
{
	public static final int STATUS_UNLOCKED = 1;
	public static final int STATUS_LOCKED = 0;
	
	private WalletDatabase database;
	private SecureWalletDatabase secureDatabase;
	
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
		return this.database.getAccountMap().getAccounts();
	}
	
	public boolean accountExists(String address)
	{
		return this.database.getAccountMap().exists(address);
	}
	
	public Account getAccount(String address)
	{
		return this.database.getAccountMap().getAccount(address);
	}
	
	public BigDecimal getUnconfirmedBalance(String address)
	{
		return this.database.getAccountMap().getUnconfirmedBalance(address);
	}
	
	public List<PrivateKeyAccount> getprivateKeyAccounts()
	{
		if(this.secureDatabase == null)
		{
			return new ArrayList<PrivateKeyAccount>();
		}
		
		return this.secureDatabase.getAccountSeedMap().getPrivateKeyAccounts();
	}
	
	public PrivateKeyAccount getPrivateKeyAccount(String address)
	{
		if(this.secureDatabase == null)
		{
			return null;
		}
		
		return this.secureDatabase.getAccountSeedMap().getPrivateKeyAccount(address);
	}
	
	public boolean exists()
	{
		return WalletDatabase.exists();
	}
	
	public List<Pair<Account, Transaction>> getLastTransactions(int limit)
	{
		if(!this.exists())
		{
			new ArrayList<Pair<Account, Transaction>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getTransactionMap().get(accounts, limit);
	}
	
	public List<Transaction> getLastTransactions(Account account, int limit)
	{
		if(!this.exists())
		{
			return new ArrayList<Transaction>();
		}

		return this.database.getTransactionMap().get(account, limit);
	}
	
	public List<Pair<Account, Block>> getLastBlocks()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Block>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getBlockMap().get(accounts);
	}

	public List<Block> getLastBlocks(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Block>();
		}

		return this.database.getBlockMap().get(account);
	}
		
	public List<Pair<Account, Name>> getNames()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Name>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getNameMap().get(accounts);
	}
	
	public List<Name> getNames(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Name>();
		}

		return this.database.getNameMap().get(account);
	}
	
	public List<Pair<Account, NameSale>> getNameSales()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, NameSale>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getNameSaleMap().get(accounts);
	}
	
	public List<NameSale> getNameSales(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<NameSale>();
		}

		return this.database.getNameSaleMap().get(account);
	}
	
	public List<Pair<Account, Poll>> getPolls()
	{
		if(!this.exists())
		{
			return new ArrayList<Pair<Account, Poll>>();
		}

		List<Account> accounts = this.getAccounts();
		return this.database.getPollMap().get(accounts);
	}
	
	public List<Poll> getPolls(Account account)
	{
		if(!this.exists())
		{
			return new ArrayList<Poll>();
		}

		return this.database.getPollMap().get(account);
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
		    this.secureDatabase.getAccountSeedMap().add(account);
		    this.database.getAccountMap().add(account);
		    
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
				this.database.getAccountMap().update(account, account.getConfirmedBalance());
			}
		}
		
		//SCAN TRANSACTIONS
		Map<Account, List<Transaction>> transactions;
		synchronized(accounts)
		{
			transactions = Controller.getInstance().scanTransactions(accounts);
		}
		
		//DELETE TRANSACTIONS
		this.database.getTransactionMap().deleteAll(accounts);
		
		//ADD TRANSACTIONS
		this.database.getTransactionMap().addAll(transactions);
	    	
		//TODO SCAN UNCONFIRMED TRANSACTIONS    
	    	    
	    //SCAN BLOCKS
	    Map<Account, List<Block>> blocks;
	    synchronized(accounts)
		{
	    	blocks = Controller.getInstance().scanBlocks(accounts);
		}
	    
	    //DELETE BLOCKS
	  	this.database.getBlockMap().deleteAll(accounts);
	  	
	  	//ADD BLOCKS
	  	this.database.getBlockMap().addAll(blocks);
	    
	    //SCAN NAMES
	    Map<Account, List<Name>> names;
	    synchronized(accounts)
		{
	    	names = Controller.getInstance().scanNames(accounts);
		}
	    
	    //DELETE NAMES
	  	this.database.getNameMap().deleteAll(accounts);
	  	
	  	//ADD NAMES
	  	this.database.getNameMap().addAll(names);
	  	
	  	//TODO SCAN UNCONFIRMED NAMES
	    
	  	//SCAN NAMESALES
	    Map<Account, List<NameSale>> nameSales;
	    synchronized(accounts)
		{
	    	nameSales = Controller.getInstance().scanNameSales(accounts);
		}
	    
	    //DELETE NAMESALES
	  	this.database.getNameSaleMap().deleteAll(accounts);
	  	
	  	//ADD NAMES
	  	this.database.getNameSaleMap().addAll(nameSales);
	  	
	  	//SCAN POLLS
	  	Map<Account, List<Poll>> polls;
	  	synchronized(accounts)
	  	{
	  		polls = Controller.getInstance().scanPolls(accounts);
	  	}
	  	
	  	//DELETE POLLS
	  	this.database.getPollMap().deleteAll(accounts);
	  	
	  	//TODO SCAN ASSETS
		Map<Account, List<Asset>> assets;
	  	synchronized(accounts)
	  	{
	  		assets = Controller.getInstance().scanAssets(accounts);
	  	}
	  	
	  	//ADD ASSETS
	  	this.database.getAssetMap().addAll(assets);
	  	
	  	//ADD POLLS
	  	this.database.getPollMap().addAll(polls);
	  	
	  	//SET LAST BLOCK
	  	this.database.setLastBlockSignature(Controller.getInstance().getLastBlock().getSignature());
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
		    this.secureDatabase.getAccountSeedMap().add(account);
		    this.database.getAccountMap().add(account);
		    
		    //SYNCHRONIZE
		    this.synchronize();
		    
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
		
		//REGISTER ON TRANSACTIONS
		this.database.getTransactionMap().addObserver(o);
		
		//REGISTER ON BLOCKS
		this.database.getBlockMap().addObserver(o);
		
		//REGISTER ON NAMES
		this.database.getNameMap().addObserver(o);
		
		//REGISTER ON NAME SALES
		this.database.getNameSaleMap().addObserver(o);
		
		//REGISTER ON POLLS
		this.database.getPollMap().addObserver(o);
		
		//REGISTER ON ASSETS
		this.database.getAssetMap().addObserver(o);
		
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
		List<Account> accounts = this.getAccounts();	
		synchronized(accounts)
		{		
			for(Account account: accounts)
			{
				//CHECK IF INVOLVED
				if(transaction.isInvolved(account))
				{
					//ADD TO ACCOUNT TRANSACTIONS
					if(!this.database.getTransactionMap().add(account, transaction))
					{					
						//UPDATE UNCONFIRMED BALANCE
						BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(account.getAddress()).add(transaction.getAmount(account));
						this.database.getAccountMap().update(account, unconfirmedBalance);
					}
				}
			}
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
					this.database.getTransactionMap().delete(account, transaction);
					
					//UPDATE UNCONFIRMED BALANCE
					BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(account.getAddress()).subtract(transaction.getAmount(account));
					this.database.getAccountMap().update(account, unconfirmedBalance);
				}
			}
		}
	}

	private void processBlock(Block block)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE NEED TO RESYNC
		byte[] lastBlockSignature = this.database.getLastBlockSignature();
		if(lastBlockSignature == null || !Arrays.equals(lastBlockSignature, block.getReference()))
		{
			Logger.getGlobal().info("Wallet not synchronized with current blockchain: synchronizing wallet.");
			this.synchronize();
		}
		
		//SET AS LAST BLOCK
		this.database.setLastBlockSignature(block.getSignature());
			
		//CHECK IF WE ARE GENERATOR
		if(this.accountExists(block.getGenerator().getAddress()))
		{
			//ADD BLOCK
			this.database.getBlockMap().add(block);
				
			//KEEP TRACK OF UNCONFIRMED BALANCE
			BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(block.getGenerator().getAddress()).add(block.getTotalFee());
			this.database.getAccountMap().update(block.getGenerator(), unconfirmedBalance);
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
			this.database.getBlockMap().delete(block);
			
			//KEEP TRACK OF UNCONFIRMED BALANCE
			BigDecimal unconfirmedBalance = this.getUnconfirmedBalance(block.getGenerator().getAddress()).subtract(block.getTotalFee());
			this.database.getAccountMap().update(block.getGenerator(), unconfirmedBalance);
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
			this.database.getNameMap().add(nameRegistration.getName());
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
			this.database.getNameMap().delete(nameRegistration.getName());
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
			this.database.getPollMap().add(pollCreation.getPoll());
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
			this.database.getPollMap().delete(pollCreation.getPoll());
		}
	}

	private void processPollVote(VoteOnPollTransaction pollVote)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		Poll poll = DBSet.getInstance().getPollMap().get(pollVote.getPoll());
		if(this.accountExists(poll.getCreator().getAddress()))
		{
			//UPDATE POLL
			this.database.getPollMap().add(poll);
		}
	}
	
	private void orphanPollVote(VoteOnPollTransaction pollVote)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
				
		//CHECK IF WE ARE OWNER
		Poll poll = DBSet.getInstance().getPollMap().get(pollVote.getPoll());
		if(this.accountExists(poll.getCreator().getAddress()))
		{
			//UPDATE POLL
			this.database.getPollMap().add(poll);
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
				Name name = DBSet.getInstance().getUpdateNameMap().get(nameUpdate);
				this.database.getNameMap().delete(nameUpdate.getOwner(), name);
			}
		}
		
		//CHECK IF WE ARE NEW OWNER
		if(this.accountExists(nameUpdate.getName().getOwner().getAddress()))
		{
			//ADD NAME
			this.database.getNameMap().add(nameUpdate.getName());
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
				Name name = DBSet.getInstance().getNameMap().get(nameUpdate.getName().getName());
				this.database.getNameMap().add(name);

			}
		}
		
		//CHECK IF WE WERE NEW OWNER
		if(this.accountExists(nameUpdate.getName().getOwner().getAddress()))
		{
			//ADD NAME
			this.database.getNameMap().delete(nameUpdate.getName());
		}
	}
	
	private void processNameSale(SellNameTransaction nameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(nameSaleTransaction.getNameSale().getName().getOwner().getAddress()))
		{
			//ADD TO DATABASE
			this.database.getNameSaleMap().add(nameSaleTransaction.getNameSale());
		}
	}
	
	private void orphanNameSale(SellNameTransaction nameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(nameSaleTransaction.getNameSale().getName().getOwner().getAddress()))
		{
			//REMOVE FROM DATABASE
			this.database.getNameSaleMap().delete(nameSaleTransaction.getNameSale());
		}
	}

	private void processCancelNameSale(CancelSellNameTransaction cancelNameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(cancelNameSaleTransaction.getOwner().getAddress()))
		{
			//REMOVE FROM DATABASE
			BigDecimal amount = DBSet.getInstance().getCancelSellNameMap().get(cancelNameSaleTransaction);
			NameSale nameSale = new NameSale(cancelNameSaleTransaction.getName(), amount);
			this.database.getNameSaleMap().delete(nameSale);
		}
	}
	
	private void orphanCancelNameSale(CancelSellNameTransaction cancelNameSaleTransaction)
	{
		//CHECK IF WE ARE SELLER
		if(this.accountExists(cancelNameSaleTransaction.getOwner().getAddress()))
		{
			//ADD TO DATABASE
			NameSale nameSale = DBSet.getInstance().getNameExchangeMap().getNameSale(cancelNameSaleTransaction.getName());
			this.database.getNameSaleMap().add(nameSale);
		}
	}
	
	private void processNamePurchase(BuyNameTransaction namePurchase)
	{
		//CHECK IF WE ARE BUYER
		if(this.accountExists(namePurchase.getBuyer().getAddress()))
		{
			//ADD NAME
			Name name = DBSet.getInstance().getNameMap().get(namePurchase.getNameSale().getKey());
			this.database.getNameMap().add(name);
		}
		
		//CHECK IF WE ARE SELLER
		Account seller = namePurchase.getSeller();
		if(this.accountExists(seller.getAddress()))
		{
			//DELETE NAMESALE
			this.database.getNameSaleMap().delete(seller, namePurchase.getNameSale());
			
			//DELETE NAME
			Name name = DBSet.getInstance().getNameMap().get(namePurchase.getNameSale().getKey());
			name.setOwner(seller);
			this.database.getNameMap().delete(seller, name);
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
			this.database.getNameMap().delete(namePurchase.getBuyer(), name);
		}
		
		//CHECK IF WE WERE SELLER
		Account seller = namePurchase.getSeller();
		if(this.accountExists(seller.getAddress()))
		{
			//ADD NAMESALE
			this.database.getNameSaleMap().add(namePurchase.getNameSale());
			
			//ADD NAME
			Name name = namePurchase.getNameSale().getName();
			this.database.getNameMap().add(name);
		}
	}
	
	private void processAssetIssue(IssueAssetTransaction assetIssue)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(assetIssue.getAsset().getOwner().getAddress()))
		{
			//ADD POLL
			this.database.getAssetMap().add(assetIssue.getAsset());
		}
	}
	
	private void orphanAssetIssue(IssueAssetTransaction assetIssue)
	{
		//CHECK IF WALLET IS OPEN
		if(!this.exists())
		{
			return;
		}
		
		//CHECK IF WE ARE OWNER
		if(this.accountExists(assetIssue.getAsset().getOwner().getAddress()))
		{
			//DELETE ASSET
			this.database.getAssetMap().delete(assetIssue.getAsset());
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
				
				//CHECK IF POLL VOTE
				if(transaction instanceof VoteOnPollTransaction)
				{
					this.processPollVote((VoteOnPollTransaction) transaction);
				}
				
				//CHECK IF ASSET ISSUE
				if(transaction instanceof IssueAssetTransaction)
				{
					this.processAssetIssue((IssueAssetTransaction) transaction);
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
			
			//CHECK IF ASSET ISSUE
			if(transaction instanceof IssueAssetTransaction)
			{
				this.processAssetIssue((IssueAssetTransaction) transaction);
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
				
				//CHECK IF POLL VOTE
				if(transaction instanceof VoteOnPollTransaction)
				{
					this.orphanPollVote((VoteOnPollTransaction) transaction);
				}
				
				//CHECK IF ASSET ISSUE
				if(transaction instanceof IssueAssetTransaction)
				{
					this.orphanAssetIssue((IssueAssetTransaction) transaction);
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
			
			//CHECK IF ASSET ISSUE
			if(transaction instanceof IssueAssetTransaction)
			{
				this.orphanAssetIssue((IssueAssetTransaction) transaction);
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
