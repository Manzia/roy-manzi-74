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

/**
 * Wraps a response to an API request for a single product.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class ProductResponse extends Response {
    /**
     * {@link Product} representation parsed from this response
     */
    private Product product;

    /**
     * Creates a new ProductResponse from an HTTP connection.
     * 
     * @param  connection HTTP connection used to retrieve this response
     * @throws RemixException Error parsing HTTP response
     */
    public ProductResponse(HttpURLConnection connection) throws RemixException {
        super(connection);
    }
    
    /**
     * Creates a new ProductResponse from a product XML file
     * 
     * @param productResponse	the product XML file
     * @throws RemixException	exception thrown if parsing of XML file fails
     * 
     * @author Roy Manzi Tumubweinee, Sept.28, 2012, Manzia Corporation
     */
    public ProductResponse(File productResponse) throws RemixException {
    	super(productResponse);
    }

    /**
     * Retrieves a {@link Product} representation parsed from this response.
     * 
     * @return {@link Product} representation parsed from this response
     */
    public Product product() {
        if(isError()) {
            return null;
        }
        else if(null == product) {
            product = new Product(getDocumentRoot());
        }
        return product;
    }
}
