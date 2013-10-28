package com.manzia.shopping.dao;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.manzia.shopping.model.MzProductResult;
import com.manzia.shopping.model.MzSearchDetail;

public class MzProductResultDataImpl implements MzProductResultDao {
	
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
	public MzProductResult getMzProductResultById(int productId) {
		MzProductResult productResult;
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return MzProductResult!");
			return null;
		}
		try {
			productResult = this.entityManager.find(MzProductResult.class, productId);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to retrieve MzProductResult", e.getLocalizedMessage());
			return null;
		}
		assert productResult != null;
		return productResult;		
	}

	@Override
	public void writeProductResultToXML(File filename) {
		// TODO Auto-generated method stub

	}

}
