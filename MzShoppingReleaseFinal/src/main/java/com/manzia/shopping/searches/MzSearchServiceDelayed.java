package com.manzia.shopping.searches;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.manzia.shopping.dao.MzProdDatabase;
import com.manzia.shopping.dao.MzUserDeviceDao;
import com.manzia.shopping.dao.MzUserDeviceDataImpl;
import com.manzia.shopping.dao.MzUserDeviceTable;
import com.manzia.shopping.model.MzSearchDetail;
import com.manzia.shopping.products.RankedProducts;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * Session Bean implementation class MzSearchServiceDelayed
 * Given a Map of query Parameters, this bean does the following
 * 1- Persist the query parameters as a MzSearchDetail entity
 * 2- Use a MzSearchService @ImmediateSearch implementation to retrieve and
 * return an initial "batch" of Products
 * 3-Respond to lifecycle events of the MzSearchDetail (search query) such as
 * 	- SearchDurationDidChange
 *  - SearchDurationIsZero
 *  - SearchItemNeedsUpdate
 */
@Stateless
@Local(MzSearchDelayInterface.class)
//@LocalBean
@MzDelayedSearch
public class MzSearchServiceDelayed implements MzSearchDelayInterface {
	
	//Logger
	public static final Logger logger = 
						Logger.getLogger(MzSearchServiceDelayed.class.getCanonicalName());
	
	// Parameter Keys
	private static final String kDeviceIdentifier = "deviceId";
	private static final String kSearchDetailBrand = "Brand";
	private static final String kSearchDetailPrice = "Price";
	private static final String kSearchDetailDuration = "Duration";
	private static final int kSearchMaxDuration = 7;		// Maximum duration for a search is 7 days
	private static final String kSearchDetailStatus = "Status";
	private static final String kSearchDetailTitle = "Title";
	private static final String kSearchDetailProfile = "Profile";
	private static final float kSearchDefaultPrice = 1.0f;		// we use 1.0 as default since some DBs may interpret 0 as NULL
	private static final String kSearchDefaultValue = "NA";		// used as default for NOT NULL string fields missing a value
	private static final String kKeyValueSeparator = ":";
	private static final String kEntrySeparator = ";";
	
	// EntityManager
	//@Inject @MzProdDatabase 
	//private EntityManager entityManager;

	@Inject @MzUserDeviceTable
	private MzUserDeviceDao userDevice;
	
    /**
     * Default constructor. 
     */
    public MzSearchServiceDelayed() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Getter for current MaxDuration in Days
     * @return - maximum duration in days
     */

	public static final int getMaxDuration() {
		return kSearchMaxDuration;
	}


	/**
     * <p> NOTE: MzSearchServiceDelayed instances do not implement this method and will
     * return NULL if called. Use an instance of MzSearchServiceImmediate to return
     * a RankedProducts object. </p> 
     * 
     * @see MzSearchService#searchProductsResponse(Map<String,String>)     * 
     */
    @Override
    public RankedProducts searchProductsResponse(Map<String,String> queryParamters) {
        // TODO Auto-generated method stub
			return null;
    }
    
