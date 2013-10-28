package com.manzia.shopping.dao;

import java.util.List;

import javax.persistence.EntityManager;

import com.manzia.shopping.model.MzProductSkus;

/**
 * Interface that exposes data access layer methods
 * for the MzProductSkus entity
 * @author Roy Manzi Tumubweinee, Jan 13, 2013, Manzia Corporation
 *
 */
public interface MzProductSkusDao {
	
	/**
	 * Method that sets the EntityManager
	 * 
	 * @param newEntityManager
	 */
	public void setEntityManager(EntityManager newEntityManager);
	
	/**
	 * Method that retrieves all the associated MzProductSkus instances for a given
	 * product Model and product Brand combination
	 * @param prodModel - the product Model
	 * @param prodBrand - the product Brand
	 * @return - List of MzProductSkus that match the specified model and brand
	 */
	public List<MzProductSkus> getSkusByModelAndBrand( String prodModel, String prodBrand);
	
	/**
	 * Method that retrieves all the associated MzProductSkus instances for a given
	 * product category
	 * @param prodCategory - the product category
	 * @return - List of MzProductSkus that match the specified category
	 */
	public List<String> getProductSkusByCategory( String prodCategory );
	
	/**
	 * Method that retrieves the category associated with a given product SKU
	 * @param productSKU - the product SKU
	 * @return - the associated product category
	 */
	public String getCategoryForSKU( String productSKU );
	
	/**
	 * Method that retreives all the Product Categories in the MzProductSkus table
	 * @return - List of all persisted product categories
	 */
	public List<String> getAllProductCategories();
	
	/**
	 * Retrieve the MzProductSkus instance associated with specified product SKU
	 * @param productSKU - the product SKU
	 * @return - the MzProductSkus instance
	 */
	public MzProductSkus getMzProductSku( String productSKU );
	
	/**
	 * Persist a new MzProductSkus instance to the MzProductSkus table
	 * @param product - the MzProductSkus instance to persist
	 * @return - true if success else false
	 */
	public boolean addMzProductSku( MzProductSkus product );
	
	/**
	 * Deletes an existing MzProductSkus instance from the MzProductSkus table
	 * @param productSKU - the product SKU
	 */
	public void deleteMzProductSku( String productSKU );

}
