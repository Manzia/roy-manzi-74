package com.manzia.shopping.searches;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.vectorizer.encoders.ContinuousValueEncoder;
import org.apache.mahout.vectorizer.encoders.FeatureVectorEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;

import com.manzia.shopping.attributes.MzFeatureUtils;
import com.manzia.shopping.bestbuy.Product;
import com.manzia.shopping.dao.MzUserDeviceDao;
import com.manzia.shopping.model.MzModelNumberPK;
import com.manzia.shopping.products.BestBuyService;
import com.manzia.shopping.products.MzProductServiceBean;
import com.manzia.shopping.products.MzProductsConverter;
import com.manzia.shopping.products.MzProductsConverterImpl;
import com.manzia.shopping.products.ObjectFactory;
import com.manzia.shopping.products.RankedProducts;
import com.manzia.shopping.vectorize.MzCosineDistance;
import com.manzia.shopping.vectorize.MzRankingService;
import com.manzia.shopping.vectorize.MzSequenceFileGenerator;

/**
 * Session Bean implementation class MzSearchServiceImmediate
 * This bean performs the following main tasks
 * 1- given a Map of query parameters, it will encode it into a Vector
 * object
 * 2- Retrieve a set of ModelNumber strings from a MzRankingService object
 * that are "closest" to the encoded Vector
 * 3- log to file the Search and retrieved ModelNumbers - this is done for
 * future extensibility where this data can be used to extend the functionality
 * and intelligence of search using Machine Learning techniques
 * 4- retrieve Products from multiple Retailer APIs using the retrieved 
 * modelNumbers above
 * 5- rank the retrieved Products by Price, Brand etc and return a ProductsResponse
 */
@Stateless
@Local(MzSearchImmediateInterface.class)
//@LocalBean
@MzImmediateSearch
public class MzSearchServiceImmediate implements MzSearchImmediateInterface {
	
	//Logger
		public static final Logger logger = 
					Logger.getLogger(MzSearchServiceImmediate.class.getCanonicalName());
	// Category Key
	private static final String kCategoryKey = MzSequenceFileGenerator.kCategoryFeatureName;
	private static final String KRegularPriceKey = "Regular Price";
	private static final String KSalePriceKey = "Sale Price";
	private static final String KBrandKey = MzSequenceFileGenerator.kBrandFeatureName;
	private static final String KNullValue = "null";
	
	// Default Number of Products to Search
	private static final int topKProducts = 4;		// For testing purposes
	
		
	// RankingService implementation
	@Inject @MzCosineDistance 
	private MzRankingService rankingService;
	
	// ProductService Implementation
	@Inject @BestBuyService
	private MzProductServiceBean productService;
	
