package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import ntp.NTP;

import org.junit.Test;

import qora.BlockGenerator;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.block.Block;
import qora.block.GenesisBlock;
import qora.crypto.Crypto;
import qora.transaction.GenesisTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.Transaction;
import database.DBSet;

public class GeneratorTests {

	@Test
	public void generateNewBlock() 
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
		
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//GENERATE 2000 NEXT BLOCKS
		Block lastBlock = genesisBlock;
		BlockGenerator blockGenerator = new BlockGenerator();
		for(int i=0; i<2000; i++)
		{	
			//GENERATE NEXT BLOCK
			Block newBlock = blockGenerator.generateNextBlock(databaseSet, generator, lastBlock);
			
			//ADD TRANSACTION SIGNATURE
			byte[] transactionsSignature = Crypto.getInstance().sign(generator, newBlock.getGeneratorSignature());
			newBlock.setTransactionsSignature(transactionsSignature);
			
			//CHECK IF BLOCK SIGNATURE IS VALID
			assertEquals(true, newBlock.isSignatureValid());
			
			//CHECK IF BLOCK IS VALID
			assertEquals(true, newBlock.isValid(databaseSet));
			
			//PROCESS NEW BLOCK
			newBlock.process(databaseSet);
			
			//LAST BLOCK IS NEW BLOCK
			lastBlock = newBlock;
		}
	}
	
	@Test
	public void addTransactions()
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(100000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
		//ADD 10 UNCONFIRMED VALID TRANSACTIONS	
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		DBSet snapshot = databaseSet.fork();
		for(int i=0; i<10; i++)
		{
			long timestamp = NTP.getTime();
			byte[] signature = PaymentTransaction.generateSignature(snapshot, generator, recipient, BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
				
			//CREATE VALID PAYMENT
			Transaction payment = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(snapshot), signature);
		
			//PROCESS IN DB
			payment.process(snapshot);
			
			//ADD TO UNCONFIRMED TRANSACTIONS
			blockGenerator.addUnconfirmedTransaction(databaseSet, payment, false);
		}
		
		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		blockGenerator.addUnconfirmedTransactions(databaseSet, newBlock);
		
		//CHECK IF BLOCK IS VALID
		assertEquals(true, newBlock.isValid(databaseSet));
	}
	
	@Test
	public void addManyTransactions()
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//PROCESS GENESISBLOCK
		GenesisBlock genesisBlock = new GenesisBlock();
		genesisBlock.process(databaseSet);
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount generator = new PrivateKeyAccount(privateKey);
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE GENERATOR HAS FUNDS
		Transaction transaction = new GenesisTransaction(generator, BigDecimal.valueOf(100000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//GENERATE NEXT BLOCK
		BlockGenerator blockGenerator = new BlockGenerator();
		Block newBlock = blockGenerator.generateNextBlock(databaseSet, generator, genesisBlock);
		
		//ADD 10 UNCONFIRMED VALID TRANSACTIONS	
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		DBSet snapshot = databaseSet.fork();
		for(int i=0; i<10000; i++)
		{
			long timestamp = NTP.getTime();
			byte[] signature = PaymentTransaction.generateSignature(snapshot, generator, recipient, BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
				
			//CREATE VALID PAYMENT
			Transaction payment = new PaymentTransaction(generator, recipient, BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, generator.getLastReference(snapshot), signature);
		
			//PROCESS IN DB
			payment.process(snapshot);
			
			//ADD TO UNCONFIRMED TRANSACTIONS
			blockGenerator.addUnconfirmedTransaction(databaseSet, payment, false);
		}
		
		//ADD UNCONFIRMED TRANSACTIONS TO BLOCK
		blockGenerator.addUnconfirmedTransactions(databaseSet, newBlock);
		
		//CHECK THAT NOT ALL TRANSACTIONS WERE ADDED TO BLOCK
		assertNotEquals(10000, newBlock.getTransactionCount());
		
		//CHECK IF BLOCK IS VALID
		assertEquals(true, newBlock.isValid(databaseSet));
	}
	
	//TODO CALCULATETRANSACTIONSIGNATURE
}
