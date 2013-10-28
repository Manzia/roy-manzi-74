package com.manzia.shopping.products;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.manzia.shopping.bestbuy.BestBuyServiceParameters;
import com.manzia.shopping.bestbuy.ErrorDocument;
import com.manzia.shopping.bestbuy.Product;
import com.manzia.shopping.bestbuy.ProductResponse;
import com.manzia.shopping.bestbuy.ProductsResponse;
import com.manzia.shopping.bestbuy.Remix;
import com.manzia.shopping.bestbuy.RemixException;
import com.manzia.shopping.dao.MzModelNumberDao;
import com.manzia.shopping.dao.MzModelNumberDataImpl;
import com.manzia.shopping.dao.MzProdDatabase;
import com.manzia.shopping.model.MzModelNumber;
import com.manzia.shopping.model.MzModelNumberPK;
import com.manzia.shopping.model.MzProductResult;
import com.manzia.shopping.services.MzItemSearchType;

/**
 * Session Bean implementation class MzProductServiceBean for the
 * BestBuyService 
 * 
 * Given a MzModelNumber object or identifier, this session bean
 * downloads 5-10 matching products from the BBYOpen Product API
 * using asynchronous methods. Each product downloaded as an XML file
 * is parsed and the associated ModelNumber is verified. If successful,
 * the product is saved to the prodDatabase.
 * 
 * The bean can also return all MzProductResult instances associated
 * with a specific modelCategory. This method is used as the basis for
 * creating category-specific SequenceFiles by the MzVectorizer classes
 */
@Stateless
@LocalBean
@BestBuyService
public class MzProductServiceBean {
	
	@Inject @MzProdDatabase private EntityManager emanager;
	
	//BestBuy API access key
	private static final String bestBuyKey = "4qabs35wxz465mfpmjvsg3c7";
	
	//Logger
	public static final Logger logger = 
			Logger.getLogger(MzProductServiceBean.class.getCanonicalName());
	
	// Alphanumeric pattern for Brand
	private final Pattern alphanums = Pattern.compile("[^A-Za-z0-9]");
	
	// Separator
	private static final String skuSeparator = ",";

    /**
     * Default constructor. 
     */
    public MzProductServiceBean() {
        // TODO Auto-generated constructor stub
    }
    
        
    /**
     * For every MzModelNumber instance in the database that does not have
     * child MzProductResult instance, the method will download, parse and
     * persist products from the BestBuy API - BBYOPen Products API. The method
     * persists 5-10 products per ModelNumber.
     * @throws Exception
     */
    public void updateProductsTable() throws Exception {
    	
    	// check injected entityManager
    	assert emanager != null;
    	
    	List<MzModelNumber> modelList;
    	MzModelNumber modelNum;
    	ProductsResponse testResponse;
    	// Create a data access object
    	MzModelNumberDao modelData = new MzModelNumberDataImpl();
    	modelData.setEntityManager(emanager);
    	
    	//Get the categories in the database
    	List<String> categories = modelData.getAllModelCategories();
    	assert categories != null;
    	
    	// get the MzModelNumber objects
    	if (!categories.isEmpty()) {
    		String categoryName = categories.get(0);
    		modelList = modelData.getModelNumberByCategory(categoryName);
    		assert modelList != null;
    		
    		// As a trail, do an update for the first MzModelNumber retrieved
    		if (!modelList.isEmpty()) {
    			modelNum = modelList.get(0);
    			//testResponse = fetchProductsForModelNumber(modelNum);
    		}
    	}
    	
    }
    
