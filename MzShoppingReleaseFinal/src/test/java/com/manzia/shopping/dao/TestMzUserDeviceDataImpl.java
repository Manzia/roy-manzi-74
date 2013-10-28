package com.manzia.shopping.dao;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.manzia.shopping.model.MzAbstractJPADbUnitTestCase;
import com.manzia.shopping.model.MzSearchDetail;
import com.manzia.shopping.model.MzSearchDetailHelper;
import com.manzia.shopping.model.MzUserDevice;

public class TestMzUserDeviceDataImpl extends MzAbstractJPADbUnitTestCase {
	
	private MzUserDevice userDevice;
	private MzUserDeviceDataImpl userDeviceImpl;
	private MzSearchDetail searchDetail;
	private static final String deviceKey = "415-309-7418";

	@Before
	public void setUp() throws Exception {
		userDeviceImpl = new MzUserDeviceDataImpl();
		userDeviceImpl.setEntityManager(manager);
	}

	@After
	public void tearDown() throws Exception {
		if (userDevice != null) {
			manager.remove(userDevice);
			manager.flush();
			userDevice = null;
		}
		// clear implementation
		userDeviceImpl = null;
	}

	@Test
	public void testAddMzSearchDetail() throws Exception {
		// Insert a MzUserDevice instance
		IDataSet setupDataSet = getDataSet("/TestMzUserDeviceFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);
		
		searchDetail = MzSearchDetailHelper.newMzSearchDetail();
		userDeviceImpl.addMzSearchDetail(searchDetail, deviceKey);
		userDevice = manager.find(MzUserDevice.class, deviceKey);
		assertNotNull("Retrieved MzUserDevice is null", userDevice);
		
		// Get back the MzSearchDetail we added and test it
		MzSearchDetail retrievedSearch = userDevice.getSearchItems().get(0);
		MzSearchDetailHelper.assertMzSearchDetail(retrievedSearch);
	}

	@Test
	public void testDeleteMzSearchDetail() throws Exception {
		// Insert a MzUserDevice instance
		IDataSet setupDataSet = getDataSet("/TestMzUserDeviceFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);

		searchDetail = MzSearchDetailHelper.newMzSearchDetail();
		userDeviceImpl.addMzSearchDetail(searchDetail, deviceKey);
		userDevice = manager.find(MzUserDevice.class, deviceKey);
		assertNotNull("Retrieved MzUserDevice is null", userDevice);
		
		MzSearchDetail retrievedSearch = userDevice.getSearchItems().get(0);
		assertNotNull("SearchDetail is null before delete", retrievedSearch);
		userDeviceImpl.deleteMzSearchDetail(retrievedSearch);
		assertTrue("Retrieved Model is not empty", userDevice.getSearchItems().isEmpty());
	}

	@Test
	public void testGetSearchDetailForUserDevice() throws Exception {
		// Insert a MzUserDevice instance
		IDataSet setupDataSet = getDataSet("/TestMzUserDeviceFile.xml");
		assertNotNull("DataSet is null", setupDataSet);
		DatabaseOperation.INSERT.execute(dbunitConnection, setupDataSet);

		searchDetail = MzSearchDetailHelper.newMzSearchDetail();
		userDeviceImpl.addMzSearchDetail(searchDetail, deviceKey);
		userDevice = manager.find(MzUserDevice.class, deviceKey);
		assertNotNull("Retrieved MzUserDevice is null", userDevice);

		List<MzSearchDetail> searches = userDeviceImpl.getSearchDetailForUserDevice(deviceKey);
		assertFalse("Retrieved List is empty", searches.isEmpty());
	}

	
	@Test
	public void testAddDeleteMzUserDevice() {
		String deviceId = "925-300-5311";
		MzUserDevice newDevice = 
				new MzUserDevice(deviceId, "newdeviceToken".getBytes(), 
						new Timestamp(System.currentTimeMillis()));
		// add device
		userDeviceImpl.addMzUserDevice(newDevice);
		
		//retrieve device
		MzUserDevice retrievedDevice = userDeviceImpl.getMzUserDeviceById(deviceId);
		assertNotNull("Retrieved MzUserDevice is null", retrievedDevice);
		
		// delete the device
		String newDeviceId = retrievedDevice.getDeviceID();
		userDeviceImpl.deleteMzUserDevice(newDeviceId);
		assertNull("Deleted MzUserDevice is not null", manager.find(MzUserDevice.class, newDeviceId));
	}

}
