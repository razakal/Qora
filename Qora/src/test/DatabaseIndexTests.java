package test;

import org.junit.Assert;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import database.NameMap;
import database.SortableList;
import qora.account.Account;
import qora.naming.Name;

public class DatabaseIndexTests {

	@Test
	public void databaseFork() 
	{
		//CREATE DATABASE
		DB database = DBMaker.newTempFileDB().make();
		
		//CREATE NAMEDATABASE
		NameMap nameDB = new NameMap(null, database);
		
		//CREATE NAMES
		Name nameA = new Name(new Account("b"), "a", "a");
		Name nameB = new Name(new Account("a"), "b", "b");
	
		//ADD TO DB
		nameDB.set("a", nameA);
		nameDB.set("b", nameB);
		
		//CHECK IF ADDED SUCCESSFULLY
		Assert.assertEquals(nameDB.get("a").getValue(), "a");
		Assert.assertEquals(nameDB.get("b").getValue(), "b");
		
		//GET INDEXED LIST
		SortableList<String, Name> list = new SortableList<String, Name>(nameDB);
		
		//GET VALUES BY DEFAULT INDEX
		Assert.assertEquals(list.get(0).getA(), "a");
		Assert.assertEquals(list.get(1).getA(), "b");
		Assert.assertEquals(list.get(0).getA(), "a");
		
		//GET VALUES BY OWNER INDEX
		list.sort(NameMap.DEFAULT_INDEX);
		Assert.assertEquals(list.get(0).getA(), "b");
		Assert.assertEquals(list.get(1).getA(), "a");
		Assert.assertEquals(list.get(0).getA(), "b");
	}	
}