    /**
     * Asynchronous method that goes over the network to retrieve the products
     * from the BestBuy API that match the product SKUs in the specified List of
     * product SKUs. This method returns at most 10 products <br>
     * 
     * @param skuList - java.util.List of product SKU strings
     * @return - ProductsResponse from the BestBuy API or NULL if the skuList argument
     * is NULL or is an Empty List
     */
    @Asynchronous
    public Future<ProductsResponse> fetchProductsForSKUs(List<String> skuList) {
    	
    	ProductsResponse modelResponse;
    	//ProductResponse itemResponse;
    	
    	// create the Remix instance
    	Remix fetchModel = new Remix(bestBuyKey);
    	assert fetchModel != null;
    	
    	//Create the list to hold the product search terms
    	List<String> productFilter = new ArrayList<String>();
    	assert productFilter != null;
    	
    	// Create the SKU query string
    	String skuQuery = createSkuQueryString(skuList);
    	assert skuQuery != null;
    	if (!skuQuery.isEmpty()) {
    		productFilter.add(skuQuery);
    	} else {
    		// In this case, we have an "empty" query i.e no SKU to use for the query so
    		// we return null and exit
    		logger.log(Level.WARNING, "Cannot fetch Products from BestBuy API...Null or Empty List of SKU strings specified!");
    		return null;
    	}
    	
    	//Create the map with the parameters to pass in the query
    	Map<String, String> modelParams = 
    			new BestBuyServiceParameters().getItemSearchParameters(MzItemSearchType.BESTBUY_PRODUCTSKU);
    	assert modelParams != null;
    	
    	// Get the products
    	try {
    		
    		modelResponse = fetchModel.getProducts(productFilter, modelParams);
    		
    		// error checking
    		if (modelResponse.isError()) {
    			ErrorDocument modelError = modelResponse.getError();
    			if (modelError != null) {
    				logger.log(Level.SEVERE, modelError.getStatus());
    				logger.log(Level.SEVERE, modelError.getMessage());
    			}
    		}    		
    	} catch (RemixException re) {
    		logger.log(Level.SEVERE, re.getLocalizedMessage());
    		throw new RuntimeException("Failed to retrieve Products for product SKUs on 1st attempt "+ re.getMessage());
    	}
    	
    	// If we did not get any products back we try once more just in case its a network-related problem
    	if (modelResponse.list().isEmpty()) {
    		// Get the products
        	try {
        		
        		modelResponse = fetchModel.getProducts(productFilter, modelParams);
        		
        		// error checking
        		if (modelResponse.isError()) {
        			ErrorDocument modelError = modelResponse.getError();
        			if (modelError != null) {
        				logger.log(Level.SEVERE, modelError.getStatus());
        				logger.log(Level.SEVERE, modelError.getMessage());
        			}
        		}    		
        	} catch (RemixException re) {
        		logger.log(Level.SEVERE, re.getLocalizedMessage());
        		throw new RuntimeException("Failed to retrieve Products for product SKUs on 2nd attempt..aborting "+ re.getMessage());
        	}
    	} 	
    	
    	return new AsyncResult<ProductsResponse>(modelResponse);
    }
    
