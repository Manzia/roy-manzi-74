package com.manzia.shopping.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.manzia.shopping.bestbuy.Product;
import com.manzia.shopping.bestbuy.ProductsResponse;
import com.manzia.shopping.dao.MzProdDatabase;
import com.manzia.shopping.dao.MzProductSkuTable;
import com.manzia.shopping.dao.MzProductSkusDao;
import com.manzia.shopping.dao.MzProductSkusDataImpl;
import com.manzia.shopping.model.MzProductSkus;
import com.manzia.shopping.products.BestBuyService;
import com.manzia.shopping.products.MzBestBuyConverter;
import com.manzia.shopping.products.MzProductServiceBean;
import com.manzia.shopping.products.MzProductsConverterImpl;
import com.manzia.shopping.products.ObjectFactory;
import com.manzia.shopping.products.RankedProductType;
import com.manzia.shopping.products.RankedProducts;
import com.manzia.shopping.reviews.MzReviewsConverter;
import com.manzia.shopping.reviews.ReviewMatches;
import com.manzia.shopping.searches.MzQueryTranslator;
import com.manzia.shopping.searches.MzSearchQuery;

/**
 * Session Bean implementation class MzTaskManagerService
 * 
 * This session bean serves as the central component in the Manzia
 * Shopping Service and performs the following: <br>
 * 1- instantiate MzSearchQuery objects <br>
 * 2- generate SolrQuery objects from the MzSearchQuery objects <br>
 * 3- get QueryResponse objects from the SolrServer <br>
 * 4- generate product Queries from QueryResponse objects for the
 * MzQueryType.RETURN_SKUS query type (this involves querying
 * the @MzProdDatabase for product data <br>
 * 5- process the ProductsResponse objects generated from the product
 * queries and submit them to the ManziaWebServices module as
 * a RankedProducts object <br>
 * 6- generate ReviewsResponse objects from QueryResponse objects for
 * the MzQueryType.RETURN_REVIEWS query type <br>
 * 7- process the ReviewsResponse objects and submit them to the 
 * ManziaWebServices module that uses these objects to generate
 * dynamic web pages<br>
 * 
 */
@Stateless
@Local(MzTaskManagerServiceInterface.class)
//@LocalBean
@MzCoreService
public class MzTaskManagerService implements MzTaskManagerServiceInterface {
	
	//Logger
	public static final Logger logger = 
			Logger.getLogger(MzTaskManagerService.class.getCanonicalName());
	
	// SolrServer instance
	@Inject @MzSolrServerOne
	private MzSolrService solrService;
	
	// Data Access Object
	@Inject @MzProductSkuTable
	private MzProductSkusDao skusDao;
	
	// Reviews Converter
	@Inject
	private MzReviewsConverter reviewConvert;
	
	// Product Converters
	@Inject @MzBestBuyConverter
	private MzProductsConverterImpl bestbuyConverter;
	
	/* 
	 * Inject all the Retailer-specific MzProductService beans here: 
	 */
	@Inject @BestBuyService
	private MzProductServiceBean productService;
	
	// Keys
	private final String skuKey = "sku";
	
	// List of all Retailer Names
	private static final String RETAILER_BESTBUY = "Best Buy";
	private static final String RETAILER_AMAZON = "Amazon";
	

