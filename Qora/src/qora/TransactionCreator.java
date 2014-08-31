package qora;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ntp.NTP;
import controller.Controller;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.assets.Asset;
import qora.block.Block;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.CreatePollTransaction;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.UpdateNameTransaction;
import qora.transaction.VoteOnPollTransaction;
import qora.voting.Poll;
import utils.Pair;
import utils.TransactionTimestampComparator;
import database.DBSet;

public class TransactionCreator
{
	private DBSet fork;
	private Block lastBlock;
	
	private void checkUpdate()
	{
		//CHECK IF WE ALREADY HAVE A FORK
		if(this.lastBlock == null)
		{
			updateFork();
		}
		else
		{
			//CHECK IF WE NEED A NEW FORK
			if(!Arrays.equals(this.lastBlock.getSignature(), Controller.getInstance().getLastBlock().getSignature()))
			{
				updateFork();
			}
		}
	}
	
	private void updateFork()
	{
		//CREATE NEW FORK
		this.fork = DBSet.getInstance().fork();
		
		//UPDATE LAST BLOCK
		this.lastBlock = Controller.getInstance().getLastBlock();
			
		//SCAN UNCONFIRMED TRANSACTIONS FOR TRANSACTIONS WHERE ACCOUNT IS CREATOR OF
		List<Transaction> transactions = DBSet.getInstance().getTransactionMap().getTransactions();
		List<Transaction> accountTransactions = new ArrayList<Transaction>();
			
		for(Transaction transaction: transactions)
		{
			if(Controller.getInstance().getAccounts().contains(transaction.getCreator()))
			{
				accountTransactions.add(transaction);
			}
		}
			
		//SORT THEM BY TIMESTAMP
		Collections.sort(accountTransactions, new TransactionTimestampComparator());
			
		//VALIDATE AND PROCESS THOSE TRANSACTIONS IN FORK
		for(Transaction transaction: accountTransactions)
		{
			if(transaction.isValid(this.fork) == Transaction.VALIDATE_OKE)
			{
				transaction.process(this.fork);
			}
			else
			{
				//THE TRANSACTION BECAME INVALID LET 
				DBSet.getInstance().getTransactionMap().delete(transaction);
			}
		}
	}
	
	public Pair<Transaction, Integer> createPayment(PrivateKeyAccount sender, Account recipient, BigDecimal amount, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = PaymentTransaction.generateSignature(this.fork, sender, recipient, amount, fee, time);
		
		//CREATE PAYMENT
		PaymentTransaction payment = new PaymentTransaction(new PublicKeyAccount(sender.getPublicKey()), recipient, amount, fee, time, sender.getLastReference(this.fork), signature);
		
		//VALIDATE AND PROCESS
		return this.afterCreate(payment);
	}
	
	public Pair<Transaction, Integer> createNameRegistration(PrivateKeyAccount registrant, Name name, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = RegisterNameTransaction.generateSignature(this.fork, registrant, name, fee, time);
		
		//CREATE NAME REGISTRATION
		RegisterNameTransaction nameRegistration = new RegisterNameTransaction(registrant, name, fee, time, registrant.getLastReference(this.fork), signature);
		
		//VALIDATE AND PROCESS
		return this.afterCreate(nameRegistration);
	}
	
	public Pair<Transaction, Integer> createNameUpdate(PrivateKeyAccount owner, Name name, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = UpdateNameTransaction.generateSignature(this.fork, owner, name, fee, time);
		
		//CREATE NAME UPDATE
		UpdateNameTransaction nameUpdate = new UpdateNameTransaction(owner, name, fee, time, owner.getLastReference(this.fork), signature);
		
		//VALIDATE AND PROCESS
		return this.afterCreate(nameUpdate);
	}
	
	public Pair<Transaction, Integer> createNameSale(PrivateKeyAccount owner, NameSale nameSale, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
				
		//TIME
		long time = NTP.getTime();
				
		//CREATE SIGNATURE
		byte[] signature = SellNameTransaction.generateSignature(this.fork, owner, nameSale, fee, time);
				
		//CREATE NAME SALE
		SellNameTransaction nameSaleTransaction = new SellNameTransaction(owner, nameSale, fee, time, owner.getLastReference(this.fork), signature);
				
		//VALIDATE AND PROCESS
		return this.afterCreate(nameSaleTransaction);
	}

