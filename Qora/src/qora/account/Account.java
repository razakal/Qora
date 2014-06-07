package qora.account;

import java.math.BigDecimal;
import java.util.Arrays;

import controller.Controller;
import qora.BlockGenerator;
import qora.block.Block;
import qora.transaction.Transaction;
import database.DatabaseSet;

public class Account {
	
	public static final int ADDRESS_LENGTH = 25;

	protected String address;
	
	private byte[] lastBlockSignature;
	private BigDecimal generatingBalance;
	
	protected Account()
	{
		this.generatingBalance = BigDecimal.ZERO.setScale(8);
	}
	
	public Account(String address)
	{
		this.address = address;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	//BALANCE
	
	public BigDecimal getUnconfirmedBalance()
	{
		return this.getUnconfirmedBalance(DatabaseSet.getInstance());
	}
	
	public BigDecimal getUnconfirmedBalance(DatabaseSet db)
	{
		return Controller.getInstance().getUnconfirmedBalance(this.getAddress());
	}
	
	public BigDecimal getConfirmedBalance()
	{
		return this.getConfirmedBalance(DatabaseSet.getInstance());
	}
	
	public BigDecimal getConfirmedBalance(DatabaseSet db)
	{
		return db.getBalanceDatabase().getBalance(getAddress());
	}

	public void setConfirmedBalance(BigDecimal amount)
	{
		this.setConfirmedBalance(amount, DatabaseSet.getInstance());
	}
	
	public void setConfirmedBalance(BigDecimal amount, DatabaseSet db)
	{
		//UPDATE BALANCE IN DB
		db.getBalanceDatabase().setBalance(getAddress(), amount);
	}
	
	public BigDecimal getBalance(int confirmations)
	{
		return this.getBalance(confirmations, DatabaseSet.getInstance());
	}
	
	public BigDecimal getBalance(int confirmations, DatabaseSet db)
	{
		//CHECK IF UNCONFIRMED BALANCE
		if(confirmations <= 0)
		{
			return this.getUnconfirmedBalance(db);
		}
		
		//IF 1 CONFIRMATION
		if(confirmations == 1)
		{
			return this.getConfirmedBalance(db);
		}
		
		//GO TO PARENT BLOCK 10
		BigDecimal balance = this.getConfirmedBalance(db);
		Block block = db.getBlockDatabase().getLastBlock();
		
		for(int i=1; i<confirmations && block != null && block instanceof Block; i++)
		{
			for(Transaction transaction: block.getTransactions())
			{
				if(transaction.isInvolved(this))
				{
					balance = balance.subtract(transaction.getAmount(this));
				}
			}
				
			block = block.getParent(db);
		}
		
		//RETURN
		return balance;
	}
	
	private void updateGeneratingBalance(DatabaseSet db)
	{
		//CHECK IF WE NEED TO RECALCULATE
		if(this.lastBlockSignature == null)
		{
			this.lastBlockSignature = db.getBlockDatabase().getLastBlockSignature();
			calculateGeneratingBalance(db);
		}
		else
		{
			//CHECK IF WE NEED TO RECALCULATE
			if(!Arrays.equals(this.lastBlockSignature, db.getBlockDatabase().getLastBlockSignature()))
			{
				this.lastBlockSignature = db.getBlockDatabase().getLastBlockSignature();
				calculateGeneratingBalance(db);
			}
		}
	}
	
	public void calculateGeneratingBalance(DatabaseSet db)
	{
		//CONFIRMED BALANCE + ALL NEGATIVE AMOUNTS IN LAST 9 BLOCKS
		BigDecimal balance = this.getConfirmedBalance(db);
		
		Block block = db.getBlockDatabase().getLastBlock();
		
		for(int i=1; i<BlockGenerator.RETARGET && block != null && block.getHeight(db) > 1; i++)
		{
			for(Transaction transaction: block.getTransactions())
			{
				if(transaction.isInvolved(this))
				{
					if(transaction.getAmount(this).compareTo(BigDecimal.ZERO) == 1)
					{
						balance = balance.subtract(transaction.getAmount(this));
					}
				}
			}
				
			block = block.getParent(db);
		}
		
		//DO NOT GO BELOW 0
		if(balance.compareTo(BigDecimal.ZERO) == -1)
		{
			balance = BigDecimal.ZERO.setScale(8);
		}
		
		this.generatingBalance = balance;
	}
	
	public BigDecimal getGeneratingBalance()
	{
		return this.getGeneratingBalance(DatabaseSet.getInstance());
	}
	
	public BigDecimal getGeneratingBalance(DatabaseSet db)
	{	
		//UPDATE
		updateGeneratingBalance(db);
		
		//RETURN
		return this.generatingBalance;
	}
	
	//REFERENCE
	
	public byte[] getLastReference()
	{
		return this.getLastReference(DatabaseSet.getInstance());
	}
	
	public byte[] getLastReference(DatabaseSet db)
	{
		return db.getReferenceDatabase().getReference(this);
	}
	
	public void setLastReference(byte[] reference)
	{
		this.setLastReference(reference, DatabaseSet.getInstance());
	}
	
	public void setLastReference(byte[] reference, DatabaseSet db)
	{
		db.getReferenceDatabase().setReference(this, reference);
	}
	
	public void removeReference() 
	{
		this.removeReference(DatabaseSet.getInstance());
	}
	
	public void removeReference(DatabaseSet db) 
	{
		db.getReferenceDatabase().remove(this);
	}
	
	//TOSTRING
	
	@Override
	public String toString()
	{
		return this.getBalance(0).toPlainString() + " - " + this.getAddress();
	}
	
	//EQUALS
	@Override
	public boolean equals(Object b)
	{
		if(b instanceof Account)
		{
			return this.getAddress().equals(((Account) b).getAddress());
		}
		
		return false;	
	}
}
