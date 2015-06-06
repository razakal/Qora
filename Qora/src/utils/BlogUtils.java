package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.block.Block;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.Transaction;
import controller.Controller;

public class BlogUtils {

	
	
	public static List<Pair<String, String>> getBlogPosts()
	{
		Pair<Block, List<Transaction>> resultlist = Controller.getInstance().scanTransactions(Controller.getInstance().getBlockByHeight(Controller.getInstance().getHeight()-10000), 10000, 1000, 10, 777, null);
		 List<Pair<String, String>> results = new ArrayList<>();
		List<Transaction> b = resultlist.getB();
		
		for (Transaction transaction : b) {
			if(transaction instanceof ArbitraryTransaction)
			{
				
				 Pair<String, String> blogpair =  new Pair<>();
				
				byte[] data = ((ArbitraryTransaction) transaction).getData();
				String string = new String(data);
				
				JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
				
				String title = (String) jsonObject.get("title");
				String post = (String) jsonObject.get("post");
				
				if(StringUtil.isNotBlank(post))
				{
					blogpair.setA(title == null? "" : title);
					blogpair.setB(post);
					
					results.add(blogpair);
				}
				
			}
		}
		
		 Collections.reverse(results);
		
		return results;
		
		
		
		
	}
	
}