    /**
     * Asynchronous method that goes over the network to retrieve the products
     * from the BestBuy API that match the modelNum and modelBrand properties of
     * the provide MzModelNumber instance. This method returns at most 10 products
     * @param model
     */
    @Asynchronous
    public Future<ProductsResponse> fetchProductsForModelNumber(MzModelNumber model) {
    	
    	ProductsResponse modelResponse;
    	ProductResponse itemResponse;
    	
    	// create the Remix instance
    	Remix fetchModel = new Remix(bestBuyKey);
    	assert fetchModel != null;
    	
    	//Create the list to hold the product search terms
    	List<String> productFilter = new ArrayList<String>();
    	assert productFilter != null;
    	
    	//Create the map with the parameters to pass in the query
    	Map<String, String> modelParams = 
    			new BestBuyServiceParameters().getItemSearchParameters(MzItemSearchType.BESTBUY_MODELNUM);
    	assert modelParams != null;
    	
    	/*Add the search terms - for the ModelBrand if there are any non-alphanumeric characters
    	 * we replace the modelBrand string with the wildcard string...this is a workaround for the
    	 * cases where for example a Retailer appends the "TM" superscript to their brand name.
    	 * 
    	 * For the modelNumber we remove all whitespaces.
    	*/
    	String modelNumString = model.getId().getModelNum();
    	String modelBrandString = model.getId().getModelBrand();
    	//modelBrandString = modelBrandString.replaceAll("[^A-Za-z0-9]", "");
    	if (!alphanums.matcher(modelBrandString).matches()) {
    		modelBrandString = "*";
    	}
    	//String encodedModelNum = URLEncoder.encode(modelNumString, "UTF-8").replace("+", "%20");
    	String encodedModelNum = quoteStringForURL(modelNumString);
    	assert encodedModelNum != null;
    	productFilter.add("modelNumber=" + encodedModelNum);
    	productFilter.add("manufacturer=" + modelBrandString);
    	
    	// Get the products
    	try {
    		
    		modelResponse = fetchModel.getProducts(productFilter, modelParams);
    		
    		// error checking
    		if (modelResponse.isError()) {
    			ErrorDocument modelError = modelResponse.getError();
    			if (modelError != null) {
    				logger.log(Level.SEVERE, modelError.getStatus());
    				logger.log(Level.SEVERE, modelError.getMessage());
    			}
    		}    		
    	} catch (RemixException re) {
    		logger.log(Level.SEVERE, re.getLocalizedMessage());
    		throw new RuntimeException("Failed to retrieve Products for ModelNumber "+ re.getMessage());
    	}
    	
    	// If we did not get any products back we try again using the SKU
    	if (modelResponse.list().isEmpty()) {
    		String modelSKU = model.getRetailerItemId();
    		if ( modelSKU != null) {
    			// Remove the pageSize attribute for the ProductResponse query
        		modelParams.remove("pageSize");
            	
            	try {
            		 		itemResponse = fetchModel.getProduct(modelSKU, modelParams);
            		
            		// error checking
            		if (itemResponse.isError()) {
            			ErrorDocument itemError = itemResponse.getError();
            			if (itemError != null) {
            				logger.log(Level.SEVERE, itemError.getStatus());
            				logger.log(Level.SEVERE, itemError.getMessage());
            			}
            		}
            	} catch (RemixException re) {
            		logger.log(Level.SEVERE, re.getLocalizedMessage());
            		throw new RuntimeException(re.getMessage());
            	}
            	// Add the product
            	if (itemResponse.product() != null) {
            		modelResponse.list().add(itemResponse.product());
            	} else {
            		logger.log(Level.INFO, "No Product Returned for SKU: {0}", new Object[]{modelSKU});
            		
            	}
    		} else {
    			logger.log(Level.INFO, "No Product Returned for ModelNumber: {0}", new Object[]{(model.getId().getModelNum())});
    		}
    	} 	
    	
    	return new AsyncResult<ProductsResponse>(modelResponse);
    }
    
