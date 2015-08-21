package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;

import utils.ByteArrayUtils;

public class HashtagPostMap extends DBMap<String, List<byte[]>> {

	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public HashtagPostMap(DBSet databaseSet, DB database) {
		super(databaseSet, database);
	}
	
	public HashtagPostMap(DBMap<String, List<byte[]>> parent) {
		super(parent);
	}


	@Override
	protected Map<String, List<byte[]>> getMap(DB database) {
		// / OPEN MAP
		BTreeMapMaker createTreeMap = database.createTreeMap("HashtagPostMap");
		return createTreeMap.makeOrGet();
	}

	@Override
	protected Map<String, List<byte[]>> getMemoryMap() {
		return new HashMap<>();
	}

	@Override
	protected List<byte[]> getDefaultValue() {
		return null;
	}

	@Override
	protected Map<Integer, Integer> getObservableData() {
		return this.observableData;
	}

	@Override
	protected void createIndexes(DB database) {
	}
	

	public void add(String hashtag, byte[] signature) {
		//no difference between lower and uppercase here
		hashtag = hashtag.toLowerCase();
		
		List<byte[]> list;
		list = get(hashtag);

		if (list == null) {
			list = new ArrayList<>();
		}

		if (!ByteArrayUtils.contains(list, signature)) {
			list.add(signature);
		}

		set(hashtag, list);

	}

	public void remove(String hashtag, byte[] signature) {
		//no difference between lower and uppercase here
		hashtag = hashtag.toLowerCase();
		
		if (contains(hashtag)) {
			List<byte[]> list = get(hashtag);
			ByteArrayUtils.remove(list, signature);
			set(hashtag, list);
		}

	}

}
