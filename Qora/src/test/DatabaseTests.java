package test;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

import database.DatabaseSet;


public class DatabaseTests {

	@Test
	public void databaseFork() 
	{
		//CREATE DATABASE
		DatabaseSet databaseSet = DatabaseSet.createEmptyDatabaseSet();
		
		//CREATE FORK
		DatabaseSet fork = databaseSet.fork();
		
		//SET BALANCE
		databaseSet.getBalanceDatabase().setBalance("test", BigDecimal.ONE);
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, databaseSet.getBalanceDatabase().getBalance("test"));
		
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.ONE, fork.getBalanceDatabase().getBalance("test"));
		
		//SET BALANCE IN FORK
		fork.getBalanceDatabase().setBalance("test", BigDecimal.TEN);
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, databaseSet.getBalanceDatabase().getBalance("test"));
				
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.TEN, fork.getBalanceDatabase().getBalance("test"));
		
		//CREATE SECOND FORK
		DatabaseSet fork2 = fork.fork();
		
		//SET BALANCE IN FORK2
		fork2.getBalanceDatabase().setBalance("test", BigDecimal.ZERO);
		
		//CHECK VALUE IN DB
		assertEquals(BigDecimal.ONE, databaseSet.getBalanceDatabase().getBalance("test"));
						
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.TEN, fork.getBalanceDatabase().getBalance("test"));
		
		//CHECK VALUE IN FORK
		assertEquals(BigDecimal.ZERO, fork2.getBalanceDatabase().getBalance("test"));
	}	
}
