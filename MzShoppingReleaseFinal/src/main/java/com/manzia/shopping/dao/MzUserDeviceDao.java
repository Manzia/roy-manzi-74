package com.manzia.shopping.dao;

import java.util.List;

import javax.persistence.EntityManager;

import com.manzia.shopping.model.MzSearchDetail;
import com.manzia.shopping.model.MzUserDevice;

/**
 * Interface that exposes the data access layer methods
 * of the MzUserDevice entity
 * @author Roy Manzi Tumubweinee
 *
 */
public interface MzUserDeviceDao {
	
	/**
	 * Method that sets the EntityManager
	 * 
	 * @param newEntityManager
	 */
	public void setEntityManager(EntityManager newEntityManager);
	
	/**
	 * Method that adds a new MzSearchDetail instance to the 
	 * database while also associating it with a MzUserDevice
	 * instance
	 * @param searchDetail - MzSearchDetail instance to be persisted
	 * @param deviceId - primary key of MzUserDevice instance to be associated
	 * @return - returns TRUE if the persistence operation succeeded
	 * else FALSE
	 */
	public boolean addMzSearchDetail(MzSearchDetail searchDetail, String deviceId);
	
	/**
	 * Method deletes a MzSearchDetail instance from the database
	 * @param searchDetail - MzSearchDetail instance to be deleted
	 */
	public void deleteMzSearchDetail(MzSearchDetail searchDetail);
	
	/**
	 * Method returns all the MzSearchDetail instances associated with a
	 * specific MzUserDevice instance
	 * @param deviceId - primary key of the MzUserDevice
	 * @return - returns a List of MzSearchDetail instances associated with
	 * the MzUserDevice whose primary key was provided
	 */
	public List<MzSearchDetail> getSearchDetailForUserDevice(String deviceId);
	
	/**
	 * Returns a MzUserDevice instance from the database
	 * @param deviceId - primary key of the MzUserDevice instance to
	 * retrieve
	 * @return - returns the requested MzUserDevice instance
	 */
	public MzUserDevice getMzUserDeviceById(String deviceId);
	
	
	/**
	 * Method persists a new MzUserDevice instance to the database
	 * @param userDevice - MzUserDevice instance to be persisted
	 * @return - returns TRUE if persistence operation succeeded else
	 * FALSE
	 */
	public boolean addMzUserDevice(MzUserDevice userDevice);
	
	/**
	 * Method deletes a MzUserDevice instance from the database
	 * @param deviceId - primary key of MzUserDevice to be deleted
	 */
	public void deleteMzUserDevice(String deviceId);
}