    /**
     * Default constructor. 
     */
    public MzSearchServiceImmediate() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Implementation flow:
     * 1- create a search Vector from the queryParameters map
     * 2- get the List of ModelNumbers + Brand that are "closest" to the search Vector
     * 3- For all retailer APIs, get a List of Products matching the modelNumbers, we make
     * make two attempts before aborting
     * 4- Rank the retrieved Products by Price and return the "best" as a RankedProducts object
     * 
     */
	@Override
	public RankedProducts searchProductsResponse(	Map<String, String> queryParameters) {
		
		// RankedProducts
		RankedProducts rankedProducts;
		ObjectFactory rankedProductsFactory = new ObjectFactory();
		rankedProducts = rankedProductsFactory.createRankedProducts();
			
		
		// check the Map
		if (queryParameters == null || queryParameters.isEmpty()) {
			logger.log(Level.WARNING, "Invalid Map of parameters specified..will return NULL!");
			return null;
		} else {
			
			// Create the Search Vector
			NamedVector searchVector = createSearchVector(queryParameters);
			assert searchVector != null;
			logger.log(Level.INFO, "Search Vector successfully created with Name: {0}", new Object[]{searchVector.getName()});
			
			// Get the closest "category:ModelNumbers:Brand" strings
			List<String> closestModelList;
			assert rankingService != null;
			// HARD-CODE FOR TESTING PURPOSES !!!
			closestModelList = rankingService.computeModelNumbersForVector(searchVector, 5);
			//closestModelList = new ArrayList<String>();
			//closestModelList.add("Laptop:2000-2a10nr:HP");
			//closestModelList.add("Mobile Phones:SPH-L710:Samsung");
			assert closestModelList != null;
			if (closestModelList.isEmpty()) {
								
				//Log
				logger.log(Level.WARNING, "Cannot created RankedProducts since Empty list of ModelNumbers:Brand strings was returned!");
				return rankedProducts;
			} else {
				
				// Iterate over the category:ModelNumbers:Brand strings and instantiate a List of MzModelNumberPK objects
				MzModelNumberPK modelPrimaryKey;
				List<MzModelNumberPK> primaryKeyList = new ArrayList<MzModelNumberPK>();
				String[] numberBrand;				
				for (String modelNum : closestModelList) {
					System.out.printf("Closest ModelNumber: %s", modelNum);
					System.out.println();
					if (modelNum.length() > 0) {
						if (modelNum.equals(KNullValue)) { continue; }	// skip modelNum with null values
						numberBrand = modelNum.split(MzSequenceFileGenerator.VECTORNAMESEPARATOR, 3);
						assert numberBrand != null;
						// check the array length we so we don't get ArrayIndex Exceptions
						if (numberBrand.length == 3) {
							//System.out.printf("ModelNumber did not split correctly: %s", modelNum);
							if (numberBrand[1] != null && numberBrand[1].length() > 0 
									&& numberBrand[2] != null && numberBrand[2].length() > 0) {
								System.out.printf("Closest modelNumber split - ModelNumber: %s, ModelBrand: %s", numberBrand[1], numberBrand[2]);
								System.out.println();
								modelPrimaryKey = new MzModelNumberPK(numberBrand[1], numberBrand[2]);
								assert modelPrimaryKey != null;
								primaryKeyList.add(modelPrimaryKey);
							}						
						}				
					}						
				}
				
				// Get the Products from the Retailer APIs using modelNumbers & associated modelBrands
				Future<List<Product>> futureBestBuyList;
				List<Product> bestBuyList = new ArrayList<Product>(); // Instantiate so we return empty List on failure!!
				try {
					
					//Retrieve from the BestBuy API
					logger.log(Level.INFO, "Will submit {0} modelNumbers to the BestBuy API", 
							new Object[]{Integer.toString(primaryKeyList.size())});
					logger.log(Level.INFO, "First modelNumber submitted is {0}", new Object[] {primaryKeyList.get(0)});
					futureBestBuyList = productService.fetchProductsForModelNumbersByPK(primaryKeyList);					
					
				} catch (Exception e) {
					logger.log(Level.WARNING, "Exception on first attempt retrieving Products from the Best Buy API! {0}", 
							new Object[]{e.getLocalizedMessage()});
					futureBestBuyList = null;
					
					// try a second time
					try {
						futureBestBuyList = productService.fetchProductsForModelNumbersByPK(primaryKeyList);
					} catch (Exception e1) {
						logger.log(Level.WARNING, "Exception on second attempt retrieving Products from the Best Buy API! {0}", 
								new Object[]{e1.getLocalizedMessage()});
						futureBestBuyList = null;						
					}
				}
				if (futureBestBuyList != null) {
					try {
						bestBuyList = futureBestBuyList.get(); // In production, check future.isDone() first before get()!!
					} catch (InterruptedException e) {
						logger.log(Level.SEVERE, "Interrupted exception while getting Products from BestBuy API: {0}", 
								new Object[]{ e.getLocalizedMessage()});
						
					} catch (ExecutionException e) {
						logger.log(Level.SEVERE, "Execution exception while getting Products from BestBuy API: {0}", 
								new Object[]{ e.getLocalizedMessage()});
						throw new RuntimeException("Execution exception getting Products from BestBuy API "+ e.getLocalizedMessage());
					}
				} else {
					logger.log(Level.WARNING, "Failed to retrieve Products from BestBuy API!");
				}
				if (!bestBuyList.isEmpty()) {
					logger.log(Level.INFO, "Retrieved {0} Products from BestBuy API!", new Object[]{ Integer.toString(bestBuyList.size())} );
					
					// convert the List of Product into a RankedProducts object
					MzProductsConverter productConverter = new MzProductsConverterImpl();
					String salePrice = queryParameters.get(KSalePriceKey);
					String regularPrice = queryParameters.get(KRegularPriceKey);
					Float queryPrice = salePrice != null ? Float.valueOf(salePrice) : Float.valueOf(regularPrice);
					rankedProducts = productConverter.convertToRankedProducts(bestBuyList, queryPrice, topKProducts);
					assert rankedProducts != null;
				}
			}
		}	
		
		return rankedProducts;
	}
	
