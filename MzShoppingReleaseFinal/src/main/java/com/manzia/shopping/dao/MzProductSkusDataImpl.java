package com.manzia.shopping.dao;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import com.manzia.shopping.model.MzProductSkus;

/**
 * Data Access Implementation for {@link MzProductSkusDao} Interface. <br>
 * Note: We use the default CDI scope @Dependent so the liefcycle of instances of this
 * class depends on the lifecycle of the client  
 * 
 * @author Roy Manzi Tumubweinee, Feb 06, 2013, Manzia Corporation
 *
 */
@MzProductSkuTable
public class MzProductSkusDataImpl implements MzProductSkusDao {
	
	// Instance variables
		private EntityManager entityManager;
		public static final Logger logger = 
				Logger.getLogger(MzProductSkusDataImpl.class.getCanonicalName());
	
		// Options
		//@Inject Instance<MzProductSkusDataImpl> skuDaoInstance;

	@Override
	@Inject
	public void setEntityManager(@MzProdDatabase EntityManager newEntityManager) {
		// return if newEntityManager is null
		if (newEntityManager == null) {
			logger.log(Level.WARNING, "Attempted to set a null EntityManager in class: {0}", 
					new Object[] {MzProductSkusDataImpl.class.getName()});
			return;
		}
		// set the entityManager
		this.entityManager = newEntityManager;
	}

	@Override
	public List<MzProductSkus> getSkusByModelAndBrand(String prodModel,
			String prodBrand) {
		if (prodModel == null || prodModel.length() < 1 || prodBrand == null || prodBrand.length() < 1) {
			logger.log(Level.WARNING, "Invalid prodModel and/or prodBrand arguments..cannot retrieve associated MzProductSkus instances!");
			throw new IllegalArgumentException("Invalid product Model and/or Brand argument for MzProductSkus retrieval specified!");
		}
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return MzProductSkus for Model and Brand!");
			throw new RuntimeException("Attempted to retrieve MzProductSkus for Model and Brand with NULL EntityManager!");
		}
		
		@SuppressWarnings("unchecked")
		List<MzProductSkus> prodList = this.entityManager.createNamedQuery("findSkusByModelAndBrand").
				setParameter("productModel", prodModel).setParameter("productBrand", prodBrand).getResultList();
		assert prodList != null;
		return prodList;
	}

	@Override
	public List<String> getProductSkusByCategory(String prodCategory) {
		// check inputs
		if (prodCategory == null || prodCategory.length() < 1) {
			logger.log(Level.WARNING, "Invalid productCategory argument..cannot retrieve MzProductSkus for Category!");
			throw new IllegalArgumentException("Invalid Category argument for MzProductSkus retrieval specified!");
		}
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return MzProductSkus for Category!");
			throw new RuntimeException("Attempted to retrieve MzProductSkus for Category with NULL EntityManager!");
		}
		
		@SuppressWarnings("unchecked")
		List<String> prodList = this.entityManager.createNamedQuery("findProductSkusByCategory").
				setParameter("productCategory", prodCategory).getResultList();
		assert prodList != null;
		return prodList;
	}

	@Override
	public String getCategoryForSKU(String productSKU) {
		// check inputs
		if (productSKU == null || productSKU.length() < 1) {
			logger.log(Level.WARNING, "Invalid productSKU argument..cannot retrieve Category for given productSKU!");
			throw new IllegalArgumentException("Invalid productSKU argument for category retrieval specified!");
		}
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return Category for productSKU!");
			throw new RuntimeException("Attempted to retrieve Category for productSKU with NULL EntityManager!");
		}
		String prodCategory = null;
		try {
			prodCategory = (String) this.entityManager.createNamedQuery("findProductCategoryForSku").
					setParameter("productSku", productSKU).getSingleResult();
		} catch (NoResultException re) {
			return prodCategory;
		} catch (NonUniqueResultException ue) {
			logger.log(Level.SEVERE, "Non unique Category result obtained for a product SKU!");
			throw new RuntimeException("Non unique Category result for product SKU " + ue.getLocalizedMessage());
		}
		
		assert prodCategory != null;
		return prodCategory;
	}

	@Override
	public List<String> getAllProductCategories() {
		
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return Product Category List!");
			throw new RuntimeException("Attempted to retrieve Product Categories with NULL EntityManager!");
		}
				
		@SuppressWarnings("unchecked")
		List<String> categories = this.entityManager.createNamedQuery("findAllProductCategories").getResultList();
		assert categories != null;
		return categories;
	}

	@Override
	public MzProductSkus getMzProductSku(String productSKU) {
		MzProductSkus productSkus;
		
		//check input
		if (productSKU == null || productSKU.length() < 1) {
			logger.log(Level.WARNING, "Invalid productSKU argument..cannot retrieve associated MzProductSkus instance!");
			throw new IllegalArgumentException("Invalid productSKU argument for retrieval specified!");
		}
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT return MzProductSkus!");
			throw new RuntimeException("Attempted to retrieve MzProductSkus instance with NULL EntityManager!");
		}
		try {
			productSkus = entityManager.find(MzProductSkus.class, productSKU);
		} catch (IllegalArgumentException e) {
			logger.log(Level.WARNING, "Failed to retrieve MzProductSkus instance for product SKU: {0} "+ e.getLocalizedMessage(), 
					new Object[]{ productSKU});
			return null;
		}
		//assert productSkus != null;
		return productSkus;
	}

	@Override
	public boolean addMzProductSku(MzProductSkus product) {
		
		// check input
		if (product == null) {
			logger.log(Level.WARNING, "Cannot add NULL MzProductSkus instance to database!");
			return false;
		}
		
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT add MzProductSkus instance!");
			throw new RuntimeException("Attempted to add MzProductSkus instance with NULL EntityManager!");
		}
		try {
			entityManager.persist(product);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to persist MzProductSkus: " + e.getLocalizedMessage());
			return false;		
		}
		return true;
	}

	@Override
	public void deleteMzProductSku(String productSKU) {
		
		//check input
		if (productSKU == null || productSKU.length() < 1) {
			logger.log(Level.WARNING, "Invalid productSKU argument..cannot delete associated MzProductSkus instance!");
			throw new IllegalArgumentException("Invalid productSKU argument for delete specified!");
		}
		
		MzProductSkus productSkus;
		if (this.entityManager == null) {
			logger.log(Level.WARNING, "EntityManager has not been set..will NOT add MzProductSkus instance!");
			throw new RuntimeException("Attempted to delete MzProductSkus instance with NULL EntityManager!");
		}
		try {
			productSkus = this.getMzProductSku(productSKU);
			if (productSkus != null) {
				entityManager.remove(productSkus);
			}			
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to delete MzProductSkus instance with productSKU: {0} "+ e.getLocalizedMessage(), 
					new Object[]{ productSKU });
			return;		
		}
	}

}
