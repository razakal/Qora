package test;

/*import static org.junit.Assert.*;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import qora.crypto.Crypto;
import qora.wallet.Wallet;*/

public class WalletTests {
/*
	@Test
	public void createWallet() throws Exception
	{
		//CREATE DATABASE
		DB database = DBMaker.newMemoryDB().make();
		DB secureDatabase = DBMaker.newMemoryDB().make();
	
		//CREATE WALLET
		Wallet wallet = new Wallet();
		boolean create = wallet.create(database, secureDatabase, Crypto.getInstance().digest("test".getBytes()), 10, false);
		
		//CHECK CREATE
		assertEquals(true, create);
		
		//CHECK VERSION
		assertEquals(1, wallet.getVersion());
		
		//CHECK ADDRESSES
		assertEquals(10, wallet.getAccounts().size());
			
		//CHECK PRIVATE KEYS
		assertEquals(10, wallet.getprivateKeyAccounts().size());
		
		//CHECK LAST BLOCKS
		assertNotNull(wallet.getLastBlocks());
		
		//CHECK LAST TRANSACTIONS
		assertNotNull(wallet.getLastTransactions());
	}
	
	@Test
	public void lockUnlock()
	{
		//CREATE DATABASE
		DB database = DBMaker.newMemoryDB().make();
		DB secureDatabase = DBMaker.newMemoryDB().make();
			
		//CREATE WALLET
		Wallet wallet = new Wallet();
		wallet.create(database, secureDatabase, Crypto.getInstance().digest("test".getBytes()), 10, false);
			
		//CHECK UNLOCKED
		assertEquals(true, wallet.isUnlocked());
		
		//LOCK
		wallet.lock();
		
		//CHECK LOCKED
		assertEquals(false, wallet.isUnlocked());
		
		//CHECK ACCOUNTS
		assertEquals(null, wallet.getprivateKeyAccounts());
		
		//UNLOCK
		wallet.unlock(secureDatabase);
		
		//CHECK UNLOCKED
		assertEquals(true, wallet.isUnlocked());
		
		//CHECK ACCOUNTS
		assertEquals(10, wallet.getprivateKeyAccounts().size());
	}
	
	*/
	
}
