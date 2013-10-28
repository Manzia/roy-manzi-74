package com.manzia.shopping.searches;

/**
 * After validation a Map of query parameters obtained from a HTTP GET
 * request, the MzQueryValidator returns one of the following statuses
 * 
 * @author Roy Manzi Tumubweinee, Oct 12, 2012
 *
 */
public enum MzQueryValidationStatus {
	UNKNOWNPARAMETERS,		// all the query parameters in the map are unknown
	NOCATEGORYKEY,			// query parameters do not include a "Category" key
	VALIDPARAMETERS			// all invalid/unknown parameters have been removed
}
