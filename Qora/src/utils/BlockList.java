package utils;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.DBSet;
import qora.block.Block;

public class BlockList extends AbstractList<Block> 
{
	private List<byte[]> blockSignatures;
	private Map<byte[], Block> blocks;
	
	public BlockList(List<byte[]> blockSignatures)
	{
		this.blockSignatures = blockSignatures;
		this.blocks = new HashMap<byte[], Block>();;
	}
	
	
	@Override
	public Block get(int index) 
	{
		if(!this.blocks.containsKey(this.blockSignatures.get(index)))
		{
			this.blocks.put(this.blockSignatures.get(index), DBSet.getInstance().getBlockMap().get(this.blockSignatures.get(index)));
		}
		
		return this.blocks.get(this.blockSignatures.get(index));
	}

	@Override
	public int size() 
	{
		return this.blockSignatures.size();
	}

}
