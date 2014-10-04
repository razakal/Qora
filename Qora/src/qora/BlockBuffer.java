package qora;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import qora.block.Block;
import settings.Settings;
import network.Peer;
import network.message.BlockMessage;
import network.message.Message;
import network.message.MessageFactory;

public class BlockBuffer extends Thread
{
	private static final int BUFFER_SIZE = 20;
	
	private List<byte[]> signatures;
	private Peer peer;
	private int counter;
	private boolean error;
	private Map<byte[], BlockingQueue<Block>> blocks;
	
	private boolean run = true;
	
	public BlockBuffer(List<byte[]> signatures, Peer peer)
	{
		this.signatures = signatures;
		this.peer = peer;
		this.counter = 0;
		this.error = false;
		
		this.blocks = new HashMap<byte[], BlockingQueue<Block>>();
		this.start();
	}
	
	public void run() 
	{
		while(this.run)
		{
			for(int i=0; i<this.signatures.size() && i<this.counter + BUFFER_SIZE; i++)
			{
				byte[] signature = this.signatures.get(i);
				
				//CHECK IF WE HAVE ALREADY LOADED THIS BLOCK
				if(!this.blocks.containsKey(signature))
				{
					//LOAD BLOCK
					this.loadBlock(signature);
				}
			}
			
			try 
			{
				Thread.sleep(10);
			} 
			catch (InterruptedException e) 
			{
				//ERROR SLEEPING
			}
		}
	}
	
	private void loadBlock(final byte[] signature)
	{
		//CREATE QUEUE
		final BlockingQueue<Block> blockingQueue = new ArrayBlockingQueue<Block>(1);
		this.blocks.put(signature, blockingQueue);
		
		//LOAD BLOCK IN THREAD
		new Thread(){
			public void run()
			{
				//CREATE MESSAGE
				Message message = MessageFactory.getInstance().createGetBlockMessage(signature);
				
				//SEND MESSAGE TO PEER
				BlockMessage response = (BlockMessage) peer.getResponse(message);
				
				//CHECK IF WE GOT RESPONSE
				if(response == null)
				{
					//ERROR
					error = true;
					return;
				}
				
				//CHECK BLOCK SIGNATURE
				if(!response.getBlock().isSignatureValid())
				{
					error = true;
					return;
				}
				
				//ADD TO LIST
				blockingQueue.add(response.getBlock());
			}
		}.start();
	}
	
	public Block getBlock(byte[] signature) throws Exception
	{
		//CHECK ERROR
		if(this.error)
		{
			throw new Exception("Block buffer error");
		}
		
		//UPDATE COUNTER
		this.counter = this.signatures.indexOf(signature);
		
		//CHECK IF ALREADY LOADED BLOCK
		if(!this.blocks.containsKey(signature))
		{
			//LOAD BLOCK
			this.loadBlock(signature);
		}
		
		//GET BLOCK
		return this.blocks.get(signature).poll(Settings.getInstance().getConnectionTimeout(), TimeUnit.MILLISECONDS);
	}
	
	public void stopThread()
	{
		try
		{
			this.run = false;
			this.join();
		}
		catch (InterruptedException e) 
		{
			//INTERRUPTED
		}
	}
	
}
