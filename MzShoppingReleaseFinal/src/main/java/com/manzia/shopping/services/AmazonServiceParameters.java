package com.manzia.shopping.services;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

/**
 * The AmazonServiceParameters Class provides the URL parameters used by the
 * MzAmazonServiceClient Class to generate item requests to the Amazon Product
 * Advertising API. A grouping of parameters is named in the MzItemSearchType or
 * MzItemLookupType enum classes and then implemented in the inherited methods
 * from the MzRetailServiceParameters interface in this class.
 * @author Roy Manzi, Manzia Corporation
 *
 */

public class AmazonServiceParameters implements MzRetailServiceParameters {
			
	// Apparel related Parameters - no getters and setters and initializers
	//for many of these intentionally to keep the logic SIMPLE....but this
	// can be extended in the future to implement more complex logic!!
	
	// Amazon Item Search Parameters
	@XmlElement(name = "Availability")
    protected final String availability;
    @XmlElement(name = "SearchIndex")
    protected final String searchIndex;
    
    // Constructor - only availability and searchIndex are initialized for
    // simplicity
    public AmazonServiceParameters(){
    	this.availability = "Available";
    	this.searchIndex = "Apparel";
    }
    
    
    // Operations exposed on Amazon's Product Advertising API
    
    private enum Operations {
    	ItemSearch, ItemLookup, BrowseNodeLookup, SimilarityLookup;
    }

	@Override
	public Map<String, String> getItemSearchParameters(
			MzItemSearchType searchType) {
		
		// Declare and initialize the return map - the default behavior if
		// the searchType passed is not implemented below is to return an
		// empty map
		
		Map<String, String> returnParameters = new HashMap<String, String>();
		
		// Set the parameters for the different searchTypes
		//NOTE: Its assumed the caller will add the KEYWORDS to the query
		
		switch (searchType) {
		case AMAZON_DEFAULT:
			returnParameters.put("Operation", Operations.ItemSearch.toString());
			returnParameters.put("SearchIndex", searchIndex);
			returnParameters.put("Availability", availability);
			returnParameters.put("ResponseGroup", "Offers,Variations,ItemIds,Images");
			returnParameters.put("Version", "2011-08-01");
			break;
		case AMAZON_BROWSENODES:
			returnParameters.put("Operation", Operations.ItemSearch.toString());
			returnParameters.put("SearchIndex", searchIndex);
			returnParameters.put("Availability", availability);
			returnParameters.put("ResponseGroup", "Offers,ItemAttributes,EditorialReview");
			returnParameters.put("Version", "2011-08-01");
			break;
			default:
				break;
			
		}
		
		return returnParameters;
	}

	@Override
	public Map<String, String> getItemDetailParameters(
			MzItemLookupType detailType) {
		// Declare and initialize the return map - the default is to return an empty map
		// if the detailType is not implemented below.
		
				Map<String, String> returnParameters = new HashMap<String, String>();
				
				// Set the parameters for the different searchTypes
				//NOTE: Its assumed the caller will add the ITEMID key/value to the query
				
				switch (detailType) {
				case AMAZON_DEFAULT:
					returnParameters.put("Operation", Operations.ItemLookup.toString());
					returnParameters.put("Availability", availability);
					returnParameters.put("ResponseGroup", "Offers,Variations," +
							"Images,ItemAttributes,Similarities");
					returnParameters.put("Version", "2011-08-01");
					break;
				case AMAZON_SIMILARITY:
					break;
				case AMAZON_VARIATIONS:
					break;
					default:
						break;
									
				}
				
				return returnParameters;		
	}
	
	public Map<String, String> getBrowseNodeParameters(
			MzBrowseNodeLookupType browseNodeType) {
		// Declare and initialize the return map - the default is to return an empty map
		// if the detailType is not implemented below.
		
				Map<String, String> returnParameters = new HashMap<String, String>();
				
				// Set the parameters for the different searchTypes
				//NOTE: Its assumed the caller will add the ITEMID key/value to the query
				
				switch (browseNodeType) {
				case AMAZON_DEFAULT:
					returnParameters.put("Operation", Operations.BrowseNodeLookup.toString());
					returnParameters.put("Availability", availability);
					returnParameters.put("ResponseGroup", "BrowseNodeInfo");
					returnParameters.put("Version", "2011-08-01");
					break;
				default:
						break;
									
				}
				
				return returnParameters;		
	}
	
}
