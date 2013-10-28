package com.manzia.shopping.services;

import java.util.Map;

public interface MzRetailServiceParameters {
	
	Map<String, String> getItemSearchParameters(MzItemSearchType searchType);
	
	Map<String,String> getItemDetailParameters(MzItemLookupType detailType);

	Map<String, String> getBrowseNodeParameters(MzBrowseNodeLookupType amazonDefault);

}
