package database.wallet;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import qora.account.PrivateKeyAccount;
import settings.Settings;

public class SecureWalletDatabase 
{
	private static final File SECURE_WALLET_FILE = new File(Settings.getInstance().getWalletDir(), "wallet.s.dat");
	
	private static final String SEED = "seed";
	private static final String NONCE = "nonce";
	
	private DB database;
	
	private AccountSeedsDatabase accountSeedsDatabase;
	
	public static boolean exists()
	{
		return SECURE_WALLET_FILE.exists();
	}
	
	public SecureWalletDatabase(String password)
	{
		//OPEN WALLET
		SECURE_WALLET_FILE.getParentFile().mkdirs();
				
		//DELETE TRANSACTIONS
		File transactionFile = new File(Settings.getInstance().getWalletDir(), "wallet.s.dat.t");
		transactionFile.delete();	
		
		this.database = DBMaker.newFileDB(SECURE_WALLET_FILE)
						.encryptionEnable(password)
			    		.closeOnJvmShutdown()
			            .make();
			    
		this.accountSeedsDatabase = new AccountSeedsDatabase(this, this.database);
	}
	
	public AccountSeedsDatabase getAccountSeedsDatabase()
	{
		return this.accountSeedsDatabase;
	}

	public void setSeed(byte[] seed) 
	{
		this.database.createAtomicVar(SEED, seed, Serializer.BYTE_ARRAY);
	}
	
	public byte[] getSeed()
	{
		return (byte[]) this.database.getAtomicVar(SEED).get();
	}

	public void setNonce(int nonce) 
	{
		 this.database.getAtomicInteger(NONCE).set(nonce);	
	}
	
	public int getNonce()
	{
		return this.database.getAtomicInteger(NONCE).intValue();
	}
	
	public int getAndIncrementNonce()
	{
		return this.database.getAtomicInteger(NONCE).getAndIncrement();
	}
	
	public void delete(PrivateKeyAccount account)
	{
		this.accountSeedsDatabase.delete(account);
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

