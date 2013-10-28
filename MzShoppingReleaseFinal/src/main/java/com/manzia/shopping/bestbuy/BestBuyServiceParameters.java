package com.manzia.shopping.bestbuy;

import java.util.HashMap;
import java.util.Map;

import com.manzia.shopping.services.MzBrowseNodeLookupType;
import com.manzia.shopping.services.MzItemLookupType;
import com.manzia.shopping.services.MzItemSearchType;
import com.manzia.shopping.services.MzRetailServiceParameters;

public class BestBuyServiceParameters implements MzRetailServiceParameters {

	@Override
	public Map<String, String> getItemSearchParameters(
			MzItemSearchType searchType) {
		// Declare and initialize the return map - the default behavior if
				// the searchType passed is not implemented below is to return an
				// empty map
				
		Map<String, String> returnParameters = new HashMap<String, String>();

		// Set the parameters for the different searchTypes
		//NOTE: Its assumed the caller will add the modelNumber and Manufacturer attributes and values to the query

		switch (searchType) {
		case BESTBUY_MODELNUM:
			returnParameters.put("show", "sku,name,categoryPath.name,modelNumber,subclass,type,condition,description,details.name," +
					"details.value,features.feature,longDescription,manufacturer,releaseDate,planPrice," +
					"regularPrice,salePrice,inStoreAvailability,onlineAvailability,orderable,largeFrontImage," +
					"thumbnailImage,mobileUrl");
			returnParameters.put("pageSize", "10");					
			break;
		case BESTBUY_PRODUCTSKU:
			returnParameters.put("show", "sku,name,categoryPath.name,modelNumber,subclass,type,condition,description,details.name," +
					"details.value,features.feature,longDescription,manufacturer,releaseDate,planPrice," +
					"regularPrice,salePrice,inStoreAvailability,onlineAvailability,orderable,largeFrontImage," +
					"thumbnailImage,mobileUrl");
			returnParameters.put("pageSize", "10");	
		default:
			break;

		}
				
		return returnParameters;
	}
	
	/**
	 * BestBuyServiceParameters class does not implement the 
	 * getItemDetailParameters method, returns null
	 */
	@Override
	public Map<String, String> getItemDetailParameters(
			MzItemLookupType detailType) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * BestBuyServiceParameters class does not implement the
	 * getBrowseNodeParameters method, returns null
	 */
	@Override
	public Map<String, String> getBrowseNodeParameters(
			MzBrowseNodeLookupType amazonDefault) {
		// TODO Auto-generated method stub
		return null;
	}

}
