package com.manzia.shopping.searches;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.manzia.shopping.vectorize.MzSequenceFileGenerator;

/**
 * Abstract class that provides some basic query validation methods
 * to validate a {@link Map} of query parameters such as: <br>
 * 1- verify existence of Category Key/Value <br>
 * 2- verify existence of sku Key/Value <br>
 * 3- confirm validity of Category value <br>
 * 4- confirm sku values are alphanumeric <br>
 * 
 * Users are expected to extend this class and validate other attributes/properties
 * of queries
 * @author Roy Manzi Tumubweinee, Feb 02, 2013, Manzia Corporation
 *
 */
public abstract class MzAbstractQueryValidator {
	
	//Logger
	public static final Logger logger = 
			Logger.getLogger(MzAbstractQueryValidator.class.getCanonicalName());
	
	// Alphanumeric pattern for Sku value
		private final Pattern alphanums = Pattern.compile("[\\p{Alnum}]+");
	
	/**
	 * Verifies if the specified {@link String} categoryName is an valid Category. <br>
	 * A categoryName string is considered valid if its included in the static CATEGORY_LIST of
	 * the {@link MzSequenceFileGenerator} class <br>
	 *  
	 * @param categoryName - {@link String} categoryName
	 * @return - true if valid categoryName else false
	 */
	public boolean verifyQueryCategory( String categoryName ) {
		
		// check input
		if (categoryName == null || categoryName.isEmpty()) return false;
		
		// Verify
		Set<String> categorySet = new HashSet<String>();
		categorySet.addAll(MzSequenceFileGenerator.getCategoryList());
		if ( !categorySet.contains(categoryName)) {
			logger.log(Level.WARNING, "Specified Value: {0} is an Invalid Category Value!", new Object[]{categoryName});
			return false;
		} else {
			return true;
		}		
	}
	
	public boolean isAlphanumericString ( String skuValue ) {
		
		// check input
		if (skuValue == null || skuValue.isEmpty()) return false;
		
		// check if its all alphanumeric, just return
    	if (alphanums.matcher(skuValue).matches()) {
    		return true;
    	} else {
    		logger.log(Level.WARNING, "Specified Value: {0} is an Invalid SKU Value!", new Object[]{skuValue});
			return false;
    	}		
	}
}
