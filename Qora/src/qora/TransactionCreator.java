package qora;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import ntp.NTP;
import controller.Controller;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.block.Block;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.UpdateNameTransaction;
import utils.ObserverMessage;
import utils.TransactionTimestampComparator;
import database.DatabaseSet;

public class TransactionCreator extends Observable
{
	private DatabaseSet fork;
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
		this.fork = DatabaseSet.getInstance().fork();
		
		//UPDATE LAST BLOCK
		this.lastBlock = Controller.getInstance().getLastBlock();
			
		//SCAN UNCONFIRMED TRANSACTIONS FOR TRANSACTIONS WHERE ACCOUNT IS CREATOR OF
		List<Transaction> transactions = DatabaseSet.getInstance().getTransactionsDatabase().getTransactions();
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
				DatabaseSet.getInstance().getTransactionsDatabase().remove(transaction);
				
				//NOTIFY
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_TRANSACTION_TYPE, transaction));
			}
		}
	}
	
	public int createPayment(PrivateKeyAccount sender, Account recipient, BigDecimal amount, BigDecimal fee)
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
	
	public int createNameRegistration(PrivateKeyAccount registrant, Name name, BigDecimal fee)
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
	
	public int createNameUpdate(PrivateKeyAccount owner, Name name, BigDecimal fee)
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
	
	public int createNameSale(PrivateKeyAccount owner, NameSale nameSale, BigDecimal fee)
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

	public int createCancelNameSale(PrivateKeyAccount owner, NameSale nameSale, BigDecimal fee)
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

	public int createNamePurchase(PrivateKeyAccount buyer, NameSale nameSale, BigDecimal fee)
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
	
	private int afterCreate(Transaction transaction)
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
		return valid;
	}
}
