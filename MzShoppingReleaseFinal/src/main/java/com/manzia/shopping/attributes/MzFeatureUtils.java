package com.manzia.shopping.attributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.manzia.shopping.vectorize.MzSequenceFileGenerator;

/**
 * Class that provides helper methods to capture the relationships
 * between the Attribute values of a Product. Given a Map of attributes
 * and values, we attempt to encode the relations between the most 
 * important attributes.
 * 
 * @author Roy Manzi Tumubweinee, Nov 19, 2012, Manzia Corporation
 *
 */
public final class MzFeatureUtils {
	
	//Logger
	public static final Logger logger = 
						Logger.getLogger(MzFeatureUtils.class.getCanonicalName());
	private static final float priceInterval = 25.00f;
	
	/**
	 * Price values are "grouped" into ranges. Given a particular price
	 * value, return the median value of the range the price falls in. The price
	 * ranges are in $25 increments e.g 0.01 - 25.00, 25.01 - 50.00, etc. <br>
	 * This enables price values to be encoded and compared in search Vectors
	 * 
	 * @param priceValue - price whose median value is to be determined
	 * @return - median price value of the range the priceValue falls in
	 */
	public static float getPriceRangeMedian( float priceValue ) {
		// check input
		if (priceValue == 0 || priceValue < 0 || Float.valueOf(priceValue).isNaN()) {
			logger.log(Level.WARNING, "Invalid float value for the Price was provided..will return 0.0f");
			return 0.0f;
		}
		
		// Result
		float priceResult = 0.0f;
		
		// Case where the priceValue is less than or equal to $25.00 we just divide by 2 to get Median price
		if (priceValue <= priceInterval) {
			priceResult = priceInterval / 2;
		}
		// PriceRange Algorithm
		int rangeIndex = (int)(priceValue / priceInterval);
		assert rangeIndex > 0;
		priceResult = (rangeIndex * priceInterval) + (priceInterval / 2); 
		
		return priceResult;		
	}
	
	/**
	 * To capture the relations between attributes and attribute values, we do the
	 * following: <br>
	 * 1- combine the brand attribute with every other attribute e.g (brand memory), each pair
	 * of which becomes a new feature <br>
	 * 2- combine the brand, price attributes with every other attribute e.g (brand, price, memory)
	 * each triple of which becomes a new feature <br>
	 * 
	 * <p> Note that we use brand and price attributes to capture the relations between attributes
	 * since these tend to be the most critical and discriminating attributes in the 
	 * product purchase decision </p>
	 * 
	 * @param featureMap - map of product attributes and values
	 * @return - map of product features and values after capturing the relations between the 
	 * product attributes and values. Product attributes are combined to generate new features that
	 * better capture attribute relationships.
	 */
	public static Map<String, String> getRelationFeatures( Map<String, String> featureMap ) {
		
		// Result
		Map<String, String> resultMap = Collections.synchronizedMap( new HashMap<String, String>());
		
		// check inputs
		if (featureMap == null || featureMap.isEmpty()) {
			logger.log(Level.WARNING, "Feature Map provided to determine Relation Features is Empty or Null!");
			return resultMap;
		}
		
		// check that we have brand and price entries in the featureMap
		String brandKey = MzSequenceFileGenerator.kBrandFeatureName;
		String priceKey = MzSequenceFileGenerator.kPriceFeatureName;
		assert brandKey != null;
		assert priceKey != null;
		if (featureMap.get(brandKey) == null || featureMap.get(priceKey) == null) {
			logger.log(Level.WARNING, "Can NOT determine Relation Features since Brand and/or Price attribute is missing!");
			return resultMap;
		} else {
			
			// Generate the new features
			String brandValue = featureMap.get(brandKey);
			assert brandValue != null;
			String priceValue = featureMap.get(priceKey);
			assert priceValue != null;
			Set<String> featureKeys = featureMap.keySet();
			Iterator<String> featureIterator = featureKeys.iterator();
			assert featureIterator != null;
			
			// Iterate and create the RelationFeatures
			while (featureIterator.hasNext()) {
				String key = featureIterator.next();
				assert key.length() > 0;
				String value = featureMap.get(key);
				assert value.length() > 0;
				if (!key.equals(brandKey)) {
					String newKey = brandKey + key;
					assert newKey != null;
					String newValue = brandValue + value;
					assert newValue != null;
					resultMap.put(newKey, newValue);
					if (!key.equals(priceKey)) {
						String tripleKey = newKey + priceKey;
						assert tripleKey != null;
						String tripleValue = newValue + priceValue;
						assert tripleValue != null;
						resultMap.put(tripleKey, tripleValue);
					}
				}
			}
		}
		return resultMap;		
	}

}