	public Pair<Transaction, Integer> createCancelNameSale(PrivateKeyAccount owner, NameSale nameSale, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
				
		//TIME
		long time = NTP.getTime();
				
		//CREATE SIGNATURE
		byte[] signature = CancelSellNameTransaction.generateSignature(this.fork, owner, nameSale.getKey(), fee, time);
				
		//CREATE CANCEL NAME SALE
		CancelSellNameTransaction cancelNameSaleTransaction = new CancelSellNameTransaction(owner, nameSale.getKey(), fee, time, owner.getLastReference(this.fork), signature);
				
		//VALIDATE AND PROCESS
		return this.afterCreate(cancelNameSaleTransaction);
	}

	public Pair<Transaction, Integer> createNamePurchase(PrivateKeyAccount buyer, NameSale nameSale, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
				
		//TIME
		long time = NTP.getTime();
				
		//CREATE SIGNATURE
		byte[] signature = BuyNameTransaction.generateSignature(this.fork, buyer, nameSale, nameSale.getName().getOwner(), fee, time);
				
		//CREATE NAME PURCHASE
		BuyNameTransaction namePurchase = new BuyNameTransaction(buyer, nameSale, nameSale.getName().getOwner(), fee, time, buyer.getLastReference(this.fork), signature);
				
		//VALIDATE AND PROCESS
		return this.afterCreate(namePurchase);
	}
	
	public Pair<Transaction, Integer> createPollCreation(PrivateKeyAccount creator, Poll poll, BigDecimal fee) 
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
						
		//TIME
		long time = NTP.getTime();
						
		//CREATE SIGNATURE
		byte[] signature = CreatePollTransaction.generateSignature(this.fork, creator, poll, fee, time);
					
		//CREATE POLL CREATION
		CreatePollTransaction pollCreation = new CreatePollTransaction(creator, poll, fee, time, creator.getLastReference(this.fork), signature);
						
		//VALIDATE AND PROCESS
		return this.afterCreate(pollCreation);
	}
	
	public Pair<Transaction, Integer> createPollVote(PrivateKeyAccount creator, String poll, int optionIndex, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
						
		//TIME
		long time = NTP.getTime();
						
		//CREATE SIGNATURE
		byte[] signature = VoteOnPollTransaction.generateSignature(this.fork, creator, poll, optionIndex, fee, time);
					
		//CREATE POLL VOTE
		VoteOnPollTransaction pollVote = new VoteOnPollTransaction(creator, poll, optionIndex, fee, time, creator.getLastReference(this.fork), signature);
						
		//VALIDATE AND PROCESS
		return this.afterCreate(pollVote);
	}
	
	public Pair<Transaction, Integer> createArbitraryTransaction(PrivateKeyAccount creator, int service, byte[] data, BigDecimal fee) 
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
								
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = ArbitraryTransaction.generateSignature(this.fork, creator, service, data, fee, time);
							
		//CREATE ARBITRARY TRANSACTION
		ArbitraryTransaction arbitraryTransaction = new ArbitraryTransaction(creator, service, data, fee, time, creator.getLastReference(this.fork), signature);
								
		//VALIDATE AND PROCESS
		return this.afterCreate(arbitraryTransaction);
	}
	
	public Pair<Transaction, Integer> createIssueAssetransaction(PrivateKeyAccount creator, String name, String description, long quantity, boolean divisible, BigDecimal fee) 
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
								
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		Asset asset = new Asset(creator, name, description, quantity, divisible, new byte[64]);
		byte[] signature = IssueAssetTransaction.generateSignature(this.fork, creator, asset, fee, time);
							
		//CREATE ISSUE ASSET TRANSACTION
		asset = new Asset(creator, name, description, quantity, divisible, signature);
		IssueAssetTransaction issueAssetTransaction = new IssueAssetTransaction(creator, asset, fee, time, creator.getLastReference(this.fork), signature);
								
		//VALIDATE AND PROCESS
		return this.afterCreate(issueAssetTransaction);
	}
	
	private Pair<Transaction, Integer> afterCreate(Transaction transaction)
	{
		//CHECK IF PAYMENT VALID
		int valid = transaction.isValid(this.fork);		
		if(valid == Transaction.VALIDATE_OKE)
		{
			//PROCESS IN FORK
			transaction.process(this.fork);
					
			//CONTROLLER ONTRANSACTION
			Controller.getInstance().onTransactionCreate(transaction);
		}
				
		//RETURN
		return new Pair<Transaction, Integer>(transaction, valid);
	}
}
