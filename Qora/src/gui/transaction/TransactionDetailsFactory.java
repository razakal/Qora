package gui.transaction;

import javax.swing.JFrame;

import qora.transaction.ArbitraryTransaction;
import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelOrderTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.CreateOrderTransaction;
import qora.transaction.CreatePollTransaction;
import qora.transaction.GenesisTransaction;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.MessageTransaction;
import qora.transaction.MultiPaymentTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransferAssetTransaction;
import qora.transaction.UpdateNameTransaction;
import qora.transaction.VoteOnPollTransaction;

public class TransactionDetailsFactory 
{
	private static TransactionDetailsFactory instance;

	public static TransactionDetailsFactory getInstance()
	{
		if(instance == null)
		{
			instance = new TransactionDetailsFactory();
		}
		
		return instance;
	}
	
	public JFrame createTransactionDetail(Transaction transaction)
	{
		switch(transaction.getType())
		{
		case Transaction.GENESIS_TRANSACTION:
			
			GenesisTransaction genesis = (GenesisTransaction) transaction;
			return new GenesisDetailsFrame(genesis);
		
		case Transaction.PAYMENT_TRANSACTION:
		
			PaymentTransaction payment = (PaymentTransaction) transaction;
			return new PaymentDetailsFrame(payment);
			
		case Transaction.REGISTER_NAME_TRANSACTION:
			
			RegisterNameTransaction nameRegistration = (RegisterNameTransaction) transaction;
			return new RegisterNameDetailsFrame(nameRegistration);
			
		case Transaction.UPDATE_NAME_TRANSACTION:
			
			UpdateNameTransaction nameUpdate = (UpdateNameTransaction) transaction;
			return new UpdateNameDetailsFrame(nameUpdate);	
			
		case Transaction.SELL_NAME_TRANSACTION:
			
			SellNameTransaction nameSale = (SellNameTransaction) transaction;
			return new SellNameDetailsFrame(nameSale);		
			
		case Transaction.CANCEL_SELL_NAME_TRANSACTION:
			
			CancelSellNameTransaction cancelNameSale = (CancelSellNameTransaction) transaction;
			return new CancelSellNameDetailsFrame(cancelNameSale);			
			
		case Transaction.BUY_NAME_TRANSACTION:
			
			BuyNameTransaction namePurchase = (BuyNameTransaction) transaction;
			return new BuyNameDetailsFrame(namePurchase);	
		
		case Transaction.CREATE_POLL_TRANSACTION:
			
			CreatePollTransaction pollCreation = (CreatePollTransaction) transaction;
			return new CreatePollDetailsFrame(pollCreation);			

		case Transaction.VOTE_ON_POLL_TRANSACTION:
			
			VoteOnPollTransaction pollVote = (VoteOnPollTransaction) transaction;
			return new VoteOnPollDetailsFrame(pollVote);
			
		case Transaction.ARBITRARY_TRANSACTION:
			
			ArbitraryTransaction arbitraryTransaction = (ArbitraryTransaction) transaction;
			return new ArbitraryTransactionDetailsFrame(arbitraryTransaction);	
			
		case Transaction.ISSUE_ASSET_TRANSACTION:
			
			IssueAssetTransaction issueAssetTransaction = (IssueAssetTransaction) transaction;
			return new IssueAssetDetailsFrame(issueAssetTransaction);	
			
		case Transaction.TRANSFER_ASSET_TRANSACTION:
			
			TransferAssetTransaction transferAssetTransaction = (TransferAssetTransaction) transaction;
			return new TransferAssetDetailsFrame(transferAssetTransaction);		
			
		case Transaction.CREATE_ORDER_TRANSACTION:
			
			CreateOrderTransaction createOrderTransaction = (CreateOrderTransaction) transaction;
			return new CreateOrderDetailsFrame(createOrderTransaction);	
		
		case Transaction.CANCEL_ORDER_TRANSACTION:
			
			CancelOrderTransaction cancelOrderTransaction = (CancelOrderTransaction) transaction;
			return new CancelOrderDetailsFrame(cancelOrderTransaction);		
			
		case Transaction.MULTI_PAYMENT_TRANSACTION:
			
			MultiPaymentTransaction MultiPaymentTransaction = (MultiPaymentTransaction) transaction;
			return new MultiPaymentDetailsFrame(MultiPaymentTransaction);

		case Transaction.MESSAGE_TRANSACTION:
			MessageTransaction messageTransaction = (MessageTransaction)transaction;
			return new MessageTransactionDetailsFrame(messageTransaction);

		}
		
		return null;
	}
}
