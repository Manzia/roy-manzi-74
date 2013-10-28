package com.manzia.shopping.dao;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.manzia.shopping.model.MzModelNumber;
import com.manzia.shopping.model.MzSearchDetail;
import com.manzia.shopping.model.MzUserDevice;

public class MzSearchDetailDataImpl implements MzSearchDetailDao {
	
	// Instance variables
		private EntityManager entityManager;
		public static final Logger logger = 
				Logger.getLogger(MzUserDeviceDataImpl.class.getCanonicalName());
		
		// Set our EntityManager instance variable
		@Override
		public void setEntityManager(EntityManager newEntityManager) {
			// return if newEntityManager is null
			if (newEntityManager == null) {
				logger.log(Level.WARNING, "Attempted to set a null EntityManager in class: ");
				return;
			}
			// set the entityManager
			this.entityManager = newEntityManager;		
		}

	@Override
	public MzSearchDetail getSearchDetailById(int searchDetailId) {
		MzSearchDetail searchDetail;
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return MzSearchDetail!");
			return null;
		}
		try {
			searchDetail = entityManager.find(MzSearchDetail.class, searchDetailId);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to retrieve MzSearchDetail", e.getLocalizedMessage());
			return null;
		}
		assert searchDetail != null;
		return searchDetail;		
	}

	@Override
	public boolean addModelNumberToSearchDetail(MzModelNumber model,
			int searchId) {
		assert model != null;
		MzSearchDetail searchDetail;
		
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT add MzModelNumber!");
			return false;
		}
		try {
			searchDetail = this.getSearchDetailById(searchId);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to retrieve MzUserDevice", e.getLocalizedMessage());
			return false;
		}
		
		// do the updates
		model.setSearchItem(searchDetail);
		searchDetail.getModelNumbers().add(model);
		
		// update DB
		this.entityManager.flush();
				
		return true;
	}

	@Override
	public List<MzModelNumber> getModelNumberForSearchDetail(int searchDetailId) {
		MzSearchDetail searchDetail;
		
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return List<MzModelNumber>!");
			return null;
		}
		try {
			searchDetail = this.getSearchDetailById(searchDetailId);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to retrieve MzUserDevice", e.getLocalizedMessage());
			return null;
		}
		assert searchDetail != null;
		return searchDetail.getModelNumbers();
	}

	@Override
	public void removeModelNumberFromSearchDetail(MzModelNumber model) {
		MzSearchDetail searchDetail;
				
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return List<MzModelNumber>!");
			return;
		}
		try {
			searchDetail = model.getSearchItem();
			model.setSearchItem(null);
			searchDetail.getModelNumbers().remove(model);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to delete MzUserDevice", e.getLocalizedMessage());
			return;
		}
	}

}
