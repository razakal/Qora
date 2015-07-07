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
import qora.assets.Order;
import qora.block.Block;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.payment.Payment;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.MessageTransaction;
import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelOrderTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.CreateOrderTransaction;
import qora.transaction.CreatePollTransaction;
import qora.transaction.DeployATTransaction;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.MultiPaymentTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransferAssetTransaction;
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
			if(transaction.isValid(this.fork) == Transaction.VALIDATE_OKE && transaction.isSignatureValid())
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
	 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForNameRegistration(Name name) 
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
				
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount registrant = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1}); 
		
		//CREATE NAME UPDATE
		RegisterNameTransaction nameRegistration = new RegisterNameTransaction(registrant, name, Transaction.MINIMUM_FEE, time, registrant.getLastReference(this.fork), signature);
		
		return new Pair(nameRegistration.calcRecommendedFee(), nameRegistration.getDataLength());
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForNameUpdate(Name name) 
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount owner = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		//CREATE NAME UPDATE
		UpdateNameTransaction nameUpdate = new UpdateNameTransaction(owner, name, Transaction.MINIMUM_FEE, time, owner.getLastReference(this.fork), signature);
				
		return new Pair(nameUpdate.calcRecommendedFee(), nameUpdate.getDataLength());
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForArbitraryTransaction(byte[] data) 
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount creator = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		//CREATE ARBITRARY TRANSACTION
		ArbitraryTransaction arbitraryTransaction = new ArbitraryTransaction(creator, 0, data, Transaction.MINIMUM_FEE, time, creator.getLastReference(this.fork), signature);
		
		return new Pair(arbitraryTransaction.calcRecommendedFee(), arbitraryTransaction.getDataLength());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Pair<BigDecimal, Integer> calcRecommendedFeeForMessage(byte[] message) 
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = new byte[64];
		
		//GENESIS ACCOUNT
		PublicKeyAccount sender = new PublicKeyAccount(new byte[]{1,1,1,1,1,1,1,1});
		
		//CREATE MESSAGE TRANSACTION
		MessageTransaction messageTx = new MessageTransaction(sender, sender, Transaction.MINIMUM_FEE, Transaction.MINIMUM_FEE, message, new byte[1], new byte[1], time, sender.getLastReference(this.fork), signature );
		
		return new Pair(messageTx.calcRecommendedFee(), messageTx.getDataLength());
	}
	
	public Pair<Transaction, Integer> createIssueAssetTransaction(PrivateKeyAccount creator, String name, String description, long quantity, boolean divisible, BigDecimal fee) 
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
	
	public Pair<Transaction, Integer> createOrderTransaction(PrivateKeyAccount creator, Asset have, Asset want, BigDecimal amount, BigDecimal price, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
								
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = CreateOrderTransaction.generateSignature(this.fork, creator, have.getKey(), want.getKey(), amount, price, fee, time);
							
		//CREATE PRDER TRANSACTION
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(creator, have.getKey(), want.getKey(), amount, price, fee, time, creator.getLastReference(this.fork), signature);
								
		//VALIDATE AND PROCESS
		return this.afterCreate(createOrderTransaction);
	}
	
	public Pair<Transaction, Integer> createCancelOrderTransaction(PrivateKeyAccount creator, Order order, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
								
		//TIME
		long time = NTP.getTime();
								
		//CREATE SIGNATURE
		byte[] signature = CancelOrderTransaction.generateSignature(this.fork, creator, order.getId(), fee, time);
							
		//CREATE PRDER TRANSACTION
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(creator, order.getId(), fee, time, creator.getLastReference(this.fork), signature);
								
		//VALIDATE AND PROCESS
		return this.afterCreate(cancelOrderTransaction);
	}
	
	public Pair<Transaction, Integer> createAssetTransfer(PrivateKeyAccount sender, Account recipient, Asset asset, BigDecimal amount, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = TransferAssetTransaction.generateSignature(this.fork, sender, recipient, asset.getKey(), amount, fee, time);
		
		//CREATE ASSET TRANSFER
		TransferAssetTransaction assetTransfer = new TransferAssetTransaction(sender, recipient, asset.getKey(), amount, fee, time, sender.getLastReference(this.fork), signature);
		
		//VALIDATE AND PROCESS
		return this.afterCreate(assetTransfer);
	}
	
	public Pair<Transaction, Integer> sendMultiPayment(PrivateKeyAccount sender, List<Payment> payments, BigDecimal fee)
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = MultiPaymentTransaction.generateSignature(this.fork, sender, payments, fee, time);
		
		//CREATE MULTI PAYMENTS
		MultiPaymentTransaction multiPayment = new MultiPaymentTransaction(sender, payments, fee, time, sender.getLastReference(this.fork), signature);
		
		//VALIDATE AND PROCESS
		return this.afterCreate(multiPayment);
	}

	public Pair<Transaction, Integer> deployATTransaction(PrivateKeyAccount creator, String name, String description, String type, String tags, byte[] creationBytes, BigDecimal amount, BigDecimal fee )
	{
		//CHECK FOR UPDATES
		this.checkUpdate();
		
		//TIME
		long time = NTP.getTime();
		
		//CREATE SIGNATURE
		byte[] signature = DeployATTransaction.generateSignature(this.fork, creator, name, description, creationBytes, amount, fee, time);
		
		//DEPLOY AT
		DeployATTransaction deployAT = new DeployATTransaction(creator, name, description, type, tags, creationBytes, amount, fee, time, creator.getLastReference(this.fork), signature);
		
		return this.afterCreate(deployAT);
		
	}
	

	public Pair<Transaction, Integer> createMessage(PrivateKeyAccount sender,
			Account recipient, BigDecimal amount, BigDecimal fee, byte[] isText,
			byte[] message, byte[] encryptMessage) {
		
		this.checkUpdate();
		
		long timestamp = NTP.getTime();
		
		byte[] signature = MessageTransaction.generateSignature(this.fork, sender, recipient, amount, fee, message, isText, encryptMessage, timestamp);
		
		MessageTransaction messageTx = new MessageTransaction(sender, recipient, amount, fee, message, isText, encryptMessage, timestamp, sender.getLastReference(this.fork), signature );
		
		return afterCreate(messageTx);
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
