package com.manzia.shopping.searches;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates the required Solr query parameters for a given {@link MzQueryType}
 * 
 * @author Roy Manzi Tumubweinee, Jan 28, 2013
 *
 */
public final class MzSolrQueryParameters {
	
	public Map<String, String> getParameters( MzQueryType queryType) {
		
		Map<String, String> paramsMap = new ConcurrentHashMap<String, String>();
		
		switch (queryType) {
		case RETURN_SKUS:
			paramsMap.put("fl", "sku");		// only return the values of the sku Field
			paramsMap.put("fq", "reviewType:mergedReview");	// search within merged Reviews only
			paramsMap.put("pf", "text^20");	// boost all q term matches in the text Field by 10
			paramsMap.put("ps", "10");		// look at terms within 10 positions for matches
			paramsMap.put("qf", "title^20");	// boost matches in the title Field by 20
			paramsMap.put("mm", "3<80%");	// at least 80% should match above 3 clauses
			//paramsMap.put("defType", "edismax");
			paramsMap.put("sort", "score desc");
			paramsMap.put("rows", "15");
			break;
		case RETURN_REVIEWS:
			paramsMap.put("fl", "*"); // return all the Fields
			paramsMap.put("defType", "edismax");
			paramsMap.put("sort", "score desc");
			paramsMap.put("rows", "15");
			break;
		case RETURN_RANKS:
			paramsMap.put("fl", "sku");		// only return the values of the sku Field
			paramsMap.put("fq", "reviewType:mergedReview");	// search within merged Reviews only
			paramsMap.put("pf", "text^20");	// boost all q term matches in the text Field by 10
			paramsMap.put("ps", "10");		// look at terms within 10 positions for matches
			paramsMap.put("qf", "title^20");	// boost matches in the title Field by 20
			paramsMap.put("mm", "3<80%");	// at least 80% should match above 3 clauses
			//paramsMap.put("defType", "edismax");
			paramsMap.put("sort", "score desc");
			paramsMap.put("rows", "25");	// Return 25 hits instead of just 15 hits
			default:
				break;
		}
		
		return paramsMap;		
	}
}