    /**
     * Asynchronous business method that fetches Products from the BestBuy API
     * @param modelList - the list of MzModelNumber objects for which Products will be
     * retrieved
     * @return - returns a ProductsResponse object that conatins all the the retrieved
     * Products from the BestBuy API
     * @throws Exception - throws an Exception for any network related failures or HTTP bad 
     * requests during the retrieval of any of the Products
     */
    @Asynchronous
    public Future<List<Product>>fetchProductsForModelNumbers(List<MzModelNumber> modelList) throws Exception
    {
    	List<Product> productsResponse;
    	Set<Future<ProductsResponse>> futureSet;
    	assert modelList != null;
    	
    	// Return if modelList is null
    	if (modelList.isEmpty()) {
    		logger.log(Level.INFO, "List of ModelNumbers to retreive is empty..Return!");
    		productsResponse = new ArrayList<Product>();	// return empty List
    		return new AsyncResult<List<Product>>(productsResponse);
    	} else {
    		
    		// Interate through the modelList and instantiate the Future objects
    		futureSet = Collections.synchronizedSet( new HashSet<Future<ProductsResponse>>());
    		assert futureSet != null;
    		    		
    		if (!modelList.isEmpty()) {
    			ListIterator<MzModelNumber> modelIterator = modelList.listIterator();
    			while (modelIterator.hasNext()) {
    				Future<ProductsResponse> futureResponse = fetchProductsForModelNumber(modelIterator.next());
    				assert futureResponse != null;
    				futureSet.add(futureResponse);
    			}
    			// Log
    			logger.log(Level.INFO, "Fetch Requests for {0} ModelNumbers from BestBuy are scheduled!", 
    					new Object[]{futureSet.size()});
    		}
    		
    		// Get the ProductsResponses from the scheduled Futures
    		int fetchCount = 0;
    		productsResponse = Collections.synchronizedList( new ArrayList<Product>());
    		ProductsResponse modelResponse;
    		for (Future<ProductsResponse> response : futureSet) {
    			//if (response.isDone()) {
    				try {
						modelResponse = response.get();
						if (modelResponse != null) {
	    					productsResponse.addAll(modelResponse.list());
	    					fetchCount++;
	    				}
					} catch (InterruptedException e) {
						logger.log(Level.SEVERE, "Interrupted exception while fetching Products!");
						modelResponse = null;
					} catch (ExecutionException e) {
						logger.log(Level.SEVERE, "Execution exception while fetching Products!");
						e.printStackTrace();
						throw new RuntimeException("Execution exception fetching products " + e.getLocalizedMessage());
					}
    				
    			//}
    		}
    		//Log
    		logger.log(Level.INFO, "Success fetching Products for {0} out of {1} ModelNumbers provided!", 
    				new Object[]{ fetchCount, modelList.size()});
    	}
    				
    	return new AsyncResult<List<Product>>(productsResponse);
    }
    /**
     * /**
     * Asynchronous business method that fetches Products from the BestBuy API
     * @param modelList - the list of MzModelNumberPK objects for which Products will be
     * retrieved (MzModelNumberPK = primary Keys of MzModelNumber objects)
     * @return - returns a ProductsResponse object that contains all the the retrieved
     * Products from the BestBuy API
     * @throws Exception - throws an Exception for any network related failures or HTTP bad 
     * requests during the retrieval of any of the Products
     *
     * @see MzProductServiceBean#fetchProductsForModelNumber(MzModelNumber)
     */
    public Future<List<Product>>fetchProductsForModelNumbersByPK(List<MzModelNumberPK> modelList) throws Exception
    {
    	List<Product> productsResponse;
    	Set<Future<ProductsResponse>> futureSet;
    	assert modelList != null;
    	
    	// Return if modelList is null
    	if (modelList.isEmpty()) {
    		logger.log(Level.INFO, "List of ModelNumbers (PrimaryKeys) to retreive is empty..Return!");
    		productsResponse = new ArrayList<Product>();	// return empty List
    		return new AsyncResult<List<Product>>(productsResponse);
    	} else {
    		
    		// Interate through the modelList and instantiate the Future objects
    		futureSet = Collections.synchronizedSet( new HashSet<Future<ProductsResponse>>());
    		assert futureSet != null;
    		    		
    		if (!modelList.isEmpty()) {
    			ListIterator<MzModelNumberPK> modelIterator = modelList.listIterator();
    			MzModelNumberPK modelPrimaryKey;
    			MzModelNumber modelNumber;
    			while (modelIterator.hasNext()) {
    				modelPrimaryKey = modelIterator.next();
    				modelNumber = new MzModelNumber(modelPrimaryKey, null, null, null, null, null);
    				assert modelNumber != null;
    				Future<ProductsResponse> futureResponse = fetchProductsForModelNumber(modelNumber);
    				assert futureResponse != null;
    				futureSet.add(futureResponse);
    			}
    			// Log
    			logger.log(Level.INFO, "Fetch Requests for {0} ModelNumbers from BestBuy are scheduled!", 
    					new Object[]{futureSet.size()});
    		}
    		
    		// Get the ProductsResponses from the scheduled Futures
    		productsResponse = Collections.synchronizedList( new ArrayList<Product>());
    		ProductsResponse modelResponse;
    		for (Future<ProductsResponse> response : futureSet) {
    			//if (response.isDone()) {
    				try {
						modelResponse = response.get();
						if (modelResponse != null) {
	    					productsResponse.addAll(modelResponse.list());
	    					
	    				}
					} catch (InterruptedException e) {
						logger.log(Level.SEVERE, "Interrupted exception while fetching Products!");
						modelResponse = null;
					} catch (ExecutionException e) {
						logger.log(Level.SEVERE, "Execution exception while fetching Products!");
						e.printStackTrace();
						throw new RuntimeException("Execution exception fetching products " + e.getLocalizedMessage());
					}
    				
    			//}
    		}
    		//Log
    		logger.log(Level.INFO, "Success fetching  {0} Products for {1} ModelNumbers provided!", 
    				new Object[]{ productsResponse.size(), modelList.size()});
    	}
    				
    	return new AsyncResult<List<Product>>(productsResponse);
    }
    
