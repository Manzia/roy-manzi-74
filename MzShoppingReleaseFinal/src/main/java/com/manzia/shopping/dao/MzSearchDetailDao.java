package com.manzia.shopping.dao;

import java.util.List;

import javax.persistence.EntityManager;

import com.manzia.shopping.model.MzModelNumber;
import com.manzia.shopping.model.MzSearchDetail;

/**
 * Interface that exposes the data access layer methods
 * associated with the MzSearchDetail entity
 * @author Roy Manzi Tumubweinee
 *
 */
public interface MzSearchDetailDao {
	
	/**
	 * Method that sets the EntityManager
	 * 
	 * @param newEntityManager
	 */
	public void setEntityManager(EntityManager newEntityManager);
	
	/**
	 * Method that retrieves a MzSearchDetail instance from
	 * the database
	 * @param searchDetailId - primary key of the MzSearchDetail
	 * to retrieve
	 * @return - returns the requested MzSearchDetail instance
	 */
	public MzSearchDetail getSearchDetailById(int searchDetailId);
	
	
	/**
	 * Method that associates a MzModelNumber instance with a
	 * MzSearchDetail instance
	 * @param model - MzModelNumber instance to be associated
	 * @param searchId - MzSearchDetail to be associated
	 * @return - returns TRUE if successfully associated else FALSE
	 */
	public boolean addModelNumberToSearchDetail(MzModelNumber model, int searchId);
	
	/**
	 * Method returns the MzModelNumber instances associated with a specific
	 * MzSearchDetail instance
	 * @param searchDetailId - primary key of MzSearchDetail whose associated
	 * MzModelNumber instances will be returned
	 * @return - List of MzModelNumber instances or empty list
	 */
	public List<MzModelNumber> getModelNumberForSearchDetail(int searchDetailId);
	
	/**
	 * Method dissociates a MzModelNumber instance from a MzSearchDetail instance
	 * NOTE: The MzModelNumber instance is NOT removed/deleted from the database instead
	 * its searchItem property is set to NULL.
	 * @param model - MzModelNumber to be dissociated
	 */
	public void removeModelNumberFromSearchDetail(MzModelNumber model);
	
}
