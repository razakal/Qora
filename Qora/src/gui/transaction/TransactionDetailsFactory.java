package gui.transaction;

import javax.swing.JFrame;

import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.GenesisTransaction;
import qora.transaction.PaymentTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.UpdateNameTransaction;

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
		}
		
		return null;
	}
}