	/**
	 * <p> NOTE: MzSearchServiceImmediate instances do not implement this method and will
     * return FALSE if called. Use an instance of MzSearchServiceDelayed to persist
     * a search as a MzSearchDetail entity instance. </p> 
	 * 
	 * @see MzSearchService#persistSearchDetail(Map, Map)
	 */
	@Override
	public boolean persistSearchDetail(Map<String, String> queryParams,	Map<String, String> pathParams, MzUserDeviceDao userDao) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * <p> NOTE: MzSearchServiceImmediate instances do not implement this method and will
     * return FALSE if called. Use an instance of MzSearchServiceDelayed to check for a
     * a valid MzUserDevice entity instance. </p> 
	 * 
	 * @see MzSearchService#checkValidDeviceId(String, EntityManager)
	 */
	@Override
	public boolean checkValidDeviceId( String deviceKey, MzUserDeviceDao manager){
		return false;
		
	}
		
	/**
	 * Method that generates a Vector from a set of query parameters
	 * 
	 * @param queryParams - Map of query parameters
	 * @return - encoded Vector
	 */
	public final NamedVector createSearchVector( Map<String, String> queryParams) {
		
		// check validity of input
		if (queryParams == null) {
			logger.log(Level.WARNING, "Map of Query Parameters is NULL...will return!");
			return null;
		}
		if (queryParams.isEmpty()) {
			logger.log(Level.WARNING, "Map of Query Parameters is Empty...will return!");
			return null;
		}
		
		// Initialize the search Vector, the Vector is given the name of the Category Key
		String categoryValue = queryParams.get(kCategoryKey);
		assert categoryValue != null;
		NamedVector searchVector = new NamedVector( 
				new SequentialAccessSparseVector(MzSequenceFileGenerator.VECTOR_SIZE), categoryValue);
		assert searchVector != null;
		Map<String, Set<Integer>> traceDictionary = new TreeMap<String, Set<Integer>>();		
		// Initialize the StringBuffer that will captures all the query values as one string to be
		// encoded as a separate feature
		List<String> features = new ArrayList<String>();
		
		// Assign relationsMap
		//Map<String, String>	relationsMap = Collections.synchronizedMap( new HashMap<String, String>());
		//relationsMap.putAll(queryParams);
		//relationsMap.remove(kCategoryKey);
		//relationsMap.remove(KRegularPriceKey);
		//relationsMap.remove(KSalePriceKey);
		
		/* Initialize the Properties File - NOTE that we MUST use the same Properties file that was
		// used to generate the SequenceFile we'll be comparing the search Vector against.
		 * NOTE: The following is assumed
		 * 1- The String values of the Keys in the Map of Query Parameters are equal to the String values of the
		 * Values in the properties File used by the MzSequenceFileGenerator to generate SequenceFiles, for example
		 * if there is a Key = "price" in the query parameters map, there must be a Property in the
		 * properties File with Value = "price"
		 * 2- Becoz of 1 above, we actually reverse the Properties File used to generate SequenceFile before we
		 * can use it to generate Search Vectors
		*/
		Properties vectorProps = reverseProperties(MzSequenceFileGenerator.getPropertiesFileName());
		assert vectorProps != null;
				
		// Iterate over the Map and encode each entry
		if (!vectorProps.isEmpty()) {
						
			String brandValue = null;
			String regPriceValue = null;
			String salePriceValue = null;
			Set<String> querySet = queryParams.keySet();
			assert querySet != null;
			Iterator<String> queryIterator = querySet.iterator();
			assert queryIterator != null;
			
			while (queryIterator.hasNext()) {
				String queryKey = new String(queryIterator.next());
				assert queryKey != null;
				String queryValue = queryParams.get(queryKey);
				assert queryValue != null;
				String featureName = vectorProps.getProperty(queryKey);
				//System.out.printf("QueryKey: %s\t\t QueryValue: %s\t\t Feature Name: %s\n", queryKey, queryValue, featureName);
				assert featureName != null;
				
				// Set the Price, Brand
				if (queryKey.equals(KBrandKey)) {
					brandValue = queryValue;
				} else if (queryKey.equals(KRegularPriceKey)) {
					regPriceValue = queryValue;
				} else if (queryKey.equals(KSalePriceKey)) {
					salePriceValue = queryValue;
				}
				
				// add to features String
				features.add(queryValue);
				
				// Encode only those details.name elements in our vectorMap
				if (featureName.length() > 0) {
					FeatureVectorEncoder encoder = new StaticWordValueEncoder(featureName);
					assert encoder != null;
					encoder.setProbes(2);
					encoder.setTraceDictionary(traceDictionary);
					encoder.addToVector(queryValue, MzSequenceFileGenerator.kMediumWeight ,searchVector);
				} else {
					logger.log(Level.WARNING, "Properties File has empty Feature Name!");
				}				
			}
			
			// Encode all the queryValues as one feature
			FeatureVectorEncoder featureEncoder = new StaticWordValueEncoder(MzSequenceFileGenerator.kProductFeaturesName);
			assert featureEncoder != null;
			featureEncoder.setTraceDictionary(traceDictionary);
			featureEncoder.setProbes(2);
			for (String feature : features) {
				featureEncoder.addToVector(feature, MzSequenceFileGenerator.kSmallWeight, searchVector);
			}			
			
			// Encode the Price, Brand and Category features separately. These features are given a significant
			// weighting in order to influence the Vector Similarity Metric.
			if (brandValue != null && brandValue.length() > 0) {
				FeatureVectorEncoder encoder = new StaticWordValueEncoder(MzSequenceFileGenerator.kBrandFeatureName);
				assert encoder != null;
				encoder.setTraceDictionary(traceDictionary);
				encoder.setProbes(2);
				encoder.addToVector(brandValue, MzSequenceFileGenerator.kLargeWeight ,searchVector);
			}
			
			FeatureVectorEncoder categoryEncoder = new StaticWordValueEncoder(MzSequenceFileGenerator.kCategoryFeatureName);
			assert categoryEncoder != null;
			categoryEncoder.setTraceDictionary(traceDictionary);
			categoryEncoder.addToVector(categoryValue, searchVector);
			
			// Encode the Price
			if (regPriceValue != null) {
				regPriceValue = salePriceValue != null ? salePriceValue : regPriceValue;
				
				// Get and encode the Median Price
				Float userPrice = Float.valueOf(regPriceValue);
				assert userPrice != null;
				float medianPrice = MzFeatureUtils.getPriceRangeMedian(userPrice);
				regPriceValue = Float.toString(medianPrice);
				if (regPriceValue.length() > 0) {
					//relationsMap.put(MzSequenceFileGenerator.kPriceFeatureName, regPriceValue);
					FeatureVectorEncoder priceEncoder = new ContinuousValueEncoder(MzSequenceFileGenerator.kPriceFeatureName);
					assert priceEncoder != null;
					priceEncoder.setTraceDictionary(traceDictionary);
					priceEncoder.setProbes(2);
					priceEncoder.addToVector(regPriceValue, MzSequenceFileGenerator.kLargeWeight, searchVector);
				}
			}
			
			// Encode the combination of Brand and Price
			String brandPriceFeature = KBrandKey + MzSequenceFileGenerator.kPriceFeatureName;
			String brandPriceValue = brandValue + regPriceValue;
			FeatureVectorEncoder brandPriceEncoder = new StaticWordValueEncoder(brandPriceFeature);
			brandPriceEncoder.setTraceDictionary(traceDictionary);
			brandPriceEncoder.setProbes(2);
			brandPriceEncoder.addToVector(brandPriceValue, MzSequenceFileGenerator.kXLargeWeight, searchVector);
			
			/* Encode the relation Features
			Map<String, String> relatedFeatures = MzFeatureUtils.getRelationFeatures(relationsMap);
			assert relatedFeatures != null;
			if (!relatedFeatures.isEmpty()) {
				Set<String> relationSet = relatedFeatures.keySet();
				Iterator<String> relationIterator = relationSet.iterator();
				assert relationIterator != null;
				while (relationIterator.hasNext()) {
					String key = relationIterator.next();
					if (key.startsWith(KBrandKey) && 
							key.endsWith(MzSequenceFileGenerator.kPriceFeatureName)) {
						FeatureVectorEncoder tripleEncoder = new StaticWordValueEncoder(key);
						tripleEncoder.setTraceDictionary(traceDictionary);
						tripleEncoder.setProbes(2);
						tripleEncoder.addToVector(relatedFeatures.get(key), MzSequenceFileGenerator.kXXLargeWeight, searchVector);
					} else if (key.startsWith(KBrandKey)) {
						FeatureVectorEncoder doubleEncoder = new StaticWordValueEncoder(key);
						doubleEncoder.setTraceDictionary(traceDictionary);
						doubleEncoder.setProbes(2);
						doubleEncoder.addToVector(relatedFeatures.get(key), MzSequenceFileGenerator.kXLargeWeight, searchVector);
					}
				}
			}*/				
			
		} else {
			logger.log(Level.WARNING, "Empty Properties File was created...cannot encode Search Vector!");
			return null;
		}		
		return searchVector;		
	}
	
