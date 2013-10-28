package com.manzia.shopping.searches;

import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;

import com.manzia.shopping.dao.MzUserDeviceDao;
import com.manzia.shopping.model.MzUserDevice;
import com.manzia.shopping.products.RankedProducts;

/**
 * Interface extended by the managed beans that provide the
 * product search services ie. immediate, delayed and notification
 * searches.
 * 
 * @author Roy Manzi Tumubweinee, Oct 4, 2012, Manzia Corporation
 *
 */

public interface MzSearchService {
	
	/**
	 * Method that returns a ranked RankedProducts object given a set of query parameters
	 * 
	 * @param queryParamters - map of query parameters. These are used to generate a 
	 * Vector from which we determine the "closest" modelNumbers that will be used
	 * to retrieve products from Retailer APIs over the network
	 * 
	 * @return - returns a RankedProducts object with a List of ranked Products that "best" match the specified
	 * query parameters
	 * 
	 */
	public RankedProducts searchProductsResponse(Map<String, String> queryParamters);
	
	/**
	 * Method that persists the relevant query and path parameters to the database as a
	 * MzSearchDetail entity object. The validation of the parameters attributes and values is
	 * assumed to have been done during the creation of the respective Maps. This would likely
	 * be done using a MzSearchValidator instance. <br>
	 *  
	 * @param queryParams - Map of query parameters
	 * @param pathParams - Map of path parameters
	 * @param userDeviceDao - data access object for the RetailerDB.user_device table
	 * @return - returns True if the persist operation was successful, False otherwise.
	 */
	public boolean persistSearchDetail( Map<String, String> queryParams, Map<String, String> pathParams, MzUserDeviceDao userDeviceDao );
	
	/**
     * Verifies that the {deviceId} path parameter we received is valid, i.e this
     * user device has registered via the MzDeviceService and is stored in the 
     * database
     * 
     * @param deviceKey - deviceId of the User Device to validate
     * @param userDeviceDao - data access object for the RetailerDB.user_device table
     * @return - if valid return true else false
     */
    public boolean checkValidDeviceId( String deviceKey, MzUserDeviceDao userDevice);
    
    /**
	 * Method reads in a Properties file whose Keys are feature "names" that will be
	 * used to encode the search Vector and then reverses the Keys and Values. This is 
	 * done so that search Vectors and the Vectors in generated SequenceFiles are feature-hashed
	 * using the same feature "names"
	 * 
	 * @param propertiesFileName - filename of PropertiesFile that will reversed
	 * @return - Properties object with the reversed Keys and Values.
	 * 
	 */
	public Properties reverseProperties(String propertiesFileName);

}
