package qora.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapdb.DB;

import com.google.common.primitives.SignedBytes;

import database.DBMap;
import database.DBSet;

public class SharedPostsMap extends DBMap<byte[], List<String>> {

	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	public SharedPostsMap(DBSet databaseSet, DB database) {
		super(databaseSet, database);
	}

	public SharedPostsMap(DBMap<byte[], List<String>> parent) {
		super(parent);
	}

	@Override
	protected Map<byte[], List<String>> getMap(DB database) {

		return database.createTreeMap("SharedPostsMap")
				.comparator(SignedBytes.lexicographicalComparator())
				.makeOrGet();

	}

	@Override
	protected Map<byte[], List<String>> getMemoryMap() {
		return new HashMap<>();
	}

	public void add(byte[] postSignature, String name) {
		List<String> list = get(postSignature);
		if (list == null) {
			list = new ArrayList<String>();
		}

		if (!list.contains(name)) {
			list.add(name);
		}

		set(postSignature, list);
	}

	public void remove(byte[] postSignature, String name) {
		List<String> list = get(postSignature);
		if (list == null) {
			return;
		}

		list.remove(name);
		
		if(list.isEmpty())
		{
			delete(postSignature);
			return;
		}

		set(postSignature, list);
	}

	@Override
	protected Map<Integer, Integer> getObservableData() {
		return this.observableData;
	}

	@Override
	protected void createIndexes(DB database) {
	}

	@Override
	protected List<String> getDefaultValue() {
		return null;
	}
}
