package com.manzia.shopping.dao;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.manzia.shopping.model.MzModelNumber;
import com.manzia.shopping.model.MzModelNumberPK;
import com.manzia.shopping.model.MzProductResult;

public class MzModelNumberDataImpl implements MzModelNumberDao {
	
	// Instance variables
	private EntityManager entityManager;
	public static final Logger logger = 
			Logger.getLogger(MzModelNumberDataImpl.class.getCanonicalName());
	
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
	public void addProductResult(MzProductResult product,
			MzModelNumberPK modelKey) throws Exception {
		assert product != null;
		assert modelKey != null;
		
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT add MzProductResult!");
			return;
		}
		
		// Get the MzModelNumber instance from the database
		MzModelNumber retrievedModel = this.entityManager.find(MzModelNumber.class, modelKey);
		assert retrievedModel != null;
		
		// add the MzProductResult
		product.setModelNumber(retrievedModel);
		retrievedModel.getProductResults().add(product);
		
		// persist the MzProductResult
		//this.entityManager.persist(product);
	}

	@Override
	public void deleteProductResult(MzProductResult product) throws Exception {
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT delete MzProductResult!");
			return;
		}
		MzModelNumber associatedModel = product.getModelNumber();
		assert associatedModel != null;
		associatedModel.getProductResults().remove(product);
		
		// remove from database
		try {
			this.entityManager.remove(product);
		} catch (Exception e) {
			throw new Exception("Delete failed :" + e.getLocalizedMessage());
		}		
	}

	@Override
	public List<MzModelNumber> getModelNumberByCategory(String categoryName) {
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return List<MzModelNumber>!");
			return null;
		}
		String name = categoryName + "%";	// modelCategory column values have an extra character I can't quite figure out!!!
		
		@SuppressWarnings("unchecked")
		List<MzModelNumber> modelNums = this.entityManager.createNamedQuery("findModelNumByCategory").
				setParameter("modelCategory", name).getResultList();
		assert modelNums != null;
		return modelNums;
	}

	@Override
	public List<String> getAllModelCategories() {
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return List<String> for all ModelCategorys!");
			return null;
		}
		@SuppressWarnings("unchecked")
		List<String> categories = this.entityManager.createNamedQuery("findAllModelCategories").getResultList();
		assert categories != null;
		return categories;
	}

}