    /**
     * @see MzSearchService#persistSearchDetail(Map, Map)
     * 
     */
    @Override
    public boolean persistSearchDetail( Map<String, String> queryParams, Map<String, String> pathParams, MzUserDeviceDao userDao ) {
    	
    	// Check Inputs
    	if (queryParams == null || queryParams.isEmpty() || pathParams == null || pathParams.isEmpty()) {
    		logger.log(Level.WARNING, "Invalid Map of query and/or path parameters was provided...will return!");
    		return false;
    	}
    	String deviceKey = pathParams.get(kDeviceIdentifier);
    	if (deviceKey == null || deviceKey.length() < 1) {
    		logger.log(Level.WARNING, "Invalid device Identifier in path parameters was provided...will return!");
    		return false;
    	}
    	
    	// Get the Data Access and Entity instances
    	MzSearchDetail searchDetail;
    	/*MzUserDeviceDao userDevice = new MzUserDeviceDataImpl();
    	assert userDevice != null;
    	if (manager != null) {
    		userDevice.setEntityManager(manager);
    	} else {
    		userDevice.setEntityManager(entityManager);
    	} */
    	
    	
    	// Set the values
    	String searchTitle = queryParams.get(kSearchDetailTitle) != null ? 
    			queryParams.get(kSearchDetailTitle) : kSearchDefaultValue;
    	String duration = pathParams.get(kSearchDetailDuration) != null ? 
    			pathParams.get(kSearchDetailDuration) : Integer.toString(0);
    	int searchDuration = Integer.parseInt(duration);
    	searchDuration = searchDuration > kSearchMaxDuration ? kSearchMaxDuration : searchDuration;
    	String price = queryParams.get(kSearchDetailPrice) != null ?
    			queryParams.get(kSearchDetailPrice) : Float.toString(kSearchDefaultPrice);
    	float searchPrice = Float.parseFloat(price) != Float.NaN ? 
    			Float.parseFloat(price) : Float.valueOf((kSearchDefaultPrice));    	
    	String status = pathParams.get(kSearchDetailStatus) != null ?
    			pathParams.get(kSearchDetailStatus) : Integer.toString(0);
    	int searchStatus = Integer.parseInt(status);    	
    	String searchBrand = queryParams.get(kSearchDetailBrand) != null ?
    			queryParams.get(kSearchDetailBrand) : kSearchDefaultValue;
    	String searchOptions = createSearchOptions(queryParams);
    	searchOptions = searchOptions != null ? searchOptions : kSearchDefaultValue;
    	String searchProfile = pathParams.get(kSearchDetailProfile) != null ?
    			pathParams.get(kSearchDetailProfile) : kSearchDefaultValue;
    	Timestamp searchCreated = new Timestamp( System.currentTimeMillis());
    	
    	// Create/Persist the entity (Pass null for searchModified field so field auto-updates)
    	boolean success = false;
    	searchDetail = new MzSearchDetail(searchBrand, searchDuration,searchPrice, searchStatus, 
    			searchTitle, searchOptions, searchProfile, searchCreated, null, null );
    	if (userDao == null ) {
    		success = userDevice.addMzSearchDetail(searchDetail, deviceKey);
    	} else {
    		success = userDao.addMzSearchDetail(searchDetail, deviceKey);
    	}
    	
		
    	return success;    	
    }
    
    /**
     * Method converts a Map<String, String> into a searchOptions value with the format
     * "key:value,key:value,key:value" <br>
     * 
     * @param options - Map of Strings that will be converted to a searchOptions string
     * @return - searchOptions string
     */
    public String createSearchOptions( Map<String, String> options) {
		
    	// check input
    	if (options == null || options.isEmpty()) {
    		logger.log(Level.WARNING, "Invalid Map of search options parameters was provided...will return NULL!");
    		return null;
    	}
    	
    	// Iterate over the Map
    	Set<String> optionSet = options.keySet();
    	Iterator<String> optionIterator = optionSet.iterator();
    	StringBuffer optionBuffer = new StringBuffer();
    	
    	while (optionIterator.hasNext()) {
    		String optionKey = optionIterator.next();
    		if (optionKey.length() > 0) {	// Probably unneccesary but what the heck!!
    			String optionValue = options.get(optionKey);
    			optionValue = optionValue.replaceAll(kKeyValueSeparator, "-");
    			optionBuffer.append(optionKey).
    			append(kKeyValueSeparator).
    			append(optionValue).
    			append(kEntrySeparator);
    		}
    	}
    	optionBuffer.setLength(optionBuffer.length()-1);	// remove the trailing ";" separator
    	return optionBuffer.toString();    	
    }
    
    /**
     * Verifies that the {deviceId} path parameter we recieved is valid, i.e this
     * user device has registered via the MzDeviceService and is stored in the 
     * database
     * 
     * @param deviceKey - deviceId of the User Device to validate
     * @param manager - optional EntityManager, if null, implementation uses default EntityManager
     * @return - if valid return true else false
     */
    public boolean checkValidDeviceId( String deviceKey, MzUserDeviceDao userDao) {
		
    	boolean success = false;
    	    	
		// Check input
		if (deviceKey == null || deviceKey.length() < 1) {
			logger.log(Level.WARNING, "Invalid deviceKey specified...!");
			return false;
		}
		
		// Check the deviceKey
    	if (userDevice.getMzUserDeviceById(deviceKey) != null) {
    		success = true;
    		logger.log(Level.INFO, "{0} is a valid Device Id", new Object[]{deviceKey});
    		// Test
    		System.out.println("Valid Device Id: " + deviceKey);
    	}  	
    	
    	return success;    	
    }
    
    /**
     * <p> NOTE: MzSearchServiceDelayed instances do not implement this method and will
     * return NULL if called. Use an instance of MzSearchServiceImmediate to return
     * a Properties object. </p> 
     * 
     * @see MzSearchService#reverseProperties(String) 
     */
    @Override
    public Properties reverseProperties(String propertiesFileName) {
		return null;
    	
    }

}