    /**
     * Default constructor. 
     */
    public MzTaskManagerService() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Wrapper method for the {@link MzTaskManagerService#fetchRelevantProducts(Map, MzSolrService, MzProductServiceBean, MzProductSkusDao)} method
     * where we use defaults for the MzSolrService Index, the MzProductServiceBean, and the Database connection (MzProductSkusDao) as follows: <br>
     * - Default for MzSolrService Index = MzSolrServerOne <br>
     * - Default for MzProductServiceBean = BestBuyService <br>
     * - Default for MzProductSkusDao database = 192.168.1.103.RetailerDB <br>
     * 
     * @param queryMap - java.util.Map of query parameters
     * @return - RankedProducts
     * @throws SolrServerException - thrown if Solr Server fails to retrieve results from Solr Index
     */
    @Override
    public RankedProducts getRelevantProducts( Map<String, String> queryMap ) throws SolrServerException {
		
    	// checks
    	assert productService != null;
    	assert solrService != null;
    	
    	/* Testing
    	System.out.println("Query Map provided to MzTaskManagerService.getRelevantProducts method:");
    	for (Iterator<Map.Entry<String, String>> queryIter = queryMap.entrySet().iterator(); queryIter.hasNext();) {
    		Map.Entry<String, String> mapEntry = queryIter.next();
    		System.out.printf("Key: %s\t\tValue: %s\n", mapEntry.getKey(), mapEntry.getValue() );
    	}*/
    	
    	RankedProducts rankedProduct = this.fetchRelevantProducts(queryMap, solrService, productService, skusDao);
    	assert rankedProduct != null;
    	
    	return rankedProduct;    	
    }
    
    /**
     * Key method that returns a RankedProducts object given a java.util.Map of query parameters
     * from the ManziaWebServices module as follows: <br>
     * 1- Convert the Map of query parameters into a MzSearchQuery object <br>
     * 2- Convert the MzSearchQuery into a SolrQuery <br>
     * 3- Submit the SolrQuery to a SolrServer (MzSolrService) and get the QueryResponse <br>
     * 4- Retrieve all the product SKUs from the QueryResponse
     * 5- Group the retrieved product SKUs by Retailer by querying the RetailerDB.product_sku table <br>
     * 6- Submit the grouped SKUs to the respective Retailer-specific MzProductServiceBean and get
     * the Response <br>
     * 7- Add the products retrieved from the Response to a common RankedProducts object and return <br>
     * 
     * @param queryMap - java.util.Map of query parameters
     * @param indexService - instance of SolrServer
     * @param prodService - instance of MzProductServiceBean
     * @param skuDao - instance of MzProductSkusDao for data access to the RetailerDB.product_sku table
     * @return - RankedProducts object
     * @throws SolrServerException - thrown when the SolrServer query(SolrParams) method fails
     */
    public RankedProducts fetchRelevantProducts( Map<String, String> queryMap, MzSolrService indexService, 
    		MzProductServiceBean prodService, MzProductSkusDao skuDao ) throws SolrServerException {
    	
    	// check input
    	if (queryMap == null || queryMap.isEmpty()) {
    		logger.log(Level.SEVERE, "Illegal Map of query parameters specified...cannot generate RankedProducts object!");
    		throw new IllegalArgumentException("Invalid Map of query Parameters specified..cannot generate RankedProducts object");
    	}
    	
    	// Default is to return an empty RankedProducts
    	RankedProducts rankedProducts = new ObjectFactory().createRankedProducts();
    	
    	// Generate MzSearchQuery
    	MzSearchQuery searchQuery = MzSearchQuery.generateSearchQuery(queryMap);
    	assert searchQuery != null;
    	
    	// Convert MzSearchQuery to SolrQuery
    	MzQueryTranslator translator = new MzQueryTranslator();
    	SolrQuery solrQuery = translator.translateQuery(searchQuery);
    	//assert solrQuery != null;
    	
    	// Submit the SolrQuery and get the Results
    	SolrDocumentList resultsList = null;
    	if (solrQuery.getQuery() != null && !solrQuery.getQuery().isEmpty()) {
    		SolrServer server = indexService.getSolrServer();
        	assert server != null;
        	
        	/* Testing Code
        	System.out.println("SolrQuery delivered to SolrServer - MzTaskManagerService.fetchRelevantProducts method:");
        	System.out.println("Query q: " + solrQuery.getQuery()); */
        	
        	QueryResponse queryResponse = server.query(solrQuery);
        	assert queryResponse != null;
        	resultsList = queryResponse.getResults();
        	assert resultsList != null;
        	
        	// log
        	logger.log(Level.INFO, "Number of Reviews Retrieved: {0}", new Object[]{ resultsList.size()});        	
    	}
    	
    	
    	// Get the product Skus and submit product queries via the MzProductServiceBean
    	resultsList = resultsList != null ? resultsList : new SolrDocumentList();
    	List<String> resultSkuList = new ArrayList<String>();
    	if (!resultsList.isEmpty()) {
    		for (SolrDocument doc : resultsList) {
        		resultSkuList.add((String)doc.getFieldValue(skuKey));
        	}
    	}    	
    	
    	/*
    	 * Submission of a MzQueryType.RETURN_SKUS leads to a QueryResponse object that contains
    	 * the sku Field, we do the following
    	 * 1- For each SKU retrieved, query the RetailerDB.product_sku table to determine the 
    	 * associated Retailer
    	 * 2- submit the retrieved SKUS to the appropriate Retailer-specific MzProductService that
    	 * then retrieves the corresponding products from the Retailer's API
    	 * 3- Convert/Add the resulting Response object into a RankedProducts object
    	 */
    	if (!resultSkuList.isEmpty()) {
    		Map<String, List<String>> retailerSkus = groupSkusByRetailerName(skuDao, resultSkuList);
        	assert retailerSkus != null;
        	
        	// Fetch products from the Retailers
        	if (!retailerSkus.isEmpty()) {
        		
        		// BEST BUY API
        		if(retailerSkus.get(RETAILER_BESTBUY) != null) {
        			List<RankedProductType> bestBuyList = fetchFromBestBuyAPI(retailerSkus, prodService);
        			if (!bestBuyList.isEmpty()) {
        				rankedProducts.getRankedProduct().addAll(bestBuyList);
        			}
        		}    		
        	}    	
    	}
    	
    	return rankedProducts;    	
    }
    
    /**
     * Given a {@link Map} of query parameters, method retrieves a {@link SolrDocumentList} of matching
     * documents/reviews from the Index associated with the MzSolrService argument and converts it to
     * a {@link ReviewMatches} JAXB-compliant instance. <br>
     * 
     * @param queryMap - {@link Map} of query parameters
     * @param indexService - {@link MzSolrService} Solr Server instance. If NULL, will use the default Solr Server
     * i.e annotated with MzSolrServerOne
     * @return - {@link ReviewMatches} of matching reviews
     * @throws SolrServerException - thrown by Solr Server
     */
    @Override
    public ReviewMatches getConvertedRelevantReviews( Map<String, String> queryMap, MzSolrService indexService ) 
    		throws SolrServerException {
		
    	// Output
    	ReviewMatches reviewMatches = new com.manzia.shopping.reviews.ObjectFactory().createReviewMatches();
    	//MzReviewsConverter reviewConverter = new MzReviewsConverter();
    	
    	// Create the Data Access Object
    	//assert factoryManager != null;
    	//EntityManager emanager = factoryManager.createEntityManager();
    	//assert emanager != null;
    	//logger.log(Level.INFO, "Injected Entity Manager is NOT Null...Proceeding in Class: {0}", 
    	//		new Object[]{MzTaskManagerService.class.getName()});
    	//MzProductSkusDao skuDao = new MzProductSkusDataImpl();
    	//skuDao.setEntityManager(emanager);
    	
    	/* Testing
    	System.out.println("Query Map provided to MzTaskManagerService.getConvertedRelevantReviews method:");
    	for (Iterator<Map.Entry<String, String>> queryIter = queryMap.entrySet().iterator(); queryIter.hasNext();) {
    		Map.Entry<String, String> mapEntry = queryIter.next();
    		System.out.printf("Key: %s\t\tValue: %s\n", mapEntry.getKey(), mapEntry.getValue() );
    	}*/
    	
    	// check Solr Server
    	if (indexService == null) {
    		SolrDocumentList docList = this.getRelevantReviews(queryMap);
    		reviewMatches = reviewConvert.convertToReviewMatchesType(docList, skusDao);
    	} else {
    		SolrDocumentList docList = this.fetchRelevantReviews(queryMap, indexService);
    		reviewMatches = reviewConvert.convertToReviewMatchesType(docList, skusDao);
    	}
    	
    	return reviewMatches;    	
    }
    
    /**
     * Wrapper method for the {@link MzTaskManagerService#fetchRelevantReviews(Map, MzSolrService)} method
     * where we use defaults for the MzSolrService Index as follows: <br>
     * - Default for MzSolrService Index = MzSolrServerOne <br>
     * 
     * @param queryMap - java.util.Map of query parameters
     * @return - SolrDocumentList
     * @throws SolrServerException - thrown by Solr Server if fails to retrieve from Solr Index
     */
    public SolrDocumentList getRelevantReviews( Map<String, String> queryMap ) throws SolrServerException {
		
    	// checks
    	assert solrService != null;
    	
    	// Search
    	SolrDocumentList docList = this.fetchRelevantReviews(queryMap, solrService);
    	assert docList != null;
    	
    	return docList;    	
    }
    
    /**
     * Given a {@link Map} of query parameters, method retrieves a {@link SolrDocumentList} of matching
     * documents/reviews from the Index associated with the MzSolrService argument. <br>
     * 
     * @param queryMap - {@link Map} of query parameters
     * @param indexService - {@link MzSolrService} Solr Server instance
     * @return - {@link SolrDocumentList} of matching reviews
     * @throws SolrServerException - thrown by Solr Server instance
     */
    
    public SolrDocumentList fetchRelevantReviews( Map<String, String> queryMap, MzSolrService indexService ) 
    		throws SolrServerException {
    	// check input
    	if (queryMap == null || queryMap.isEmpty()) {
    		logger.log(Level.SEVERE, "Map of query parameters specified is NULL or Empty...cannot generate ReviewsResponse object!");
    		throw new IllegalArgumentException("Map of query Parameters specified is NULL or Empty..cannot generate ReviewsResponse object");
    	}
    	
    	/* 
    	 * check for the "sku" Key
    	 * Note: The "sku" Key in the Map of query parameters is the flag that indicates to return Review objects
    	 * rather than Products
    	 */
    	if (!queryMap.containsKey(skuKey)) {
    		logger.log(Level.SEVERE, "No sku Key found in input Map of query parameters..cannot retrieve Reviews. Abort!");
    		throw new IllegalArgumentException("No sku Key found in input Map of query parameters..cannot retrieve Reviews. Abort! ");
    	}
    	
    	// Generate MzSearchQuery
    	MzSearchQuery searchQuery = MzSearchQuery.generateSearchQuery(queryMap);
    	assert searchQuery != null;
    	
    	// Convert MzSearchQuery to SolrQuery
    	MzQueryTranslator translator = new MzQueryTranslator();
    	SolrQuery solrQuery = translator.translateQuery(searchQuery);
    	assert solrQuery != null;
    	/*Iterator<String> queryParams = solrQuery.getParameterNamesIterator();
    	while( queryParams.hasNext()) {
    		System.out.println(queryParams.next());
    	}*/
    	
    	// Submit the SolrQuery and get the Results
    	SolrDocumentList resultsList = null;
    	if (solrQuery.getQuery() != null && !solrQuery.getQuery().isEmpty()) {
    		SolrServer server = indexService.getSolrServer();
        	assert server != null;
        	
        	// Testing Code
        	//System.out.println("SolrQuery delivered to SolrServer - MzTaskManagerService.fetchRelevantReviews method:");
        	//System.out.println("Query q: " + solrQuery.getQuery());
        	
        	QueryResponse queryResponse = server.query(solrQuery);
        	assert queryResponse != null;
        	resultsList = queryResponse.getResults();
        	assert resultsList != null;
        	
        	// Log
        	logger.log(Level.INFO, "Number of Reviews Retrieved: {0}", new Object[]{ resultsList.size()});        	
    	}
    	
    	    	
    	return resultsList != null ? resultsList : new SolrDocumentList();
    }
    
    
    /**
     * Method fetches Products in the form of a ProductsResponse object from the Best Buy API
     * using a List of Best Buy specific product SKU strings 
     * @param retailerSku - java.util.Map whose Keys are Retailer Names e.g "Best Buy" and Values
     * are Lists of associated product Sku strings
     * @param prodService - Best-Buy specific MzProductServiceBean that does the actual retrieval
     * of the ProductsResponse from the Best Buy API
     * @return
     */
    public final List<RankedProductType> fetchFromBestBuyAPI( Map<String, List<String>> retailerSku, 
    		@BestBuyService MzProductServiceBean prodService ) {
    	
    	// Best Buy ProductsResponse
    	Future<ProductsResponse> futureResponse;	// Best Buy API Response
    	ProductsResponse bestBuyProducts = null;			// Best Buy ProductsResponse
    	List<RankedProductType> rankedList = new ArrayList<RankedProductType>();
    	
    	// Fetch
    	futureResponse = prodService.fetchProductsForSKUs(retailerSku.get(RETAILER_BESTBUY));
		
		// Retrieve the ProductsResponse from the Future
		try {
			bestBuyProducts = futureResponse.get();
		} catch (InterruptedException e) {
			bestBuyProducts = null;			// Has the effect of causing the Test to fail!
			logger.log(Level.WARNING, "InterruptedException thrown while retrieving ProductsResponse from " +
					"Future<ProductsResponse> - Best Buy API: " + e.getLocalizedMessage());
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "ExecutionException thrown while retrieving ProductsResponse from " +
					"Future<ProductsResponse> - Best Buy API: " + e.getLocalizedMessage());
			throw new RuntimeException("ExecutionException thrown while retrieving ProductsResponse from " +
					"Future<ProductsResponse> - Best Buy API: " + e.getLocalizedMessage());
		}
	
		// Convert to RankedProductType objects
		if (bestBuyProducts != null) {
			//MzProductsConverterImpl bestBuyConverter = new MzProductsConverterImpl();
			//System.out.println("Best Buy Product retrieved: " + bestBuyProducts.list().get(0).getSku());
			for (Product product : bestBuyProducts.list()) {

				// check we have a valid Product and then add it to our RankedProducts object
				if (bestbuyConverter.isValidProduct(product)) {
					RankedProductType rankedProduct = bestbuyConverter.convertToRankedProductType(product);
					if ( rankedProduct != null) {
						// add the Product
						rankedList.add(rankedProduct);
					}        			
				}
			}
		}    	
    	
    	return rankedList;    	
    }
    