	/**
	 * Method reads in a Properties file whose Keys are feature "names" that will be
	 * used to encode the search Vector and then reverses the Keys and Values. This is 
	 * done so that search Vectors and the Vectors in the SequenceFile are feature-hashed
	 * using the same feature "names"
	 * 
	 * @param propertiesFileName - filename of PropertiesFile that will reversed
	 * @return - Properties object with the reversed Keys and Values.
	 * 
	 */
	public Properties reverseProperties(String propertiesFileName) {
		
		// Check input validity
		if (propertiesFileName == null || propertiesFileName.isEmpty()) {
			throw new IllegalArgumentException("Properties File name is invalid!");
		}
		File propertiesFile = new File(System.getProperty("user.dir"), propertiesFileName);
		assert propertiesFile != null;
		
		// Properties
		Properties vectorMap = new Properties();
		Properties reverseVectorMap = new Properties();
		
		// Read in the Properties File
		if (propertiesFile.exists() && propertiesFile.isFile()) {
			try {
				BufferedReader propertiesReader = 
						new BufferedReader(new FileReader(propertiesFile));
				assert propertiesReader != null;
				vectorMap.load(propertiesReader);
				logger.log(Level.INFO, "Loaded Properties File..." + propertiesFile);
				propertiesReader.close();
			} catch (FileNotFoundException fe) {
				logger.log(Level.SEVERE, "Input Properties File Not Found", fe);
				fe.printStackTrace();
			} catch (IOException ie) {
				logger.log(Level.SEVERE, "IO Exception loading Properties file", ie);
				ie.printStackTrace();
			}
		 } else {
			 throw new IllegalArgumentException("Specified Properties File does not exist!");
		 }
		
		// Reverse the Keys and Values
		Set<Entry<Object, Object>> vectorSet = vectorMap.entrySet();
		assert vectorSet != null;
		Iterator<Entry<Object, Object>> vectorIterator = vectorSet.iterator();
		assert vectorIterator != null;
		while (vectorIterator.hasNext()) {
			Entry<Object, Object> vectorEntry = vectorIterator.next();
			reverseVectorMap.setProperty((String)vectorEntry.getValue(), (String)vectorEntry.getKey());
		}
		
		// Return
		return reverseVectorMap;
		
	}

	

}
