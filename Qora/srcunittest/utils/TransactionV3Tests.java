package utils;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ntp.NTP;

import org.junit.Test;

import database.DBSet;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.Crypto;
import qora.transaction.GenesisTransaction;
import qora.transaction.MessageTransactionV3;
import qora.transaction.Transaction;


public class TransactionV3Tests {

	@Test
	public void validateMessageTransactionV3() 
	{
		
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
						
		//CREATE KNOWN ACCOUNT
		byte[] seed = Crypto.getInstance().digest("test".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		
		byte[] data = "test123!".getBytes();
		
		PrivateKeyAccount creator = new PrivateKeyAccount(privateKey);
		Account recipient = new Account("QfreeNWCeaU3BiXUxktaJRJrBB1SDg2k7o");		

		long timestamp = NTP.getTime();

		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		Transaction transaction = new GenesisTransaction(creator, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		transaction.process(databaseSet);
		
		creator.setConfirmedBalance(61l, BigDecimal.valueOf(100).setScale(8), databaseSet);
		
		byte[] signature = MessageTransactionV3.generateSignature(
				databaseSet, 
				creator, recipient, 61l, //	ATFunding
				BigDecimal.valueOf(10).setScale(8), 
				BigDecimal.valueOf(1).setScale(8), 
				data, 
				new byte[] { 1 },
				new byte[] { 0 },
				timestamp
				);
		
		MessageTransactionV3 messageTransactionV3 = new MessageTransactionV3(
				creator, recipient, 61l, //	ATFunding 
				BigDecimal.valueOf(10).setScale(8), 
				BigDecimal.valueOf(1).setScale(8), 
				data, 
				new byte[] { 1 },
				new byte[] { 0 },
				timestamp,
				creator.getLastReference(databaseSet),
				signature
				);
		
		messageTransactionV3.process(databaseSet);
		
		assertEquals(BigDecimal.valueOf(999).setScale(8), creator.getConfirmedBalance(databaseSet));
		assertEquals(BigDecimal.valueOf(90).setScale(8), creator.getConfirmedBalance(61l, databaseSet));
		assertEquals(BigDecimal.valueOf(10).setScale(8), recipient.getConfirmedBalance(61l, databaseSet));
		
		byte[] rawMessageTransactionV3 = messageTransactionV3.toBytes();
		
		MessageTransactionV3 messageTransactionV3_2 = null;
		try {
			messageTransactionV3_2 = (MessageTransactionV3) MessageTransactionV3.Parse(Arrays.copyOfRange(rawMessageTransactionV3, 4, rawMessageTransactionV3.length));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(new String(messageTransactionV3.getData()), new String(messageTransactionV3_2.getData()));
		assertEquals(messageTransactionV3.getCreator(), messageTransactionV3_2.getCreator());
		assertEquals(messageTransactionV3.getRecipient(), messageTransactionV3_2.getRecipient());
		assertEquals(messageTransactionV3.getKey(), messageTransactionV3_2.getKey());
		assertEquals(messageTransactionV3.getAmount(), messageTransactionV3_2.getAmount());
		assertEquals(messageTransactionV3.isEncrypted(), messageTransactionV3_2.isEncrypted());
		assertEquals(messageTransactionV3.isText(), messageTransactionV3_2.isText());
	}
	
}
