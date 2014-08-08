package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import ntp.NTP;

import org.junit.Test;

import database.DBSet;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.crypto.Crypto;
import qora.crypto.Ed25519;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.CreatePollTransaction;
import qora.transaction.GenesisTransaction;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;
import qora.transaction.UpdateNameTransaction;
import qora.transaction.VoteOnPollTransaction;
import qora.voting.Poll;
import qora.voting.PollOption;

public class TransactionTests {

	//GENESIS
	
	@Test
	public void validateSignatureGenesisTransaction() 
	{
		Ed25519.load();
		
		//CHECK VALID SIGNATURE
		Transaction transaction = new GenesisTransaction(new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g"), BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		assertEquals(true, transaction.isSignatureValid());
	}
	
	@Test
	public void validateGenesisTransaction() 
	{
		Ed25519.load();
		
		//CREATE MEMORYDB
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//CHECK NORMAL VALID
		Transaction transaction = new GenesisTransaction(new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g"), BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		assertEquals(Transaction.VALIDATE_OKE, transaction.isValid(databaseSet));
		
		//CHECK INVALID ADDRESS
		transaction = new GenesisTransaction(new Account("test"), BigDecimal.valueOf(-1000).setScale(8), NTP.getTime());
		assertNotEquals(Transaction.VALIDATE_OKE, transaction.isValid(databaseSet));
		
		//CHECK NEGATIVE AMOUNT
		transaction = new GenesisTransaction(new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g"), BigDecimal.valueOf(-1000).setScale(8), NTP.getTime());
		assertNotEquals(Transaction.VALIDATE_OKE, transaction.isValid(databaseSet));
	}
	
	@Test
	public void parseGenesisTransaction() 
	{
		//CREATE TRANSACTION
		Account account = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		Transaction transaction = new GenesisTransaction(account, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		
		//CONVERT TO BYTES
		byte[] rawTransaction = transaction.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			Transaction parsedTransaction = TransactionFactory.getInstance().parse(rawTransaction);
			
			//CHECK INSTANCE
			assertEquals(true, parsedTransaction instanceof GenesisTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(transaction.getSignature(), parsedTransaction.getSignature()));
			
			//CHECK AMOUNT
			assertEquals(transaction.getAmount(account), parsedTransaction.getAmount(account));			
			
			//CHECK TIMESTAMP
			assertEquals(transaction.getTimestamp(), parsedTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawTransaction = new byte[transaction.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawTransaction);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OKE
		}	
	}
	
	@Test
	public void processGenesisTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//PROCESS TRANSACTION
		Account account = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		Transaction transaction = new GenesisTransaction(account, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CHECK AMOUNT
		assertEquals(BigDecimal.valueOf(1000).setScale(8), account.getConfirmedBalance(databaseSet));
		
		//CHECK REFERENCE
		assertEquals(true, Arrays.equals(transaction.getSignature(), account.getLastReference(databaseSet)));
	}
	
	@Test
	public void orphanGenesisTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//PROCESS TRANSACTION
		Account account = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		Transaction transaction = new GenesisTransaction(account, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//ORPHAN TRANSACTION
		transaction.orphan(databaseSet);
		
		//CHECK AMOUNT
		assertEquals(BigDecimal.ZERO, account.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE
		assertEquals(true, Arrays.equals(new byte[0], account.getLastReference(databaseSet)));
	}
	
	//PAYMENT
	
	@Test
	public void validateSignaturePaymentTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = NTP.getTime();
		byte[] signature = PaymentTransaction.generateSignature(databaseSet, sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		
		//CREATE PAYMENT
		Transaction payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF PAYMENT SIGNATURE IS VALID
		assertEquals(true, payment.isSignatureValid());
		
		//INVALID SIGNATURE
		payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp+1, sender.getLastReference(databaseSet), new byte[0]);
		
		//CHECK IF PAYMENT SIGNATURE IS INVALID
		assertEquals(false, payment.isSignatureValid());
	}
	
	@Test
	public void validatePaymentTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = NTP.getTime();
		byte[] signature = PaymentTransaction.generateSignature(databaseSet, sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE VALID PAYMENT
		Transaction payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);

		//CHECK IF PAYMENT IS VALID
		assertEquals(Transaction.VALIDATE_OKE, payment.isValid(databaseSet));
		
		//CREATE INVALID PAYMENT INVALID RECIPIENT ADDRESS
		payment = new PaymentTransaction(sender, new Account("test"), BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
	
		//CHECK IF PAYMENT IS INVALID
		assertNotEquals(Transaction.VALIDATE_OKE, payment.isValid(databaseSet));
		
		//CREATE INVALID PAYMENT NEGATIVE AMOUNT
		payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(-100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF PAYMENT IS INVALID
		assertNotEquals(Transaction.VALIDATE_OKE, payment.isValid(databaseSet));	
		
		//CREATE INVALID PAYMENT NEGATIVE FEE
		payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(-1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
				
		//CHECK IF PAYMENT IS INVALID
		assertNotEquals(Transaction.VALIDATE_OKE, payment.isValid(databaseSet));	
		
		//CREATE INVALID PAYMENT WRONG REFERENCE
		payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, new byte[0], signature);
						
		//CHECK IF PAYMENT IS INVALID
		assertNotEquals(Transaction.VALIDATE_OKE, payment.isValid(databaseSet));	
	}
	
	@Test
	public void parsePaymentTransaction() 
	{
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
						
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = NTP.getTime();
		byte[] signature = PaymentTransaction.generateSignature(databaseSet, sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE VALID PAYMENT
		Transaction payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CONVERT TO BYTES
		byte[] rawPayment = payment.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			PaymentTransaction parsedPayment = (PaymentTransaction) TransactionFactory.getInstance().parse(rawPayment);
			
			//CHECK INSTANCE
			assertEquals(true, parsedPayment instanceof PaymentTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(payment.getSignature(), parsedPayment.getSignature()));
			
			//CHECK AMOUNT SENDER
			assertEquals(payment.getAmount(sender), parsedPayment.getAmount(sender));	
			
			//CHECK AMOUNT RECIPIENT
			assertEquals(payment.getAmount(recipient), parsedPayment.getAmount(recipient));	
			
			//CHECK FEE
			assertEquals(payment.getFee(), parsedPayment.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(payment.getReference(), parsedPayment.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(payment.getTimestamp(), parsedPayment.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawPayment = new byte[payment.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawPayment);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OKE
		}	
	}
	
	@Test
	public void processPaymentTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
					
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
			
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
			
		//CREATE SIGNATURE
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = NTP.getTime();
		byte[] signature = PaymentTransaction.generateSignature(databaseSet, sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
			
		//CREATE PAYMENT
		Transaction payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		payment.process(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(899).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.valueOf(100).setScale(8), recipient.getConfirmedBalance(databaseSet));
		
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(payment.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(payment.getSignature(), recipient.getLastReference(databaseSet)));
		
		//CREATE SIGNATURE
		signature = PaymentTransaction.generateSignature(databaseSet, sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		
		//CREATE PAYMENT
		payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		payment.process(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(798).setScale(8), sender.getConfirmedBalance(databaseSet));
						
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.valueOf(200).setScale(8), recipient.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(payment.getSignature(), sender.getLastReference(databaseSet)));
					
		//CHECK REFERENCE RECIPIENT NOT CHANGED
		assertEquals(true, Arrays.equals(payment.getReference(), recipient.getLastReference(databaseSet)));
	}
	
	@Test
	public void orphanPaymentTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
					
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
			
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
			
		//CREATE SIGNATURE
		Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		long timestamp = NTP.getTime();
		byte[] signature = PaymentTransaction.generateSignature(databaseSet, sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
			
		//CREATE PAYMENT
		Transaction payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		payment.process(databaseSet);
		
		//CREATE PAYMENT2
		signature = PaymentTransaction.generateSignature(databaseSet, sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		Transaction payment2  = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		payment.process(databaseSet);
		
		//ORPHAN PAYMENT
		payment2.orphan(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(899).setScale(8), sender.getConfirmedBalance(databaseSet));
						
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.valueOf(100).setScale(8), recipient.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(payment.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(payment.getSignature(), recipient.getLastReference(databaseSet)));

		//ORPHAN PAYMENT
		payment.orphan(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), sender.getConfirmedBalance(databaseSet));
								
		//CHECK BALANCE RECIPIENT
		assertEquals(BigDecimal.valueOf(0).setScale(8), recipient.getConfirmedBalance(databaseSet));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
						
		//CHECK REFERENCE RECIPIENT
		assertEquals(true, Arrays.equals(new byte[0], recipient.getLastReference(databaseSet)));
	}

	//REGISTER NAME
	
	@Test
	public void validateSignatureRegisterNameTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE NAME
		Name name = new Name(sender, "test", "this is the value");
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
		
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(true, nameRegistration.isSignatureValid());
		
		//INVALID SIGNATURE
		nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), new byte[0]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, nameRegistration.isSignatureValid());
	}
	
	@Test
	public void validateRegisterNameTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(Transaction.VALIDATE_OKE, nameRegistration.isValid(databaseSet));
		nameRegistration.process(databaseSet);
		
		//CREATE INVALID NAME REGISTRATION INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		name = new Name(sender, longName, "this is the value");
		nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		

		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, nameRegistration.isValid(databaseSet));
		
		//CREATE INVALID NAME REGISTRATION INVALID NAME LENGTH
		String longValue = "";
		for(int i=1; i<10000; i++)
		{
			longValue += "oke";
		}
		name = new Name(sender, "test2", longValue);
		nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		

		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_VALUE_LENGTH, nameRegistration.isValid(databaseSet));
		
		//CREATE INVALID NAME REGISTRATION NAME ALREADY TAKEN
		name = new Name(sender, "test", "this is the value");
		nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NAME_ALREADY_REGISTRED, nameRegistration.isValid(databaseSet));
		
		//CREATE INVALID NAME NOT ENOUGH BALANCE
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		name = new Name(invalidOwner, "test2", "this is the value");
		nameRegistration = new RegisterNameTransaction(invalidOwner, name, BigDecimal.ONE.setScale(8), timestamp, invalidOwner.getLastReference(databaseSet), signature);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NO_BALANCE, nameRegistration.isValid(databaseSet));
		
		//CREATE NAME REGISTRATION INVALID REFERENCE
		name = new Name(sender, "test2", "this is the value");
		nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, invalidOwner.getLastReference(databaseSet), signature);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, nameRegistration.isValid(databaseSet));
		
		//CREATE NAME REGISTRATION INVALID FEE
		name = new Name(sender, "test2", "this is the value");
		nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ZERO.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NEGATIVE_FEE, nameRegistration.isValid(databaseSet));
	}

	@Test
	public void parseRegisterNameTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE NAME REGISTRATION
		RegisterNameTransaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CONVERT TO BYTES
		byte[] rawNameRegistration = nameRegistration.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			RegisterNameTransaction parsedRegistration = (RegisterNameTransaction) TransactionFactory.getInstance().parse(rawNameRegistration);
			
			//CHECK INSTANCE
			assertEquals(true, parsedRegistration instanceof RegisterNameTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(nameRegistration.getSignature(), parsedRegistration.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(nameRegistration.getAmount(sender), parsedRegistration.getAmount(sender));	
			
			//CHECK NAME OWNER
			assertEquals(nameRegistration.getName().getOwner().getAddress(), parsedRegistration.getName().getOwner().getAddress());	
			
			//CHECK NAME NAME
			assertEquals(nameRegistration.getName().getName(), parsedRegistration.getName().getName());	
			
			//CHECK NAME VALUE
			assertEquals(nameRegistration.getName().getValue(), parsedRegistration.getName().getValue());	
			
			//CHECK FEE
			assertEquals(nameRegistration.getFee(), parsedRegistration.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(nameRegistration.getReference(), parsedRegistration.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(nameRegistration.getTimestamp(), parsedRegistration.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawNameRegistration = new byte[nameRegistration.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawNameRegistration);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OKE
		}	
	}

	@Test
	public void processRegisterNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameRegistration.process(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(999).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameRegistration.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK NAME EXISTS
		assertEquals(true, databaseSet.getNameMap().contains(name));
	}
	
	@Test
	public void orphanRegisterNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameRegistration.process(databaseSet);
		nameRegistration.orphan(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK NAME EXISTS
		assertEquals(false, databaseSet.getNameMap().contains(name));
	}

	//UPDATE NAME
	
	@Test
	public void validateSignatureUpdateNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE NAME
		Name name = new Name(sender, "test", "this is the value");
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = UpdateNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.ONE.setScale(8), timestamp);
		
		//CREATE NAME UPDATE
		Transaction nameUpdate = new UpdateNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(true, nameUpdate.isSignatureValid());
		
		//INVALID SIGNATURE
		nameUpdate = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), new byte[0]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, nameUpdate.isSignatureValid());

	}
	
	@Test
	public void validateUpdateNameTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(Transaction.VALIDATE_OKE, nameRegistration.isValid(databaseSet));
		nameRegistration.process(databaseSet);
		
		//CREATE NAME UPDATE
		name.setValue("new value");
		Transaction nameUpdate = new UpdateNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
	
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(Transaction.VALIDATE_OKE, nameUpdate.isValid(databaseSet));
		
		//CREATE INVALID NAME UPDATE INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		name = new Name(sender, longName, "this is the value");
		nameUpdate = new UpdateNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		

		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, nameUpdate.isValid(databaseSet));
		
		//CREATE INVALID NAME UPDATE NAME DOES NOT EXIST
		name = new Name(sender, "test2", "this is the value");
		nameUpdate = new UpdateNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NAME_DOES_NOT_EXIST, nameUpdate.isValid(databaseSet));
		
		//CREATE INVALID NAME UPDATE INCORRECT OWNER
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		name = new Name(invalidOwner, "test2", "this is the value");
		nameRegistration = new RegisterNameTransaction(invalidOwner, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		nameRegistration.process(databaseSet);	
		
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.INVALID_NAME_OWNER, nameUpdate.isValid(databaseSet));
				
		//CREATE INVALID NAME UPDATE NO BALANCE
		name = new Name(invalidOwner, "test2", "this is the value");
		nameUpdate = new UpdateNameTransaction(invalidOwner, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NO_BALANCE, nameUpdate.isValid(databaseSet));
				
		//CREATE NAME UPDATE INVALID REFERENCE
		name = new Name(sender, "test", "this is the value");
		nameUpdate = new UpdateNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, new byte[]{}, signature);
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, nameUpdate.isValid(databaseSet));
		
		//CREATE NAME REGISTRATION INVALID FEE
		name = new Name(sender, "test", "this is the value");
		nameUpdate = new UpdateNameTransaction(sender, name, BigDecimal.ZERO.setScale(8).subtract(BigDecimal.ONE), timestamp, sender.getLastReference(databaseSet), signature);
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NEGATIVE_FEE, nameUpdate.isValid(databaseSet));
	}

	@Test
	public void parseUpdateNameTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE NAME UPDATE
		UpdateNameTransaction nameUpdate = new UpdateNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CONVERT TO BYTES
		byte[] rawNameUpdate = nameUpdate.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			UpdateNameTransaction parsedUpdate = (UpdateNameTransaction) TransactionFactory.getInstance().parse(rawNameUpdate);
			
			//CHECK INSTANCE
			assertEquals(true, parsedUpdate instanceof UpdateNameTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(nameUpdate.getSignature(), parsedUpdate.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(nameUpdate.getAmount(sender), parsedUpdate.getAmount(sender));	
			
			//CHECK OWNER
			assertEquals(nameUpdate.getOwner().getAddress(), parsedUpdate.getOwner().getAddress());	
			
			//CHECK NAME OWNER
			assertEquals(nameUpdate.getName().getOwner().getAddress(), parsedUpdate.getName().getOwner().getAddress());	
			
			//CHECK NAME NAME
			assertEquals(nameUpdate.getName().getName(), parsedUpdate.getName().getName());	
			
			//CHECK NAME VALUE
			assertEquals(nameUpdate.getName().getValue(), parsedUpdate.getName().getValue());	
			
			//CHECK FEE
			assertEquals(nameUpdate.getFee(), parsedUpdate.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(nameUpdate.getReference(), parsedUpdate.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(nameUpdate.getTimestamp(), parsedUpdate.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawNameUpdate = new byte[nameUpdate.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawNameUpdate);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OKE
		}	
	}

	@Test
	public void processUpdateNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameRegistration.process(databaseSet);
		
		//CREATE NAME UPDATE
		name = new Name(new Account("XYLEQnuvhracK2WMN3Hjif67knkJe9hTQn"), "test", "new value");
		Transaction nameUpdate = new UpdateNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameUpdate.process(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(998).setScale(8), sender.getConfirmedBalance(databaseSet));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameUpdate.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME EXISTS
		assertEquals(true, databaseSet.getNameMap().contains(name));
		
		//CHECK NAME VALUE
		name =  databaseSet.getNameMap().get("test");
		assertEquals("new value", name.getValue());
		
		//CHECK NAME OWNER
		assertEquals("XYLEQnuvhracK2WMN3Hjif67knkJe9hTQn", name.getOwner().getAddress());
	}

	
	@Test
	public void orphanUpdateNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameRegistration.process(databaseSet);
		
		//CREATE NAME UPDATE
		name = new Name(new Account("XYLEQnuvhracK2WMN3Hjif67knkJe9hTQn"), "test", "new value");
		Transaction nameUpdate = new UpdateNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameUpdate.process(databaseSet);
		nameUpdate.orphan(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(999).setScale(8), sender.getConfirmedBalance(databaseSet));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameRegistration.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME EXISTS
		assertEquals(true, databaseSet.getNameMap().contains(name));
		
		//CHECK NAME VALUE
		name =  databaseSet.getNameMap().get("test");
		assertEquals("new value", name.getValue());
		
		//CHECK NAME OWNER
		assertEquals(sender.getAddress(), name.getOwner().getAddress());
	}
	
	//SELL NAME
	
	@Test
	public void validateSignatureSellNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE NAME
		NameSale nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = SellNameTransaction.generateSignature(databaseSet, sender, nameSale, BigDecimal.ONE.setScale(8), timestamp);
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(true, nameSaleTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), new byte[0]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, nameSaleTransaction.isSignatureValid());
	}
	
	@Test
	public void validateSellNameTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(Transaction.VALIDATE_OKE, nameRegistration.isValid(databaseSet));
		nameRegistration.process(databaseSet);
		
		//CREATE NAME SALE
		NameSale nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
	
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(Transaction.VALIDATE_OKE, nameSaleTransaction.isValid(databaseSet));
		
		//CREATE INVALID NAME SALE INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		nameSale = new NameSale(longName, BigDecimal.ONE.setScale(8));
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		

		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, nameSaleTransaction.isValid(databaseSet));
		
		//CREATE INVALID NAME SALE NAME DOES NOT EXIST
		nameSale = new NameSale("test2", BigDecimal.ONE.setScale(8));
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NAME_DOES_NOT_EXIST, nameSaleTransaction.isValid(databaseSet));
		
		//CREATE INVALID NAME UPDATE INCORRECT OWNER
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		name = new Name(invalidOwner, "test2", "this is the value");
		nameRegistration = new RegisterNameTransaction(invalidOwner, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		nameRegistration.process(databaseSet);	
		
		//CHECK IF NAME UPDATE IS INVALID
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		assertEquals(Transaction.INVALID_NAME_OWNER, nameSaleTransaction.isValid(databaseSet));
				
		//CREATE INVALID NAME UPDATE NO BALANCE
		nameSale = new NameSale("test2", BigDecimal.ONE.setScale(8));
		nameSaleTransaction = new SellNameTransaction(invalidOwner, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NO_BALANCE, nameSaleTransaction.isValid(databaseSet));
				
		//CREATE NAME UPDATE INVALID REFERENCE
		nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, new byte[]{}, signature);
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, nameSaleTransaction.isValid(databaseSet));
		
		//CREATE NAME REGISTRATION INVALID FEE
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ZERO.setScale(8).subtract(BigDecimal.ONE), timestamp, sender.getLastReference(databaseSet), signature);
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NEGATIVE_FEE, nameSaleTransaction.isValid(databaseSet));
		
		//CREATE NAME UPDATE PROCESS 
		nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		nameSaleTransaction.process(databaseSet);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NAME_ALREADY_FOR_SALE, nameSaleTransaction.isValid(databaseSet));
	}

	@Test
	public void parseSellNameTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1).setScale(8));
		byte[] signature = SellNameTransaction.generateSignature(databaseSet, sender, nameSale, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE NAME UPDATE
		SellNameTransaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CONVERT TO BYTES
		byte[] rawNameSale = nameSaleTransaction.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			SellNameTransaction parsedNameSale = (SellNameTransaction) TransactionFactory.getInstance().parse(rawNameSale);
			
			//CHECK INSTANCE
			assertEquals(true, parsedNameSale instanceof SellNameTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(nameSaleTransaction.getSignature(), parsedNameSale.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(nameSaleTransaction.getAmount(sender), parsedNameSale.getAmount(sender));	
			
			//CHECK OWNER
			assertEquals(nameSaleTransaction.getOwner().getAddress(), parsedNameSale.getOwner().getAddress());	
			
			//CHECK NAMESALE NAME
			assertEquals(nameSaleTransaction.getNameSale().getKey(), parsedNameSale.getNameSale().getKey());	
			
			//CHECK NAMESALE AMOUNT
			assertEquals(nameSaleTransaction.getNameSale().getAmount(), parsedNameSale.getNameSale().getAmount());	
			
			//CHECK FEE
			assertEquals(nameSaleTransaction.getFee(), parsedNameSale.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(nameSaleTransaction.getReference(), parsedNameSale.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(nameSaleTransaction.getTimestamp(), parsedNameSale.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawNameSale = new byte[nameSaleTransaction.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawNameSale);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OKE
		}	
	}

	@Test
	public void processSellNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameRegistration.process(databaseSet);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		signature = SellNameTransaction.generateSignature(databaseSet, sender, nameSale, BigDecimal.valueOf(1).setScale(8), timestamp);			
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameSaleTransaction.process(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(998).setScale(8), sender.getConfirmedBalance(databaseSet));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameSaleTransaction.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME SALE EXISTS
		assertEquals(true, databaseSet.getNameExchangeMap().contains("test"));
		
		//CHECK NAME SALE AMOUNT
		nameSale =  databaseSet.getNameExchangeMap().getNameSale("test");
		assertEquals(BigDecimal.ONE.setScale(8), nameSale.getAmount());
	}

	@Test
	public void orphanSellNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameRegistration.process(databaseSet);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		signature = SellNameTransaction.generateSignature(databaseSet, sender, nameSale, BigDecimal.valueOf(1).setScale(8), timestamp);			
						
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameSaleTransaction.process(databaseSet);
		nameSaleTransaction.orphan(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(999).setScale(8), sender.getConfirmedBalance(databaseSet));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameRegistration.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME SALE EXISTS
		assertEquals(false, databaseSet.getNameExchangeMap().contains("test"));
	}
	
	
	//CANCEL SELL NAME
	
	@Test
	public void validateSignatureCancelSellNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = CancelSellNameTransaction.generateSignature(databaseSet, sender, "test", BigDecimal.ONE.setScale(8), timestamp);
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new CancelSellNameTransaction(sender, "test", BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(true, nameSaleTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		nameSaleTransaction = new CancelSellNameTransaction(sender, "test", BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), new byte[0]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, nameSaleTransaction.isSignatureValid());
	}
	
	@Test
	public void validateCancelSellNameTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(Transaction.VALIDATE_OKE, nameRegistration.isValid(databaseSet));
		nameRegistration.process(databaseSet);
		
		//CREATE NAME SALE
		NameSale nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
	
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(Transaction.VALIDATE_OKE, nameSaleTransaction.isValid(databaseSet));
		nameSaleTransaction.process(databaseSet);
		
		//CREATE CANCEL NAME SALE
		CancelSellNameTransaction cancelNameSaleTransaction = new CancelSellNameTransaction(sender, nameSale.getKey(), BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		

		//CHECK IF CANCEL NAME UPDATE IS VALID
		assertEquals(Transaction.VALIDATE_OKE, cancelNameSaleTransaction.isValid(databaseSet));
		
		//CREATE INVALID CANCEL NAME SALE INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		
		cancelNameSaleTransaction = new CancelSellNameTransaction(sender, longName, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		

		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, cancelNameSaleTransaction.isValid(databaseSet));
		
		//CREATE INVALID CANCEL NAME SALE NAME DOES NOT EXIST
		cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test2", BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NAME_DOES_NOT_EXIST, cancelNameSaleTransaction.isValid(databaseSet));
		
		//CREATE INVALID NAME UPDATE INCORRECT OWNER
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		name = new Name(invalidOwner, "test2", "this is the value");
		nameRegistration = new RegisterNameTransaction(invalidOwner, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		nameRegistration.process(databaseSet);	
		
		//CREATE NAME SALE
		nameSale = new NameSale("test2", BigDecimal.ONE.setScale(8));
		nameSaleTransaction = new SellNameTransaction(invalidOwner, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		nameSaleTransaction.process(databaseSet);	
		
		//CHECK IF NAME UPDATE IS INVALID
		cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test2", BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		assertEquals(Transaction.INVALID_NAME_OWNER, cancelNameSaleTransaction.isValid(databaseSet));
				
		//CREATE INVALID NAME UPDATE NO BALANCE
		cancelNameSaleTransaction = new CancelSellNameTransaction(invalidOwner, "test2", BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
				
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NO_BALANCE, cancelNameSaleTransaction.isValid(databaseSet));
				
		//CREATE NAME UPDATE INVALID REFERENCE
		cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test", BigDecimal.ONE.setScale(8), timestamp, new byte[]{}, signature);
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, cancelNameSaleTransaction.isValid(databaseSet));
		
		//CREATE NAME REGISTRATION INVALID FEE
		cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test", BigDecimal.ZERO.setScale(8).subtract(BigDecimal.ONE), timestamp, sender.getLastReference(databaseSet), signature);
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NEGATIVE_FEE, cancelNameSaleTransaction.isValid(databaseSet));
		
		//CREATE NAME UPDATE PROCESS 
		cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test", BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		cancelNameSaleTransaction.process(databaseSet);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NAME_NOT_FOR_SALE, cancelNameSaleTransaction.isValid(databaseSet));
	}

	@Test
	public void parseCancelSellNameTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = CancelSellNameTransaction.generateSignature(databaseSet, sender, "test", BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE CANCEL NAME SALE
		CancelSellNameTransaction cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test", BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CONVERT TO BYTES
		byte[] rawCancelNameSale = cancelNameSaleTransaction.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			CancelSellNameTransaction parsedCancelNameSale = (CancelSellNameTransaction) TransactionFactory.getInstance().parse(rawCancelNameSale);
			
			//CHECK INSTANCE
			assertEquals(true, parsedCancelNameSale instanceof CancelSellNameTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(cancelNameSaleTransaction.getSignature(), parsedCancelNameSale.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(cancelNameSaleTransaction.getAmount(sender), parsedCancelNameSale.getAmount(sender));	
			
			//CHECK OWNER
			assertEquals(cancelNameSaleTransaction.getOwner().getAddress(), parsedCancelNameSale.getOwner().getAddress());	
			
			//CHECK NAME
			assertEquals(cancelNameSaleTransaction.getName(), parsedCancelNameSale.getName());	
			
			//CHECK FEE
			assertEquals(cancelNameSaleTransaction.getFee(), parsedCancelNameSale.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(cancelNameSaleTransaction.getReference(), parsedCancelNameSale.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(cancelNameSaleTransaction.getTimestamp(), parsedCancelNameSale.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawCancelNameSale = new byte[cancelNameSaleTransaction.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawCancelNameSale);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OKE
		}	
	}
	
	@Test
	public void processCancelSellNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameRegistration.process(databaseSet);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		signature = SellNameTransaction.generateSignature(databaseSet, sender, nameSale, BigDecimal.valueOf(1).setScale(8), timestamp);			
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameSaleTransaction.process(databaseSet);
		
		//CREATE SIGNATURE
		signature = CancelSellNameTransaction.generateSignature(databaseSet, sender, "test", BigDecimal.valueOf(1).setScale(8), timestamp);			
			
		//CREATE CANCEL NAME SALE
		Transaction cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test", BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		cancelNameSaleTransaction.process(databaseSet);	
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(997).setScale(8), sender.getConfirmedBalance(databaseSet));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(cancelNameSaleTransaction.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME SALE EXISTS
		assertEquals(false, databaseSet.getNameExchangeMap().contains("test"));
	}

	@Test
	public void orphanCancelSellNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameRegistration.process(databaseSet);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		signature = SellNameTransaction.generateSignature(databaseSet, sender, nameSale, BigDecimal.valueOf(1).setScale(8), timestamp);			
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameSaleTransaction.process(databaseSet);
		
		//CREATE SIGNATURE
		signature = CancelSellNameTransaction.generateSignature(databaseSet, sender, "test", BigDecimal.valueOf(1).setScale(8), timestamp);			
			
		//CREATE CANCEL NAME SALE
		Transaction cancelNameSaleTransaction = new CancelSellNameTransaction(sender, "test", BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		cancelNameSaleTransaction.process(databaseSet);	
		cancelNameSaleTransaction.orphan(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(998).setScale(8), sender.getConfirmedBalance(databaseSet));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(nameSaleTransaction.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK NAME SALE EXISTS
		assertEquals(true, databaseSet.getNameExchangeMap().contains("test"));
		
		//CHECK NAME SALE AMOUNT
		nameSale =  databaseSet.getNameExchangeMap().getNameSale("test");
		assertEquals(BigDecimal.ONE.setScale(8), nameSale.getAmount());
	}
	
	//BUY NAME
	
	@Test
	public void validateSignatureBuyNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		NameSale nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		byte[] signature = BuyNameTransaction.generateSignature(databaseSet, sender, nameSale, nameSale.getName(databaseSet).getOwner(), BigDecimal.ONE.setScale(8), timestamp);
		
		//CREATE NAME SALE
		Transaction buyNameTransaction = new BuyNameTransaction(sender, nameSale, nameSale.getName(databaseSet).getOwner(), BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(true, buyNameTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		buyNameTransaction = new BuyNameTransaction(sender,nameSale, nameSale.getName(databaseSet).getOwner(), BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), new byte[0]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, buyNameTransaction.isSignatureValid());
	}
	
	@Test
	public void validateBuyNameTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//CREATE KNOWN ACCOUNT
		seed = Crypto.getInstance().digest("buyer".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount buyer = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE BUYER HAS FUNDS
		transaction = new GenesisTransaction(buyer, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF NAME REGISTRATION IS VALID
		assertEquals(Transaction.VALIDATE_OKE, nameRegistration.isValid(databaseSet));
		nameRegistration.process(databaseSet);
		
		//CREATE NAME SALE
		NameSale nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
	
		//CHECK IF NAME UPDATE IS VALID
		assertEquals(Transaction.VALIDATE_OKE, nameSaleTransaction.isValid(databaseSet));
		nameSaleTransaction.process(databaseSet);
		
		//CREATE NAME PURCHASE
		BuyNameTransaction namePurchaseTransaction = new BuyNameTransaction(buyer, nameSale, nameSale.getName(databaseSet).getOwner(), BigDecimal.ONE.setScale(8), timestamp, buyer.getLastReference(databaseSet), signature);		

		//CHECK IF NAME PURCHASE IS VALID
		assertEquals(Transaction.VALIDATE_OKE, namePurchaseTransaction.isValid(databaseSet));
		
		//CREATE INVALID NAME PURCHASE INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		
		nameSale = new NameSale(longName, nameSale.getAmount());
		namePurchaseTransaction = new BuyNameTransaction(buyer, nameSale, nameSale.getName(databaseSet).getOwner(), BigDecimal.ONE.setScale(8), timestamp, buyer.getLastReference(databaseSet), signature);		

		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, namePurchaseTransaction.isValid(databaseSet));
		
		//CREATE INVALID NAME PURCHASE NAME DOES NOT EXIST
		nameSale = new NameSale("test2", BigDecimal.ONE.setScale(8));
		namePurchaseTransaction = new BuyNameTransaction(buyer, nameSale,nameSale.getName(databaseSet).getOwner(), BigDecimal.ONE.setScale(8), timestamp, buyer.getLastReference(databaseSet), signature);		
		
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NAME_DOES_NOT_EXIST, namePurchaseTransaction.isValid(databaseSet));
		
		//CREATE INVALID NAME PURCHASE NAME NOT FOR SALE
		Name test2 = new Name(sender, "test2", "oke");
		databaseSet.getNameMap().add(test2);
		
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NAME_NOT_FOR_SALE, namePurchaseTransaction.isValid(databaseSet));
						
		//CREATE INVALID NAME PURCHASE ALREADY OWNER
		nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		namePurchaseTransaction = new BuyNameTransaction(sender, nameSale,nameSale.getName(databaseSet).getOwner(), BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		
		
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.BUYER_ALREADY_OWNER, namePurchaseTransaction.isValid(databaseSet));
				
		//CREATE INVALID NAME UPDATE NO BALANCE
		buyer.setConfirmedBalance(BigDecimal.ZERO.setScale(8), databaseSet);
		namePurchaseTransaction = new BuyNameTransaction(buyer, nameSale,nameSale.getName(databaseSet).getOwner(),BigDecimal.ONE.setScale(8), timestamp, buyer.getLastReference(databaseSet), signature);		
		
		//CHECK IF NAME UPDATE IS INVALID
		assertEquals(Transaction.NO_BALANCE, namePurchaseTransaction.isValid(databaseSet));
		buyer.setConfirmedBalance(BigDecimal.valueOf(1000).setScale(8), databaseSet);
				
		//CREATE NAME UPDATE INVALID REFERENCE
		namePurchaseTransaction = new BuyNameTransaction(buyer, nameSale, nameSale.getName(databaseSet).getOwner(),BigDecimal.ONE.setScale(8), timestamp, new byte[]{}, signature);		
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, namePurchaseTransaction.isValid(databaseSet));
		
		//CREATE NAME REGISTRATION INVALID FEE
		namePurchaseTransaction = new BuyNameTransaction(buyer, nameSale, nameSale.getName(databaseSet).getOwner(), BigDecimal.ZERO.setScale(8).subtract(BigDecimal.ONE), timestamp, buyer.getLastReference(databaseSet), signature);
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.NEGATIVE_FEE, namePurchaseTransaction.isValid(databaseSet));
	}

	@Test
	public void parseBuyNameTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		NameSale nameSale = new NameSale("test", BigDecimal.valueOf(1).setScale(8));
		byte[] signature = BuyNameTransaction.generateSignature(databaseSet, sender, nameSale,nameSale.getName(databaseSet).getOwner(), BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE CANCEL NAME SALE
		BuyNameTransaction namePurchaseTransaction = new BuyNameTransaction(sender, nameSale, nameSale.getName(databaseSet).getOwner(), BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CONVERT TO BYTES
		byte[] rawNamePurchase = namePurchaseTransaction.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			BuyNameTransaction parsedNamePurchase = (BuyNameTransaction) TransactionFactory.getInstance().parse(rawNamePurchase);
			
			//CHECK INSTANCE
			assertEquals(true, parsedNamePurchase instanceof BuyNameTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(namePurchaseTransaction.getSignature(), parsedNamePurchase.getSignature()));
			
			//CHECK AMOUNT BUYER
			assertEquals(namePurchaseTransaction.getAmount(sender), parsedNamePurchase.getAmount(sender));	
			
			//CHECK OWNER
			assertEquals(namePurchaseTransaction.getBuyer().getAddress(), parsedNamePurchase.getBuyer().getAddress());	
			
			//CHECK NAME
			assertEquals(namePurchaseTransaction.getNameSale().getKey(), parsedNamePurchase.getNameSale().getKey());	
		
			//CHECK FEE
			assertEquals(namePurchaseTransaction.getFee(), parsedNamePurchase.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(namePurchaseTransaction.getReference(), parsedNamePurchase.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(namePurchaseTransaction.getTimestamp(), parsedNamePurchase.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawNamePurchase = new byte[namePurchaseTransaction.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawNamePurchase);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OKE
		}	
	}
	
	@Test
	public void processBuyNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//CREATE KNOWN ACCOUNT
		seed = Crypto.getInstance().digest("buyer".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount buyer = new PrivateKeyAccount(privateKey);		
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE BUYER HAS FUNDS
		transaction = new GenesisTransaction(buyer, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);			
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameRegistration.process(databaseSet);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		signature = SellNameTransaction.generateSignature(databaseSet, sender, nameSale, BigDecimal.valueOf(1).setScale(8), timestamp);			
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameSaleTransaction.process(databaseSet);
		
		//CREATE SIGNATURE
		signature = CancelSellNameTransaction.generateSignature(databaseSet, sender, "test", BigDecimal.valueOf(1).setScale(8), timestamp);			
			
		//CREATE NAME PURCHASE
		Transaction purchaseNameTransaction = new BuyNameTransaction(buyer, nameSale, nameSale.getName(databaseSet).getOwner(), BigDecimal.ONE.setScale(8), timestamp, buyer.getLastReference(databaseSet), signature);			
		purchaseNameTransaction.process(databaseSet);	
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(998).setScale(8), buyer.getConfirmedBalance(databaseSet));
		
		//CHECK BALANCE SELLER
		assertEquals(BigDecimal.valueOf(999).setScale(8), sender.getConfirmedBalance(databaseSet));
						
		//CHECK REFERENCE BUYER
		assertEquals(true, Arrays.equals(purchaseNameTransaction.getSignature(), buyer.getLastReference(databaseSet)));
				
		//CHECK NAME OWNER
		name = databaseSet.getNameMap().get("test");
		assertEquals(name.getOwner().getAddress(), buyer.getAddress());
	
		//CHECK NAME SALE EXISTS
		assertEquals(false, databaseSet.getNameExchangeMap().contains("test"));
	}

	@Test
	public void orphanBuyNameTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//CREATE KNOWN ACCOUNT
		seed = Crypto.getInstance().digest("buyer".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount buyer = new PrivateKeyAccount(privateKey);		
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE BUYER HAS FUNDS
		transaction = new GenesisTransaction(buyer, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);			
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Name name = new Name(sender, "test", "this is the value");
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, sender, name, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE NAME REGISTRATION
		Transaction nameRegistration = new RegisterNameTransaction(sender, name, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameRegistration.process(databaseSet);
		
		//CREATE SIGNATURE
		NameSale nameSale = new NameSale("test", BigDecimal.ONE.setScale(8));
		signature = SellNameTransaction.generateSignature(databaseSet, sender, nameSale, BigDecimal.valueOf(1).setScale(8), timestamp);			
		
		//CREATE NAME SALE
		Transaction nameSaleTransaction = new SellNameTransaction(sender, nameSale, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		nameSaleTransaction.process(databaseSet);
		
		//CREATE SIGNATURE
		signature = CancelSellNameTransaction.generateSignature(databaseSet, sender, "test", BigDecimal.valueOf(1).setScale(8), timestamp);			
			
		//CREATE NAME PURCHASE
		Transaction purchaseNameTransaction = new BuyNameTransaction(buyer, nameSale, nameSale.getName(databaseSet).getOwner(), BigDecimal.ONE.setScale(8), timestamp, buyer.getLastReference(databaseSet), signature);			
		purchaseNameTransaction.process(databaseSet);	
		purchaseNameTransaction.orphan(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), buyer.getConfirmedBalance(databaseSet));
		
		//CHECK BALANCE SELLER
		assertEquals(BigDecimal.valueOf(998).setScale(8), sender.getConfirmedBalance(databaseSet));
						
		//CHECK REFERENCE BUYER
		assertEquals(true, Arrays.equals(transaction.getSignature(), buyer.getLastReference(databaseSet)));
				
		//CHECK NAME OWNER
		name = databaseSet.getNameMap().get("test");
		assertEquals(name.getOwner().getAddress(), sender.getAddress());
	
		//CHECK NAME SALE EXISTS
		assertEquals(true, databaseSet.getNameExchangeMap().contains("test"));
	}
	
	//CREATE POLL
	
	@Test
	public void validateSignatureCreatePollTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE POLL
		Poll poll = new Poll(sender, "test", "this is the value", new ArrayList<PollOption>());
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = CreatePollTransaction.generateSignature(databaseSet, sender, poll, BigDecimal.valueOf(1).setScale(8), timestamp);
		
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF POLL CREATION IS VALID
		assertEquals(true, pollCreation.isSignatureValid());
		
		//INVALID SIGNATURE
		pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), new byte[0]);
		
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(false, pollCreation.isSignatureValid());
	}
		
	@Test
	public void validateCreatePollTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test")));
		byte[] signature = CreatePollTransaction.generateSignature(databaseSet, sender, poll, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF POLL CREATION IS VALID
		assertEquals(Transaction.VALIDATE_OKE, pollCreation.isValid(databaseSet));
		pollCreation.process(databaseSet);
		
		//CREATE INVALID POLL CREATION INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		poll = new Poll(sender, longName, "this is the value", Arrays.asList(new PollOption("test")));
		pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		

		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, pollCreation.isValid(databaseSet));
		
		//CREATE INVALID POLL CREATION INVALID DESCRIPTION LENGTH
		String longDescription = "";
		for(int i=1; i<10000; i++)
		{
			longDescription += "oke";
		}
		poll = new Poll(sender, "test2", longDescription, Arrays.asList(new PollOption("test")));
		pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		

		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_DESCRIPTION_LENGTH, pollCreation.isValid(databaseSet));
		
		//CREATE INVALID POLL CREATION NAME ALREADY TAKEN
		poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test")));
		pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.POLL_ALREADY_CREATED, pollCreation.isValid(databaseSet));
		
		//CREATE INVALID POLL CREATION NO OPTIONS 
		poll = new Poll(sender, "test2", "this is the value", new ArrayList<PollOption>());
		pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_OPTIONS_LENGTH, pollCreation.isValid(databaseSet));
		
		//CREATE INVALID POLL CREATION INVALID OPTION LENGTH
		poll = new Poll(sender, "test2", "this is the value", Arrays.asList(new PollOption(longName)));
		pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		
				
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_OPTION_LENGTH, pollCreation.isValid(databaseSet));
		
		//CREATE INVALID POLL CREATION INVALID DUPLICATE OPTIONS
		poll = new Poll(sender, "test2", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("test")));
		pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		
						
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.DUPLICATE_OPTION, pollCreation.isValid(databaseSet));
		
		//CREATE INVALID POLL CREATION NOT ENOUGH BALANCE
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		poll = new Poll(sender, "test2", "this is the value", Arrays.asList(new PollOption("test")));
		pollCreation = new CreatePollTransaction(invalidOwner, poll, BigDecimal.ONE.setScale(8), timestamp, invalidOwner.getLastReference(databaseSet), signature);		
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.NO_BALANCE, pollCreation.isValid(databaseSet));
		
		//CREATE POLL CREATION INVALID REFERENCE
		poll = new Poll(sender, "test2", "this is the value", Arrays.asList(new PollOption("test")));
		pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, invalidOwner.getLastReference(databaseSet), signature);		
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, pollCreation.isValid(databaseSet));
		
		//CREATE POLL CREATION INVALID FEE
		poll = new Poll(sender, "test2", "this is the value", Arrays.asList(new PollOption("test")));
		pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ZERO.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);		
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.NEGATIVE_FEE, pollCreation.isValid(databaseSet));
	}

	@Test
	public void parseCreatePollTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("second option")));
		byte[] signature = CreatePollTransaction.generateSignature(databaseSet, sender, poll, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE POLL CREATION
		CreatePollTransaction pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CONVERT TO BYTES
		byte[] rawPollCreation = pollCreation.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			CreatePollTransaction parsedPollCreation = (CreatePollTransaction) TransactionFactory.getInstance().parse(rawPollCreation);
			
			//CHECK INSTANCE
			assertEquals(true, parsedPollCreation instanceof CreatePollTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(pollCreation.getSignature(), parsedPollCreation.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(pollCreation.getAmount(sender), parsedPollCreation.getAmount(sender));	
			
			//CHECK POLL CREATOR
			assertEquals(pollCreation.getPoll().getCreator().getAddress(), parsedPollCreation.getPoll().getCreator().getAddress());	
			
			//CHECK POLL NAME
			assertEquals(pollCreation.getPoll().getName(), parsedPollCreation.getPoll().getName());	
			
			//CHECK POLL DESCRIPTION
			assertEquals(pollCreation.getPoll().getDescription(), parsedPollCreation.getPoll().getDescription());	
			
			//CHECK POLL OPTIONS SIZE
			assertEquals(pollCreation.getPoll().getOptions().size(), parsedPollCreation.getPoll().getOptions().size());	
			
			//CHECK POLL OPTIONS
			for(int i=0; i<pollCreation.getPoll().getOptions().size(); i++)
			{
				//CHECK OPTION NAME
				assertEquals(pollCreation.getPoll().getOptions().get(i).getName(), parsedPollCreation.getPoll().getOptions().get(i).getName());	
			}
			
			//CHECK FEE
			assertEquals(pollCreation.getFee(), parsedPollCreation.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(pollCreation.getReference(), parsedPollCreation.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(pollCreation.getTimestamp(), parsedPollCreation.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawPollCreation = new byte[pollCreation.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawPollCreation);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OKE
		}	
	}

	@Test
	public void processCreatePollTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("second option")));
		byte[] signature = CreatePollTransaction.generateSignature(databaseSet, sender, poll, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		pollCreation.process(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(999).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(pollCreation.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK POLL EXISTS
		assertEquals(true, databaseSet.getPollMap().contains(poll));
	}
	
	@Test
	public void orphanCreatePollTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("second option")));
		byte[] signature = CreatePollTransaction.generateSignature(databaseSet, sender, poll, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);				
		pollCreation.process(databaseSet);
		pollCreation.orphan(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK POLL EXISTS
		assertEquals(false, databaseSet.getPollMap().contains(poll));
	}
	
	//VOTE ON POLL
	
	@Test
	public void validateSignatureVoteOnPollTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = VoteOnPollTransaction.generateSignature(databaseSet, sender, "test", 5, BigDecimal.valueOf(1).setScale(8), timestamp);
		
		//CREATE POLL VOTE
		Transaction pollVote = new VoteOnPollTransaction(sender, "test", 5, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF POLL VOTE IS VALID
		assertEquals(true, pollVote.isSignatureValid());
		
		//INVALID SIGNATURE
		pollVote = new VoteOnPollTransaction(sender, "test", 5, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), new byte[0]);
		
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(false, pollVote.isSignatureValid());
	}
		
	@Test
	public void validateVoteOnPollTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("test2")));
		byte[] signature = CreatePollTransaction.generateSignature(databaseSet, sender, poll, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF POLL CREATION IS VALID
		assertEquals(Transaction.VALIDATE_OKE, pollCreation.isValid(databaseSet));
		pollCreation.process(databaseSet);
		
		//CREATE POLL VOTE
		signature = VoteOnPollTransaction.generateSignature(databaseSet, sender, poll.getName(), 0, BigDecimal.valueOf(1).setScale(8), timestamp);
		Transaction pollVote = new VoteOnPollTransaction(sender, poll.getName(), 0, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF POLL VOTE IS VALID
		assertEquals(Transaction.VALIDATE_OKE, pollVote.isValid(databaseSet));
		pollVote.process(databaseSet);
		
		//CREATE INVALID POLL VOTE INVALID NAME LENGTH
		String longName = "";
		for(int i=1; i<1000; i++)
		{
			longName += "oke";
		}
		pollVote = new VoteOnPollTransaction(sender, longName, 0, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	

		//CHECK IF POLL VOTE IS INVALID
		assertEquals(Transaction.INVALID_NAME_LENGTH, pollVote.isValid(databaseSet));
		
		//CREATE INVALID POLL VOTE POLL DOES NOT EXIST
		pollVote = new VoteOnPollTransaction(sender, "test2", 0, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(Transaction.POLL_NO_EXISTS, pollVote.isValid(databaseSet));
		
		//CREATE INVALID POLL VOTE INVALID OPTION
		pollVote = new VoteOnPollTransaction(sender, "test", 5, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(Transaction.OPTION_NO_EXISTS, pollVote.isValid(databaseSet));
		
		//CREATE INVALID POLL VOTE INVALID OPTION
		pollVote = new VoteOnPollTransaction(sender, "test", -1, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
				
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(Transaction.OPTION_NO_EXISTS, pollVote.isValid(databaseSet));
		
		//CRTEATE INVALID POLL VOTE VOTED ALREADY
		pollVote = new VoteOnPollTransaction(sender, "test", 0, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		pollVote.process(databaseSet);
		
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(Transaction.ALREADY_VOTED_FOR_THAT_OPTION, pollVote.isValid(databaseSet));
		
		//CREATE INVALID POLL VOTE NOT ENOUGH BALANCE
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		pollVote = new VoteOnPollTransaction(invalidOwner, "test", 0, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(Transaction.NO_BALANCE, pollVote.isValid(databaseSet));
		
		//CREATE POLL CREATION INVALID REFERENCE
		pollVote = new VoteOnPollTransaction(sender, "test", 1, BigDecimal.ONE.setScale(8), timestamp, invalidOwner.getLastReference(databaseSet), signature);	
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, pollVote.isValid(databaseSet));
		
		//CREATE POLL CREATION INVALID FEE
		pollVote = new VoteOnPollTransaction(sender, "test", 1, BigDecimal.ZERO.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF POLL CREATION IS INVALID
		assertEquals(Transaction.NEGATIVE_FEE, pollVote.isValid(databaseSet));
	}

	
	@Test
	public void parseVoteOnPollTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = VoteOnPollTransaction.generateSignature(databaseSet, sender, "test", 0, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE POLL Vote
		VoteOnPollTransaction pollVote = new VoteOnPollTransaction(sender, "test", 0, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CONVERT TO BYTES
		byte[] rawPollVote = pollVote.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			VoteOnPollTransaction parsedPollVote = (VoteOnPollTransaction) TransactionFactory.getInstance().parse(rawPollVote);
			
			//CHECK INSTANCE
			assertEquals(true, parsedPollVote instanceof VoteOnPollTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(pollVote.getSignature(), parsedPollVote.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(pollVote.getAmount(sender), parsedPollVote.getAmount(sender));	
			
			//CHECK CREATOR
			assertEquals(pollVote.getCreator().getAddress(), parsedPollVote.getCreator().getAddress());	
			
			//CHECK POLL
			assertEquals(pollVote.getPoll(), parsedPollVote.getPoll());	
			
			//CHECK POLL OPTION
			assertEquals(pollVote.getOption(), parsedPollVote.getOption());	
			
			//CHECK FEE
			assertEquals(pollVote.getFee(), parsedPollVote.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(pollVote.getReference(), parsedPollVote.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(pollVote.getTimestamp(), parsedPollVote.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawPollVote = new byte[pollVote.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawPollVote);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OKE
		}	
	}

	
	@Test
	public void processVoteOnPollTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("second option")));
		byte[] signature = CreatePollTransaction.generateSignature(databaseSet, sender, poll, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		pollCreation.process(databaseSet);
		
		//CREATE POLL VOTE
		Transaction pollVote = new VoteOnPollTransaction(sender, poll.getName(), 0, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		pollVote.process(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(998).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(pollVote.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK POLL VOTER
		assertEquals(true, databaseSet.getPollMap().get(poll.getName()).getOptions().get(0).hasVoter(sender));
		
		//CREATE POLL VOTE
		pollVote = new VoteOnPollTransaction(sender, poll.getName(), 1, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		pollVote.process(databaseSet);
				
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(997).setScale(8), sender.getConfirmedBalance(databaseSet));
						
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(pollVote.getSignature(), sender.getLastReference(databaseSet)));
				
		//CHECK POLL VOTER
		assertEquals(false, databaseSet.getPollMap().get(poll.getName()).getOptions().get(0).hasVoter(sender));
		
		//CHECK POLL VOTER
		assertEquals(true, databaseSet.getPollMap().get(poll.getName()).getOptions().get(1).hasVoter(sender));
	}
	
	@Test
	public void orphanVoteOnPollTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Poll poll = new Poll(sender, "test", "this is the value", Arrays.asList(new PollOption("test"), new PollOption("second option")));
		byte[] signature = CreatePollTransaction.generateSignature(databaseSet, sender, poll, BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE POLL CREATION
		Transaction pollCreation = new CreatePollTransaction(sender, poll, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		pollCreation.process(databaseSet);
		
		//CREATE POLL VOTE
		signature = VoteOnPollTransaction.generateSignature(databaseSet, sender, poll.getName(), 0, BigDecimal.valueOf(1).setScale(8), timestamp);
		Transaction pollVote = new VoteOnPollTransaction(sender, poll.getName(), 0, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);			
		pollVote.process(databaseSet);
		pollVote.orphan(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(999).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(pollCreation.getSignature(), sender.getLastReference(databaseSet)));
		
		//CHECK POLL VOTER
		assertEquals(false, databaseSet.getPollMap().get(poll.getName()).hasVotes());
		
		//CHECK POLL VOTER
		assertEquals(false, databaseSet.getPollMap().get(poll.getName()).getOptions().get(0).hasVoter(sender));

	}
	
	//ARBITRARY TRANSACTION
	
	@Test
	public void validateSignatureArbitraryTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = ArbitraryTransaction.generateSignature(databaseSet, sender, 4889, "test".getBytes(), BigDecimal.valueOf(1).setScale(8), timestamp);
		
		//CREATE ARBITRARY TRANSACTION
		Transaction arbitraryTransaction = new ArbitraryTransaction(sender, 4889, "test".getBytes(), BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF ARBITRARY TRANSACTION IS VALID
		assertEquals(true, arbitraryTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		arbitraryTransaction = new ArbitraryTransaction(sender, 4889, "test".getBytes(), BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), new byte[0]);
		
		//CHECK IF POLL VOTE IS INVALID
		assertEquals(false, arbitraryTransaction.isSignatureValid());
	}
		
	@Test
	public void validateArbitraryTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] data = "test".getBytes();
		byte[] signature = ArbitraryTransaction.generateSignature(databaseSet, sender, 4776, data, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE ARBITRARY TRANSACTION
		Transaction arbitraryTransaction = new ArbitraryTransaction(sender, 4776, data, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF ARBITRARY TRANSACTION IS VALID
		assertEquals(Transaction.VALIDATE_OKE, arbitraryTransaction.isValid(databaseSet));
		arbitraryTransaction.process(databaseSet);
		
		//CREATE INVALID ARBITRARY TRANSACTION INVALID data LENGTH
		byte[] longData = new byte[5000];
		arbitraryTransaction = new ArbitraryTransaction(sender, 4776, longData, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	

		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.INVALID_DATA_LENGTH, arbitraryTransaction.isValid(databaseSet));
		
		//CREATE INVALID ARBITRARY TRANSACTION NOT ENOUGH BALANCE
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		arbitraryTransaction = new ArbitraryTransaction(invalidOwner, 4776, data, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.NO_BALANCE, arbitraryTransaction.isValid(databaseSet));
		
		//CREATE ARBITRARY TRANSACTION INVALID REFERENCE
		arbitraryTransaction = new ArbitraryTransaction(sender, 4776, data, BigDecimal.ONE.setScale(8), timestamp, invalidOwner.getLastReference(databaseSet), signature);	
		
		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, arbitraryTransaction.isValid(databaseSet));
		
		//CREATE ARBITRARY TRANSACTION INVALID FEE
		arbitraryTransaction = new ArbitraryTransaction(sender, 4776, data, BigDecimal.ZERO.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.NEGATIVE_FEE, arbitraryTransaction.isValid(databaseSet));
	}

	
	@Test
	public void parseArbitraryTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = ArbitraryTransaction.generateSignature(databaseSet, sender, 4776, "test".getBytes(), BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE ARBITRARY TRANSACTION
		ArbitraryTransaction arbitraryTransaction = new ArbitraryTransaction(sender, 4776, "test".getBytes(),BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CONVERT TO BYTES
		byte[] rawArbitraryTransaction = arbitraryTransaction.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			ArbitraryTransaction parsedArbitraryTransaction = (ArbitraryTransaction) TransactionFactory.getInstance().parse(rawArbitraryTransaction);
			
			//CHECK INSTANCE
			assertEquals(true, parsedArbitraryTransaction instanceof ArbitraryTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(arbitraryTransaction.getSignature(), parsedArbitraryTransaction.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(arbitraryTransaction.getAmount(sender), parsedArbitraryTransaction.getAmount(sender));	
			
			//CHECK CREATOR
			assertEquals(arbitraryTransaction.getCreator().getAddress(), parsedArbitraryTransaction.getCreator().getAddress());	
			
			//CHECK VERSION
			assertEquals(arbitraryTransaction.getService(), parsedArbitraryTransaction.getService());	
			
			//CHECK DATA
			assertEquals(true, Arrays.equals(arbitraryTransaction.getData(), parsedArbitraryTransaction.getData()));	
			
			//CHECK FEE
			assertEquals(arbitraryTransaction.getFee(), parsedArbitraryTransaction.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(arbitraryTransaction.getReference(), parsedArbitraryTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(arbitraryTransaction.getTimestamp(), parsedArbitraryTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawArbitraryTransaction = new byte[arbitraryTransaction.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawArbitraryTransaction);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OKE
		}	
	}

	
	@Test
	public void processArbitraryTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = ArbitraryTransaction.generateSignature(databaseSet, sender, 4776, "test".getBytes(), BigDecimal.valueOf(1).setScale(8), timestamp);
						
		//CREATE ARBITRARY TRANSACTION
		ArbitraryTransaction arbitraryTransaction = new ArbitraryTransaction(sender, 4776, "test".getBytes(),BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		arbitraryTransaction.process(databaseSet);				
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(999).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(arbitraryTransaction.getSignature(), sender.getLastReference(databaseSet)));
	}
	
	@Test
	public void orphanArbitraryTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
								
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
					
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
				
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = ArbitraryTransaction.generateSignature(databaseSet, sender, 4776, "test".getBytes(), BigDecimal.valueOf(1).setScale(8), timestamp);
								
		//CREATE ARBITRARY TRANSACTION
		ArbitraryTransaction arbitraryTransaction = new ArbitraryTransaction(sender, 4776, "test".getBytes(),BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		arbitraryTransaction.process(databaseSet);	
		arbitraryTransaction.orphan(databaseSet);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), sender.getConfirmedBalance(databaseSet));
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
	}
	
	//ISSUE ASSET TRANSACTION
	
	@Test
	public void validateSignatureIssueAssetTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
				
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE ASSET
		Asset asset = new Asset(sender, "test", "strontje", 50000l, false);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] signature = IssueAssetTransaction.generateSignature(databaseSet, sender, asset, BigDecimal.valueOf(1).setScale(8), timestamp);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(sender, asset, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CHECK IF ISSUE ASSET TRANSACTION IS VALID
		assertEquals(true, issueAssetTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		issueAssetTransaction = new IssueAssetTransaction(sender, asset, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), new byte[0]);
		
		//CHECK IF ISSUE ASSET IS INVALID
		assertEquals(false, issueAssetTransaction.isSignatureValid());
	}
		
	/*@Test
	public void validateArbitraryTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		byte[] data = "test".getBytes();
		byte[] signature = ArbitraryTransaction.generateSignature(databaseSet, sender, 4776, data, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE ARBITRARY TRANSACTION
		Transaction arbitraryTransaction = new ArbitraryTransaction(sender, 4776, data, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF ARBITRARY TRANSACTION IS VALID
		assertEquals(Transaction.VALIDATE_OKE, arbitraryTransaction.isValid(databaseSet));
		arbitraryTransaction.process(databaseSet);
		
		//CREATE INVALID ARBITRARY TRANSACTION INVALID data LENGTH
		byte[] longData = new byte[5000];
		arbitraryTransaction = new ArbitraryTransaction(sender, 4776, longData, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	

		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.INVALID_DATA_LENGTH, arbitraryTransaction.isValid(databaseSet));
		
		//CREATE INVALID ARBITRARY TRANSACTION NOT ENOUGH BALANCE
		seed = Crypto.getInstance().digest("invalid".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidOwner = new PrivateKeyAccount(privateKey);
		arbitraryTransaction = new ArbitraryTransaction(invalidOwner, 4776, data, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.NO_BALANCE, arbitraryTransaction.isValid(databaseSet));
		
		//CREATE ARBITRARY TRANSACTION INVALID REFERENCE
		arbitraryTransaction = new ArbitraryTransaction(sender, 4776, data, BigDecimal.ONE.setScale(8), timestamp, invalidOwner.getLastReference(databaseSet), signature);	
		
		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, arbitraryTransaction.isValid(databaseSet));
		
		//CREATE ARBITRARY TRANSACTION INVALID FEE
		arbitraryTransaction = new ArbitraryTransaction(sender, 4776, data, BigDecimal.ZERO.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);	
		
		//CHECK IF ARBITRARY TRANSACTION IS INVALID
		assertEquals(Transaction.NEGATIVE_FEE, arbitraryTransaction.isValid(databaseSet));
	}*/

	
	@Test
	public void parseIssueAssetTransaction() 
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Asset asset = new Asset(sender, "test", "strontje", 50000l, false);
		byte[] signature = IssueAssetTransaction.generateSignature(databaseSet, sender, asset, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(sender, asset, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		
		//CONVERT TO BYTES
		byte[] rawIssueAssetTransaction = issueAssetTransaction.toBytes();
		
		try 
		{	
			//PARSE FROM BYTES
			IssueAssetTransaction parsedIssueAssetTransaction = (IssueAssetTransaction) TransactionFactory.getInstance().parse(rawIssueAssetTransaction);
			
			//CHECK INSTANCE
			assertEquals(true, parsedIssueAssetTransaction instanceof IssueAssetTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), parsedIssueAssetTransaction.getSignature()));
			
			//CHECK ISSUER
			assertEquals(issueAssetTransaction.getIssuer().getAddress(), parsedIssueAssetTransaction.getIssuer().getAddress());
			
			//CHECK OWNER
			assertEquals(issueAssetTransaction.getAsset().getOwner().getAddress(), parsedIssueAssetTransaction.getAsset().getOwner().getAddress());
			
			//CHECK NAME
			assertEquals(issueAssetTransaction.getAsset().getName(), parsedIssueAssetTransaction.getAsset().getName());
				
			//CHECK DESCRIPTION
			assertEquals(issueAssetTransaction.getAsset().getDescription(), parsedIssueAssetTransaction.getAsset().getDescription());
				
			//CHECK QUANTITY
			assertEquals(issueAssetTransaction.getAsset().getQuantity(), parsedIssueAssetTransaction.getAsset().getQuantity());
			
			//DIVISIBLE
			assertEquals(issueAssetTransaction.getAsset().isDivisible(), parsedIssueAssetTransaction.getAsset().isDivisible());
			
			//CHECK FEE
			assertEquals(issueAssetTransaction.getFee(), parsedIssueAssetTransaction.getFee());	
			
			//CHECK REFERENCE
			assertEquals(true, Arrays.equals(issueAssetTransaction.getReference(), parsedIssueAssetTransaction.getReference()));	
			
			//CHECK TIMESTAMP
			assertEquals(issueAssetTransaction.getTimestamp(), parsedIssueAssetTransaction.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawIssueAssetTransaction = new byte[issueAssetTransaction.getDataLength()];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawIssueAssetTransaction);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OKE
		}	
	}

	
	@Test
	public void processIssueAssetTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Asset asset = new Asset(sender, "test", "strontje", 50000l, false);
		byte[] signature = IssueAssetTransaction.generateSignature(databaseSet, sender, asset, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(sender, asset, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		issueAssetTransaction.process(databaseSet);
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.valueOf(999).setScale(8), sender.getConfirmedBalance(databaseSet));
		
		//CHECK ASSET EXISTS SENDER
		long key = databaseSet.getIssueAssetMap().get(issueAssetTransaction);
		assertEquals(true, databaseSet.getAssetMap().contains(key));
		
		//CHECK ASSET IS CORRECT
		assertEquals(true, Arrays.equals(databaseSet.getAssetMap().get(key).toBytes(), asset.toBytes()));
		
		//CHECK ASSET BALANCE SENDER
		assertEquals(true, databaseSet.getBalanceMap().get(sender.getAddress(), key).compareTo(new BigDecimal(asset.getQuantity())) == 0);
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(issueAssetTransaction.getSignature(), sender.getLastReference(databaseSet)));
	}
	
	
	@Test
	public void orphanIssueAssetTransaction()
	{
		Ed25519.load();
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount sender = new PrivateKeyAccount(privateKey);
				
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		//CREATE SIGNATURE
		long timestamp = NTP.getTime();
		Asset asset = new Asset(sender, "test", "strontje", 50000l, false);
		byte[] signature = IssueAssetTransaction.generateSignature(databaseSet, sender, asset, BigDecimal.valueOf(1).setScale(8), timestamp);
				
		//CREATE ISSUE ASSET TRANSACTION
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(sender, asset, BigDecimal.ONE.setScale(8), timestamp, sender.getLastReference(databaseSet), signature);
		issueAssetTransaction.process(databaseSet);
		long key = databaseSet.getIssueAssetMap().get(issueAssetTransaction);
		issueAssetTransaction.orphan(databaseSet);
		
		//CHECK BALANCE ISSUER
		assertEquals(BigDecimal.valueOf(1000).setScale(8), sender.getConfirmedBalance(databaseSet));
		
		//CHECK ASSET EXISTS SENDER
		assertEquals(false, databaseSet.getAssetMap().contains(key));
		
		//CHECK ASSET BALANCE SENDER
		assertEquals(0, databaseSet.getBalanceMap().get(sender.getAddress(), key).longValue());
				
		//CHECK REFERENCE SENDER
		assertEquals(true, Arrays.equals(transaction.getSignature(), sender.getLastReference(databaseSet)));
	}
	
}
