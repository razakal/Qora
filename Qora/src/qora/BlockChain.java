package qora;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qora.account.Account;
import qora.block.Block;
import qora.block.GenesisBlock;
import qora.naming.Name;
import qora.naming.NameSale;
import qora.transaction.Transaction;
import database.DatabaseSet;

public class BlockChain
{
	public static final int MAX_SIGNATURES = 500;
	
	public BlockChain()
	{	
		//CREATE GENESIS BLOCK
    	Block genesisBlock = new GenesisBlock();	
        if(!DatabaseSet.getInstance().getBlockDatabase().containsBlock(genesisBlock.getSignature()))
        {
        	//PROCESS
        	genesisBlock.process();
        }
	}
	
	public int getHeight() {
		
		//GET LAST BLOCK
		byte[] lastBlockSignature = DatabaseSet.getInstance().getBlockDatabase().getLastBlockSignature();
		
		//RETURN HEIGHT
		return DatabaseSet.getInstance().getHeightDatabase().getHeightBySignature(lastBlockSignature);
	}

	public List<byte[]> getSignatures(byte[] parent) {
		
		List<byte[]> headers = new ArrayList<byte[]>();
		
		//CHECK IF BLOCK EXISTS
		if(DatabaseSet.getInstance().getBlockDatabase().containsBlock(parent))
		{
			Block parentBlock = DatabaseSet.getInstance().getBlockDatabase().getBlock(parent).getChild();
			
			int counter = 0;
			while(parentBlock != null && counter < MAX_SIGNATURES)
			{
				headers.add(parentBlock.getSignature());
				
				parentBlock = parentBlock.getChild();
				
				counter ++;
			}
		}
		
		return headers;		
	}

	public Block getBlock(byte[] header) {

		return DatabaseSet.getInstance().getBlockDatabase().getBlock(header);
	}

	public boolean isNewBlockValid(Block block) {
		
		//CHECK IF NOT GENESIS
		if(block instanceof GenesisBlock)
		{
			return false;
		}
		
		//CHECK IF SIGNATURE IS VALID
		if(!block.isSignatureValid())
		{
			return false;
		}
		
		//CHECK IF WE KNOW REFERENCE
		if(!DatabaseSet.getInstance().getBlockDatabase().containsBlock(block.getReference()))
		{
			return false;
		}
		
		//CHECK IF REFERENCE IS LASTBLOCK
		if(!Arrays.equals(DatabaseSet.getInstance().getBlockDatabase().getLastBlock().getSignature(), block.getReference()))
		{
			return false;
		}
		
		//CHECK IF BLOCK IS VALID
		if(!block.isValid())
		{
			return false;
		}
		
		return true;
	}

	public Map<Account, List<Transaction>> scanTransactions(List<Account> accounts) 
	{
		//CREATE MAP
		Map<Account, List<Transaction>> transactions= new HashMap<Account, List<Transaction>>();
		
		for(Account account: accounts)
		{
			transactions.put(account, new ArrayList<Transaction>());
		}
			
		//START FROM GENESIS BLOCK
		Block block = new GenesisBlock();
		do
		{
			//FOR ALL TRANSACTIONS IN BLOCK
			for(Transaction transaction: block.getTransactions())
			{
				//FOR ALL ACCOUNTS
				for(Account account: accounts)
				{
					//CHECK IF ACCOUNT IS INVOLVED
					if(transaction.isInvolved(account))
					{
						transactions.get(account).add(transaction);
					}
				}
			}
			
			//SET BLOCK TO CHILD
			block = block.getChild();
		}
		while(block != null);
		
		//RETURN
		return transactions;
	}

	public Map<Account, List<Block>> scanBlocks(List<Account> accounts) 
	{
		//CREATE MAP
		Map<Account, List<Block>> blocks = new HashMap<Account, List<Block>>();
		
		for(Account account: accounts)
		{
			blocks.put(account, new ArrayList<Block>());
		}
			
		//START FROM GENESIS BLOCK
		Block block = new GenesisBlock();
		do
		{
			for(Account account: accounts)
			{
				if(block.getGenerator().getAddress().equals(account.getAddress()))
				{
					blocks.get(account).add(block);
				}
			}
			
			//SET BLOCK TO CHILD
			block = block.getChild();
		}
		while(block != null);
		
		//RETURN
		return blocks;
	}
	
	public Map<Account, List<Name>> scanNames(List<Account> accounts)
	{
		//CREATE MAP
		Map<Account, List<Name>> names = new HashMap<Account, List<Name>>();
		
		for(Account account: accounts)
		{
			names.put(account, new ArrayList<Name>());
		}
			
		//SCAN ALL NAMES
		for(Name name: DatabaseSet.getInstance().getNameDatabase().getNames())
		{
			for(Account account: accounts)
			{
				if(account.getAddress().equals(name.getOwner().getAddress()))
				{
					names.get(account).add(name);
				}
			}
		}
		
		//RETURN
		return names;		
	}
	
	public Map<Account, List<NameSale>> scanNameSales(List<Account> accounts)
	{
		//CREATE MAP
		Map<Account, List<NameSale>> nameSales = new HashMap<Account, List<NameSale>>();
		
		for(Account account: accounts)
		{
			nameSales.put(account, new ArrayList<NameSale>());
		}
			
		//SCAN ALL NAME SALES
		for(NameSale nameSale: DatabaseSet.getInstance().getNameExchangeDatabase().getNameSales())
		{
			for(Account account: accounts)
			{
				if(account.getAddress().equals(nameSale.getName().getOwner().getAddress()))
				{
					nameSales.get(account).add(nameSale);
				}
			}
		}
		
		//RETURN
		return nameSales;		
	}

	public Block getLastBlock() 
	{	
		return DatabaseSet.getInstance().getBlockDatabase().getLastBlock();
	}
}
