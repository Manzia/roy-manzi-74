package com.manzia.shopping.searches;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrQuery;

/**
 * Performs simple query validation such as <br>
 * 1- verify Category value is valid <br>
 * 2- verify the SKU value is alphanumeric <br>
 * 3- ensures that a valid query MUST have either a Category value
 * or a Sku value or both <br>
 * 
 * @author Roy Manzi Tumubweinee, Feb 2, 2013, Manzia Corporation
 *
 */
public final class MzSimpleQueryValidator extends MzAbstractQueryValidator {
	
	// Keys
	private static final String kCategoryKey = "Category";
	private static final String kSkuKey = "sku";
	private static final String kSolrCategoryKey = "category";
	
	// Query Key Pattern, i.e q1=term1 term2&q2=term3...
	protected static final Pattern QUERY_PATTERN = Pattern.compile("q[\\d]+");
	
	/**
	 * Validates a specified {@link Map} of query parameters <br>
	 * 1- verify Category value is valid <br>
	 * 2- verify the SKU value is alphanumeric <br>
	 * 3- ensures that a valid query MUST have either a Category value
	 * or a Sku value or both <br>
	 * @see MzAbstractQueryValidator#verifyQueryCategory(String)
	 * 
	 * @param queryMap - {@link Map} of query parameters
	 * @return - true if a valid {@link Map} else false
	 */
	public boolean isValidQueryMap (Map<String, String> queryMap ) {
		
		// check input
		if (queryMap == null || queryMap.isEmpty()) return false;
		
		// Validate
		String categoryStr = queryMap.get(kCategoryKey);
		String skuStr = queryMap.get(kSkuKey);
		if (categoryStr == null && skuStr == null) return false;
		
		boolean validCategory;
		boolean validSku;
		boolean success;
		validCategory = verifyQueryCategory(categoryStr) ? true : false;
		validSku = isAlphanumericString(skuStr) ? true : false;
		
		if (categoryStr == null) {
			// In this case we have a Sku value but no category value
			success = validSku;
		} else if (skuStr == null ) {
			// In this case we have a category value but no Sku value
			success = validCategory;
		} else {
			// In this case we have both category and Sku values
			success = validCategory && validSku;
		}
		
		return success;		
	}
	
	/**
	 * <p>Method checks that the {@link Map} of Query Parameters contains at least one valid
	 * query i.e contains Keys with the pattern "q[0-9]+", for example q1, q23 etc,
	 *  each of which represents a sub-query (Term or Phrase) </p>
	 *  
	 * @param queryMap - {@link Map} of Query Parameters
	 * @return - True if at least one valid query exists else false
	 */
	public boolean containsValidQueries ( Map<String, String> queryMap ) {
		
		// Iterate over the Map
		boolean success = false;
		for (Iterator<Map.Entry<String, String>> queryIter = queryMap.entrySet().iterator(); queryIter.hasNext();) {
			Map.Entry<String, String> mapEntry = queryIter.next();
			if (QUERY_PATTERN.matcher(mapEntry.getKey()).matches()) {
				success = true;
				break;
			}
		}		
		return success;		
	}
	
	/**
	 * Helper method that strips the query {@link String} from a {@link SolrQuery}
	 * of the initial 'category:Value' string e.g the typical query is of the form: <br>
	 * - category:Tablets "attractive display" portable <br>
	 * @param query - {@link String} to extract from
	 * @return - query Terms
	 */
	public String extractQueryTerms( String query ) {
		
		assert query != null;
		if (!query.startsWith(kSolrCategoryKey)) return query;
		String[] splitStr = query.split(" ", 2);	// Split into 2 on whitespace
		assert splitStr != null;
		if (splitStr.length != 2) return query; 
		return splitStr[1].length() > 0 ? splitStr[1] : query;		
	}
}
