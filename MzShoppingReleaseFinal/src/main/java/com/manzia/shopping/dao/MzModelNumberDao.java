package com.manzia.shopping.dao;

import java.util.List;

import javax.persistence.EntityManager;

import com.manzia.shopping.model.MzModelNumber;
import com.manzia.shopping.model.MzModelNumberPK;
import com.manzia.shopping.model.MzProductResult;

/**
 * Interface that exposes data access layer methods
 * for the MzModelNumber entity
 * @author Roy Manzi Tumubweinee
 *
 */

public interface MzModelNumberDao {
	
	/**
	 * Method that sets the EntityManager
	 * 
	 * @param newEntityManager
	 */
	public void setEntityManager(EntityManager newEntityManager);
	
	/**
	 * Method that adds a MzProductResult instance to a MzModelNumber instance
	 * and persists it to the database
	 * @param product - the MzProductResult instance to be persisted
	 * to the database
	 * @param modelKey - primary key of MzModelNumber with which the MzProductResult
	 * will be associated
	 * @throws Exception - throws an exception if the persist operation
	 * fails
	 */
	void addProductResult(MzProductResult product, MzModelNumberPK modelKey) throws Exception;
	
	/**
	 * Method that deletes a MzProductResult instance from a MzModelNumber instance
	 * in the database
	 * @param product - MzProductResult instance to be deleted from
	 * to the database
	 * @throws Exception - throws an exception if the delete operation
	 * fails
	 */
	void deleteProductResult(MzProductResult product) throws Exception;
	
	/**
	 * Retrieve all MzModelNumber instances associated with a given Category
	 * @param categoryName - MzModelNumber instances with this categoryName as the
	 * value of their modelCategory property will be retrieved
	 * @return - returns a List of matching MzModelNumber instance or an empty List if no
	 * matching instances are found
	 */
	List<MzModelNumber> getModelNumberByCategory(String categoryName);
	
	
	/**
	 * Retrieve all the distinct modelCategorys' in the database
	 * @return - returns a List of strings of all modelCategory values
	 */
	List<String> getAllModelCategories(); 
	

}