    /**
     * Method creates a SKU query string that is acceptable to the BestBuy API of
     * the format by concatenating the product sku string in specified List: <br>
     * -- "sku in(5067889,4647789,3838388)
     * @param skuList - java.util.List of product SKU strings
     * @return - SKU query string
     */
    public final String createSkuQueryString( List<String> skuList ) {
		
    	// check inputs
    	if (skuList == null || skuList.isEmpty()) {
    		logger.log(Level.WARNING, "Cannot generate SKU QueryString from Empty or NULL List of product SKUs..return Empty String");
    		return new String();
    	}
    	
    	// Iterate
    	StringBuffer skuBuffer = new StringBuffer();
    	skuBuffer.append("sku in(");
    	for(Iterator<String> iter = skuList.iterator(); iter.hasNext();) {
            skuBuffer.append(iter.next());
            if(iter.hasNext()) {
                skuBuffer.append(skuSeparator);
            }
        }
    	skuBuffer.append(")");
    	    	
    	return skuBuffer.toString().replaceAll("\\s", "%20");   // replace all Spaces before we return 	
    }
    
    /**
     * Method adds quotes to the entire string and does not do any escaping
     * of illegal characters except for the space character.
     *  Quotes use the UTF-8 encoding i.e %22 hex value (double quotes ") and %20
     *  for the space character
     * 
     * @param quoteURL - string to quote
     * @return - returns quoted string or null if null argument.
     * @see MzProductServiceBean#escapeStringForURL(String)
     */
    public final String quoteStringForURL( String quoteURL ) {
		
    	// check input
    	if ( quoteURL == null ){
    		return null;
    	} else if (quoteURL.length() == 0) {
    		return quoteURL;
    	}
    	
    	// Replace space characters
    	String out = quoteURL.replaceAll("\\s", "%20");
    	
    	StringBuffer buffer = new StringBuffer();
    	if (out != null) {
    		buffer.append("%22").append(out).append("%22");
    	}    	
    	return buffer.toString();    	
    }
    
    /**
     * Method encodes a given String to conform to standard URL specifications
     * by escaping illegal characters and reserved characters
     * @param urlToEscape - string to encode
     * @return - encoded String if any reserved characters were found or null if null passed
     */
    public final String escapeStringForURL( String urlToEscape ) {
		
    	// check input
    	if ( urlToEscape == null ){
    		return null;
    	} else if (urlToEscape.length() == 0) {
    		return urlToEscape;
    	}
    	
    	// If its all alphanumeric, just return
    	if (alphanums.matcher(urlToEscape).matches()) {
    		return urlToEscape;
    	}
    	
    	// Escape and quote
    	String out;
    	try {
    		out = URLEncoder.encode(urlToEscape, "UTF-8")
    			//.replaceAll("$", "%24")
    			.replace("+", "%20")
    			.replace(".", "%2E")
    			.replace("*", "%2A")
        		.replace(",", "%2C")
        		//.replaceAll(";", "%3B")
        		//.replaceAll(":", "%3A")
        		//.replaceAll("&", "%26")
        		//.replaceAll("+", "%2B")
        		//.replaceAll("=", "%3D")
        		//.replaceAll("?", "%3F")
        		.replace("/", "%2F");
        		//.replaceAll("[", "%5B")
        		//.replaceAll("]", "%5D")
        		//.replaceAll("@", "%40")
        		//.replaceAll("\\", "%5C");    		
    		
    	} catch (UnsupportedEncodingException e) {
    		logger.log(Level.WARNING, "Unsupported character for URL encoding for String: {0}", 
    				new Object[] {urlToEscape});
    		out = urlToEscape;
    	}    	
    	
    	return out;    	
    }
    
    /**
     * Method returns all MzProductResult instances in the database associated
     * with a specific modelCategory e.g Laptops
     * @param categoryName - string name of modelCategory
     * @return - List of MzProductResult instances associated with modelCategory
     */
    public List<MzProductResult> getProductsByCategory(String categoryName) {
    	
    	return null;
    }

}
