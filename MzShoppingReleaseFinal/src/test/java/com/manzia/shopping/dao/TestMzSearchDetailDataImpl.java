package com.manzia.shopping.dao;

import static org.junit.Assert.*;

import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.manzia.shopping.model.MzAbstractJPADbUnitTestCase;
import com.manzia.shopping.model.MzModelNumber;
import com.manzia.shopping.model.MzModelNumberHelper;
import com.manzia.shopping.model.MzModelNumberPK;
import com.manzia.shopping.model.MzSearchDetail;
import com.manzia.shopping.model.MzSearchDetailHelper;
import com.manzia.shopping.model.MzUserDevice;

public class TestMzSearchDetailDataImpl extends MzAbstractJPADbUnitTestCase {
	
	private MzUserDeviceDataImpl userDeviceImpl;
	private MzUserDevice userDevice;
	private MzSearchDetailDataImpl searchImpl;
	private MzSearchDetail searchDetail;
	private MzModelNumberPK keyModel;
	MzModelNumber retrievedModel;
	private static final String deviceKey = "415-309-7418";

	@Before
	public void setUp() throws Exception {
		userDeviceImpl = new MzUserDeviceDataImpl();
		userDeviceImpl.setEntityManager(manager);
		searchImpl = new MzSearchDetailDataImpl();
		searchImpl.setEntityManager(manager);
	}

	@After
	public void tearDown() throws Exception {
		if (userDevice != null) {
			manager.remove(userDevice);			
		}		
		if (retrievedModel != null) {
			manager.remove(retrievedModel);			
		}
		// clear implementation
		manager.flush();
		userDeviceImpl = null;
		searchImpl = null;
		userDevice = null;
		retrievedModel = null;
	}

	@Test
	public void testAddModelNumberToSearchDetail() throws Exception {
		// Insert a MzUserDevice instance
		IDataSet setupDataSet = getDataSet("/TestMzUserDeviceFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);

		searchDetail = MzSearchDetailHelper.newMzSearchDetail();
		userDeviceImpl.addMzSearchDetail(searchDetail, deviceKey);
		userDevice = manager.find(MzUserDevice.class, deviceKey);		
		
		// Insert a MzModelNumber instance
		IDataSet setDataSet = getDataSet("/TestMzModelNumberFile.xml");
		assertNotNull("DataSet is null", setDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setDataSet);
		keyModel = MzModelNumberHelper.testPrimaryKey();
		retrievedModel = manager.find(MzModelNumber.class, keyModel);
		assertNotNull("RetrievedModel is null", retrievedModel);
		
		// add the model
		searchImpl.addModelNumberToSearchDetail(retrievedModel, searchDetail.getSearchItemID());
		assertFalse("ModelNumber list is empty", searchDetail.getModelNumbers().isEmpty());
	}

	
	@Test
	public void testRemoveModelNumberFromSearchDetail() throws Exception {
		// Insert a MzUserDevice instance
		IDataSet setupDataSet = getDataSet("/TestMzUserDeviceFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);

		searchDetail = MzSearchDetailHelper.newMzSearchDetail();
		userDeviceImpl.addMzSearchDetail(searchDetail, deviceKey);
		userDevice = manager.find(MzUserDevice.class, deviceKey);		

		// Insert a MzModelNumber instance
		IDataSet setDataSet = getDataSet("/TestMzModelNumberFile.xml");
		assertNotNull("DataSet is null", setDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setDataSet);
		keyModel = MzModelNumberHelper.testPrimaryKey();
		retrievedModel = manager.find(MzModelNumber.class, keyModel);
		assertNotNull("RetrievedModel is null", retrievedModel);

		// add the model
		searchImpl.addModelNumberToSearchDetail(retrievedModel, searchDetail.getSearchItemID());
		
		// remove the added model
		searchImpl.removeModelNumberFromSearchDetail(retrievedModel);
		assertTrue("ModelNumber list is not empty", searchDetail.getModelNumbers().isEmpty());
	}

}
