package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import database.DBSet;


public class DatabaseTests {

	@Test
	public void databaseFork() 
	{
		//CREATE DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE FORK
		DBSet fork = databaseSet.fork();
		
		//SET BALANCE
		databaseSet.getBalanceMap().set("test", BigDecimal.ONE);
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, databaseSet.getBalanceMap().get("test"));
		
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.ONE, fork.getBalanceMap().get("test"));
		
		//SET BALANCE IN FORK
		fork.getBalanceMap().set("test", BigDecimal.TEN);
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, databaseSet.getBalanceMap().get("test"));
				
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.TEN, fork.getBalanceMap().get("test"));
		
		//CREATE SECOND FORK
		DBSet fork2 = fork.fork();
		
		//SET BALANCE IN FORK2
		fork2.getBalanceMap().set("test", BigDecimal.ZERO);
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, databaseSet.getBalanceMap().get("test"));
						
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.TEN, fork.getBalanceMap().get("test"));
		
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.ZERO, fork2.getBalanceMap().get("test"));
	}	
}
