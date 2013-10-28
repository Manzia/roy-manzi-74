package com.manzia.shopping.dao;

import java.io.File;

import javax.persistence.EntityManager;

import com.manzia.shopping.model.MzProductResult;

/**
 * Interface that exposes the data access layer methods 
 * associated with the MzProductResult entity
 * @author Roy Manzi Tumubweinee
 *
 */
public interface MzProductResultDao {
	
	/**
	 * Method that sets the EntityManager
	 * 
	 * @param newEntityManager
	 */
	public void setEntityManager(EntityManager newEntityManager);
	
	/**
	 * Method that retrieves a MzProductResult instance from the
	 * database
	 * @param productId - primary key of the MzProductResult instance
	 * to retrieve
	 * @return - returns requested MzProductResult or null
	 */
	public MzProductResult getMzProductResultById(int productId);
	
	/**
	 * Uses JAXB API to write the MzProductResult instance to an XML
	 * file
	 * @param filename - filename that will be written to
	 */
	public void writeProductResultToXML(File filename);
}
