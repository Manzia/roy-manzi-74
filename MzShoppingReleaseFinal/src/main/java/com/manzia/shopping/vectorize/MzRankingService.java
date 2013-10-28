package com.manzia.shopping.vectorize;

import java.util.List;

import org.apache.mahout.math.NamedVector;

/**
 * Interface to be implemented by managed beans that compute
 * Vector similarity using different distance measures
 * 
 * @author Roy Manzi Tumubweinee, Oct 4, 2012, Manzia Corporation
 *
 */

public interface MzRankingService {
	/**
	 * Method that returns a List of modelNumbers as Strings given a search Vector
	 * 
	 * @param searchVector - Vector for which ModelNumbers will be computed
	 * @param numClosest - how many ModelNumbers to return. Default value is 5
	 * @return
	 */
	
	public List<String> computeModelNumbersForVector(NamedVector searchVector, int numClosest);

}
