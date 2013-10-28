/*
 * COPYRIGHT
 * Copyright (c) 2009, Matt Williams <matt@mattwilliamsnyc.com>
 *
 * LICENSE
 *
 * This source file is subject to the new BSD license bundled with this package
 * in the file, LICENSE. This license is also available through the web at:
 * {@link http://www.opensource.org/licenses/bsd-license.php}. If you did not
 * receive a copy of the license, and are unable to obtain it through the web,
 * please send an email to matt@mattwilliamsnyc.com, and I will send you a copy.
 */
package com.manzia.shopping.bestbuy;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * Response to a Remix API call to a "collection" resource.
 * 
 * Provides convenience methods for accessing collection properties (e.g. paging information).
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
abstract class CollectionResponse extends Response {
    /**
     * Return the list of entities returned with this response.
     * 
     *  @return Entities (e.g. Store, Product) returned with this response
     */
    abstract public List<?> list();

    /**
     * Instantiates a new CollectionResponse.
     * 
     * @param  connection HTTP connection used to place an API call
     * @throws RemixException
     */
    public CollectionResponse(HttpURLConnection connection) throws RemixException {
        super(connection);
    }
    
    /**
     * Instantiates a new CollectionResponse from a product XML file
     * 
     * @param productResponse	product XML file
     * @throws RemixException	Remix Exception
     * 
     * @author Roy Manzi Tumubweinee, Sept.28, 2012, Manzia Corporation
     */
    public CollectionResponse(File productResponse) throws RemixException {
    	super(productResponse);
    }

    /**
     * Results page represented by the response document.
     * 
     * @return Page number
     */
    public int currentPage() {
        return Integer.valueOf(getAttribute("currentPage"));
    }

    /**
     * Total number of pages available in the result set matching an API call.
     * 
     * @return Total number of result pages
     */
    public int totalPages() {
        return Integer.valueOf(getAttribute("totalPages"));
    }

    /**
     * Number (position) of the first result included in this response.
     * 
     * @return Sequential position of the first result included in this response
     */
    public int from() {
        return Integer.valueOf(getAttribute("from"));
    }

    /**
     * Number (position) of the last result included in this response.
     * 
     * @return Sequential position of the last result included in this response
     */
    public int to() {
        return Integer.valueOf(getAttribute("to"));
    }

    /**
     * Total number of results matching an API query.
     * 
     * @return Total number of results
     */
    public int total() {
        return Integer.valueOf(getAttribute("total"));
    }

    /**
     * Time (in seconds) required to execute an API query.
     * 
     * @return Number of seconds required to execute the API query 
     */
    public float queryTime() {
        return Float.valueOf(getAttribute("queryTime"));
    }

    /**
     * Time (in seconds) required for the response document to be returned. 
     *
     * @return Number of seconds required for the response document to be returned
     */
    public float totalTime() {
        return Float.valueOf(getAttribute("totalTime"));
    }

    /**
     * Canonical URL associated with a response document.
     * 
     * @return Canonical URL associated with a response document
     */
    public String canonicalUrl() {
        return getAttribute("canonicalUrl");
    }
}
