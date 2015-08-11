package qora.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import controller.Controller;
import database.DBSet;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.Transaction;
import utils.GZIP;
import utils.StorageUtils;

public class WebNameStorageHistoryHelper {
	
	
	public static List<List<NamestorageKeyValueHistory>> getHistory(String name, int maxStorageTx)
	{
		List<List<NamestorageKeyValueHistory>> results= new ArrayList<>();
		
		OrphanNameStorageHelperMap orphanNameStorageHelperMap = DBSet.getInstance().getOrphanNameStorageHelperMap();
		
		List<byte[]> list = new ArrayList<>( orphanNameStorageHelperMap.get(name));
		Collections.reverse(list);
		
		for (int i = 0; i < list.size() && i < maxStorageTx; i++) {
			
			byte[] bs = list.get(i);
			
			Transaction transaction = Controller.getInstance().getTransaction(bs);
			if(transaction != null)
			{
				results.add(getKeyHistory((ArbitraryTransaction) transaction));
			}else
			{
				maxStorageTx++;
			}
			
		}
		
		
		return results;
	}

	@SuppressWarnings("unchecked")
	public static List<NamestorageKeyValueHistory> getKeyHistory(ArbitraryTransaction tx) {

		
//		MAKE SURE OTHER VALUE IN INNER JSON DON'T MAKE PROBLEMS!
		String jsonString = new String(tx.getData());

		jsonString = GZIP.webDecompress(jsonString);

		JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);

		List<NamestorageKeyValueHistory> results = new ArrayList<>();
		Set<String> keySet = jsonObject.keySet();

		OrphanNameStorageMap orphanNameStorageMap = DBSet.getInstance()
				.getOrphanNameStorageMap();

		Map<String, String> map = orphanNameStorageMap.get(tx.getSignature());

		Set<String> orphanKeySet = map.keySet();

		// initializing tempnamestorage with old values
		NameStorageMap tempNameStorageMap = new NameStorageMap(null);
		String name = (String) jsonObject.get("name");
		for (String key : orphanKeySet) {
			tempNameStorageMap.add(name, key, map.get(key));
		}
		// changing in temp namestorage
		StorageUtils.addTxChangesToStorage(jsonObject, name,
				tempNameStorageMap, null);

		for (String mainkey : keySet) {

			String innerJson = (String) jsonObject.get(mainkey);

			JSONObject innerJsonObject = (JSONObject) JSONValue
					.parse(innerJson);

			if(innerJsonObject != null)
			{
				Set<String> innerKeyset = innerJsonObject.keySet();
				
				for (String innerkey : innerKeyset) {
					results.add(new NamestorageKeyValueHistory(map.get(innerkey),
							(String) innerJsonObject.get(innerkey),
							tempNameStorageMap.getOpt(name, innerkey), mainkey,
							innerkey, name));
				}
			}

		}
		
		return results;

	}

}
