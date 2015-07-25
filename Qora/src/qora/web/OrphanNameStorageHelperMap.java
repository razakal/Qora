package qora.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapdb.DB;

import utils.ByteArrayUtils;
import database.DBMap;
import database.DBSet;

public class OrphanNameStorageHelperMap extends DBMap<String, List<byte[]>> {

	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	public OrphanNameStorageHelperMap(DBSet databaseSet, DB database) {
		super(databaseSet, database);
	}

	public OrphanNameStorageHelperMap(DBMap<String, List<byte[]>> parent) {
		super(parent);
	}


	@Override
	protected Map<String, List<byte[]>> getMap(DB database) {
		
		
		return   database.createTreeMap("OrphanNameStorageHelperMap")
		            .makeOrGet();
		
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
	protected void createIndexes(DB database) {}
	
	
	public void add(String name, byte[] signatureOfTx)
	{
		List<byte[]> list = this.get(name);
		if(list == null)
		{
			list = new ArrayList<>();
		}
		
		if(!ByteArrayUtils.contains(list, signatureOfTx))
		{
			list.add(signatureOfTx);
		}
		
		
		set(name, list);
		
		
	}
	
	public void remove(String name, byte[] signatureOfTx)
	{
		List<byte[]> list = this.get(name);
		if(list == null)
		{
			return;
		}
		
		ByteArrayUtils.remove(list, signatureOfTx);
		
		set(name, list);
		
	}
	
	
	
	public void remove(byte[] txAndName)
	{
		this.remove(txAndName);
	}

	@Override
	protected List<byte[]> getDefaultValue() {
		return null;
	}

}
