package com.manzia.shopping.dao;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.manzia.shopping.model.MzSearchDetail;
import com.manzia.shopping.model.MzUserDevice;

/**
 * Data Access Implementation for {@link MzUserDeviceDao} Interface. <br>
 * Note: We use the default CDI scope @Dependent so the liefcycle of instances of this
 * class depends on the lifecycle of the client  
 * 
 * @author Roy Manzi Tumubweinee, Feb 06, 2013, Manzia Corporation
 *
 */
@MzUserDeviceTable
public class MzUserDeviceDataImpl implements MzUserDeviceDao {
	
	// Instance variables
	private EntityManager entityManager;
	public static final Logger logger = 
			Logger.getLogger(MzUserDeviceDataImpl.class.getCanonicalName());
	
	// Set our EntityManager instance variable
	@Override
	@Inject
	public void setEntityManager(@MzProdDatabase EntityManager newEntityManager) {
		// return if newEntityManager is null
		if (newEntityManager == null) {
			logger.log(Level.WARNING, "Attempted to set a null EntityManager in class: {0}", 
					new Object[] { MzUserDeviceDataImpl.class.getName()});
			return;
		}
		// set the entityManager
		this.entityManager = newEntityManager;		
	}
	
	@Override
	public boolean addMzSearchDetail(MzSearchDetail searchDetail,
			String deviceId) {
		assert searchDetail != null;
		assert deviceId != null;
		MzUserDevice userDevice;
		
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT add MzSearchDetail!");
			return false;
		}
		try {
			userDevice = entityManager.find(MzUserDevice.class, deviceId);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to retrieve MzUserDevice: " + e.getLocalizedMessage());
			return false;
		}
		
		// do the updates
		searchDetail.setUserDevice(userDevice);
		userDevice.getSearchItems().add(searchDetail);
		
		// update DB
		this.entityManager.flush();
				
		return true;
	}

	@Override
	public void deleteMzSearchDetail(MzSearchDetail searchDetail) {
		assert searchDetail != null;
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT delete MzSearchDetail!");
			return;
		}
		MzUserDevice userDevice = searchDetail.getUserDevice();
		assert userDevice != null;
		userDevice.getSearchItems().remove(searchDetail);
		
		// remove from database
		try {
			entityManager.remove(searchDetail);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to delete MzSearchDetail: "+ e.getLocalizedMessage());
			return;
		}		
	}

	@Override
	public List<MzSearchDetail> getSearchDetailForUserDevice(String deviceId) {
		MzUserDevice userDevice;
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return List<MzSearchDetail>!");
			return null;
		}
		try {
			userDevice = entityManager.find(MzUserDevice.class, deviceId);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to retrieve MzUserDevice:" + e.getLocalizedMessage());
			return null;
		}
		assert userDevice != null;
		return userDevice.getSearchItems();
	}

	@Override
	public MzUserDevice getMzUserDeviceById(String deviceId) {
		MzUserDevice userDevice;
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return MzUserDevice!");
			return null;
		}
		try {
			userDevice = entityManager.find(MzUserDevice.class, deviceId);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to retrieve MzUserDevice: "+ e.getLocalizedMessage());
			return null;
		}
		assert userDevice != null;
		return userDevice;
	}

	@Override
	public boolean addMzUserDevice(MzUserDevice userDevice) {
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return MzUserDevice!");
			return false;
		}
		try {
			entityManager.persist(userDevice);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to persist MzUserDevice: " + e.getLocalizedMessage());
			return false;		
		}
		return true;
	}

	@Override
	public void deleteMzUserDevice(String deviceId) {
		MzUserDevice userDevice;
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT delete MzUserDevice!");
			return;
		}
		try {
			userDevice = this.getMzUserDeviceById(deviceId);
			entityManager.remove(userDevice);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to persist MzUserDevice: "+ e.getLocalizedMessage());
			return;		
		}
	}

}
