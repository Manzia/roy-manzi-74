package com.manzia.shopping.model;

import java.sql.Connection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 * Superclass for the database tests - this class
 * sets up and closes the database (connection)
 * @author admin
 *
 */

public abstract class MzAbstractJPATestCase {
	private static EntityManagerFactory entityFactory;
	protected static Connection connection;
	protected static EntityManager manager;
	
	@BeforeClass
	public static void setupDatabase() throws Exception {
		entityFactory = 
				Persistence.createEntityManagerFactory("ManziaShoppingRelease");
		assertNotNull("Entity Factory is null", entityFactory);
		// We set up the entityManager here since we need to get the connection
		// and pass that to sub-classes like dbUnit that test the database..
		// this implies we use the same entityManager for all tests
		manager = entityFactory.createEntityManager();
		assertNotNull("Entity Manager is null", manager);
		
		manager.getTransaction().begin();
		connection = manager.unwrap(Connection.class);
		assertNotNull("Connection is null", connection);
	}
	
	@AfterClass
	public static void closeDatabase() throws Exception {
		// commit all changes
		manager.getTransaction().commit();
		
		// close the connection
		if (connection != null) {
			connection.close();
			connection = null;
		}
		
		//close the EntityManager
		if (manager != null) {
			manager.close();			
		}
		
		// close the EntityManagerFactory
		if (entityFactory != null) {
			entityFactory.close();
		}
	}
}
