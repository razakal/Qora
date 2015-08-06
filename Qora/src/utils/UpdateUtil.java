package utils;

import java.util.List;

import qora.block.Block;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.Transaction;
import database.BlockMap;
import database.DBSet;
import database.SortableList;

public class UpdateUtil {

	public static void repopulateNameStorage(int height)
	{
		DBSet.getInstance().getNameStorageMap().reset();
		DBSet.getInstance().getOrphanNameStorageHelperMap().reset();
		DBSet.getInstance().getOrphanNameStorageMap().reset();
		
		SortableList<byte[], Block> blocks = DBSet.getInstance().getBlockMap().getList();
		blocks.sort(BlockMap.HEIGHT_INDEX);
		
		for (int i=height; i<blocks.size(); i++)
		{
			Block b = (Block) blocks.get(i).getB();
			List<Transaction> txs = b.getTransactions();
			for (Transaction tx : txs)
			{
				if (tx instanceof ArbitraryTransaction)
				{
					ArbitraryTransaction arbTx = (ArbitraryTransaction) tx;
					int service = arbTx.getService();
					if ( service == 10 )
					{
						StorageUtils.processUpdate(arbTx.getData(), arbTx.getSignature(), arbTx.getCreator(), DBSet.getInstance());
					}
					else if (service == 777) 
					{
						//addToBlogMapOnDemand(DBSet.getInstance());
						//addToBlogMapOnDemand(db);
					}
				}
			}
			
		}
		
		
	}
}
