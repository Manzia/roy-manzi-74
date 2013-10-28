package com.manzia.shopping.attributes;

import java.io.File;
import java.util.Set;

/**
 * Interface to be implemented for classes that map attributes values. Specifically
 * mapping Manzia (attribute, value) pairs to Retailer-specific (attribute, value)
 * pairs. Because we compute distance of "search" vectors we need to use the same
 * attributes and sets of values so we are actually comparing apples to apples when
 * we compare a search Vector to a Vector derived from a Retailer's data. *
 *
 * @author Roy Manzi Tumubweinee, Nov 02, 2012. Manzia Corporation
 *
 */
public interface MzAttributeMapper {
	
	/**
	 * Given an (attribute, value) pair from Retailer's data, determine and
	 * return the corresponding value from the set of Manzia (attribute, value)
	 * pairs<br>
	 * @param attribute - String attribute
	 * @param value - String value
	 * @return - Equivalent String value from the set of Manzia (attribute, value) set or nil
	 */
	public String mappedValueForAttribute( String category, String attribute, String value);
	
	// Setter and getter for attribute FileName
	public File getAttributesFile();

	public void setAttributesFile(File attributesFile);
	
	/**
	 * Method returns all the Brand attribute Values in the Manzia Attributes File
	 * associated with this MzAttributeMapper
	 * @return - set of all Brand attribute values in the Manzia Attributes File
	 */
	public Set<String> allBrandAttributeValues();

}
