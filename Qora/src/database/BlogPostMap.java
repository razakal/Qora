package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;

import utils.ByteArrayUtils;

public class BlogPostMap extends DBMap<String, List<byte[]>> {

	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	public final static String MAINBLOG = "QORA";

	public BlogPostMap(DBSet databaseSet, DB database) {
		super(databaseSet, database);
	}

	public BlogPostMap(DBMap<String, List<byte[]>> parent) {
		super(parent);
	}

	@Override
	protected Map<String, List<byte[]>> getMap(DB database) {
		// / OPEN MAP
		BTreeMapMaker createTreeMap = database.createTreeMap("BlogPostMap");
		return createTreeMap.makeOrGet();
	}

	@Override
	protected Map<String, List<byte[]>> getMemoryMap() {
		return new HashMap<>();
	}

	@Override
	protected Map<Integer, Integer> getObservableData() {
		return this.observableData;
	}

	@Override
	protected void createIndexes(DB database) {
	}

	@Override
	protected List<byte[]> getDefaultValue() {
		return null;
	}

	public void add(String blogname, byte[] signature) {
		List<byte[]> list;
		if (blogname == null) {
			blogname = MAINBLOG;
		}
		list = get(blogname);

		if (list == null) {
			list = new ArrayList<>();
		}

		if (!ByteArrayUtils.contains(list, signature)) {
			list.add(signature);
		}

		set(blogname, list);

	}

	public void remove(String blogname, byte[] signature) {
		if (blogname == null) {
			blogname = MAINBLOG;
		}

		if (contains(blogname)) {
			List<byte[]> list = get(blogname);
			ByteArrayUtils.remove(list, signature);
			set(blogname, list);
		}

	}
	
	

}
