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
import qora.transaction.ArbitraryTransaction;
import qora.transaction.Transaction;
import qora.voting.Poll;
import utils.Pair;
import database.DBSet;

public class BlockChain
{
	public static final int MAX_SIGNATURES = 500;
	
	public BlockChain()
	{	
		//CREATE GENESIS BLOCK
    	Block genesisBlock = new GenesisBlock();	
        if(!DBSet.getInstance().getBlockMap().contains(genesisBlock.getSignature()))
        {
        	//PROCESS
        	genesisBlock.process();
        }
	}
	
	public int getHeight() {
		
		//GET LAST BLOCK
		byte[] lastBlockSignature = DBSet.getInstance().getBlockMap().getLastBlockSignature();
		
		//RETURN HEIGHT
		return DBSet.getInstance().getHeightMap().get(lastBlockSignature);
	}

	public List<byte[]> getSignatures(byte[] parent) {
		
		List<byte[]> headers = new ArrayList<byte[]>();
		
		//CHECK IF BLOCK EXISTS
		if(DBSet.getInstance().getBlockMap().contains(parent))
		{
			Block parentBlock = DBSet.getInstance().getBlockMap().get(parent).getChild();
			
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

		return DBSet.getInstance().getBlockMap().get(header);
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
		if(!DBSet.getInstance().getBlockMap().contains(block.getReference()))
		{
			return false;
		}
		
		//CHECK IF REFERENCE IS LASTBLOCK
		if(!Arrays.equals(DBSet.getInstance().getBlockMap().getLastBlockSignature(), block.getReference()))
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
	
	public Pair<Block, List<Transaction>> scanTransactions(Block block, int blockLimit, int transactionLimit, int type, int service, Account account) 
	{	
		//CREATE LIST
		List<Transaction> transactions = new ArrayList<Transaction>();
		
		//IF NO BLOCK START FROM GENESIS
		if(block == null)
		{
			block = new GenesisBlock();
		}
		
		//START FROM BLOCK
		int scannedBlocks = 0;
		do
		{		
			//FOR ALL TRANSACTIONS IN BLOCK
			for(Transaction transaction: block.getTransactions())
			{
				//CHECK IF ACCOUNT INVOLVED
				if(account != null && !transaction.isInvolved(account))
				{
					continue;
				}
				
				//CHECK IF TYPE OKE
				if(type != -1 && transaction.getType() != type)
				{
					continue;
				}
				
				//CHECK IF SERVICE OKE
				if(service != -1 && transaction.getType() == Transaction.ARBITRARY_TRANSACTION)
				{
					ArbitraryTransaction arbitraryTransaction = (ArbitraryTransaction) transaction;
					
					if(arbitraryTransaction.getService() != service)
					{
						continue;
					}
				}
				
				//ADD TO LIST
				transactions.add(transaction);
			}
			
			//SET BLOCK TO CHILD
			block = block.getChild();
			scannedBlocks++;
		}
		//WHILE BLOCKS EXIST && NOT REACHED TRANSACTIONLIMIT && NOT REACHED BLOCK LIMIT
		while(block != null && (transactions.size() < transactionLimit || transactionLimit == -1) && (scannedBlocks < blockLimit || blockLimit == -1)); 
		
		//CHECK IF WE REACHED THE END
		if(block == null)
		{
			block = this.getLastBlock();
		}
		else
		{
			block = block.getParent();
		}
		
		//RETURN PARENT BLOCK AS WE GET CHILD RIGHT BEFORE END OF WHILE
		return new Pair<Block, List<Transaction>>(block, transactions);
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
		for(Name name: DBSet.getInstance().getNameMap().getValues())
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
		for(NameSale nameSale: DBSet.getInstance().getNameExchangeMap().getNameSales())
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

	public Map<Account, List<Poll>> scanPolls(List<Account> accounts)
	{
		//CREATE MAP
		Map<Account, List<Poll>> polls = new HashMap<Account, List<Poll>>();
		
		for(Account account: accounts)
		{
			polls.put(account, new ArrayList<Poll>());
		}
			
		//SCAN ALL POLLS
		for(Poll poll: DBSet.getInstance().getPollMap().getValues())
		{
			for(Account account: accounts)
			{
				if(account.getAddress().equals(poll.getCreator().getAddress()))
				{
					polls.get(account).add(poll);
				}
			}
		}
		
		//RETURN
		return polls;		
	}
	
	public Block getLastBlock() 
	{	
		return DBSet.getInstance().getBlockMap().getLastBlock();
	}
}
