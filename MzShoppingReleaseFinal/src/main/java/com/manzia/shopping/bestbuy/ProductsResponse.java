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
import java.util.ArrayList;
import java.util.List;

/**
 * Response to a Remix API call to the "products" collection resource.
 * 
 * @author  Matt Williams <matt@mattwilliamsnyc.com>
 * @version $Id$
 */
public class ProductsResponse extends CollectionResponse {
    /**
     * List of {@link Product products} returned with this response 
     */
    private List<Product> products;

    /**
     * Creates a new ProductsResponse from an HTTP connection.
     * 
     * @param  connection HTTP connection used to retrieve this response
     * @throws RemixException Error parsing HTTP response
     */
    public ProductsResponse(HttpURLConnection connection) throws RemixException {
        super(connection);
    }
    
    /**
     * Creates a new ProductsResponse from a product XML file
     * 
     * @param productResponse	product XML file
     * @throws RemixException	thrown when there is an error parsing the product XML file
     * 
     * @author Roy Manzi Tumubweinee, Sept.28, 2012, Manzia Corporation
     * 
     */
    public ProductsResponse(File productResponse) throws RemixException {
    	super(productResponse);
    }

    /**
     * Returns a list of {@link Product products} returned with this response.
     * 
     * @return List of {@link Product products} returned with this response
     */
    public List<Product> list() {
        if(null == products) {
            products = new ArrayList<Product>();
            Element doc = getDocumentRoot();
            if(null != doc && doc.hasChildren()) {
                for(Element product : doc.getChildren()) {
                    products.add(new Product(product));
                }
            }
        }
        return products;
    }
}
