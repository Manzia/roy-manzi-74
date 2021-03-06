package com.manzia.shopping.searches;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;

import com.manzia.shopping.vectorize.MzSequenceFileGenerator;

/**
 * Validates that the query parameters (keys and values) are valid i.e
 * the keys are contained in the Properties file used internally to generate
 * SequenceFiles by the MzSequenceFileGenerator class. <br>
 * 
 * <p> Returns a Map of valid query parameters or NULL if
 * 1- all query parameters specified were unknown
 * 2- query parameters do NOT include a "Category" key
 * </p>
 * 
 * @author Roy Manzi Tumubweinee
 *
 */
public class MzQueryValidator {
	
	//Logger
	public static final Logger logger = 
			Logger.getLogger(MzQueryValidator.class.getCanonicalName());
	
	//Category Key
	private static final String KCategoryKey = "Category";
		
	/**
	 * <p>Validate the Map of query parameters according to the following main conditions. The keys in
	 * the map of query parameters are compared to the values of the Properties file of feature names/values
	 * used to feature hash the Vectors stored in Sequence Files generated by MzSequenceFileGenerator. </p>
	 * 
	 * 1- Remove all entries with unknown Keys <br>
	 * 2- If all entries have unknown Keys, log and return null <br>
	 * 3- If there is no "Category" Key, log and return null <br>
	 * 4- If the value of the "Category" key is not in the MzSequenceFileGenerator CATEGORY_LIST log <br>
	 * and return null
	 * 
	 * @param queryParams - the Map of query parameters to validate
	 * @param searchService - a MzSearchService implementation that implements the {@code reverseProperties(File)} method
	 * @return - a Map of valid query parameters or null for the conditions above.
	 */
	public Map<String, String> validateQueryParameters( Map<String, String> queryParams, MzSearchService searchService) {
		
		Map<String, String> validMap = null;
		
		// check inputs
		assert searchService != null;
		if (queryParams == null || queryParams.isEmpty()) {
			logger.log(Level.WARNING, "Invalid map of query parameters was provided, will return NULL!");
			return null;
		}
		
		if (!queryParams.containsKey(KCategoryKey)) {
			logger.log(Level.WARNING, "Map of query parameters provided does not have Category Key, will return NULL!");
			return null;
		}
		
		// Check the value of the Category Key
		String categoryValue = queryParams.get(KCategoryKey);
		Set<String> categorySet = new HashSet<String>();
		categorySet.addAll(MzSequenceFileGenerator.getCategoryList());
		if (categoryValue == null || !categorySet.contains(categoryValue)) {
			logger.log(Level.WARNING, "Invalid value for Category Key in map of query parameters provided, will return NULL!");
			return null;
		}
		
		// Read in the Properties File
		Properties featureMap = searchService.reverseProperties(MzSequenceFileGenerator.getPropertiesFileName());
		if (featureMap == null) {
			logger.log(Level.WARNING, "Was unable to read Properties File of feature names/values...aborted!");
			throw new RuntimeException("Unable to read Properties File with feature names/values...will Abort!");
		} else  {
			
			// Create the iterators
			logger.log(Level.INFO, "Will start validation of the query parameters....!");
			validMap = new ConcurrentHashMap<String, String>();
			Set<String> querySet = queryParams.keySet();
			assert querySet != null;
			Iterator<String> queryIterator = querySet.iterator();
			assert queryIterator != null;
			
			while (queryIterator.hasNext()) {
				String queryKey = queryIterator.next();
				if (queryKey != null && queryKey.length() > 0) {
					if (featureMap.containsKey((String)queryKey)) {
						// Test
						System.out.printf("Query Key: %s Query Value: %s\n", queryKey, queryParams.get(queryKey));
						validMap.put(queryKey, queryParams.get(queryKey));
					}
				}
			}
			logger.log(Level.INFO, "Finished validation of query Parameters and found {0} valid parameters", 
					new Object[] { Integer.toString(validMap.size())} );
		}
		
		// Return the map
		if (validMap != null & validMap.size() > 0) {
			return validMap;
		} else {
			return null;
		}		
	}
}