    /**
     * Given a java.util.List of product Sku strings, method queries the RetailerDB.product_sku table
     * to determine the associated Retailer Name e.g Best Buy <br>
     * 
     * @param skuDao - MzProductSkusDao data access object used to access the RetailerDB.product_sku table
     * @param skuList - java.util.List of product Sku strings
     * @return - a java.util.Map whose Keys are the Retailer Names and Values are Lists of product Skus associated
     * with this Retailer
     */
    public final Map<String, List<String>> groupSkusByRetailerName( MzProductSkusDao skuDao, List<String> skuList) {
		
    	// Output
    	Map<String, List<String>> retailerSkus = new HashMap<String, List<String>>();
    	
    	// check inputs
    	if (skuList == null || skuList.isEmpty()) {
    		logger.log(Level.WARNING, "Cannot group product Skus by Retailer Name...specified List of Skus is Empty or NULL");
    		return retailerSkus;
    	}
    	if (skuDao == null) {
    		logger.log(Level.WARNING, "Cannot group product Skus by Retailer Name...specified Data Access Object, MzProductSkusDao is NULL");
    		return retailerSkus;
    	}
    	// Iterate
    	for (Iterator<String> skuIter = skuList.iterator(); skuIter.hasNext();) {
    		String skuString = skuIter.next();
    		MzProductSkus productSku = skuDao.getMzProductSku(skuString);
    		if (productSku != null) {
    			String retailerKey = productSku.getRetailerName();
    			if (retailerKey != null && !retailerKey.isEmpty()) {
    				
    				// If we already have this retailer Name as Key in the retailerSkus Map add the sku String to the
    				// associated List else create a new Entry and add to the Map
    				if (retailerSkus.containsKey(retailerKey)) {
    					retailerSkus.get(retailerKey).add(skuString);
    				} else {
    					List<String> retailerSkuList = new ArrayList<String>();
    					retailerSkuList.add(skuString);
    					retailerSkus.put(retailerKey, retailerSkuList);
    				}
    			}
    		}
    	}
    	
    	return retailerSkus;    	
    }

}
