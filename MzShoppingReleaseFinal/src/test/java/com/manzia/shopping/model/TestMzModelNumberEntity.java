package com.manzia.shopping.model;

import static org.junit.Assert.*;

import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestMzModelNumberEntity extends MzAbstractJPADbUnitTestCase {
	private MzModelNumber model;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		
		// remove the inserted item
		if (model != null) {
			manager.remove(model);
		}
	}

	// Test the INSERT & RETRIEVE
		@Test
		public void testLoadMzModelNumber() throws Exception{
			IDataSet setupDataSet = getDataSet("/TestMzModelNumberFile.xml");
			assertNotNull("DataSet is null", setupDataSet);
			DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);
			MzModelNumberPK key = MzModelNumberHelper.testPrimaryKey();
			model = manager.find(MzModelNumber.class, key);
			assertNotNull("Retrieved ModelNumber null", model);
			
			// check that we returned the same instance values as we inserted
			MzModelNumberHelper.assertMzModelNumber(model);
		}

}
